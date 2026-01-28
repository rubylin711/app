package com.prime.dtv.service.Demux;

import static java.lang.Byte.toUnsignedInt;

import android.media.tv.tuner.filter.Filter;
import android.media.tv.tuner.filter.FilterCallback;
import android.media.tv.tuner.filter.FilterEvent;
import android.media.tv.tuner.filter.SectionEvent;
import android.util.Log;

import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Table.Eit;
import com.prime.dtv.service.Table.Table;
import com.prime.dtv.service.Util.Utils;
import com.prime.dtv.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

public class DemuxSectionCallback {
    private static final String TAG = "DemuxSectionCallback";

    // Table ID to Last section number(or UTC_time) = 8 bytes, CRC32 = 4 bytes
    private static final int MIN_DATA_LENGTH = 8 + 4;
    private static final int MIN_DATA_LENGTH_WITHOUT_CRC = 8;
    private static final int MIN_DATA_LENGTH_WITH_PRIVATE_SECTION = 3;
    // Table ID + Section syntax indicator + Private bit + Reserved bits + Section
    // length
    private static final int NUM_OF_BYTES_UNTIL_SECTION_LENGTH = 3;

    private int mFlagReceived = 0;
    private final List<Integer> mArrivedSectionNumberList = new ArrayList<>();

    private int mSectionNumber = 0;
    private int mLastSectionNumber = 0;
    private int mSectionRepeatedCount = 0;
    private int mCheckCrc = 0;
    private int mAbortFlag = 0;
    private boolean mMultiTable;
    private static final boolean mEitNoParsing = true;

    private final List<Integer> mMutiTransportStreamId = new ArrayList<>();
    private final List<List<Integer>> mSectionNumberList = new ArrayList<>();
    private final List<Integer> mLastSectionNumberList = new ArrayList<>();
    private final List<Integer> mSectionRepeatedCountList = new ArrayList<>();

    FilterCallback mFilterCallback = null;
    FilterCompleteCallback mFilterCompleteCallback = null;

    public DemuxSectionCallback(FilterCompleteCallback filterCompleteCallback, byte tableId) {
        mFilterCompleteCallback = filterCompleteCallback;
        // Log.d(TAG,"tableId = " + Integer.toString(tableId,16));
        createFilterCallback(tableId);
        mMultiTable = false;
        // Log.d(TAG,"mMultiTable = " + mMultiTable);
    }

    public void setMultiTransportStreamId(int TransportStreamId) {
        mMutiTransportStreamId.add(TransportStreamId);
        // mSectionNumberList.add(0);
        mSectionNumberList.add(new LinkedList<>());
        mLastSectionNumberList.add(0);
        mSectionRepeatedCountList.add(0);
        mMultiTable = true;
        // Log.d(TAG,"TransportStreamId = " + TransportStreamId);
        // Log.d(TAG,"mMultiTable = " + mMultiTable);
    }

