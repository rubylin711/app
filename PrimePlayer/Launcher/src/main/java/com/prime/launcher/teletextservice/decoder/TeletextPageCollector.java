package com.prime.launcher.teletextservice.decoder;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.prime.dtv.service.subtitle.BufferParser;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Reference documentation :
 * - ETSI EN 300 472 v1.3.1 (2003-05)
 * Digital Video Broadcasting (DVB); Specification for conveying ITU-R System B Teletext in DVB
 * bitstreams
 * - ETS 300 706
 * Enhanced Teletext specification
 */
public class TeletextPageCollector extends TeletextCodec {
    private static final String TAG = "TeletextPageCollector";
    private static final boolean DEBUG = false;
    //private static final boolean DEBUG = true;
    private final int UNIT_ID_TXT_DATA = 0x02;
    private final int UNIT_ID_TXT_SUBTITLE_DATA = 0x03;
    private final int DATA_BLOCK_SIZE = 40;
    private final int MAX_DATA_BYTE_PER_PAGE = 1500;
    private final int PAGETENS_PAGEUNITS_LIMIT = 9;
    private final int PACKET0_DATABLOCK_SIZE = 38;
    private final int SUBCODE_COUNT = 5;
    private final int SUBCODE_S1 = 0;
    private static final long CLOCK_DATA_INSERT_THRESHOLD = 200L; // In ms
    private static final int PAGE_COUNT_BEFORE_INSERT = 5;
    private static final int DATA_CODING_RAW = 0;
    private static final int DATA_CODING_ODD_PARITY = 1;
    private static final int DATA_CODING_HAMMING_84 = 2;

    private int[] mDataArray;
    private int mDataCount = 0;
    private int mCurrentPageNo = 0;
    private int mPrevPacketNumber = 0;
    private int mPrevPageNo = -1;
    private int mPrevpacketno = 0;
    private int mhasSamePacketNo = 0;
    private int mSubPageNo = 0;
    private boolean mIsHeaderParsed = false;
    private ContentResolver mResolver;
    private ArrayList<ContentValues> mValueList = new ArrayList<>();
    private boolean mInsertValueList = false;
    private long mLastClockPageUpdate;
    private Context mContentContext;


    protected void reset() {
        mValueList.clear();
        mInsertValueList = false;
        mLastClockPageUpdate = 0L;
        //super.reset();
    }

    int readLsbHamByte(BufferParser parser, String description) {
        int value = parser.readInt(8, description);
        return ((value & 0x1) << 3) |
                (value & 0x4) |
                ((value & 0x10) >> 3) |
                ((value & 0x40) >> 6);
    }

    int readLsbHamInt16(BufferParser parser, String description) {
        return (readLsbHamByte(parser, description) | (readLsbHamByte(parser, description) << 4));
    }

    int readLsbParityByte(BufferParser parser) {
        int value = parser.readInt(8, "lsb parity byte");
        return (byteReverse(value) & 0x7f);
    }

    private int byteReverse(int n) {
        n = (((n >> 1) & 0x55) | ((n << 1) & 0xaa));
        n = (((n >> 2) & 0x33) | ((n << 2) & 0xcc));
        n = (((n >> 4) & 0x0f) | ((n << 4) & 0xf0));
        return n;
    }

    public TeletextPageCollector() {
        super("TeletextPageCollector");
        reset();
    }

    public void setContext(Context context) {
        mContentContext = context;
        //To store the page data
        mDataArray = new int[MAX_DATA_BYTE_PER_PAGE];
        mResolver = mContentContext.getContentResolver();
    }

    private void updateToTable(String pageSubpage, String page, String data) {
        if (page.contentEquals("0")) { // Clock data
            long time = SystemClock.uptimeMillis();
            long timeElapsed = time - mLastClockPageUpdate;
            // Allow clock data insert in next write cycle
            if (timeElapsed > CLOCK_DATA_INSERT_THRESHOLD) {
                mInsertValueList = true;
                mLastClockPageUpdate = time;
            } else { // Ignore clock data, optimisation for db usage
                return;
            }
        } else if (page.contentEquals("100")) { // Allow index page index in next write cycle
            mInsertValueList = true;
        }
        ContentValues columns = new ContentValues();
        //To put the data value
        columns.put(DtvContract.Pages.COLUMN_PAGE_SUBPAGE, pageSubpage);
        columns.put(DtvContract.Pages.COLUMN_PAGE, page);
        columns.put(DtvContract.Pages.COLUMN_DATA, data);
        mValueList.add(columns);
    }

