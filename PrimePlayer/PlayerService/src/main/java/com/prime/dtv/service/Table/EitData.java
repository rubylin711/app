package com.prime.dtv.service.Table;

import android.util.Log;

import com.prime.dtv.service.Table.Desciptor.CAIdentifierDescriptor;
import com.prime.dtv.service.Table.Desciptor.DataBroadcastDescriptor;
import com.prime.dtv.service.Table.Desciptor.DescBase;
import com.prime.dtv.service.Table.Desciptor.Descriptor;
import com.prime.dtv.service.Table.Desciptor.ExtendedEventDescriptor;
import com.prime.dtv.service.Table.Desciptor.LinkageDescriptor;
import com.prime.dtv.service.Table.Desciptor.ParentalRatingDescriptor;
import com.prime.dtv.service.Table.Desciptor.PrivateDataSpecifierDescriptor;
import com.prime.dtv.service.Table.Desciptor.ShortEventDescriptor;
import com.prime.dtv.service.Util.Utils;
import com.prime.datastructure.sysdata.EPGEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

// ref:
// https://cs.android.com/android/platform/superproject/+/master:packages/apps/TV/tuner/src/com/android/tv/tuner/data/PsipData.java
// https://cs.android.com/android/platform/superproject/+/master:packages/apps/TV/tuner/src/com/android/tv/tuner/data/SectionParser.java
public class EitData extends TableData {
    private static final String TAG = "EitData";
    // min EIT length if no events
    private static final int MIN_EIT_LENGTH = 18;
    // length from table_id to last_table_id = MIN_EIT_LENGTH - CRC32
    private static final int HEADER_LENGTH = MIN_EIT_LENGTH - 4;
    // min event length if no descriptors
    private static final int MIN_EVENT_LENGTH = 12;

    private final ConcurrentHashMap<Integer, Section> mSectionMap;

    public interface EitListener {
//        void onParsed(int serviceId, List<Event> events);
        void onParsed(int serviceId, List<EPGEvent> epgEvents);
    }

    private final EitListener mListener;

    public EitData(EitListener listener) {
        mSectionMap = new ConcurrentHashMap<>();
        mListener = listener;
    }

    @Override
    public void parsing(byte[] data, int lens) {
        if (data.length != lens) {
            Log.e(TAG, "parsing: data length error!");
            return;
        }

        Section section = new Section();
        // Parse header first to check whether we should parse this EIT
        if (!section.parseHeader(data)) {
            Log.e(TAG, "parsing: parse header fail!");
            return;
        }

        // The currentNextIndicator indicates that the section sent is currently applicable.
        int currentNextIndicator = section.getCurrentNextIndicator();
        if (currentNextIndicator == 0) {
            Log.w(TAG, "parsing: section not applicable, currentNextIndicator = " + currentNextIndicator);
            return;
        }

        int versionNumber = section.getVersionNumber();
        Section oldSection = mSectionMap.get(section.hashCode());
        int oldVersionNumber = oldSection == null ? -1 : oldSection.getVersionNumber();
        Log.d(TAG, "parsing: check version, old = " + oldVersionNumber + " cur = " + versionNumber);
        // The versionNumber shall be incremented when a change in the information carried within
        // the section occurs.
        if (oldSection != null && versionNumber == oldVersionNumber) {
            Log.w(TAG, "parsing: version not changed.");
            return;
        }

        // Section looks ok, parse events
        if (!section.parseEvents(data)) {
            Log.e(TAG, "parsing: parse events fail!");
            return;
        }

        // Update
        mSectionMap.put(section.hashCode(), section);

        // notify listener
        if (mListener != null) {
//            mListener.onParsed(section.getServiceId(), section.getEventList());
            mListener.onParsed(section.getServiceId(), getEPGEvents(section));
        }
    }

    public List<EPGEvent> getAllEventList() {
        List<EPGEvent> epgEventList = new ArrayList<>();

        for (Section section : mSectionMap.values()) {
            epgEventList.addAll(getEPGEvents(section));
        }

        return epgEventList;
    }

