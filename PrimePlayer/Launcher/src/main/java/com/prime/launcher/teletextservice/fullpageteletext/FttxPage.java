package com.prime.launcher.teletextservice.fullpageteletext;

import android.util.Log;
import android.util.SparseArray;

import java.util.Locale;

public class FttxPage {
    private static final String TAG = "TeletextService_Page";
    public static final int MAX_LINES_BY_PAGE = 25;
    public static final int MAX_CHARS_BY_LINE = 40;

    public static class FttxPageLink {
        int pageNumber;
        int pos;
        char []codes;
    }

    private boolean mUpdated;
    private boolean mHeaderParsed;

    private SparseArray<FttxLine> mLines;
    private FttxGXCharset mCharset;

    private int mPageNumber;

    FttxPageLink pageLink0; // Red button page link
    FttxPageLink pageLink1; // Green button page link
    FttxPageLink pageLink2; // Yellow button page link
    FttxPageLink pageLink3; // Blue button page link

    public FttxPage(int fontId) {
        mLines = new SparseArray<>();
        mCharset = new FttxGXCharset(fontId);
    }

    public void clear() {
        Log.i(TAG, "********* clear_Page ***********");
        mLines.clear();
        mUpdated = false;
        mHeaderParsed = false;
    }

    public SparseArray<FttxLine> getLines() {
        return mLines;
    }

    public boolean isUpdated() {
        return mUpdated;
    }

    void setUpdated(boolean updated) {
        mUpdated = updated;
    }

    public boolean isHeaderParsed() {
        return mHeaderParsed;
    }

    void setHeaderParsed(boolean parsed) {
        mHeaderParsed = parsed;
    }

    public FttxGXCharset getCharset() {
        return mCharset;
    }

    void setNationalSubsetCode(int nationalSubset) {
        mCharset.setG0Charset(nationalSubset);
    }

    protected void setPageNumber(int pageNumber) {
        mPageNumber = pageNumber;
    }

    public int getPageNumber() {
        return mPageNumber;
    }

    FttxLine getLine(int number) {
        FttxLine line = mLines.get(number);
        if (line == null) {
            line = new FttxLine(number);
            mLines.put(number, line);
        }
        return line;
    }

    public void setCharAt(int column, int row, char newChar) {
        FttxLine line = mLines.get(row);
        if (line == null) {
            Log.w(TAG, String.format("Page : try to set char at (%d,%d), but there is no line here",
                    column, row));
            return;
        }
        line.setCharAt(column, newChar);
    }

    public void updatePageLink(int position, FttxPageLink link) {
        switch (position) {
            case 0:
                pageLink0 = link;
                break;
            case 1:
                pageLink1 = link;
                break;
            case 2:
                pageLink2 = link;
                break;
            case 3:
                pageLink3 = link;
                break;
            default:
                break;
        }
    }

    public int getPageLink(int position) {
        switch (position) {
            case 0:
                return pageLink0.pageNumber;
            case 1:
                return pageLink1.pageNumber;
            case 2:
                return pageLink2.pageNumber;
            case 3:
                return pageLink3.pageNumber;
            default:
                return -1;
        }
    }

    public void createNavigationLine() {
        FttxLine line = getLine(FttxPage.MAX_LINES_BY_PAGE - 1);
        line.clear(false);
        String codes = String.format("%c%c%c- %c%c%c+ %c%c%c%.12s %c%c%c%.12s",
                0x01, 0x1D, 0x00,
                0x02, 0x1D, 0x00,
                0x03, 0x1D, 0x00, new String(pageLink2.codes),
                0x06, 0x1D, 0x00, new String(pageLink3.codes));
        for (int i = 0; i < FttxPage.MAX_CHARS_BY_LINE; i++) {
            if (i < codes.length()) {
                line.setCode(i, codes.charAt(i), mCharset, false);
            } else {
                line.setCode(i, 0x20, mCharset, false);
            }
        }
    }

    private int getMagazine() {
        return (mPageNumber / 100);
    }

    private int getMagazineIndex(int magazine) {
        return magazine == 8 ? 0 : magazine;
    }

    private int getMagazineNumber(int magazine) {
        return magazine == 0 ? 8 : magazine;
    }

    public int getNextMagazineNorm(int magazine) {
        return (magazine == 8 ? 100 : (magazine + 1) * 100);
    }

    private int getPage() {
        return mPageNumber & 0x00FF;
    };

    public int previousPageNumber() {
        return (getPage() == 0x00 ? (getMagazine() == 1 ? 0x899 : (mPageNumber - 0x100) + 0x99 )
                : ((mPageNumber & 0x000F) == 0 ? (mPageNumber - 0x10) + 9:  mPageNumber - 1));
    }

    public int nextPageNumber() {
        return (getPage() ==0x99 ? getNextMagazineNorm(getMagazine())
                : ((mPageNumber & 0x000F) == 9 ? ((mPageNumber & 0xFF0) + 10 ): mPageNumber + 1));
    }

    public int nextTenPageNumber() {
        return getPage() > 0x89 ? mPageNumber - 0x90 : mPageNumber + 10;
    }

    public int nextMagazineNorm() {
        return (getMagazine() == 8 ? 100 : (getMagazine() + 1) * 100);
    }

    public int currentMagazineNorm() {
        return getMagazine()  * 100;
    }

    void dump() {
        Log.i(TAG, String.format("TeletextSubtitleCode, page (nb_lines:%d", mLines.size()));
        for (int i = 0; i < mLines.size(); ++i) {
            FttxLine line = mLines.valueAt(i);
            Log.i(TAG, String.format("Line %d, nb segment:%d", line.number, line.segments.size()));
            for (FttxSegment seg : line.segments) {
                String segmentDesc =
                        String.format(Locale.US,
                                "text:<%s> %d,%d  [fore:%x, back:%x, size(%d,%d), visible:%b]",
                                new String(line.chars, seg.getStart(), seg.getLength()),
                                seg.getStart(), seg.getLength(),
                                seg.getForeground(), seg.getBackground(),
                                seg.doubleWidth ? 2 : 1, seg.doubleHeight ? 2 : 1,
                                seg.visible);
                Log.i(TAG, String.format(" seg:%s", segmentDesc));
            }
        }
    }
}
