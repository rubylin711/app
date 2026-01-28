/***************************************************************************************************
 * This code implements Close Cpation for ISDBTb Brazil
 * It does not implement all aspects of the standard but only what is found in the field of
 * terrestrial broadcasting in Brazil.
 *
 **************************************************************************************************/

/***************************************************************************************************
 * Reference documentation :
 *
 * Base standard :
 * ABNT NBR 15606-1, Section 11.6: Subtitles and superimposed characters
 * ARIB STD-B24, Volume 1, Part 3: Coding of Caption and Superimpose
 *
 * Character coding :
 * ABNT NBR 15606-1, Chapter 11.4: Character coding
 * ARIB STD-B24, Volume 1, Part 2, Chapter 7: Character coding
 *
 * Guidelines :
 * ABNT NBR 15608-3, Chapter 22: Multiple captions transmission
 * ABNT NBR 15608-3, Annex A: Caption PES structure
 * ARIB  TR-B14,  Volume  3,  Section  2,  Chapter  4: Operation  of  caption  and  superimpose encoding (Profile A)
 * ARIB TR-B14, Volume 3, Section 4, Chapter 6: Operation of closed caption coding (Profile C)
 *
 ***************************************************************************************************/


package com.prime.dtv.service.subtitle.isdbtCC;

import android.graphics.Bitmap;
import android.util.Log;

