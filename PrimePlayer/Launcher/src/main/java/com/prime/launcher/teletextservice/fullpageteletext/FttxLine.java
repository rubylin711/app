package com.prime.launcher.teletextservice.fullpageteletext;

import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class FttxLine {
    private static final String TAG = "FttxLine";
    private static final int[] sColors = {
            Color.BLACK,
            Color.RED,
            Color.GREEN,
            Color.YELLOW,
            Color.BLUE,
            Color.MAGENTA,
            Color.CYAN,
            Color.WHITE
    };

    int number;
    ArrayList<FttxSegment> segments;
    private FttxSegment segment;
    char[] chars;
    boolean mMosaicflag = false;

    FttxLine(int number) {
        this.number = number;
        this.segments = new ArrayList<>();
        this.chars = new char[40];
    }

    public int getLineNumber() {
        return number;
    }

    public ArrayList<FttxSegment> getSegments() {
        return segments;
    }

    public char[] getChars() {
        return chars;
    }

    void setCharAt(int column, char newChar) {
        int pos = 0;
        chars[column - pos] = newChar;
    }

    void clear(boolean subtitlePage) {
        segments.clear();
        initSegment(subtitlePage);
        Arrays.fill(chars, 0, chars.length, (char) 0x20);
    }

    private void initSegment(boolean subtitlePage){
        segment = new FttxSegment();
        segment.setBackground(subtitlePage ? Color.TRANSPARENT : Color.BLACK);
        segments.add(segment);
    }

    private void newSegment(int pos, boolean setAfter) {
        if (segment.getLength() != 0) {
            FttxSegment newSegment = new FttxSegment(segment);
            if (setAfter) {
                segment.length = pos + 1 - segment.getStart();
                newSegment.start = pos + 1;
            } else {
                segment.length = pos - segment.getStart();
                newSegment.start = pos;
            }
            segments.add(newSegment);
            segment = newSegment;
        }
    }

    private void flash(int pos) {
        newSegment(pos, true);
        segment.flash = true;
    }

    private void steady(int pos) {
        newSegment(pos, false);
        segment.flash = false;
    }

    private void startBox(int pos) {
        newSegment(pos, true);
        // don't really understand the double shot for start box, as described in note,
        // table 26, section 12.2, ETSI 300706
        segment.visible = true;
        segment.start = pos + 1;
    }

    private void endBox(int pos) {
        // although spec says that this is a "Set-After" control, using a "Set-At" gives better
        // rendering (no trailing space after the text)
        newSegment(pos, false);
        segment.visible = false;
    }

    private void setAlphaColor(int pos, int color) {
        newSegment(pos, true);
        segment.foreground = color;
    }

    private void setDoubleSize(int pos, boolean setAfter, boolean doubleWidth,
            boolean doubleHeight) {
        newSegment(pos, setAfter);
        segment.doubleWidth = doubleWidth;
        segment.doubleHeight = doubleHeight;
    }

    private void setMosaicsColor(int pos, int color) {
        newSegment(pos, true);
        segment.foreground = color;
        segment.mosaic = true;
    }

    private void setBackground(int pos, int color) {
        newSegment(pos, false);
        segment.background = color;
    }

    void setCode(int pos, int code, FttxGXCharset charset, boolean subtitlePage) {
        // set-after : apply after the following character space
        // set-at : apply immediately
        switch (code) {
            case 0x0: // alpha black/set-after
            case 0x1: // alpha red/set-after
            case 0x2: // alpha green/set-after
            case 0x3: // alpha yellow/set-after
            case 0x4: // alpha blue/set-after
            case 0x5: // alpha magenta/set-after
            case 0x6: // alpha cyan/set-after
            case 0x7: // alpha white/set-after
                setAlphaColor(pos, sColors[code]);
                mMosaicflag = false;
                break;
            case 0x8: // flash/set-after
                Log.i(TAG, "flash :" + pos);
                flash(pos);
                break;
            case 0x9: // steady/set-at
                Log.i(TAG, "steady :" + pos);
                steady(pos);
                break;
            case 0xA: // end box/set-after
                endBox(pos);
                if (subtitlePage) {
                    setBackground(pos, Color.TRANSPARENT);
                }
                break;
            case 0xB: // start box/set-after
                startBox(pos);
                if (subtitlePage) {
                    setBackground(pos, Color.BLACK);
                }
                break;
            case 0xC: // normal size/set-at
                setDoubleSize(pos, false, false, false);
                break;
            case 0xD: // double height/set-after
                setDoubleSize(pos, true, false, true);
                break;
            case 0xE: // double width/set-after
                setDoubleSize(pos, true, true, false);
                break;
            case 0xF: // double size/set-after
                setDoubleSize(pos, true, true, true);
                break;
            case 0x10: // mosaics black/set-after
            case 0x11: // mosaics red/set-after
            case 0x12: // mosaics green/set-after
            case 0x13: // mosaics yellow/set-after
            case 0x14: // mosaics blue/set-after
            case 0x15: // mosaics magenta/set-after
            case 0x16: // mosaics cyan/set-after
            case 0x17: // mosaics white/set-after
                mMosaicflag = true;
                setMosaicsColor(pos, sColors[code - 0x10]);
                break;
            case 0x18: // conceal/set-at
                Log.i(TAG, "TeletextSubtitleCodec : conceal not handled");
                break;
            case 0x19: // contiguous mosaics graphics/set-at
                Log.i(TAG, "TeletextSubtitleCodec : (contiguous) mosaics mode not handled");
                break;
            case 0x1A: // separated mosaics graphics/set-at
                Log.i(TAG, "TeletextSubtitleCodec : (surround) mosaics mode not handled");
                break;
            case 0x1B: // esc/set-after
                // toggles between the first and second G0 sets
                Log.i(TAG, "TeletexttSubtitleCodec : (release) esc not handled");
                break;
            case 0x1C: // black background/set-at
                setBackground(pos, Color.BLACK);
                break;
            case 0x1D: // new background/set-at
                setBackground(pos, segment.getForeground());
                break;
            case 0X1E: // hold mosaics/set-at
                Log.i(TAG, "TeletextSubtitleCodec : (hold) mosaics mode not handled");
                break;
            case 0X1F: // release mosaics/set-at
                Log.i(TAG, "TeletextSubtitleCodec : (release) mosaics mode not handled");
                break;
            default:
                break;
        }

        if (code >= 0x00 && code <= 0x1F) {
            // Control codes by default is space character
            chars[pos] = 0x20;
        } else {
            if (mMosaicflag) {
                chars[pos] = charset.getG1Char(code);
            } else {
                chars[pos] = charset.getG0Char(code);
            }
        }
        segment.length = pos + 1 - segment.getStart();
    }

    public boolean checkDoubleHeight(){
        for (FttxSegment segm : segments) {
            if (segm.isDoubleHeight()) {
                return true;
            }
        }
        return false;
    }
}