    private List<EPGEvent> getEPGEvents(Section section) {
        List<EPGEvent> epgEventList = new ArrayList<>();
        for (Event event : section.getEventList()) {
            EPGEvent epgEvent = new EPGEvent();
            // event id & table id
            epgEvent.set_event_id(event.getEventId());
            epgEvent.set_table_id(section.getTableId());

            // triple id
            epgEvent.set_original_network_id(section.getOriginalNetworkId());
            epgEvent.set_transport_stream_id(section.getTransportStreamId());
            epgEvent.set_s_id(section.getServiceId());

            // event type
            if (epgEvent.get_table_id() == Table.EIT_PRESENT_FOLLOWING_TABLE_ID
                    || epgEvent.get_table_id() == Table.EIT_OTHER_PRESENT_FOLLOWING_TABLE_ID) {
                // refer to pesi service, sectionNumber 0 = present, 1 = follow
                epgEvent.set_event_type(section.getSectionNumber() == 0
                        ? EPGEvent.EPG_TYPE_PRESENT : EPGEvent.EPG_TYPE_FOLLOW);
            }
            else {
                epgEvent.set_event_type(EPGEvent.EPG_TYPE_SCHEDULE);
            }

            // event start time & end time
            long startTime = event.getStartTime();
            long duration = event.getDuration();
            epgEvent.set_start_time(startTime);
            epgEvent.set_duration(duration);
            epgEvent.set_end_time(startTime+duration);

            // event content from ShortEventDescriptor
            List<ShortEventDescriptor> shortEventDescList = event.getShortEventDescList();
            String eventName = "";
            String languageCode = "";
            String eventText = "";

            if (!shortEventDescList.isEmpty()) {
                ShortEventDescriptor shortEventDesc = shortEventDescList.get(0); // temp use first desc
                languageCode = shortEventDesc.getLanguageCode();
                eventName = shortEventDesc.getEventName();
                eventText = shortEventDesc.getEventText();
            }
            epgEvent.set_event_name(eventName);
            epgEvent.set_event_name_lang_codec(languageCode);
            epgEvent.set_short_event(eventText);
            epgEvent.set_short_event_lang_codec(languageCode);

            // event content from ExtendedEventDescriptor
            // this is only a temp format
            List<ExtendedEventDescriptor> extendEventDescList = event.getExtendedEventDescList();
            StringBuilder stringBuilder = new StringBuilder();
            String extendLanguageCode = "";
            for (ExtendedEventDescriptor desc : extendEventDescList) {
                String descLanguageCode = desc.getLanguageCode();
                if (extendLanguageCode.isEmpty() && !descLanguageCode.isEmpty()) {
                    extendLanguageCode = desc.getLanguageCode();
                }

                for (ExtendedEventDescriptor.Item item : desc.getItemList()) {
                    stringBuilder.append(item.getItemDescription());
                    stringBuilder.append(":");
                    stringBuilder.append(item.getItem());
                    stringBuilder.append("\n");
                }

                stringBuilder.append(desc.getText());
                stringBuilder.append("\n");
            }
            epgEvent.set_extended_event(stringBuilder.toString());

            // event content from ParentalRatingDescriptor
            int epgParentalRating = 0xFF; // refer to pesi service
            List<ParentalRatingDescriptor> parentalRatingDescList = event.getParentalRatingDescList();
            if (!parentalRatingDescList.isEmpty()) {
                ParentalRatingDescriptor parentalRatingDesc = parentalRatingDescList.get(0); // temp use first desc
                List<ParentalRatingDescriptor.ParentalRating> parentalRatingList = parentalRatingDesc.getParentalRatingList();
                if (!parentalRatingList.isEmpty()) {
                    // tmp use first rating
                    // refer to pesi service, epgParentalRating is actually min age
                    epgParentalRating = parentalRatingList.get(0).getMinAge(); // tmp use first rating
                }
            }
            epgEvent.set_parental_rate(epgParentalRating);

            // add to list
            epgEventList.add(epgEvent);
        }

        return epgEventList;
    }

    private static class Section {
        private static final String TAG = "Section";

        private int mTableId;
        private int mSectionLength;
        private int mServiceId;
        private int mVersionNumber;
        private int mCurrentNextIndicator;
        private int mSectionNumber;
        private int mLastSectionNumber;
        private int mTransportStreamId;
        private int mOriginalNetworkId;
        private int mSegmentLastSectionNumber;
        private int mLastTableId;

        private final List<Event> mEventList;

        public Section() {
            mEventList = new ArrayList<>();
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + mTableId;
            result = 31 * result + mServiceId;
            result = 31 * result + mSectionNumber;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Section) {
                Section another = (Section) obj;
                return mTableId == another.getTableId()
                        && mServiceId == another.getServiceId()
                        && mSectionNumber == another.getSectionNumber();
            }
            return false;
        }

        // parse all
        // data = all EIT data
        public boolean parse(byte[] data) {
            // EIT lengths should > 18
            if (data.length < MIN_EIT_LENGTH) {
                Log.e(TAG, "parse: data length error!");
                return false;
            }

            if (!Eit.isEitTableId(data[0])) {
                Log.e(TAG, "parse: table id error!");
                return false;
            }

            return parseHeader(data) && parseEvents(data);
        }

