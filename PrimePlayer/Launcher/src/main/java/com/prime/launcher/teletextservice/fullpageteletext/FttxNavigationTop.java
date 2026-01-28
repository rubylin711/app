package com.prime.launcher.teletextservice.fullpageteletext;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class FttxNavigationTop extends FttxNavigationBase {
    private static final String TAG = "FttxNavigationTop";
    private static final boolean DEBUG = false;
    //private static final boolean DEBUG = true;
    private static final int DEFAULT_LINK0_LENGTH = (FttxPage.MAX_CHARS_BY_LINE - 24) / 2;
    private static final int DEFAULT_LINK1_LENGTH = DEFAULT_LINK0_LENGTH;
    private static final int DEFAULT_LINK2_LENGTH = 12;
    private static final int DEFAULT_LINK3_LENGTH = 12;
    private static final int PACKET_1_23_INDEX = 46;
    private static final int PACKET_1_23_DATA_BYTE_POS = 6;

    public enum BttPageTypes {
        BTT_NO_PAGE,
        BTT_SUBTITLE,
        BTT_PROGR_INDEX_S,
        BTT_PROGR_INDEX_M,
        BTT_BLOCK_S,
        BTT_BLOCK_M,
        BTT_GROUP_S,
        BTT_GROUP_M,
        BTT_NORMAL_S,
        BTT_NORMAL_9,
        BTT_NORMAL_M,
        BTT_NORMAL_11,
        BTT_12,
        BTT_13,
        BTT_14,
        BTT_15,
        BTT_FF
    }

    public enum TopPageTypes {
        TOP_PAGE_TYPE_UNKNOWN,
        TOP_PAGE_TYPE_MPT,
        TOP_PAGE_TYPE_AIT,
        TOP_PAGE_TYPE_MPT_EXT
    }

    public class TopPage {
        TopPageTypes type;
        int pageNumber;
        int subPagenumber;
        char []text;
    }

    public static class AitPage {
        int pageNumber;
        boolean updated;
    }

    boolean mBttUpdated;
    boolean mAitUpdated;
    ArrayList<TopPage> mGroupBlockPages;
    ArrayList<AitPage> mAitPages;
    HashMap<Integer, Integer> mNormalMap;
    HashMap<Integer, Integer> mGroupMap;
    HashMap<Integer, Integer> mBlockMap;
    private FttxGXCharset mCharset;

    public FttxNavigationTop(PageReader pageReader) {
        super(FttxNavigationBase.NAVIGATION_TYPE_TOP, pageReader);
        mGroupBlockPages = new ArrayList<>();
        mAitPages = new ArrayList<>();
        mNormalMap = new HashMap<>();
        mGroupMap = new HashMap<>();
        mBlockMap = new HashMap<>();
        mBttUpdated = false;
        mAitUpdated = false;
    }

    public void reset() {
        mGroupBlockPages.clear();
        mAitPages.clear();
        mBttUpdated = false;
        mAitUpdated = false;
        mNormalMap.clear();
        mGroupMap.clear();
        mBlockMap.clear();
    }

    @Override
    public void build(FttxPage page) {
        Log.d(TAG, "build BTT[" + mBttUpdated + "] AIT[" + mAitUpdated + "]");
        mCharset = page.getCharset();
        parseBtt(page);
        parseAit(page);

        /**
         * --------------------------------------------------------
         * TOP : Build a line :   - / + /  Group+1  / Block+1
         * --------------------------------------------------------
         */
        boolean parseSuccess = mBttUpdated && mAitUpdated;
        if (parseSuccess) {
            //printDebugInfo();

            FttxPage.FttxPageLink pageLink0 = new FttxPage.FttxPageLink();
            pageLink0.pageNumber = page.previousPageNumber();
            pageLink0.pos = 0;
            pageLink0.codes = new char[] { '-' };

            FttxPage.FttxPageLink pageLink1 = new FttxPage.FttxPageLink();
            pageLink1.pageNumber = page.nextPageNumber();
            pageLink1.pos = DEFAULT_LINK0_LENGTH;
            pageLink1.codes = new char[] { '+' };

            // Try to find an existing page in current magazine
            int currentMagazineNumber = page.currentMagazineNorm();
            int nextMagazineNumber = page.nextMagazineNorm();
            pageLink0.pageNumber = pageLink0.pageNumber < currentMagazineNumber ?
                    currentMagazineNumber : pageLink0.pageNumber;
            pageLink1.pageNumber = pageLink1.pageNumber >= nextMagazineNumber ?
                    page.getPageNumber() : pageLink1.pageNumber;

            int prevPageNumber = pageLink0.pageNumber;
            for (;prevPageNumber > currentMagazineNumber; prevPageNumber--) {
                if (normalPageExists(prevPageNumber) || groupPageExists(prevPageNumber)) {
                    pageLink0.pageNumber = prevPageNumber;
                    break;
                }
            }

            int nextPageNo = pageLink1.pageNumber;
            for (;nextPageNo < nextMagazineNumber; nextPageNo++) {
                if (normalPageExists(nextPageNo) || groupPageExists(nextPageNo)) {
                    pageLink1.pageNumber = nextPageNo;
                    break;
                }
            }

            page.updatePageLink(NAVIGATION_POS_RED, pageLink0);
            page.updatePageLink(NAVIGATION_POS_GREEN, pageLink1);

            int groupLinkPage = -1;
            int blockLinkPage = -1;
            for (int pageNumber = page.getPageNumber() + 1;
                 pageNumber < 900 && (groupLinkPage == -1 || blockLinkPage == -1); pageNumber++) {
                if (groupLinkPage == -1) {
                    // Wrap after 800
                    groupLinkPage = mGroupMap.getOrDefault(pageNumber < 800 ?
                            pageNumber : pageNumber - 700, -1);
                }
                if (blockLinkPage == -1) {
                    blockLinkPage = mBlockMap.getOrDefault(pageNumber, -1);
                }
            }

            // Group
            FttxPage.FttxPageLink pageLink2 = new FttxPage.FttxPageLink();
            TopPage topPage = getPage(groupLinkPage);
            if (topPage != null) {
                // Need decimal interpretation of HEX value
                pageLink2.pageNumber = Integer.valueOf(Integer.toHexString(topPage.pageNumber));
                pageLink2.pos = pageLink1.pos + DEFAULT_LINK1_LENGTH;
                pageLink2.codes = topPage.text != null ? topPage.text
                        : String.format("    %3X    ", topPage.pageNumber).toCharArray();
            } else {
                // Page number + 10
                pageLink2.pageNumber = page.nextTenPageNumber();
                pageLink2.pos = pageLink1.pos + DEFAULT_LINK1_LENGTH;
                pageLink2.codes = String.format("    %3d    ", pageLink2.pageNumber).toCharArray();
            }

            // Block
            FttxPage.FttxPageLink pageLink3 = new FttxPage.FttxPageLink();
            topPage = getPage(blockLinkPage);
            if (topPage != null) {
                // Need decimal interpretation of HEX value
                pageLink3.pageNumber = Integer.valueOf(Integer.toHexString(topPage.pageNumber));
                pageLink3.pos = pageLink2.pos + DEFAULT_LINK2_LENGTH;
                pageLink3.codes = topPage.text != null ? topPage.text
                        : String.format("    %3X    ", topPage.pageNumber).toCharArray();
            } else {
                // Magazine + 1
                pageLink3.pageNumber = 100; // Will be interpreted as a HEX value in db query
                pageLink3.pos = pageLink2.pos + DEFAULT_LINK2_LENGTH;
                Log.d(TAG, "Block link created[" + pageLink3.pageNumber + "]");
                // Check existence and move forward if needed
                int pageNoNorm = page.nextMagazineNorm();
                for (; pageNoNorm < 800; pageNoNorm += 100) {
                    if (blockPageExists(pageNoNorm)) {
                        pageLink3.pageNumber = pageNoNorm;
                        break;
                    }
                }
                // Try to find label for it
                topPage = getPage(pageLink3.pageNumber);
                if (topPage != null) {
                    Log.d(TAG, "Block link found [" + new String(topPage.text) + "]");
                    pageLink3.codes = topPage.text;
                } else {
                    pageLink3.codes = String.format("    %3d    ", pageLink3.pageNumber).toCharArray();
                }
            }

            Log.d(TAG, "Build navigation -[" + pageLink0.pageNumber + "] +["
                    + pageLink1.pageNumber + "] G[" +  pageLink2.pageNumber + "] B["
                    + pageLink3.pageNumber + "]");

            page.updatePageLink(NAVIGATION_POS_YELLOW, pageLink2);
            page.updatePageLink(NAVIGATION_POS_CYAN, pageLink3);

            page.createNavigationLine();
        }
    }

    @Override
    public boolean exists() {
        return mBttUpdated;
    }

    public boolean isUpdated() {
        return mBttUpdated;
    }

    private void parseBtt(FttxPage page) {
        if (!mBttUpdated) {
            long t1 = System.currentTimeMillis();
            String[] data = mPageReader.readPage(0x1F0);
            Log.d(TAG, "⚠️ readPage() 耗時: " + (System.currentTimeMillis() - t1) + " ms");
            if (data != null) {
                int pageNumber = 100;
                int packetNumber = Integer.parseInt(data[0]);

                // Parse lines(packets) Y[1,23] in two parts Y[1,19] and Y[20,23]
                // Parse page props Y[1,20] 100-899
                int line = PACKET_1_23_INDEX - 1;
                for (; line < data.length; line += PACKET_1_23_INDEX) {
                    packetNumber = Integer.parseInt(data[line]);
                    if (packetNumber > 20) {
                        break;
                    }
                    boolean lineExist = false;
                    if (packetNumber > 0 && packetNumber < 24) { //Y = 1-23
                        lineExist = true;
                    }
                    if (DEBUG) {
                        Log.d(TAG, "parseBtt packetNumber[" + packetNumber + "]");
                    }
                    if (lineExist) {
                        int lineStart = line + PACKET_1_23_DATA_BYTE_POS;
                        int lineEnd = line + PACKET_1_23_DATA_BYTE_POS + FttxPage.MAX_CHARS_BY_LINE;
                        for (int column = lineStart; column < lineEnd; column++) {
                            // Additional components data are stored in raw format
                            int code = readLsbHamByte(Integer.parseInt(data[column]));
                            BttPageTypes btt = BttPageTypes.BTT_NO_PAGE;
                            if (code >= BttPageTypes.BTT_NO_PAGE.ordinal()
                                    && code <= BttPageTypes.BTT_FF.ordinal()) {
                                btt = BttPageTypes.values()[code];
                            }
                            switch (btt) {
                                case BTT_SUBTITLE:
                                case BTT_PROGR_INDEX_S:
                                case BTT_PROGR_INDEX_M:
                                case BTT_NORMAL_S:
                                case BTT_NORMAL_9:
                                case BTT_NORMAL_M:
                                case BTT_NORMAL_11:
                                case BTT_12:
                                case BTT_13:
                                case BTT_14:
                                case BTT_15: {
                                    // Normal page
                                    mNormalMap.put(pageNumber, pageNumber);
                                    break;
                                }
                                case BTT_BLOCK_S:
                                case BTT_BLOCK_M: {
                                    // Block page
                                    mBlockMap.put(pageNumber, pageNumber);
                                    break;
                                }
                                case BTT_GROUP_S:
                                case BTT_GROUP_M: {
                                    // Group page
                                    mGroupMap.put(pageNumber, pageNumber);
                                    break;
                                }
                                case BTT_NO_PAGE:
                                case BTT_FF:
                                default: {
                                    // No page
                                    break;
                                }
                            }
                            pageNumber++;
                        }
                    } else {
                        pageNumber += FttxPage.MAX_CHARS_BY_LINE;
                    }
                }

                // Parse page linking table Y[21,23]
                for (; line < data.length; line += PACKET_1_23_INDEX) {
                    packetNumber = Integer.parseInt(data[line]);
                    int lineStart = line + PACKET_1_23_DATA_BYTE_POS;
                    int lineEnd = line + PACKET_1_23_DATA_BYTE_POS + FttxPage.MAX_CHARS_BY_LINE;
                    /* 5 * 8 bytes in line */
                    for (int column = lineStart; column < lineEnd; column += 8) {
                        TopPage topPage = parseToPage(column, data);
                        switch (topPage.type) {
                            case TOP_PAGE_TYPE_AIT: {
                                if (DEBUG) {
                                    Log.d(TAG, "parseBtt packetNumber[" + packetNumber
                                            + "] adding AIT PAGE topPage.pageNumber["
                                            + topPage.pageNumber + "]");
                                }
                                boolean found = false;
                                for (AitPage aitPage : mAitPages) {
                                    if (aitPage.pageNumber == topPage.pageNumber) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found && mAitPages.size() < 3) {
                                    AitPage ait = new AitPage();
                                    ait.pageNumber = topPage.pageNumber;
                                    mAitPages.add(ait);
                                }
                                break;
                            }
                            case TOP_PAGE_TYPE_MPT:
                                Log.d(TAG, "parseBtt packetNumber[" + packetNumber + "] MPT");
                                break;
                            case TOP_PAGE_TYPE_MPT_EXT:
                                Log.d(TAG, "parseBtt packetNumber[" + packetNumber + "] MPT-EXT");
                                break;
                            default:
                                break;
                        }
                    }
                }
                mBttUpdated = true;
            }
        }
    }

    private void parseAit(FttxPage page) {
        if (mBttUpdated && !mAitUpdated) {
            for (AitPage aitPage : mAitPages) {
                if (!aitPage.updated) {
                    String[] data = mPageReader.readPage(aitPage.pageNumber);
                    if (data != null) {
                        int packetNumber = Integer.parseInt(data[0]);
                        int line = PACKET_1_23_INDEX - 1;
                        for (; line < data.length; line += PACKET_1_23_INDEX) {
                            packetNumber = Integer.parseInt(data[line]);
                            if (packetNumber > 23) {
                                break;
                            }
                            int lineStart = line + PACKET_1_23_DATA_BYTE_POS;
                            int lineEnd = line + PACKET_1_23_DATA_BYTE_POS
                                    + FttxPage.MAX_CHARS_BY_LINE;
                            Log.d(TAG, "parseAit packetNumber[" + packetNumber+ "]");

                            TopPage groupBlockPage = parseToPage(lineStart, data); // 8 length
                            groupBlockPage.text = parseText(lineStart + 8, data); // 12 length
                            mGroupBlockPages.add(groupBlockPage);

                            groupBlockPage = parseToPage(lineStart + 20, data); // 8 length
                            groupBlockPage.text = parseText(lineStart + 28, data); // 12 length
                            mGroupBlockPages.add(groupBlockPage);
                        }
                        aitPage.updated = true;
                    }
                }
            }

            mAitUpdated = true;
            for (AitPage aitPage : mAitPages) {
                if (!aitPage.updated) {
                    mAitUpdated = false;
                    break;
                }
            }
        }
    }

    /**
     * Parse 8 bytes of data for page and sub-page number as well as page type
     * Additional components data are stored in raw format
     */
    private TopPage parseToPage(int dataPos, String[] data) {
        int[] decData = new int[8];
        StringBuilder pageInfo = new StringBuilder();
        TopPage topPage = new TopPage();
        topPage.pageNumber = 0;
        topPage.subPagenumber = 0;

        for (int i = 0; i < 8; i++) {
            decData[i] = readLsbHamByte(Integer.parseInt(data[dataPos + i]));
            pageInfo.append(data[i]);
        }

        // Page
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (; i < 3; i++) {
            int digit = readLsbHamByte(Integer.parseInt(data[dataPos + i]));
            topPage.pageNumber |= digit << (4 * (2 - i));
            sb.append(Integer.toHexString(digit));
        }
        String pageNumber = sb.toString();

        // Subpage
        sb = new StringBuilder();
        for (; i < 7; i++) {
            int digit = readLsbHamByte(Integer.parseInt(data[dataPos + i]));
            topPage.subPagenumber += digit << (4 * (6 - i));
            sb.append(Integer.toHexString(digit));
        }
        topPage.subPagenumber &= 0x3f7f;
        String subPageNumber = sb.toString();

        // Page type
        int typeCode = readLsbHamByte(Integer.parseInt(data[dataPos + i]));
        Log.d(TAG, "parseToPage page[" + pageNumber + "] page["
                + Integer.toHexString(topPage.pageNumber) + "] subPageNumber[" + subPageNumber
                + "] subPage[" + Integer.toHexString(topPage.subPagenumber) + "] pageType["
                + typeCode + "]");
        switch (typeCode) {
            case 1:
                topPage.type = TopPageTypes.TOP_PAGE_TYPE_MPT;
                break;
            case 2:
                topPage.type = TopPageTypes.TOP_PAGE_TYPE_AIT;
                break;
            case 3:
                topPage.type = TopPageTypes.TOP_PAGE_TYPE_MPT_EXT;
                break;
            default:
                topPage.type = TopPageTypes.TOP_PAGE_TYPE_UNKNOWN;
                break;
        }

        return topPage;
    }

    /**
     * Parse 12 bytes of data for link label
     * Additional components data are stored in raw format
     */
    char[] parseText(int dataPos, String[] data) {
        char[] label = new char[12];
        for (int i = 0; i < 12; i++) {
            // Decode byte then map to G0 char
            label[i] = mCharset.getG0Char(readLsbParityByte(Integer.parseInt(data[dataPos + i])));
        }
        return label;
    }

    /**
     * Find a navigation page (group or block) from AIT
     */
    private TopPage getPage(int pageNumber) {
        int converted = reinterpretDecAsHex(pageNumber);
        if (DEBUG) {
            Log.d(TAG, "getPage pageNumber[" + pageNumber + "] converted[" +
                    String.format("%x", converted) + "]");
        }
        for (TopPage page : mGroupBlockPages) {
            if (page.pageNumber == converted) {
                return page;
            }
        }
        return null;
    }

    /**
     * If page exists amongst group or block pages
     * it will be searched further for label
     */
    private boolean blockPageExists(int pageNumber) {
        if (mBlockMap.containsKey(pageNumber)) {
            return true;
        } else if (mGroupMap.containsKey(pageNumber)) {
            return true;
        }
        return false;
    }

    private boolean normalPageExists(int pageNumber) {
        if (mNormalMap.containsKey(pageNumber)) {
            return true;
        }
        return false;
    }

    private boolean groupPageExists(int pageNumber) {
        if (mGroupMap.containsKey(pageNumber)) {
            return true;
        }
        return false;
    }

    private void printDebugInfo() {
        for (TopPage topPage : mGroupBlockPages) {
            Log.d(TAG, "Build parsed group-block page[" + Integer.toHexString(topPage.pageNumber)
                    + "] subpage[" + topPage.subPagenumber + "] text[" +
                    String.format("%s", new String(topPage.text)) + "] type[" + topPage.type + "]");
        }

        for (Integer key : mNormalMap.keySet()) {
            Log.d(TAG, "Normal [" + key + "] [" + Integer.toHexString(key) + "]");
        }

        for (Integer key : mGroupMap.keySet()) {
            Log.d(TAG, "Group [" + key + "] [" + Integer.toHexString(key) + "]");
        }

        for (Integer key : mBlockMap.keySet()) {
            Log.d(TAG, "Block [" + key + "] [" + Integer.toHexString(key) + "]");
        }
    }
}