    private void insertToDatabase() {
        if (mValueList.size() >= PAGE_COUNT_BEFORE_INSERT || mInsertValueList) {
            for (ContentValues cv : mValueList) {
                String page = cv.getAsString(DtvContract.Pages.COLUMN_PAGE);
                if ("100".equals(page)) {
                    //Log.d(TAG, "📥 Writing page 100 to DB: " + cv.toString());
                    Log.d(TAG, "📥 Writing page 100 to DB: " );
                }
            }
            ContentValues[] values = new ContentValues[mValueList.size()];
            mValueList.toArray(values);
            mResolver.bulkInsert(DtvContract.Pages.CONTENT_URI, values);
            mValueList.clear();
            mInsertValueList = false;
        }
    }

    private boolean isHeaderParsed() {
        return mIsHeaderParsed;
    }

    private void setHeaderParsed(boolean parsed) {
        mIsHeaderParsed = parsed;
    }

    private int parsingPageNumber(int magazine, int pageTens, int pageUnits) {
        // FF page code used for clock update, will be saved in form of 0:0
        if (pageUnits == 15 && pageTens == 15) {
            pageUnits = 0;
            pageTens = 0;
            magazine = 0;
        }
        //To allow only decimal page number. We are not allowing to store the hex value pages
        //Except additional components page
        if (pageTens > PAGETENS_PAGEUNITS_LIMIT || pageUnits > PAGETENS_PAGEUNITS_LIMIT) {
            if (pageTens != 0xF && !(pageUnits == 0x0 || pageUnits == 0xE)) {
                setHeaderParsed(false);
                return -1;
            }
        }
        //To get the value in hex
        String pageUnitStr = Integer.toHexString(pageUnits);
        String pageTenStr = Integer.toHexString(pageTens);
        String magazineNo = Integer.toString(magazine);
        String currPageNo = magazineNo + pageTenStr + pageUnitStr;

        //To store the current page number
        mCurrentPageNo = Integer.parseInt(currPageNo, 16);

        // Logic to detect single page reception end
        if (mCurrentPageNo != mPrevPageNo && mPrevPageNo != -1
                || mPrevPacketNumber != 0 && mCurrentPageNo == mPrevPageNo) {
            //Using below to reduce the data storing in pages table
            int[] dataArray = new int[mDataCount + 1];
            System.arraycopy(mDataArray, 0, dataArray, 0, dataArray.length);

            //To convert page data to string for insert into table
            String PageDataStr = Arrays.toString(dataArray).replaceAll("\\s+", "");

            //To form the page number as per EN 300 706 spec
            String sCurPageNo = Integer.toString(mPrevPageNo);
            String sSubPageNo = Integer.toString(mSubPageNo);

            String pageSubpageKey = String.format("%x:%x", mPrevPageNo, mSubPageNo);
            if (DEBUG) {
                Log.d(TAG, "Inserting [" + sCurPageNo + "] m-page[" + String.format("%x:%x",
                        mPrevPageNo, mSubPageNo) + "] sSubPageNo[" + sSubPageNo + "]");
            }
            //Update to Pages Table
            if (mPrevPageNo != 0) {
                updateToTable(pageSubpageKey, String.format("%x", mPrevPageNo), PageDataStr);
            } else {
                updateToTable(sCurPageNo + ":" + sSubPageNo, sCurPageNo, PageDataStr);
            }

            //To fill the array as zero after update the data into the pages table
            Arrays.fill(dataArray, 0);
            Arrays.fill(mDataArray, 0);

            //To reset variables
            mDataCount = 0;
            setHeaderParsed(false);
            mPrevpacketno = 0;
            mhasSamePacketNo = 0;
        }
        return mCurrentPageNo;
    }