    private FilterCallback getTableFilterCallback() {
        return new FilterCallback() {
            @Override
            public void onFilterEvent(Filter filter, FilterEvent[] events) {
                for (FilterEvent event : events) {
                    if (mAbortFlag == 1) {
                        mFlagReceived = 0;
                        break;
                    }

                    if (event instanceof SectionEvent) {
                        SectionEvent sectionEvent = (SectionEvent) event;
                        int tableId;// = sectionEvent.getTableId();
                        int version;// = sectionEvent.getVersion();
                        int dataLength = sectionEvent.getDataLength();
                        // mSectionNumber = sectionEvent.getSectionNumber();

                        if (dataLength < MIN_DATA_LENGTH) {
                            Log.e(TAG, "Data length not enough, error !!");
                            continue;
                        }

                        byte[] buffer = new byte[dataLength];
                        try {
                            filter.read(buffer, 0, dataLength);
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }

                        // section length = 12 bits of 2nd and 3rd byte
                        int sectionLength = Utils.getInt(buffer, 1, 2, Utils.MASK_12BITS);
                        // last section number = 8th byte
                        mLastSectionNumber = Byte.toUnsignedInt(buffer[7]);

                        tableId = Utils.getInt(buffer, 0, 1, Utils.MASK_8BITS);
                        version = (toUnsignedInt(buffer[5]) & 0x3F) >> 1;
                        mSectionNumber = Byte.toUnsignedInt(buffer[6]);

                        // Log.d(TAG, "onFilterEvent: dataLength = " + dataLength);
                        // Log.d(TAG, "onFilterEvent: tableId = " + tableId);
                        // Log.d(TAG, "onFilterEvent: version = " + version);
                        // Log.d(TAG, "onFilterEvent: sectionNumber = " + mSectionNumber);

                        // Log.d(TAG, "onFilterEvent: " + Arrays.toString(buffer));
                        // Log.d(TAG, "onFilterEvent: sectionLength = " + sectionLength);
                        // Log.d(TAG, "onFilterEvent: lastSectionNumber = " + mLastSectionNumber);
                        if (sectionLength != dataLength - NUM_OF_BYTES_UNTIL_SECTION_LENGTH) {
                            Log.e(TAG, "Section length wrong, error !!");
                            continue;
                        }

                        // if new arrived section number
                        if (!mArrivedSectionNumberList.contains(mSectionNumber)) {
                            // send to com/prime/dtv/Service/Table/Table.java onPesiFilterEvent
                            mFilterCompleteCallback.onPesiFilterEvent(buffer, dataLength);

                            mArrivedSectionNumberList.add(mSectionNumber);
                            // Log.e(TAG,"Add mSectionNumber to mArrivedSectionNumberList");
                            if (mArrivedSectionNumberList.size() > mLastSectionNumber) {
                                /* complete the whole table, exit */
                                mFlagReceived = 0;
                                // Log.e(TAG,"complete the whole table and exit");
                            }
                        } else { // if section number is already arrived before
                            mSectionRepeatedCount++;
                            if (mSectionRepeatedCount > (mLastSectionNumber + 1)) {
                                /* too much repeated sections */
                                // Log.d(TAG,"too much repeated sections !!, mSectionRepeatedCount =
                                // "+mSectionRepeatedCount);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFilterStatusChanged(Filter filter, int status) {
            }
        };
    }

    private FilterCallback getTdtFilterCallback() {
        return new FilterCallback() {
            @Override
            public void onFilterEvent(Filter filter, FilterEvent[] events) {
                for (FilterEvent event : events) {
                    if (mAbortFlag == 1) {
                        mFlagReceived = 0;
                        break;
                    }

                    if (event instanceof SectionEvent) {
                        SectionEvent sectionEvent = (SectionEvent) event;
                        int tableId;// = sectionEvent.getTableId();
                        // int version = sectionEvent.getVersion();
                        int dataLength = sectionEvent.getDataLength();

                        if (dataLength < MIN_DATA_LENGTH_WITHOUT_CRC) {
                            Log.e(TAG, "Data length not enough, error !!");
                            continue;
                        }

                        byte[] buffer = new byte[dataLength];
                        try {
                            filter.read(buffer, 0, dataLength);
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }

                        // section length = 12 bits of 2nd and 3rd byte
                        int sectionLength = Utils.getInt(buffer, 1, 2, Utils.MASK_12BITS);

                        tableId = Utils.getInt(buffer, 0, 1, Utils.MASK_8BITS);

                        // Log.d(TAG, "onFilterEvent: dataLength = " + dataLength);
                        // Log.d(TAG, "onFilterEvent: tableId = " + tableId);
                        // Log.d(TAG, "onFilterEvent: " + Arrays.toString(buffer));
                        // Log.d(TAG, "onFilterEvent: sectionLength = " + sectionLength);
                        if (sectionLength != dataLength - NUM_OF_BYTES_UNTIL_SECTION_LENGTH) {
                            Log.e(TAG, "Section length wrong, error !!");
                            continue;
                        }

                        // send to com/prime/dtv/Service/Table/Table.java onPesiFilterEvent
                        mFilterCompleteCallback.onPesiFilterEvent(buffer, dataLength);

                        /* complete the whole table, exit */
                        mFlagReceived = 0;
                    }
                }
            }

            @Override
            public void onFilterStatusChanged(Filter filter, int status) {
            }
        };
    }

    private FilterCallback getTotFilterCallback() {
        return new FilterCallback() {
            @Override
            public void onFilterEvent(Filter filter, FilterEvent[] events) {
                for (FilterEvent event : events) {
                    if (mAbortFlag == 1) {
                        mFlagReceived = 0;
                        break;
                    }

                    if (event instanceof SectionEvent) {
                        SectionEvent sectionEvent = (SectionEvent) event;
                        int tableId;// = sectionEvent.getTableId();
                        // int version = sectionEvent.getVersion();
                        int dataLength = sectionEvent.getDataLength();

                        if (dataLength < MIN_DATA_LENGTH) {
                            Log.e(TAG, "Data length not enough, error !!");
                            continue;
                        }

                        byte[] buffer = new byte[dataLength];
                        try {
                            filter.read(buffer, 0, dataLength);
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }

                        // section length = 12 bits of 2nd and 3rd byte
                        int sectionLength = Utils.getInt(buffer, 1, 2, Utils.MASK_12BITS);

                        tableId = Utils.getInt(buffer, 0, 1, Utils.MASK_8BITS);
                        // Log.d(TAG, "onFilterEvent: dataLength = " + dataLength);
                        // Log.d(TAG, "onFilterEvent: tableId = " + tableId);

                        // Log.d(TAG, "onFilterEvent: " + Arrays.toString(buffer));
                        // Log.d(TAG, "onFilterEvent: sectionLength = " + sectionLength);
                        if (sectionLength != dataLength - NUM_OF_BYTES_UNTIL_SECTION_LENGTH) {
                            Log.e(TAG, "Section length wrong, error !!");
                            continue;
                        }

                        // send to com/prime/dtv/Service/Table/Table.java onPesiFilterEvent
                        mFilterCompleteCallback.onPesiFilterEvent(buffer, dataLength);

                        /* complete the whole table, exit */
                        mFlagReceived = 0;
                    }
                }
            }

            @Override
            public void onFilterStatusChanged(Filter filter, int status) {
            }
        };
    }

    private FilterCallback getEitFilterCallback() {
        return new FilterCallback() {
            @Override
            public void onFilterEvent(Filter filter, FilterEvent[] events) {
                for (FilterEvent event : events) {
                    if (mAbortFlag == 1) {
                        mFlagReceived = 0;
                        break;
                    }

                    if (event instanceof SectionEvent) {
                        SectionEvent sectionEvent = (SectionEvent) event;
                        int tableId;// = sectionEvent.getTableId();
                        int version;// = sectionEvent.getVersion();
                        int dataLength = sectionEvent.getDataLength();

                        if (dataLength < MIN_DATA_LENGTH) {
                            Log.e(TAG, "Data length not enough, error !!");
                            continue;
                        }

                        byte[] buffer = new byte[dataLength];
                        try {
                            filter.read(buffer, 0, dataLength);
                        } catch (Exception e) {
                            mFlagReceived = 0;
                            break;
                        }
                        if (mEitNoParsing == false) {
                            // section length = 12 bits of 2nd and 3rd byte
                            int sectionLength = Utils.getInt(buffer, 1, 2, Utils.MASK_12BITS);

                            tableId = Utils.getInt(buffer, 0, 1, Utils.MASK_8BITS);
                            version = (toUnsignedInt(buffer[5]) & 0x3F) >> 1;
                            mSectionNumber = Byte.toUnsignedInt(buffer[6]);

                            // Log.d(TAG, "onFilterEvent: dataLength = " + dataLength);
                            // Log.d(TAG, "onFilterEvent: tableId = " + tableId);
                            // Log.d(TAG, "onFilterEvent: version = " + version);
                            // Log.d(TAG, "onFilterEvent: sectionNumber = " + mSectionNumber);

                            // Log.d(TAG, "onFilterEvent: " + Arrays.toString(buffer));
                            // Log.d(TAG, "onFilterEvent: sectionLength = " + sectionLength);
                            if (sectionLength != dataLength - NUM_OF_BYTES_UNTIL_SECTION_LENGTH) {
                                Log.e(TAG, "Section length wrong, error !!");
                                continue;
                            }
                        }
                        if (Pvcfg.isCheckSectionComplete() == true) {
                            mSectionNumber = Byte.toUnsignedInt(buffer[6]);
                            mLastSectionNumber = Byte.toUnsignedInt(buffer[7]);
                            // Log.d(TAG, "onFilterEvent: " + Arrays.toString(buffer));
                            // Log.d(TAG, "onFilterEvent: sectionLength = " + sectionLength);
                            // Log.d(TAG, "Eit onFilterEvent: lastSectionNumber = " + mLastSectionNumber+"
                            // mSectionNumber = "+mSectionNumber);
                            // Log.d(TAG,"Eit mArrivedSectionNumberList =
                            // "+mArrivedSectionNumberList.toString());
                            // if new arrived section number
                            if (!mArrivedSectionNumberList.contains(mSectionNumber)) {
                                // send to com/prime/dtvplayer/Service/Table/Table.java onPesiFilterEvent
                                mFilterCompleteCallback.onPesiFilterEvent(buffer, dataLength);

                                mArrivedSectionNumberList.add(mSectionNumber);
                                // Log.e(TAG, "Add mSectionNumber to mArrivedSectionNumberList");
                                if (mArrivedSectionNumberList.size() > mLastSectionNumber) {
                                    /* complete the whole table, exit */
                                    mFlagReceived = 0;
                                    // Log.e(TAG, "complete the whole table and exit");
                                }
                            } else { // if section number is already arrived before
                                mSectionRepeatedCount++;
                                if (mSectionRepeatedCount > (mLastSectionNumber + 1)) {
                                    /* too much repeated sections */
                                    // Log.d(TAG,"too much repeated sections !!, mSectionRepeatedCount =
                                    // "+mSectionRepeatedCount);
                                }
                            }
                        } else {
                            // send to com/prime/dtvplayer/Service/Table/Table.java onPesiFilterEvent
                            mFilterCompleteCallback.onPesiFilterEvent(buffer, dataLength);
                        }
                    }
                }
            }

            @Override
            public void onFilterStatusChanged(Filter filter, int status) {
                mFilterCompleteCallback.onPesiFilterStatusChanged(status);
            }
        };
    }

    private FilterCallback getSdtOtherFilterCallback() {
        return new FilterCallback() {
            @Override
            public void onFilterEvent(Filter filter, FilterEvent[] events) {
                for (FilterEvent event : events) {
                    if (mAbortFlag == 1) {
                        mFlagReceived = 0;
                        break;
                    }

                    if (event instanceof SectionEvent) {
                        SectionEvent sectionEvent = (SectionEvent) event;
                        int tableId;// = sectionEvent.getTableId();
                        int version;// = sectionEvent.getVersion();
                        int dataLength = sectionEvent.getDataLength();
                        int i, size, sectionLength, transportStreamId;

                        if (dataLength < MIN_DATA_LENGTH) {
                            Log.e(TAG, "Data length not enough, error !!");
                            continue;
                        }

                        byte[] buffer = new byte[dataLength];
                        try {
                            filter.read(buffer, 0, dataLength);
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }

                        tableId = Utils.getInt(buffer, 0, 1, Utils.MASK_8BITS);
                        version = (toUnsignedInt(buffer[5]) & 0x3F) >> 1;
                        mSectionNumber = Byte.toUnsignedInt(buffer[6]);

                        // Log.d(TAG, "onFilterEvent: dataLength = " + dataLength);
                        // Log.d(TAG, "onFilterEvent: tableId = " + Integer.toString(tableId,16));
                        // Log.d(TAG, "onFilterEvent: version = " + version);
                        // Log.d(TAG, "onFilterEvent: sectionNumber = " + mSectionNumber);

                        sectionLength = Utils.getInt(buffer, 1, 2, Utils.MASK_12BITS);
                        if (sectionLength != dataLength - NUM_OF_BYTES_UNTIL_SECTION_LENGTH) {
                            Log.e(TAG, "Section length wrong, error !!");
                            continue;
                        }

                        if (mMultiTable) {
                            transportStreamId = Utils.getInt(buffer, 3, 2, Utils.MASK_16BITS);
                            tableId = Utils.getInt(buffer, 0, 1, Utils.MASK_8BITS);
                            // Log.d(TAG, "onFilterEvent: " + Arrays.toString(buffer));
                            // Log.d(TAG, "onFilterEvent: tableId = " + Integer.toString(tableId,16));
                            // Log.d(TAG, "onFilterEvent: transportStreamId = " + transportStreamId);
                            // Log.d(TAG, "onFilterEvent: mMutiTransportStreamId.size() = " +
                            // mMutiTransportStreamId.size());
                            // Log.d(TAG, "onFilterEvent: mSectionNumberList.size() = " +
                            // mSectionNumberList.size());
                            // Log.d(TAG, "onFilterEvent: mLastSectionNumberList.size() = " +
                            // mLastSectionNumberList.size());
                            // Log.d(TAG, "onFilterEvent: mSectionRepeatedCountList.size() = " +
                            // mSectionRepeatedCountList.size());
                            if ((mMutiTransportStreamId.size() == mSectionNumberList.size() &&
                            // mMutiTransportStreamId.size() == mSectionNumberList.size() &&
                                    mMutiTransportStreamId.size() == mLastSectionNumberList.size() &&
                                    mMutiTransportStreamId.size() == mSectionRepeatedCountList.size()) == false) {
                                // (TAG, "onFilterEvent: List Size error");
                                mFlagReceived = 0;
                                break;
                            }
                            size = mMutiTransportStreamId.size();

                            for (i = 0; i < size; i++) {
                                // Log.d(TAG, "onFilterEvent: mMutiTransportStreamId.get(" + i + ") = " +
                                // mMutiTransportStreamId.get(i));
                                if (transportStreamId == mMutiTransportStreamId.get(i)) {
                                    break;
                                }
                            }
                            if (i >= size) {
                                // Log.d(TAG, "Not find data");
                                continue;
                            }
                            // mSectionNumber = sectionEvent.getSectionNumber();
                            mLastSectionNumberList.set(i, Byte.toUnsignedInt(buffer[7]));
                            // Log.d(TAG, "i = "+i+" mLastSectionNumber = "+Byte.toUnsignedInt(buffer[7]));
                            // Log.d(TAG, "i = "+i+" mSectionNumber = "+mSectionNumber);
                            // Log.d(TAG, "onFilterEvent: LastSectionNumberList = " +
                            // Byte.toUnsignedInt(buffer[7]));
                            // Log.d(TAG, "onFilterEvent: mLastSectionNumberList.get(" + i + ") = " +
                            // mLastSectionNumberList.get(i));
                            // Log.d(TAG, "onFilterEvent: mSectionNumber = " + mSectionNumber);
                            // Log.d(TAG, "onFilterEvent: mSectionNumberList.get(" + i + ") = " +
                            // mSectionNumberList.get(i));
                            if (!mSectionNumberList.get(i).contains(mSectionNumber)) {
                                mFilterCompleteCallback.onPesiFilterEvent(buffer, dataLength);
                                mSectionNumberList.get(i).add(mSectionNumber);
                                // Log.d(TAG, "onFilterEvent: Add mSectionNumber to mSectionNumberList");
                                // Log.d(TAG, "i = "+i+" Add mSectionNumber to mSectionNumberList");
                                if (mSectionNumberList.get(i).size() > mLastSectionNumberList.get(i)) {
                                    /* complete the whole table, exit */
                                    // Log.d(TAG, "onFilterEvent: Remove mMutiTransportStreamId.get(" + i + ") = " +
                                    // mMutiTransportStreamId.get(i));
                                    // Log.d(TAG, "Remove mMutiTransportStreamId.get(" + i + ") = " +
                                    // mMutiTransportStreamId.get(i));
                                    mMutiTransportStreamId.remove(i);
                                    mLastSectionNumberList.remove(i);
                                    mSectionRepeatedCountList.remove(i);
                                    mSectionNumberList.remove(i);
                                    // Log.d(TAG, "onFilterEvent: mMutiTransportStreamId.size() = " +
                                    // mMutiTransportStreamId.size());
                                    if (mMutiTransportStreamId.size() == 0) {
                                        // Log.d(TAG, "onFilterEvent: mFlagReceived = " + mFlagReceived);
                                        mFlagReceived = 0;
                                    }
                                }
                            } else { // if section number is already arrived before
                                mSectionRepeatedCountList.set(i, mSectionRepeatedCountList.get(i) + 1);
                                // Log.d(TAG, "onFilterEvent: mSectionRepeatedCountList.get(" + i + ") = " +
                                // mSectionRepeatedCountList.get(i));
                                // Log.d(TAG, "onFilterEvent: mLastSectionNumberList.get(" + i + ") = " +
                                // mLastSectionNumberList.get(i));
                                if (mSectionRepeatedCountList.get(i) > (mLastSectionNumberList.get(i) + 1)) {
                                    /* too much repeated sections */
                                    Log.d(TAG, "too much repeated sections !!");
                                }
                            }
                        } else {
                            mLastSectionNumber = Byte.toUnsignedInt(buffer[7]);
                            // Log.d(TAG, "onFilterEvent: " + Arrays.toString(buffer));
                            // Log.d(TAG, "onFilterEvent: sectionLength = " + sectionLength);
                            // Log.d(TAG, "onFilterEvent: lastSectionNumber = " + mLastSectionNumber);
                            // if new arrived section number
                            if (!mArrivedSectionNumberList.contains(mSectionNumber)) {
                                // send to com/prime/dtvplayer/Service/Table/Table.java onPesiFilterEvent
                                mFilterCompleteCallback.onPesiFilterEvent(buffer, dataLength);

                                mArrivedSectionNumberList.add(mSectionNumber);
                                Log.e(TAG, "Add mSectionNumber to mArrivedSectionNumberList");
                                if (mArrivedSectionNumberList.size() > mLastSectionNumber) {
                                    /* complete the whole table, exit */
                                    mFlagReceived = 0;
                                    Log.e(TAG, "complete the whole table and exit");
                                }
                            } else { // if section number is already arrived before
                                mSectionRepeatedCount++;
                                if (mSectionRepeatedCount > (mLastSectionNumber + 1)) {
                                    /* too much repeated sections */
                                    // Log.d(TAG,"too much repeated sections !!, mSectionRepeatedCount =
                                    // "+mSectionRepeatedCount);
                                }
                            }
                        }
                    }
                }
                // Log.d(TAG, "for (FilterEvent event : events) leave");
            }

            @Override
            public void onFilterStatusChanged(Filter filter, int status) {
            }
        };
    }

    private FilterCallback getPrivateSectionFilterCallback() {
        return new FilterCallback() {
            @Override
            public void onFilterEvent(Filter filter, FilterEvent[] events) {
                for (FilterEvent event : events) {
                    if (mAbortFlag == 1) {
                        mFlagReceived = 0;
                        Log.d(TAG, "mFlagReceived = 0");
                        break;
                    }

                    if (event instanceof SectionEvent) {
                        SectionEvent sectionEvent = (SectionEvent) event;
                        int tableId;// = sectionEvent.getTableId();
                        // int version = sectionEvent.getVersion();
                        int dataLength = sectionEvent.getDataLength();

                        if (dataLength < MIN_DATA_LENGTH_WITH_PRIVATE_SECTION) {
                            Log.e(TAG, "Data length not enough, error !!");
                            continue;
                        }

                        byte[] buffer = new byte[dataLength];
                        try {
                            filter.read(buffer, 0, dataLength);
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }

                        // section length = 12 bits of 2nd and 3rd byte
                        int sectionLength = Utils.getInt(buffer, 1, 2, Utils.MASK_12BITS);
                        /*
                         * tableId=Utils.getInt(buffer, 0, 1, Utils.MASK_8BITS);
                         * int tmp1,tmp2,tmp3;
                         * tmp1 = Utils.getInt(buffer, 3, 1, Utils.MASK_8BITS);
                         * tmp2 = Utils.getInt(buffer, 4, 1, Utils.MASK_8BITS);
                         * tmp3 = Utils.getInt(buffer, 5, 1, Utils.MASK_8BITS);
                         * if(tmp1 == 255 && tmp2 == 255 && tmp3 == 255) {
                         * break;
                         * }
                         */
                        // Log.d(TAG, "onFilterEvent: dataLength = " + dataLength);
                        // Log.d(TAG, "onFilterEvent: tableId = " + tableId);
                        // Log.d(TAG, "onFilterEvent: " + Arrays.toString(buffer));
                        // Log.d(TAG, "onFilterEvent: sectionLength = " + sectionLength);
                        if (sectionLength != dataLength - NUM_OF_BYTES_UNTIL_SECTION_LENGTH) {
                            Log.e(TAG, "Section length wrong, error !!");
                            continue;
                        }

                        // send to com/prime/dtv/Service/Table/Table.java onPesiFilterEvent
                        mFilterCompleteCallback.onPesiFilterEvent(buffer, dataLength);

                        /* complete the whole table, exit */
                        // mFlagReceived = 0;
                    }
                }
            }

            @Override
            public void onFilterStatusChanged(Filter filter, int status) {
            }
        };
    }

    private FilterCallback getDsmccFilterCallback() {
        return new FilterCallback() {
            @Override
            public void onFilterEvent(Filter filter, FilterEvent[] events) {
                for (FilterEvent event : events) {
                    if (mAbortFlag == 1) {
                        mFlagReceived = 0;
                        break;
                    }

                    if (event instanceof SectionEvent) {
                        SectionEvent sectionEvent = (SectionEvent) event;
                        int dataLength = sectionEvent.getDataLength();
                        if (dataLength < MIN_DATA_LENGTH_WITH_PRIVATE_SECTION) {
                            Log.e(TAG, "DSMCC: data length too small");
                            continue;
                        }

                        byte[] buffer = new byte[dataLength];
                        try {
                            filter.read(buffer, 0, dataLength);
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }

                        // 仍做 section_length 合法性檢查（避免破包）
                        int sectionLength = Utils.getInt(buffer, 1, 2, Utils.MASK_12BITS);
                        if (sectionLength != dataLength - NUM_OF_BYTES_UNTIL_SECTION_LENGTH) {
                            Log.e(TAG, "DSMCC: section_length mismatch");
                            continue;
                        }

                        // ✳️ 關鍵：DSM-CC 不做 section number 去重/完備檢查，直接長期轉發
                        mFilterCompleteCallback.onPesiFilterEvent(buffer, dataLength);

                        // ✳️ 不要：mFlagReceived = 0;（不結束，長期監聽）
                    }
                }
            }

            @Override
            public void onFilterStatusChanged(Filter filter, int status) {
                mFilterCompleteCallback.onPesiFilterStatusChanged(status);
            }
        };
    }

    private void createFilterCallback(byte tableId) {
        // Log.d(TAG, "mMultiTable = " +mMultiTable);
        if (tableId == Table.TDT_TABLE_ID) {
            mFilterCallback = getTdtFilterCallback();
        } else if (tableId == Table.TOT_TABLE_ID) {
            mFilterCallback = getTotFilterCallback();
        } else if (Eit.isEitTableId(tableId)) {
            mFilterCallback = getEitFilterCallback();
        } else if (((tableId == Table.SDT_OTHER_TS_TABLE_ID) || (tableId == Table.SDT_ACTUAL_TS_TABLE_ID))) {
            // Log.d(TAG, "getSdtOtherFilterCallback tableId = "
            // +Integer.toString(tableId,16) );
            mFilterCallback = getSdtOtherFilterCallback();
        } else if (((tableId == Table.EMM_EMERGENCY_TABLE_ID) || (tableId == Table.EMM_NORMAL_TABLE_ID))) {
            // Log.d(TAG, "getPrivateSectionFilterCallback()");
            mFilterCallback = getPrivateSectionFilterCallback();
        } else if (tableId == (byte) 0x3B || tableId == (byte) 0x3C) {
            // DSM-CC UN/Download data; 長期監聽、每段直送解析器
            mFilterCallback = getDsmccFilterCallback();
        } else {
            // Log.d(TAG, "getTableFilterCallback tableId = " +Integer.toString(tableId,16)
            // );
            mFilterCallback = getTableFilterCallback();
        }
    }

    public int getAbortFlag() {
        return mAbortFlag;
    }

    public void setAbortFlag(int abortFlag) {
        this.mAbortFlag = abortFlag;
    }

    public interface FilterCompleteCallback {
        void onPesiFilterEvent(byte[] data, int dataLen);

        void onPesiFilterStatusChanged(int status);
    }

    public FilterCallback getFilterCallback() {
        return mFilterCallback;
    }

    public void setFlagReceived(int flagReceived) {
        this.mFlagReceived = flagReceived;
    }

    public int getFlagReceived() {
        return mFlagReceived;
    }

    public int getSectionNumber() {
        return mSectionNumber;
    }

    public void setSectionNumber(int sectionNumber) {
        this.mSectionNumber = sectionNumber;
    }

    public int getLastSectionNumber() {
        return mLastSectionNumber;
    }

    public void setLastSectionNumber(int lastSectionNumber) {
        this.mLastSectionNumber = lastSectionNumber;
    }

    public int getSectionArrivedCount() {
        return mArrivedSectionNumberList.size();
    }

    public int getSectionRepeatedCount() {
        return mSectionRepeatedCount;
    }

    public void setSectionRepeatedCount(int sectionRepeatedCount) {
        this.mSectionRepeatedCount = sectionRepeatedCount;
    }

    public int getCheckCrc() {
        return mCheckCrc;
    }

    public void setCheckCrc(int checkCrc) {
        this.mCheckCrc = checkCrc;
    }
}
