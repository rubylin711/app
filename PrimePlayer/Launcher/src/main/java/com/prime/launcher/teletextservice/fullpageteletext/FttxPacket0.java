package com.prime.launcher.teletextservice.fullpageteletext;

import android.util.Log;

public class FttxPacket0 {
    private static final String TAG = "FttxPacket0";
    private static final int PKT0_PAGE_SELECTION_POS = 1;
    private static final int PKT0_DATA_BLOCK_SIZE = 24;
    private static final int PKT0_CLOCK_POS = 32;
    private static final int PKT0_CLOCK_BLOCK_SIZE = 8;
    private int pagesubpageno[];
    FttxLine line;
    boolean c4; // Erase page
    boolean c5; // Newsflash
    boolean c6; // Subtitle
    boolean c7;  // Suppress header
    boolean c8;  // Update indicator
    boolean c9;  // Interrupted sequence
    boolean c10; // Inhibit display
    boolean c11; // Magazine serial
    boolean c12; // National option character subset
    boolean c13; // National option character subset
    boolean c14; // National option character subset
    private static String[] sClockData;
    private static char[] sPageSelection;

    public FttxPacket0() {
        this.pagesubpageno = new int[10];
    }

    private void parseControlBytes(int byte9, int byte11, int byte12, int byte13) {
        c4 = ((byte9 >> 3) & 0x1) == 0x1;
        c5 = ((byte11 >> 2) & 0x1) == 0x1;
        c6 = ((byte11 >> 3) & 0x1) == 0x1;
        c7 = ((byte12) & 0x1) == 0x1;
        c8 = ((byte12 >> 1) & 0x1) == 0x1;
        c9 = ((byte12 >> 2) & 0x1) == 0x1;
        c10 = ((byte12 >> 3) & 0x1) == 0x1;
        c11 = ((byte13) & 0x1) == 0x1;
        c12 = ((byte13 >> 1) & 0x1) == 0x1;
        c13 = ((byte13 >> 2) & 0x1) == 0x1;
        c14 = ((byte13 >> 3) & 0x1) == 0x1;
    }

    public int parse(int dataByte, String[] items, FttxPage page, int pageNumber, int subpageNumber,
            int byte9, int byte11, int byte12, int byte13) {
        Log.d(TAG, "*****packet_0 parse: pgNo: " + pageNumber + ", + spgNo: " + subpageNumber);

        parseControlBytes(byte9, byte11, byte12, byte13);

        //Draw Packet 0 line in subtitle page only while selecting new page number
        if (c6 && sPageSelection == null) {
            line.clear(true);
            return 1;
        }

        int nationalSubset =
                ((byte13 >> 3) & 0x1) | // c14
                        ((byte13 >> 1) & 0x2) | // c13
                        ((byte13 << 1) & 0x4); // c12;

        // clear page
        if (c4) {
            page.clear();
        }

        page.setNationalSubsetCode(nationalSubset);

        // header test code
        int pos = 0;
        pagesubpageno[0] = 0x2;    // green alpha color
        pagesubpageno[1] = pageNumber;
        pagesubpageno[2] = 0x2E;
        pagesubpageno[3] = subpageNumber;
        pagesubpageno[4] = 0x7;    // white alpha color
        line = page.getLine(0);
        line.clear(c6);

        page.setPageNumber(pageNumber);
        for (int j = 0; j < 5; j++) {
            switch (j) {
                // pagenumber 3digit display by using unicode index number
                case 1:
                    String pageStr = Integer.toString(pageNumber);
                    for (int k = 0; k < pageStr.length(); k++) {
                        line.setCode(pos, pageStr.codePointAt(k), page.getCharset(), c6);
                        ++pos;
                    }
                    break;
                // subpage 2digit display by using unicode index number
                case 3:
                    String subPageStr = Integer.toString(subpageNumber);
                    if (subpageNumber <= 9) {
                        subPageStr = "0" + subPageStr;
                    }

                    for (int n = 0; n < 2; n++) {
                        line.setCode(pos, subPageStr.codePointAt(n), page.getCharset(),
                                c6);
                        ++pos;
                    }
                    break;

                // other than pageno & subpageno
                default:
                    line.setCode(pos, pagesubpageno[j], page.getCharset(), c6);
                    ++pos;
                    break;
            }
        }

        for (int i = 0; i < PKT0_DATA_BLOCK_SIZE; ++i) {
            int code = Integer.parseInt(items[dataByte + i]);
            line.setCode(pos, code, page.getCharset(), c6);
            ++pos;
        }

        setPageNumberSelection(page);
        setClockDataToPage(page);

        page.setUpdated(true);
        page.setHeaderParsed(true);

        if (!c6) {
            return pageNumber;
        } else {
            return 1;
        }
    }

    /**
     * Check if clock data update is enabled
     */
    public boolean updateClock() {
        return !c7 && !c10;
    }

    public String[] getClockData() {
        synchronized (this) {
            return sClockData;
        }
    }

    /**
     * Update current with the latest clock data
     * @param clockData clock data
     */
    public void updateClockData(String[] clockData) {
        synchronized (this) {
            sClockData = clockData;
        }
    }

    /**
     * Compare clock data
     * @param clockData clock data to compare
     * @return true if equals
     */
    public boolean isClockDataUpToDate(String[] clockData) {
        boolean equals = true;
        synchronized (this) {
            if (sClockData == null) {
                return false;
            }
            for (int i = 7; i >= 0; --i) {
                if (sClockData[i] != null && !sClockData[i].contentEquals(clockData[i])) {
                    equals = false;
                    break;
                }
            }
            return equals;
        }
    }

    /**
     * Set clock data to page
     * @param page page to be updated with clock data
     */
    public void setClockDataToPage(FttxPage page) {
        synchronized (this) {
            if (sClockData == null || (c6 && sPageSelection == null)) {
                return;
            }
            int clockPos = PKT0_CLOCK_POS;
            for (int i = 0; i < PKT0_CLOCK_BLOCK_SIZE; ++i) {
                int code = Integer.parseInt(sClockData[i]);
                line.setCode(clockPos, code, page.getCharset(), c6);
                ++clockPos;
            }
        }
    }

    public void updatePageNumberSelection(char[] pageSelection) {
        synchronized (this) {
            sPageSelection = pageSelection;
        }
    }

    public void setPageNumberSelection(FttxPage page) {
        synchronized (this) {
            int startPos = PKT0_PAGE_SELECTION_POS;
            char[] pageChars = sPageSelection == null ?
                    Integer.toString(pagesubpageno[1]).toCharArray() : sPageSelection;
            for (char num : pageChars) {
                page.setCharAt(startPos, 0, num);
                startPos++;
            }
        }
    }

    public int getPageNumber() {
        return pagesubpageno[1];
    }

    public int getSubpageNumber() {
        return pagesubpageno[3];
    }

    public int getMagazineNumber() {
        return pagesubpageno[1] / 100;
    }
}