    //To store packet-0 header data in array
    private void parsingPacket0(BufferParser parser, int packetNumber, int reserved,
            int fieldParity, int lineOffset, int framingCode, int magazineAndPacketAddress) {
        if (DEBUG) {
            Log.d(TAG, "mDataCount:"+ mDataCount);
        }
        mDataArray[mDataCount] = packetNumber;
        mDataArray[++mDataCount] = reserved;
        mDataArray[++mDataCount] = fieldParity;
        mDataArray[++mDataCount] = lineOffset;
        mDataArray[++mDataCount] = framingCode;
        mDataArray[++mDataCount] = magazineAndPacketAddress;
        mDataArray[++mDataCount] = mCurrentPageNo;

        if (DEBUG) {
            Log.d(TAG, "parsingPacket0 page[" + String.format("%x", mCurrentPageNo) + "]");
        }
        //To store data block of packet0
        for (int count = 0; count < PACKET0_DATABLOCK_SIZE; count++) {
            if (count <= SUBCODE_COUNT) {
                int dataBlock = readLsbHamByte(parser, "packet0_datablock");
                mDataArray[++mDataCount] = dataBlock;

                //To store the sub-page number from the data block of packet0
                if (count == SUBCODE_S1) {
                    mSubPageNo = dataBlock;
                }
            } else {
                //To read 32 byte data block for display page header
                int dataBlock = readLsbParityByte(parser);
                mDataArray[++mDataCount] = dataBlock;
            }
        }
    }

    //To store packet 1-25 header data in array
    private void parsingPacket1to25(BufferParser parser, int byteCoding, int packetNumber,
                                    int reserved, int fieldParity, int lineOffset, int framingCode,
                                    int magazineAndPacketAddress) {
        mDataArray[++mDataCount] = packetNumber;
        mDataArray[++mDataCount] = reserved;
        mDataArray[++mDataCount] = fieldParity;
        mDataArray[++mDataCount] = lineOffset;
        mDataArray[++mDataCount] = framingCode;
        mDataArray[++mDataCount] = magazineAndPacketAddress;

        if (DEBUG) {
            Log.d(TAG, "parsingPacket1to25 packet[" + packetNumber + "] page["
                    + String.format("%x", mPrevPageNo) + "] byteCoding[" + byteCoding + "]");
        }
        //To store data block of packet1-25
        for (int count = 0; count < DATA_BLOCK_SIZE; ++count) {
            int dataBlock;
            switch (byteCoding) {
                case DATA_CODING_RAW:
                    dataBlock = parser.readInt(8, "packet1to25_datablock");
                    break;
                case DATA_CODING_ODD_PARITY:
                    dataBlock = readLsbParityByte(parser);
                    break;
                case DATA_CODING_HAMMING_84:
                default:
                    dataBlock = readLsbHamByte(parser, "packet1to25_datablock");
                    break;
            }
            mDataArray[++mDataCount] = dataBlock;
        }
    }

    //To store packet 26-31 header data in array
    private void parsingPacket26to31(BufferParser parser, int byteCoding, int packetNumber,
                                     int reserved, int fieldParity, int lineOffset, int framingCode,
                                     int magazineAndPacketAddress) {
        mDataArray[++mDataCount] = packetNumber;
        mDataArray[++mDataCount] = reserved;
        mDataArray[++mDataCount] = fieldParity;
        mDataArray[++mDataCount] = lineOffset;
        mDataArray[++mDataCount] = framingCode;
        mDataArray[++mDataCount] = magazineAndPacketAddress;

        if (packetNumber == 27) {
            //To store data block of packet27(coded hamming data)
            for (int count = 0; count < DATA_BLOCK_SIZE; ++count) {
                int dataBlock = readLsbHamByte(parser, "packet27_datablock");
                mDataArray[++mDataCount] = dataBlock;
            }
        } else {
            //To store data block of packet1-25
            for (int count = 0; count < DATA_BLOCK_SIZE; ++count) {
                int dataBlock = parser.readInt(8, "packet26_31_datablock");
                mDataArray[++mDataCount] = dataBlock;
            }
        }
    }

    private void parseTeletextDataField(BufferParser parser) {
        // -----Packet header-----
        int reserved = parser.readInt(2, "reserved_future_use");
        int fieldParity = parser.readInt(1, "field_parity");
        int lineOffset = parser.readInt(5, "line_offset");
        int framingCode = readLsbHamByte(parser, "framing_code");
        int magazineAndPacketAddress = readLsbHamInt16(parser, "magazine_and_packet_address");
        //To get the magazine and packet number
        int magazine = (0x7 & magazineAndPacketAddress) == 0 ? 8 : (0x7 & magazineAndPacketAddress);
        int packetNumber = (magazineAndPacketAddress >> 0x3);
        // Parsing Packet - 0
        if (packetNumber == 0) {
            int pageUnits = readLsbHamByte(parser, "page_units");
            int pageTens = readLsbHamByte(parser, "page_tens");
            parsePacket(parser, packetNumber, magazine, pageTens, pageUnits, reserved,
                    fieldParity, lineOffset, framingCode, magazineAndPacketAddress);
        } else { // Parsing Packet - !0
            parsePacket(parser, packetNumber, magazine, 0, 0,
                    reserved, fieldParity, lineOffset, framingCode, magazineAndPacketAddress);
        }
    }