        // only parse header
        // data = all EIT data or only header
        public boolean parseHeader(byte[] data) {
            if (data == null || data.length < HEADER_LENGTH) {
                Log.e(TAG, "parseHeader: data length error!");
                return false;
            }

            // table_id = 1st byte
            mTableId = Byte.toUnsignedInt(data[0]);
            Log.d(TAG, "parseHeader: table_id = " + mTableId);
            // section_length = 12 bits of 2nd and 3rd byte
            mSectionLength = Utils.getInt(data, 1, 2, Utils.MASK_12BITS);
            Log.d(TAG, "parseHeader: section_length = " + mSectionLength);
            // service_id = 4th and 5th byte
            mServiceId = Utils.getInt(data, 3, 2, Utils.MASK_16BITS);
            Log.d(TAG, "parseHeader: service_id = " + mServiceId);
            // version_number = 5 middle bits(mask = 0x3E = 00111110) of 6th byte and right shift 1
            mVersionNumber = Utils.getInt(data, 5, 1, 0x3E) >> 1;
            Log.d(TAG, "parseHeader: version_number = " + mVersionNumber);
            // current_next_indicator = 1 bit of 6th byte
            mCurrentNextIndicator = Utils.getInt(data, 5, 1, Utils.MASK_1BIT);
            Log.d(TAG, "parseHeader: current_next_indicator = " + mCurrentNextIndicator);

            // section_number = 7th byte
            mSectionNumber = Byte.toUnsignedInt(data[6]);
            Log.d(TAG, "parseHeader: section_number = " + mSectionNumber);
            // last_section_number = 8th byte
            mLastSectionNumber = Byte.toUnsignedInt(data[7]);
            Log.d(TAG, "parseHeader: last_section_number = " + mLastSectionNumber);
            // transport_stream_id = 9th and 10th byte
            mTransportStreamId = Utils.getInt(data, 8, 2, Utils.MASK_16BITS);
            Log.d(TAG, "parseHeader: transport_stream_id = " + mTransportStreamId);
            // original_network_id = 11th and 12th byte
            mOriginalNetworkId = Utils.getInt(data, 10, 2, Utils.MASK_16BITS);
            Log.d(TAG, "parseHeader: original_network_id = " + mOriginalNetworkId);
            // segment_last_section_number = 13th byte
            mSegmentLastSectionNumber = Byte.toUnsignedInt(data[12]);
            Log.d(TAG, "parseHeader: segment_last_section_number = " + mSegmentLastSectionNumber);
            // last_table_id = 14th byte
            mLastTableId = Byte.toUnsignedInt(data[13]);
            Log.d(TAG, "parseHeader: last_table_id = " + mLastTableId);

            return true;
        }

        // only parse events
        // data = all EIT data
        public boolean parseEvents(byte[] data) {
            if (data.length < MIN_EIT_LENGTH) {
                Log.e(TAG, "parseEvents: data length error!");
                return false;
            }

            int curPos = HEADER_LENGTH; // start from the end of EIT header
            while (curPos + MIN_EVENT_LENGTH < data.length) {
                // descriptors_loop_length = 12 bits of 11th and 12th byte in eventData
                int descriptorsLoopLength = Utils.getInt(data, curPos+10, 2, Utils.MASK_12BITS);
                int eventLength = MIN_EVENT_LENGTH + descriptorsLoopLength;

                if (curPos + eventLength > data.length) {
                    Log.e(TAG, "parseEvents: event length error!");
                    return false;
                }

                // copy one event data to eventData
                byte[] eventData = new byte[eventLength];
                System.arraycopy(data, curPos, eventData, 0, eventLength);
                // let Event class to parse event data
                Event event = new Event(eventData);
                mEventList.add(event);

                curPos = curPos + eventLength;
            }

            return true;
        }

        public int getTableId() {
            return mTableId;
        }

        public int getSectionLength() {
            return mSectionLength;
        }

        public int getServiceId() {
            return mServiceId;
        }

        public int getVersionNumber() {
            return mVersionNumber;
        }

        public int getCurrentNextIndicator() {
            return mCurrentNextIndicator;
        }

        public int getSectionNumber() {
            return mSectionNumber;
        }

        public int getLastSectionNumber() {
            return mLastSectionNumber;
        }

        public int getTransportStreamId() {
            return mTransportStreamId;
        }

        public int getOriginalNetworkId() {
            return mOriginalNetworkId;
        }

