package com.prime.dtv.service.subtitle.isdbtCC;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

import com.prime.dtv.service.subtitle.BufferParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharDataParser {
    public static final String TAG = "CharDataParser";

    private static final String[] c0Names = {"BEL", "APB", "APF", "APD", "APU", "CS", "APR", "LS1", "LS0", null, null,
            null, null, null, null, "PAPF", null, "CAN", "SS2", null, "ESC", "APS", "SS3", "RS", "US"};
    private static final String[] c1Names = {"DEL", "BKF", "RDF", "GRF", "YLF", "BLF", "MGF", "CNF", "WHF", "SSZ",
            "MSZ", "NSZ", "SZX", null, null, null, null, "COL", "FLC", "CDC", "POL", "WMM", "MACRO", null, "HLC",
            "RPC", "SPL", "STL", "CSI", null, "TIME", null, null};
    private static final char CSI = 0x9b, ESC = 0x1b, COL = 0x90, APS = 0x1c, SS3 = 0x1d;
    private static final char[] csiIds = {83, 84, 110, 97, 86, 95, 87, 75, 92, 88, 89, 66, 93, 94, 98, 101, 99, 100, 102, 104, 105};
    private static final String[] csiNames = {"SWF", "CCC", "RCS", "ACPS", "SDF", "SDP", "SSM", "PLD",
            "PLU", "SHS", "SVS", "GSM", "GAA", "SRC", "TCC", "CFS", "ORN", "MDF", "XCS", "PRA", "ACS", "SCS"};
    // The SBTVD_CHAR_SET  is similar to LATIN_9 with adaptation
    private static final String[] SBTVD_CHAR_SET = {
            //_0____1____2____3____4____5____6____7____8____9____a____b____c____d____e____f
            " ", " ", " ", "0", "@", "P", "`", "p", " ", " ", " ", "°", "À", "Ð", "à", "ð",  // _0
            " ", " ", "!", "1", "A", "Q", "a", "q", " ", " ", "¡", "±", "Á", "Ñ", "á", "ñ",  // _1
            " ", " ", "\"", "2", "B", "R", "b", "r", " ", " ", "¢", "²", "Â", "Ò", "â", "ò", // _2
            " ", " ", "#", "3", "C", "S", "c", "s", " ", " ", "£", "³", "Ã", "Ó", "ã", "ó",  // _3
            " ", " ", "$", "4", "D", "T", "d", "t", " ", " ", "€", "Ž", "Ä", "Ô", "ä", "ô",  // _4
            " ", " ", "%", "5", "E", "U", "e", "u", " ", " ", "¥", "µ", "Å", "Õ", "å", "õ",  // _5
            " ", " ", "&", "6", "F", "V", "f", "v", " ", " ", "Š", "¶", "Æ", "Ö", "æ", "ö",  // _6
            " ", " ", "'", "7", "G", "W", "g", "w", " ", " ", "§", "·", "Ç", "×", "ç", "÷",  // _7
            " ", " ", "(", "8", "H", "X", "h", "x", " ", " ", "š", "ž", "È", "Ø", "è", "ø",  // _8
            " ", " ", ")", "9", "I", "Y", "i", "y", " ", " ", "©", "¹", "É", "Ú", "é", "ù",  // _9
            " ", " ", "*", ":", "J", "Z", "j", "z", " ", " ", "ª", "º", "Ê", "Ú", "ê", "ú",  // _a
            " ", " ", "+", ";", "K", "[", "k", "{", " ", " ", "«", "»", "Ë", "Û", "ë", "û",  // _b
            " ", " ", ",", "<", "L", "\\", "l", "|", " ", " ", "¬", "Œ", "Ì", "Ü", "ì", "ü",  // _c
            " ", " ", "-", "=", "M", "]", "m", "}", " ", " ", "ÿ", "œ", "Í", "Ý", "í", "ý",  // _d
            " ", " ", ".", ">", "N", "^", "n", "~", " ", " ", "®", "Ÿ", "Î", "Þ", "î", "þ",  // _e
            " ", " ", "/", "?", "O", "_", "o", " ", " ", " ", "¯", "¿", "Ï", "ß", "ï", " "   // _f
    };
    private static final String[] extraCP = {"☼", "¦", "¨", "´", "¸", "¼", "½", "¾", " ", " ", " ", " ", " ", " ", " ", " ",
            "…", "█", "‘", "’", "“", "”", "•", "™", "⅛", "⅜", "⅝", "⅞", "♪"};

    private static final int SMALL_CHAR_SIZE = 0;
    private static final int MIDDLE_CHAR_SIZE = 1;
    private static final int NORMAL_CHAR_SIZE = 2;

    private static int mCaptionNumber;

    // Parameters by text lines   <line, param>
    private final Map<Integer, Integer> mForeGroundColor;
    private final Map<Integer, Integer> mBackgroundColor;
    private final Map<Integer, Integer> mLineActivePosition;
    private final Map<Integer, Integer> mColumnActivePosition;
    private final Map<Integer, Integer> mLineActivePositionForward;


    public boolean clearScreen;
    private List<StringBuffer> mItems;
    private boolean mExtraCodepage;
    private int mWritingFormatWidth;
    private int mWritingFormatHeight;
    private int mDisplayPositionX;
    private int mDisplayPositionY;
    private int mDisplayFormatX;
    private int mDisplayFormatY;
    private int mCharacterDotX;
    private int mCharacterDotY;
    private int mHorizontalSpacing;
    private int mVerticalSpacing;
    private int mRasterColor;
    private int mCharSize;


    // constructor
    CharDataParser() {
        mForeGroundColor = new HashMap<>();
        mForeGroundColor.put(0, Color.WHITE);

        mBackgroundColor = new HashMap<>();
        mBackgroundColor.put(0, Color.BLACK);

        mLineActivePosition = new HashMap<>();
        mLineActivePosition.put(0, 14);

        mColumnActivePosition = new HashMap<>();
        mColumnActivePosition.put(0, 0);

        mLineActivePositionForward = new HashMap<>();
        mLineActivePositionForward.put(0, 0);

        mItems = new ArrayList<>();

        mWritingFormatWidth = 960;
        mWritingFormatHeight = 540;
        mDisplayPositionX = 138;
        mDisplayPositionY = 100;
        mDisplayFormatX = 684;
        mDisplayFormatY = 390;
        mCharacterDotX = 36;
        mCharacterDotY = 36;
        mHorizontalSpacing = 2;
        mRasterColor = 8; // transparent
        mCharSize = NORMAL_CHAR_SIZE; // do not chnge size
    }


    private char readChar(BufferParser parser) {
        return (char) parser.readInt(8, "char");
    }

    void parse(BufferParser parser) {
        char c;
        StringBuffer sb = null;
        String aChar;

        boolean cmdSequence = false;
        int itemRange = -1;
        int lastTextItemRange = 0;

        while (parser.remaining() > 2) { // 2 for CRC16
            c = readChar(parser);
            if (c < 0x20 || (c > 0x7e && c < 0xa0)) {
                if (!cmdSequence) {
                    itemRange++;
                    if (sb != null) {
                        sb.append("}");
                        mItems.add(sb);
                    }
                    sb = new StringBuffer();
                    sb.append("cmd: {");
                }
                cmdSequence = true;
                parseCodeChar(parser, sb, c, itemRange);
            } else {
                // Text Char
                if (cmdSequence) {
                    itemRange++;
                    lastTextItemRange = itemRange;
                    if ((sb != null) && (sb.length() >= 3)) {
                        // remove the |
                        sb.delete(sb.length() - 3, sb.length());
                        sb.append("}");
                        mItems.add(sb);
                    }
                    sb = new StringBuffer();
                    sb.append("text: {");
                }
                cmdSequence = false;
                if (mExtraCodepage) {
                    // TvLog.i("Extra code page : %s", c);
                    mExtraCodepage = false;
                    if (c > 0x2f && c < 0x4c)
                        aChar = extraCP[c - 0x30];
                    else if (c == 0x21)
                        aChar = extraCP[0x1c];
                    else
                        aChar = " ";
                } else
                    aChar = SBTVD_CHAR_SET[(c >> 4) + ((c & 0xf) << 4)];
                sb.append(aChar);
            }
        }

        sb.append("}");
        mItems.add(sb);

        mCaptionNumber++;
        /*
        TvLog.i("CC Caption %d", mCaptionNumber);
        for (int i = 0; i < mItems.size(); i++) {
            TvLog.i("    CC Item %d  :  %s", i, mItems.get(i));
        }
        */
    }


    private void parseCodeChar(BufferParser parser, StringBuffer sb, char c, int itemRange) {
        if (c == 0)
            return;
        String codeName = null;
        if (c > 6)
            Log.d(TAG,"control code = 0x"+ Integer.toHexString(c));
            if (c < 0x20)
                codeName = c0Names[c - 7];
            else
                codeName = c1Names[c - 0x7f];
        if (codeName == null) {
            Log.d(TAG, "CC unmanaged code char "+Integer.toHexString(c));
            sb.append(Integer.toHexString(c));
        } else {
            sb.append(codeName);
            switch (codeName) {
                case "APS":
                    parseActivePosition(parser, sb, itemRange);
                    break;
                case "APF":
                    parseActivePositionForward(itemRange);
                    break;
                case "APR":
                    parseActivePositionReturn(itemRange);
                    break;
                case "ESC":
                    parseEscapeSeq(parser, sb);
                    break;
                case "CSI":
                    parseCsiCommand(parser, sb);
                    break;
                case "COL":
                    parseColor(parser, sb, itemRange);
                    break;
                case "SS3":
                    mExtraCodepage = true;
                    break;
                case "SSZ":
                    mCharSize = SMALL_CHAR_SIZE;
                    break;
                case "MSZ":
                    mCharSize = MIDDLE_CHAR_SIZE;
                    break;
                case "NSZ":
                    mCharSize = NORMAL_CHAR_SIZE;
                    break;
                case "CS":
                    clearScreen = true;
                    break;
                case "WHF":
                    mForeGroundColor.put(itemRange, Color.WHITE);
                    break;
                default:
                    Log.d(TAG, "CC unmanaged code = "+ codeName);
                    break;
            }
        }

        sb.append(" | ");
    }

    private void parseActivePosition(BufferParser parser, StringBuffer sb, int itemRange) {
        char c = readChar(parser);
        sb.append(" Ln:");
        sb.append(c & 0x3f);
        mLineActivePosition.put(itemRange, c & 0x3f);
        c = readChar(parser);
        sb.append(" Col:");
        sb.append(c & 0x3f);
        mColumnActivePosition.put(itemRange, c & 0x3f);
        if ((c & 0x3f) != 0) {
            Log.d(TAG, "CC unmanaged APS column value = "+(c & 0x3f));
        }
    }

    private void parseActivePositionForward(int itemRange) {
        int numberOfApf;
        if (mLineActivePositionForward.containsKey(itemRange)) {
            numberOfApf = mLineActivePositionForward.get(itemRange);
            mLineActivePositionForward.replace(itemRange, numberOfApf + 1);
        } else {
            mLineActivePositionForward.put(itemRange, 1);
        }
        Log.d(TAG, "CC unmanaged APF");
    }

    private void parseActivePositionReturn(int itemRange) {
        Log.d(TAG, "CC APR managed by linefeed, see previousLineNumber ");
    }

    private void parseEscapeSeq(BufferParser parser, StringBuffer sb) {
        char c = readChar(parser);
        sb.append(Integer.toHexString(c));
        c = readChar(parser);
        sb.append(Integer.toHexString(c));
    }

    private void getSSIParameters(StringBuffer parameters, int[] array) {
        int endPosition = parameters.indexOf(SBTVD_CHAR_SET[(0x3B >> 4) + ((0x3B & 0xf) << 4)]);
        if (endPosition == -1) {
            // one param only
            array[0] = Integer.parseInt(parameters.toString());
        } else {
            // 2 params
            String s = parameters.substring(0, endPosition);
            array[0] = Integer.parseInt(s);
            s = parameters.substring(endPosition + 1);
            array[1] = Integer.parseInt(s);
        }
    }

    private void parseSWF(int value) {
        switch (value) {
            case 5:
                mWritingFormatHeight = 1080;
                mWritingFormatWidth = 1920;
                break;
            case 7:
                mWritingFormatHeight = 540;
                mWritingFormatWidth = 960;
                break;
            case 9:
                mWritingFormatHeight = 420;
                mWritingFormatWidth = 480;
                break;
            case 11:
                mWritingFormatHeight = 720;
                mWritingFormatWidth = 1280;
                break;
            default:
                Log.d(TAG, "CC unmanaged SWF : "+ value);
                break;

        }
    }

    private void parseCsiCommand(BufferParser parser, StringBuffer sb) {
        sb.append(" ");
        int cmdMaxLen = 8;

        StringBuffer parameters = new StringBuffer();
//        Log.d(TAG,"parseCsiCommand parser.getLength() = "+parser.getLength());
//        Log.d(TAG,"parseCsiCommand 111 parser.getPosInBits() = "+parser.getPosInBits());
        char c = readChar(parser);
//        Log.d(TAG,"parseCsiCommand 222 parser.getPosInBits() = "+parser.getPosInBits());
//        Log.d(TAG,"parseCsiCommand 222 c = 0x"+Integer.toHexString(c));
        while ((c != 0x20) && (cmdMaxLen > 0) && (parser.remaining() > 0)) {
            sb.append(c);
            cmdMaxLen--;
            parameters.append(c);
            // parameters.add(c);
            c = readChar(parser);
//            Log.d(TAG,"parseCsiCommand 333 c = 0x"+Integer.toHexString(c));
        }
//        Log.d(TAG,"parseCsiCommand 444 "+
//                        " parser.getLength() = "+parser.getLength() +
//                        " parser.getPosInBits() = "+parser.getPosInBits());
        c = readChar(parser);
//        Log.d(TAG,"parseCsiCommand 444 c = 0x"+Integer.toHexString(c));
        int[] paramIntArray = {0, 0};
        for (int i = 0; i < csiIds.length; i++) {
            if (csiIds[i] == c) {
                sb.append(" ");
                sb.append(csiNames[i]);
                getSSIParameters(parameters, paramIntArray);
                switch (csiNames[i]) {
                    case "SWF":
                        parseSWF(paramIntArray[0]);
                        if (parameters.length() > 1) {
                            Log.d(TAG, "CC unmanaged part of SWF : parameters "+ parameters);
                        }
                        break;
                    case "SDP":
                        mDisplayPositionX = paramIntArray[0];
                        mDisplayPositionY = paramIntArray[1];
                        break;
                    case "SDF":
                        mDisplayFormatX = paramIntArray[0];
                        mDisplayFormatY = paramIntArray[1];
                        Log.d(TAG,"SDF change mDisplayFormatX = "+mDisplayFormatX+" mDisplayFormatY = "+mDisplayFormatY);
                        break;
                    case "SSM":
                        mCharacterDotX = paramIntArray[0];
                        mCharacterDotY = paramIntArray[1];
                        Log.d(TAG,"SSM change mCharacterDotX = "+mCharacterDotX+" mCharacterDotY = "+mCharacterDotY);
                        break;
                    case "SHS":
                        mHorizontalSpacing = paramIntArray[0];
                        break;
                    case "SVS":
                        mVerticalSpacing = paramIntArray[0];
                        break;
                    case "RCS":
                        mRasterColor = paramIntArray[0];
                        break;
                    default:
                        Log.d(TAG, "CC unmanaged CSI command : "+ csiIds[i]);
                        break;
                }
            }
        }
    }

    private void parseColor(BufferParser parser, StringBuffer sb, int itemRange) {
        char c;
        c = readChar(parser);
        sb.append(" ");
        String[] area = {"FG", "BG", "HFG", "HBG"};
        int i = (c >> 4) & 0x7;
        if (i > 3)
            sb.append(area[i - 4]);
        String[] colors = {"black", "red", "green", "yellow", "blue", "magenta", "cyan", "white", "transparent"};
        int[] colorsValue = {Color.BLACK, Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.MAGENTA, Color.CYAN, Color.WHITE, Color.TRANSPARENT};
        int j = c & 0xf;
        sb.append(">");
        if (j > 8) {
            sb.append("H-");
            j -= 8;
        }
        if (j < 9) {
            sb.append(colors[j]);
            switch (area[i - 4]) {
                case "FG":
                    mForeGroundColor.put(itemRange, colorsValue[j]);
                    break;
                case "BG":
                    mBackgroundColor.put(itemRange, colorsValue[j]);
                    break;
                default:
                    Log.d(TAG, "CC unmanaged half color");
                    break;
            }
        }
    }

    private int getLineParam(Map<Integer, Integer> map, int itemNumber) {
        int lineParam = 0;
        int i = itemNumber;
        while (i >= 0) {
            Integer rcInteger = map.get(i);
            if (rcInteger != null) {
                lineParam = rcInteger;
                break;
            }
            i--;
        }
        return lineParam;
    }

    public Bitmap createBitmap() {
        Paint paint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);

        // Tiresias font required for ISDBTb CC
        paint.setTypeface(Typeface.create("sans-serif-condensed-light", Typeface.NORMAL));

        Paint.FontMetrics metrics = new Paint.FontMetrics();
        paint.setLinearText(true);
        prime_rect_setting();

        Bitmap bitmap = Bitmap.createBitmap(mWritingFormatWidth, mWritingFormatHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        float lineHeight = mDisplayFormatY / 15; // as not set in SWF, we guess 15 lines max
        float charWidth = mCharacterDotX;
        float charHeight = mCharacterDotY;

//        Log.d(TAG,"mCharacterDotX = "+mCharacterDotX+" mCharacterDotY = "+mCharacterDotY);
        // Arib B14 8.1.9.2
        // we guess it superseds the character size received in SSM
        switch (mCharSize) {
            case SMALL_CHAR_SIZE:
                charWidth = charWidth / 2;
                charHeight = charHeight / 2;
                break;
            case MIDDLE_CHAR_SIZE:
                charWidth = charWidth / 2;
                break;
            case NORMAL_CHAR_SIZE:
            default:
                // do not change size
                break;

        }

        // Also adjust vertical spacing in the same proportion than the character dot
        int adjustedVerticalSpacing = (int) ((float) mVerticalSpacing * (float) lineHeight / (float) mCharacterDotX);
        // patch for OIT, otherwise, on some channels, third line of text is out of screen when there are blank text lines
        // between text line. And also ref STB does not display the spacing.
        adjustedVerticalSpacing = 0;

        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        Typeface typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
        paint.setTypeface(typeface);

        // compute the text size of one line
        float textSizeOneLine = charHeight;
        paint.setTextSize(textSizeOneLine);
        paint.getFontMetrics(metrics);
        float currentTextSize = -metrics.top + metrics.bottom;

        textSizeOneLine = textSizeOneLine * lineHeight / currentTextSize;
        int previousLineNumber = -1;

        for (int i = 0; i < mItems.size(); i++) {
            String headerString = "text: {";
            int lineNumber = getLineParam(mLineActivePosition, i);

            int position = mItems.get(i).indexOf(headerString);
            if (position == -1)
                continue;

            if (lineNumber <= previousLineNumber)
                lineNumber = previousLineNumber + 1;
            previousLineNumber = lineNumber;

            // remove "text: { }"
            mItems.get(i).delete(position, position + headerString.length());
            mItems.get(i).delete(mItems.get(i).length() - 1, mItems.get(i).length());

            float y = mDisplayPositionY + lineNumber * (adjustedVerticalSpacing + lineHeight);
//            Log.d(TAG,"textSizeOneLine = "+textSizeOneLine);
            paint.setTextSize(textSizeOneLine);

            paint.setTextScaleX(0.8f);
            float scale = ((float) mWritingFormatHeight / (float) mDisplayFormatY)*0.8f;
            paint.setTextScaleX(scale);

            float textWidth = paint.measureText(new String(mItems.get(i)));

            // compute position
            float x = mDisplayPositionX;

            // Patch for a diplay more similar to to other STB and TV
            x = x * 2;


            // draw background
            paint.setColor(getLineParam(mBackgroundColor, i));
            canvas.drawRect(x, y, x + textWidth, y + lineHeight, paint);

            // draw text
            paint.getFontMetrics(metrics);
            paint.setColor(getLineParam(mForeGroundColor, i));
            float textY = y - metrics.top; //
            canvas.drawText(new String(mItems.get(i)), x, textY, paint);
        }

        return bitmap;
    }

    private void prime_rect_setting() //base on 1920*1080
    {
        mWritingFormatHeight = 1080;
        mWritingFormatWidth = 1920;
        mDisplayPositionY = 500;
        mDisplayPositionX = 80;
        mCharacterDotY = 100;
    }
}