    private void parsePacket(BufferParser parser, int packetNumber, int magazine,
                                        int pageTens, int pageUnits, int reserved, int fieldParity,
                                        int lineOffset, int framingCode,
                                        int magazineAndPacketAddress) {
        if (packetNumber == 0) { // Parsing packet 0
            int pageNumber = parsingPageNumber(magazine, pageTens, pageUnits);
            if (!isHeaderParsed() && pageNumber != -1) {
                parsingPacket0(parser, packetNumber, reserved, fieldParity,
                        lineOffset, framingCode, magazineAndPacketAddress);
                mPrevPageNo = pageNumber;
                setHeaderParsed(true);
            } else {
                //To avoid for invalid page data insertion
                return;
            }
        } else if (packetNumber <= 25 && isHeaderParsed()) { //Parsing Packet 1-25
            //For store packet data incrementally
            if (packetNumber > mPrevpacketno) {
                //Packet1to25 -parsing
                int byteCoding;
                /**
                 * Reference ETSI EN 300 706 - E.15
                 * BTT is 0x1F0 per specification, others are recommendation
                 * To avoid BTT parsing in collector assumption is that it is per recommendation
                 */
                if (mCurrentPageNo == 0x1F0 || mCurrentPageNo == 0x1F1 || mCurrentPageNo == 0x1F2
                        || mCurrentPageNo == 0x1F3 || mCurrentPageNo == 0x1F4) {
                    byteCoding = DATA_CODING_RAW;
                } else {
                    byteCoding = DATA_CODING_ODD_PARITY;
                }
                parsingPacket1to25(parser, byteCoding, packetNumber, reserved, fieldParity,
                        lineOffset, framingCode, magazineAndPacketAddress);
                //To store packet number
                mPrevpacketno = packetNumber;
            }
        } else if (packetNumber <= 31 && isHeaderParsed())    //Parsing Packet 26-31
        {
            //To avoid same packet storing
            if (packetNumber != mhasSamePacketNo) {
                //Packet26to31 -parsing
                parsingPacket26to31(parser, DATA_CODING_ODD_PARITY, packetNumber, reserved,
                        fieldParity, lineOffset, framingCode, magazineAndPacketAddress);
                mhasSamePacketNo = packetNumber;
            }
        }
        mPrevPacketNumber = packetNumber;
    }
    
    @Override
    public void parse(InputBuffer inputBuffer) {
        //Log.d(TAG, "✅ parse() triggered with size = " + inputBuffer.size);
        //Log.d(TAG, "HEX dump of PES: " + bytesToHex(inputBuffer.buffer.array(), inputBuffer.offset, Math.min(inputBuffer.size, 16)));
        BufferParser parser = new BufferParser();
        parser.setBytes(inputBuffer.buffer.array(), inputBuffer.offset, inputBuffer.size);
        
        int dataIdentifier = 0;
        do {
            //To get the valid data identifier from the pes data
            dataIdentifier = parser.readInt(8, "data_identifier");
        } while (dataIdentifier == 0xFF);

        if (dataIdentifier < 0x10 || dataIdentifier > 0x1F) {
            Log.w(TAG, "Invalid data identifier: " + dataIdentifier);
            notifyInputBuffer(inputBuffer);
            return;
        }

        int unitId = 0;
        int unitLength = 0;

        while (parser.getPosInBytes() < inputBuffer.size) {
            int startPos = parser.getPosInBytes();
            //To get the unit-Id and data unit Length
            unitId = parser.readInt(8, "data_unit_id");
            unitLength = parser.readInt(8, "data_unit_length");

            // unitLength must be equals to 0x02 (cf 300472, section 4.4), but 0x03 is handled
            // also to support some streams which have full-page teletext under this unit id
            if (unitId == UNIT_ID_TXT_DATA || unitId == UNIT_ID_TXT_SUBTITLE_DATA) {
                parseTeletextDataField(parser);
            }
            parser.setPosInBits((startPos + unitLength + 2) * 8);
        }
        // Bulk insert when whole input buffer parsed
        insertToDatabase();
        notifyInputBuffer(inputBuffer);
    }
	private static String bytesToHex(byte[] bytes, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + length; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString();
    }

}