        public int getSegmentLastSectionNumber() {
            return mSegmentLastSectionNumber;
        }

        public int getLastTableId() {
            return mLastTableId;
        }

        public List<Event> getEventList() {
            Log.d(TAG, "getEventList: size = " + mEventList.size());
            return mEventList;
        }
    }

    public static class Event {
        private static final String TAG  = "Event";
        private final Descriptor mDescriptorHelper;

        private int mEventId;
        private final byte[] mRawStartTimeBytes;
        private final byte[] mRawDurationBytes;
        private LocalDateTime mStartTimeJava;
        private long mStartTime; // millis
        private Duration mDurationJava;
        private long mDuration; // millis
        private int mRunningStatus;
        private int mFreeCAMode;
        private int mDescriptorsLoopLength;

        public Event(byte[] rawEventBytes) {
            mDescriptorHelper = new Descriptor();
            mRawStartTimeBytes = new byte[5]; // 40 bits = 5 bytes
            mRawDurationBytes = new byte[3]; // 24 bits = 3 bytes

            parsing(rawEventBytes);
        }

        private void parsing(byte[] rawEventBytes) {
            if (rawEventBytes.length < MIN_EVENT_LENGTH) {
                Log.e(TAG, "parsing: data length error!");
                return;
            }

            // event_id, 1st and 2nd byte
            mEventId = Utils.getInt(rawEventBytes, 0, 2, Utils.MASK_16BITS);
            Log.d(TAG, "parsing: event id = " + mEventId);

            // start_time
            // copy raw start_time (3rd to 7th byte = 5 bytes)
            System.arraycopy(rawEventBytes, 2, mRawStartTimeBytes, 0, 5);
            Log.d(TAG, "parsing: raw start time = " + Arrays.toString(mRawStartTimeBytes));
            // get LocalDateTime from raw start_time
            mStartTimeJava = Utils.getLocalDateTimeFromRawBytes(mRawStartTimeBytes);
            Log.d(TAG, "parsing: java LocalDateTime = " + mStartTimeJava);
            // get time in millis from raw start_time
            mStartTime = Utils.getTimeMillisFromRawBytes(mRawStartTimeBytes);
            Log.d(TAG, "parsing: start time in millis = " + mStartTime);

            // duration
            // copy raw duration (8th to 10th byte = 3 bytes)
            System.arraycopy(rawEventBytes, 7, mRawDurationBytes, 0, 3);
            Log.d(TAG, "parsing: raw duration = " + Arrays.toString(mRawDurationBytes));
            // get LocalTime from raw duration
            mDurationJava = Utils.getDurationFromRawBytes(mRawDurationBytes);
            Log.d(TAG, "parsing: java Duration = " + mDurationJava);
            // get duration in millis from raw duration
            mDuration = Utils.getDurationMillisFromRawBytes(mRawDurationBytes);
            Log.d(TAG, "parsing: duration in millis = " + mDuration);

            // running_status, first 3 bits of 11th byte(mask = 0xE0 = 11100000) and right shift 5
            mRunningStatus = Utils.getInt(rawEventBytes, 10, 1, 0xE0) >> 5;
            Log.d(TAG, "parsing: running status = " + mRunningStatus);

            // freed_CA_mode, 4th bit of 11th byte(mask = 0x10 = 00010000)  and right shift 4
            mFreeCAMode = Utils.getInt(rawEventBytes, 10, 1, 0x10) >> 4;
            Log.d(TAG, "parsing: free CA mode = " + mFreeCAMode);

            // descriptors
            // descriptors_loop_length = 12 bits of 11th and 12th byte in eventData
            mDescriptorsLoopLength = Utils.getInt(rawEventBytes, 10, 2, Utils.MASK_12BITS);

            // copy all descriptor data to descriptorsLoopData
            if (mDescriptorsLoopLength > 0) {
                byte[] descriptorsLoopData = new byte[mDescriptorsLoopLength];
                // descriptor data start from 13th byte
                System.arraycopy(rawEventBytes, 12, descriptorsLoopData, 0, mDescriptorsLoopLength);
                // parse
                parseDescriptors(descriptorsLoopData);
            }
        }

        private void parseDescriptors(byte[] descriptorsLoopData) {
            int curPos = 0;
            while (curPos < descriptorsLoopData.length) {
                int descriptorLength = Byte.toUnsignedInt(descriptorsLoopData[curPos+1]);
                byte[] descriptorData = new byte[descriptorLength+2]; // +2 = descriptorTag byte + descriptorLength byte
                System.arraycopy(descriptorsLoopData, curPos, descriptorData, 0, descriptorLength+2);
                mDescriptorHelper.ParsingDescriptor(descriptorData, descriptorData.length);
                curPos = curPos + descriptorData.length;
            }
        }

        public int getEventId() {
            return mEventId;
        }

        public byte[] getRawStartTimeBytes() {
            return mRawStartTimeBytes;
        }

        public byte[] getRawDurationBytes() {
            return mRawDurationBytes;
        }

        public LocalDateTime getStartTimeJava() {
            return mStartTimeJava;
        }

        public long getStartTime() {
            return mStartTime;
        }

        public Duration getDurationJava() {
            return mDurationJava;
        }

        public long getDuration() {
            return mDuration;
        }

        public int getRunningStatus() {
            return mRunningStatus;
        }

        public int getFreeCAMode() {
            return mFreeCAMode;
        }

        public int getDescriptorsLoopLength() {
            return mDescriptorsLoopLength;
        }

        public Descriptor getDescriptor() {
            return mDescriptorHelper;
        }

        public List<LinkageDescriptor> getLinkageDescList() {
            List<DescBase> descBaseList = mDescriptorHelper.getDescriptorList(Descriptor.LINKAGE_DESC);
            List<LinkageDescriptor> resultList = new ArrayList<>();
            for (DescBase descBase : descBaseList) {
                resultList.add((LinkageDescriptor) descBase);
            }

            return resultList;
        }

        public List<ShortEventDescriptor> getShortEventDescList() {
            List<DescBase> descBaseList = mDescriptorHelper.getDescriptorList(Descriptor.SHORT_EVENT_DESC);
            List<ShortEventDescriptor> resultList = new ArrayList<>();
            for (DescBase descBase : descBaseList) {
                resultList.add((ShortEventDescriptor) descBase);
            }

            return resultList;
        }

        public List<ExtendedEventDescriptor> getExtendedEventDescList() {
            List<DescBase> descBaseList = mDescriptorHelper.getDescriptorList(Descriptor.EXTENDED_EVENT_DESC);
            List<ExtendedEventDescriptor> resultList = new ArrayList<>();
            for (DescBase descBase : descBaseList) {
                resultList.add((ExtendedEventDescriptor) descBase);
            }

            // ascending sort
            resultList.sort(new Comparator<ExtendedEventDescriptor>() {
                @Override
                public int compare(ExtendedEventDescriptor o1, ExtendedEventDescriptor o2) {
                    return o1.getDescriptorNumber() - o2.getDescriptorNumber();
                }
            });

            return resultList;
        }

        public List<CAIdentifierDescriptor> getCAIdentifierDescList() {
            List<DescBase> descBaseList = mDescriptorHelper.getDescriptorList(Descriptor.CA_IDENTIFIER_DESC);
            List<CAIdentifierDescriptor> resultList = new ArrayList<>();
            for (DescBase descBase : descBaseList) {
                resultList.add((CAIdentifierDescriptor) descBase);
            }

            return resultList;
        }

        public List<ParentalRatingDescriptor> getParentalRatingDescList() {
            List<DescBase> descBaseList = mDescriptorHelper.getDescriptorList(Descriptor.PARENTAL_RATING_DESC);
            List<ParentalRatingDescriptor> resultList = new ArrayList<>();
            for (DescBase descBase : descBaseList) {
                resultList.add((ParentalRatingDescriptor) descBase);
            }

            return resultList;
        }

        public List<PrivateDataSpecifierDescriptor> getPrivateDataSpecifierDescList() {
            List<DescBase> descBaseList = mDescriptorHelper.getDescriptorList(Descriptor.PRIVATE_DATA_SPECIFIER_DESC);
            List<PrivateDataSpecifierDescriptor> resultList = new ArrayList<>();
            for (DescBase descBase : descBaseList) {
                resultList.add((PrivateDataSpecifierDescriptor) descBase);
            }

            return resultList;
        }

        public List<DataBroadcastDescriptor> getDataBroadcastDescList() {
            List<DescBase> descBaseList = mDescriptorHelper.getDescriptorList(Descriptor.DATA_BROADCAST_DESC);
            List<DataBroadcastDescriptor> resultList = new ArrayList<>();
            for (DescBase descBase : descBaseList) {
                resultList.add((DataBroadcastDescriptor) descBase);
            }

            return resultList;
        }

        /* not implemented desc */
        // stuffing_descriptor
        // time_shifted_event_descriptor
        // component_descriptor
        // content_descriptor
        // telephone_descriptor
        // multilingual_component_descriptor
        // short_smoothing_buffer_descriptor
        /* not implemented desc */
    }
}