import com.prime.dtv.service.subtitle.BufferParser;
import com.prime.dtv.service.subtitle.SubtitleCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class IsdbccSubtitleCodec extends SubtitleCodec {
    public static final String TAG = "IsdbccSubtitleCodec";
    private static final int UNIT_ID_SUBTITLE_DATA = 0x03;
    private static final int DEFAULT_TIMEOUT_SEC = 10;

    // private int mDataComponentId;
    // private int mDMF;
    // private int mTiming;

    public IsdbccSubtitleCodec() {
        super("IsdbccSubtitleCodec");
        reset();
    }

    public void setIsdbccInfo(int dataComponentId, int DMF, int timing) {
        // mDataComponentId = dataComponentId;
        // mDMF = DMF;
        // mTiming = timing;
    }

    public void parse(InputBuffer inputBuffer) {
        // This method parse the PES

        if (inputBuffer.size == 0) {
            Log.d(TAG, "isdbcc caption PES size =0");
            notifyInputBuffer(inputBuffer);
            return;
        }


        CcPes pes;

        BufferParser parser = new BufferParser();
        parser.setBytes(inputBuffer.buffer.array(), inputBuffer.offset, inputBuffer.size);

        // See ARIB STB24 Vol 3
        int dataIdentifier = parser.readInt(8, "data_identifier");
        if (dataIdentifier != 0x80) {
            notifyError(String.format("unexpected data identifier (%x vs 0x20) in pes", dataIdentifier));
            notifyInputBuffer(inputBuffer);
            return;
        }
        int privateStreamId = parser.readInt(8, "private_stream_id");
        parser.skip(4, "reserved");
        int pesDataPacketLengthHeader = parser.readInt(4, "pesDataPacketLengthHeader");
        parser.skip(8 * pesDataPacketLengthHeader, "PES_data_private_byte");

        int dataGroupId = parser.readInt(6, "data_group_id");
        switch (dataGroupId) {
            case 0x00:
            case 0x20:
                // caption management group A or B
                pes = new CcManagementData(parser, dataGroupId);
                pes.checkFieldsValuesManaged();
                break;
            case 0x01:
            case 0x21:
                // caption statement group A or B
                pes = new CcStatementData(parser, dataGroupId);
                pes.checkFieldsValuesManaged();
                break;
            default:
                Log.d(TAG, "unexpected dataGroupId in pes : "+ dataGroupId);
                notifyError(String.format("unexpected dataGroupId in pes : %x", dataGroupId));
                notifyInputBuffer(inputBuffer);
                return;
        }

        //parser.printReadFields(TAG);
        if ((pes.bitmap != null) || (pes.clearScreen)) {
            notifyOutputBuffer(pes.bitmap, inputBuffer.presentationTimeUs, DEFAULT_TIMEOUT_SEC);
        }
        notifyInputBuffer(inputBuffer);
    }

    class CcDataUnit {
        int unitSeparator; // 0x1F
        int dataUnitParameter;
        int dataUnitSize;
    }

    class CcPes {

        static final int DATA_UNIT_NO_DATA_UNIT = 0x00;
        static final int DATA_UNIT_STATEMENT_BODY = 0x20;
        static final int DATA_UNIT_GRAPHICS = 0x28;
        static final int DATA_UNIT_SOUND = 0x2C;
        static final int DATA_UNIT_1_BYTES_DRCS = 0x30;
        static final int DATA_UNIT_2_BYTES_DRCS = 0x31;
        static final int DATA_UNIT_COLOR_MAP = 0x34;
        static final int DATA_UNIT_BITMAP = 0x35;

        // This class header and generic data among statement and management Data
        boolean managamentData;
        int dataGroupId;
        int dataGroupVersion;
        int dataGroupLinkNumber;
        int lastDataGroupLinkNumber;
        int timeControlMode; // TMD ;
        int dataGroupSize;

        // found in Data unit
        int unitSeparator;
        int dataUnitParameter;
        int dataUnitSize;

        Bitmap bitmap;                // bitmap resulting of this PES parsing
        boolean clearScreen;         // does this PES request to clear screen ?

        CcPes(BufferParser parser, int data_group_id) {
            // Parse header and common part between statement and management data
            dataGroupId = data_group_id;
            dataGroupVersion = parser.readInt(2, "data_group_version");
            dataGroupLinkNumber = parser.readInt(8, "data_group_link_number");
            lastDataGroupLinkNumber = parser.readInt(8, "last_data_group_link_number");

            dataGroupSize = parser.readInt(16, "data_group_size");
            timeControlMode = parser.readInt(2, "TMD");
            parser.skip(6, "Reserved");
        }


        void parseDataUnit(BufferParser parser, int size) {
            unitSeparator = parser.readInt(8, "unit_separator"); // 0x1F
            if (unitSeparator != 0x1F) {
                Log.d(TAG, " uncommon unitSeparator "+ unitSeparator);
            }
            dataUnitParameter = parser.readInt(8, "data_unit_parameter");
            dataUnitSize = parser.readInt(24, "data_unit_size");
        }

        void checkFieldsValuesManaged() {
            switch (dataUnitParameter) {
                case DATA_UNIT_NO_DATA_UNIT:
                    break;
                case DATA_UNIT_STATEMENT_BODY:
                    if (managamentData) {
                        Log.d(TAG, "CC Unmanaged dislay area");
                    }
                    break;
                case DATA_UNIT_GRAPHICS: // gemoetric graphics
                    Log.d(TAG, "CC unmanaged geometric graphics");
                    break;
                case DATA_UNIT_SOUND: // sound
                    Log.d(TAG, "CC unmanaged sound");
                    break;
                case DATA_UNIT_1_BYTES_DRCS: // 1 byte DRCS
                    Log.d(TAG, "CC unmanaged 1 byte DRCS ");
                    break;
                case DATA_UNIT_2_BYTES_DRCS: // 2 bytes DRCS
                    Log.d(TAG, "CC unmanaged 2 bytes DRCS ");
                    break;
                case DATA_UNIT_COLOR_MAP: // color map
                    Log.d(TAG, "CC unmanaged color map");
                    break;
                case DATA_UNIT_BITMAP: // bitmap;
                    Log.d(TAG, "CC unmanaged bitmap");
                    break;
                default:
                    Log.d(TAG, "CC unmanaged dataUnitParameter "+ dataUnitParameter);
                    break;
            }

            if ((timeControlMode != 0b01) && (timeControlMode != 0b00)) {
                Log.d(TAG, "CC unmanaged timeControlMode "+ timeControlMode);
            }

            if (lastDataGroupLinkNumber > 0) {
                notifyError(String.format(Locale.US,
                        "CC unmanaged lastDataGroupLinkNumber > 0 : %d", lastDataGroupLinkNumber));
            }
        }
    }

    class CcStatementData extends CcPes {
        // This class parses statement specific data

        int presentationStartTime;
        int dataUnitLoopLength;
        CcDataUnit dataUnit;

        CcStatementData(BufferParser parser, int data_group_id) {
            super(parser, data_group_id);
            managamentData = false;
            if ((timeControlMode == 0b01) || (timeControlMode == 0b10)) {
                presentationStartTime = parser.readInt(36, "STM");
                parser.skip(4, "Reserved");
            }
            dataUnitLoopLength = parser.readInt(24, "data_unit_lopp_length");
            if (dataUnitLoopLength > 0) {
                parseDataUnit(parser, dataUnitLoopLength);
                parseDataUnitData(parser);
            }
        }

        void parseDataUnitData(BufferParser parser) {
            if (dataUnitParameter == DATA_UNIT_STATEMENT_BODY) {
                parseCharacterData(parser);
            }
        }

        void parseCharacterData(BufferParser parser) {
            CharDataParser dataParser = new CharDataParser();
            dataParser.parse(parser);
            bitmap = dataParser.createBitmap();
            clearScreen = dataParser.clearScreen;
        }

        void checkFieldsValuesManaged() {
            super.checkFieldsValuesManaged();
        }

    }

    class CcManagementData extends CcPes {
        // this class parses management specific Data

        int offsetTime;
        int numLanguages;
        CcDataUnit dataUnit;
        List<CcLanguageInfo> languagesInfo;
        int dataUnitLoopLength;

        CcManagementData(BufferParser parser, int data_group_id) {
            super(parser, data_group_id);
            managamentData = true;
            if (timeControlMode == 0b10) {
                offsetTime = parser.readInt(36, "OTM");
                parser.skip(4, "reserved");
            }

            languagesInfo = new ArrayList<>();
            numLanguages = parser.readInt(8, "num_languages");

            for (int i = 0; i < numLanguages; i++) {
                CcLanguageInfo languageInfo = new CcLanguageInfo(parser);
                languagesInfo.add(languageInfo);
            }

            dataUnitLoopLength = parser.readInt(24, "data_unit_loop_length");
            if (dataUnitLoopLength > 0) {
                parseDataUnit(parser, dataUnitLoopLength);
                parseDataUnitData(parser);
            }
        }

        void parseDataUnitData(BufferParser parser) {
            // Nothing supposed to be received
            Log.d(TAG, "CC unmanaged data-unit in managemnt data");
        }

        void parseDisplayArea(BufferParser parser) {
            String text = parser.readString(dataUnitSize * 8, "text");
            Log.d(TAG, "Display Area => "+ text);
        }

        void checkFieldsValuesManaged() {
            super.checkFieldsValuesManaged();
            if (numLanguages > 1) {
                Log.d(TAG, "CC unmanaged multiple languges "+ numLanguages);
            } else if (numLanguages==0) {
                Log.d(TAG, "CC no language in PES");
            } else if (!languagesInfo.get(0).iso639LanguageCode.equals("por")) {
                Log.d(TAG, "CC unmanaged languges "+ languagesInfo.get(0).iso639LanguageCode);
            }

            for (int i = 0; i < languagesInfo.size(); i++) {
                int characterCoding = languagesInfo.get(i).characterCoding;
                if (characterCoding != 0) {
                    Log.d(TAG, "CC unmanaged characterCoding "+ characterCoding);
                }
                int rollupMode = languagesInfo.get(i).rollupMode;
                if (rollupMode != 0) {
                    // TvLog.i("CC unmanaged rollup %d", rollupMode);
                }
            }

            if (timeControlMode == 0b10) {
                Log.d(TAG, "CC unmanaged TMD in management data : "+ timeControlMode);
            }
        }

        class CcLanguageInfo {
            // this class parses language loop data found in management data
            int languageTag;
            int displayMode;
            int displayConditionDesignation;
            String iso639LanguageCode;
            int displayFormat;
            int characterCoding;
            int rollupMode;

            CcLanguageInfo(BufferParser parser) {
                languageTag = parser.readInt(3, "language_tag");
                parser.skip(1, "reserved");
                displayMode = parser.readInt(4, "DMF");
                switch (displayMode) {
                    case 0b1100:
                    case 0b1101:
                    case 0b1110:
                        displayConditionDesignation = parser.readInt(8, "DC");
                        break;
                    default:
                        break;
                }
                iso639LanguageCode = parser.readString(24, "ISO_639_language_code");
                displayFormat = parser.readInt(4, "display_format");
                // TvLog.i("Language info : format = %d, manguage = %s", displayFormat, iso639LanguageCode);
                characterCoding = parser.readInt(2, "character_coding");
                rollupMode = parser.readInt(2, "rollup_mode");
            }
        }
    }
}
