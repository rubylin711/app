package com.prime.dtv.service.subtitle.dvb;

import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;

import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.service.subtitle.BufferParser;
import com.prime.dtv.service.subtitle.SubtitleCodec;

import java.util.Hashtable;
import java.util.Locale;

/**
 * Reference documentation : ETSI EN 300 743 v1.5.1 (2014-01)
 * Digital Video Broadcasting (DVB); Subtitling systems
 */
public class DvbSubtitleCodec extends SubtitleCodec {


    private static final String TAG = "DvbSubtitleCodec";

    private static final int START_SEGMENT_BYTE = 0x0f;
    private static final int END_PES_BYTE = 0xff;

    private static final int SEGMENT_PAGE_COMPOSITION = 0x10;
    private static final int SEGMENT_REGION_COMPOSITION = 0x11;
    private static final int SEGMENT_CLUT_DEFINITION = 0x12;
    private static final int SEGMENT_OBJECT_DATA = 0x13;
    private static final int SEGMENT_DISPLAY_DEFINITION = 0x14;
    private static final int SEGMENT_DISPARITY_SIGNALING = 0x15;
    private static final int SEGMENT_END_OF_DISPLAY_SET = 0x80;

    private static final int PAGE_STATE_NORMAL_CASE = 0;
    private static final int PAGE_STATE_ACQUISITION_POINT = 1;
    private static final int PAGE_STATE_MODE_CHANGE = 2;

    private int mPageId;
    private int mAncillaryPageId;

    private int mDisplayVersion;
    private int mDisplayWidth;
    private int mDisplayHeight;
    private boolean mDisplayHasWindow;
    private int mDisplayWindowX;
    private int mDisplayWindowY;
    private int mDisplayWindowWidth;
    private int mDisplayWindowHeight;

    private Page mPage;
    private Hashtable<Integer, Region> mRegions;
    private Hashtable<Integer, GraphicalUnit> mGraphicalUnits;
    private Hashtable<Integer, Clut> mCluts;

    private boolean mMustStop;
    private boolean mDisplayPage;

    private GraphicalContext mGraphicalContext;

    public DvbSubtitleCodec() {
        super("DvbSubtitleCodec");
        mRegions = new Hashtable<>();
        mGraphicalUnits = new Hashtable<>();
        mCluts = new Hashtable<>();
        mGraphicalContext = new GraphicalContext();
        mPageId = -1;
        mAncillaryPageId = -1;
        reset();
    }

    protected void reset() {
        super.reset();
        mDisplayWidth = 720;
        mDisplayHeight = 576;
        mDisplayVersion = -1;
        mPage = null;
        mRegions.clear();
        mGraphicalUnits.clear();
        mCluts.clear();
    }

    private Region getRegion(int id) {
        Region region;
        if (mRegions.containsKey(id)) {
            region = mRegions.get(id);
        } else {
            region = new Region(id);
            mRegions.put(id, region);
        }
        return region;
    }

    private GraphicalUnit getGraphicalUnit(int id) {
        GraphicalUnit unit;
        if (mGraphicalUnits.containsKey(id)) {
            unit = mGraphicalUnits.get(id);
        } else {
            unit = new GraphicalUnit(id);
            mGraphicalUnits.put(id, unit);
        }
        return unit;
    }

    private Clut getClut(int id) {
        Clut clut;
        if (mCluts.containsKey(id)) {
            clut = mCluts.get(id);
        } else {
            clut = new Clut(id);
            mCluts.put(id, clut);
        }
        return clut;
    }

    private void parseDisplayDefinitionSegment(BufferParser parser) {
        int versionNumber = parser.readInt(4, "dds_version_number");
        if (versionNumber == mDisplayVersion)
            return;
        mDisplayHasWindow = parser.readBool("display_window_flag");
        parser.skip(3, "reserved");
        mDisplayWidth = parser.readInt(16, "display_width") + 1;
        mDisplayHeight = parser.readInt(16, "display_height") + 1;
        if (mDisplayHasWindow) {
            int xMin = parser.readInt(16, "display_window_horizontal_position_minimum");
            int xMax = parser.readInt(16, "display_window_horizontal_position_maximum");
            int yMin = parser.readInt(16, "display_window_vertical_position_minimum");
            int yMax = parser.readInt(16, "display_window_vertical_position_maximum");
            mDisplayWindowX = xMin;
            mDisplayWindowY = yMin;
            mDisplayWindowWidth = xMax - xMin;
            mDisplayWindowHeight = yMax - yMin;
        }
    }

    private void parsePageCompositionSegment(BufferParser parser, int pageId, int segmentLength) {
        int endPos = parser.getPosInBytes() + segmentLength;
        int timeOut = parser.readInt(8, "page_time_out");
        int versionNumber = parser.readInt(4, "page_version_number");
        int state = parser.readInt(2, "page_state");
        parser.skip(2, "reserved");
        switch (state) {
            case PAGE_STATE_NORMAL_CASE:
                if (mPage == null) {
                    LogUtils.i("page state is 'normal case' but we have no page yet");
                    return;
                }
                if (mPage.versionNumber == versionNumber) {

                    return;
                }
                break;
            case PAGE_STATE_ACQUISITION_POINT:
            case PAGE_STATE_MODE_CHANGE:
                LogUtils.i("PAGE_STATE_MODE_CHANGE ==");
                mPage = new Page(mPageId);
                mRegions.clear();
                mGraphicalUnits.clear();
                break;
        }

        mDisplayPage = true;
        mPage.state = state;
        mPage.timeoutSec = timeOut;
        mPage.versionNumber = versionNumber;
        mPage.clearCompositions();

        while (parser.getPosInBytes() < endPos) {
            int id = parser.readInt(8, "region_id");
            parser.skip(8, "reserved");
            int x = parser.readInt(16, "region_horizontal_address");
            int y = parser.readInt(16, "region_vertical_address");
            mPage.addRegionComposition(getRegion(id), x, y);
        }
    }

    private void parseRegionCompositionSegment(BufferParser parser, int segmentLength) {
        int endPos = parser.getPosInBytes() + segmentLength;
        int regionId = parser.readInt(8, "region_id");
        int versionNumber = parser.readInt(4, "region_version_number");
        boolean needFill = parser.readBool("region_fill_flag");
        parser.skip(3, "reserved");
        int width = parser.readInt(16, "region_width");
        int height = parser.readInt(16, "region_height");
        parser.skip(3, "region_level_of_compatibility");
        int depth = parser.readInt(3, "region_depth");
        parser.skip(2, "reserved");
        int clutId = parser.readInt(8, "CLUT_id");
        int region8BitPixelCode = parser.readInt(8, "region_8-bit_pixel_code");
        int region4BitPixelCode = parser.readInt(4, "region_4-bit_pixel_code");
        int region2BitPixelCode = parser.readInt(2, "region_2-bit_pixel_code");
        parser.skip(2, "reserved");

        Region region;
        if (mRegions.containsKey(regionId)) {
            region = mRegions.get(regionId);
        } else {
            region = new Region(regionId);
            mRegions.put(regionId, region);
        }
        if (region.versionNumber == versionNumber)
            return;
        region.versionNumber = versionNumber;
        region.needFill = needFill;
        // recreate the region buffer if the region width and height changed
        if (!region.needFill && (region.width != width || region.height != height)) {
            region.needFill = true;
        }
        region.width = width;
        region.height = height;
        switch (depth) { // as defined in Table 5, section 7.2.3
            case 1:
                region.depth = 2;
                break;
            case 2:
                region.depth = 4;
                break;
            case 3:
                region.depth = 8;
                break;
            default:
                LogUtils.w("unexpected depth : " + depth);
                region.depth = 8;
                break;
        }
        region.clutId = clutId;
        if (depth == 1)
            region.backgroundColor = region2BitPixelCode;
        else if (depth == 2)
            region.backgroundColor = region4BitPixelCode;
        else if (depth == 3)
            region.backgroundColor = region8BitPixelCode;

        // To create the region buffer during start of the each region, no need to create the region
        // buffer for all the times.If we create the region buffer for each of the region,
        // the previous text may dissappear.So here we have create the region buffer
        // when the region fill flag is set
        if (region.needFill) {
            region.createBuffer();
        }
        while (parser.getPosInBytes() < endPos) {
            int objectId = parser.readInt(16, "object_id");
            int objectType = parser.readInt(2, "object_type");
            int objectProvider = parser.readInt(2, "object_provider_flag");
            if (objectProvider != 0) {
                LogUtils.i("graphical unit not is stream, not handled");
                continue;
            }
            int x = parser.readInt(12, "object_horizontal_position");
            parser.skip(4, "reserved");
            int y = parser.readInt(12, "object_vertical_position");
            int foreground = 0;
            int background = 0;
            if (objectType == 1 || objectType == 2) {
                foreground = parser.readInt(8, "foreground_pixel_code");
                background = parser.readInt(8, "background_pixel_code");
            }
            GraphicalUnit graphicalUnit = getGraphicalUnit(objectId);
            if (objectType == 0) {
                region.addBitmap(graphicalUnit, x, y);
            } else if (objectType == 1 || objectType == 2) {
                region.addText(graphicalUnit, x, y, foreground, background);
            } else {
                LogUtils.i("undefined graphical unit, do nothing");
            }
        }
    }

    private void parseClutDefinitionSegment(BufferParser parser, int segmentLength) {
        int endPos = parser.getPosInBytes() + segmentLength;
        int clutId = parser.readInt(8, "CLUT-id");
        int clutVersionNumber = parser.readInt(4, "CLUT_version_number");
        Clut clut;
        if (mCluts.containsKey(clutId)) {
            clut = mCluts.get(clutId);
            if (clut.versionNumber == clutVersionNumber)
                return;
        } else {
            clut = new Clut(clutId);
            mCluts.put(clutId, clut);
        }
        clut.versionNumber = clutVersionNumber;

        parser.skip(4, "reserved");
        while (parser.getPosInBytes() < endPos) {
            int entryId = parser.readInt(8, "CLUT_entry_id");
            boolean is2BitEntry = parser.readBool("2-bit/entry_CLUT_flag");
            boolean is4BitEntry = parser.readBool("4-bit/entry_CLUT_flag");
            boolean is8BitEntry = parser.readBool("8-bit/entry_CLUT_flag");

            parser.skip(4, "reserved");
            boolean isFullRange = parser.readBool("full_range_flag");
            int yValue;
            int crValue;
            int cbValue;
            int tValue;
            if (isFullRange) {
                yValue = parser.readInt(8, "Y-value");
                crValue = parser.readInt(8, "Cr-value");
                cbValue = parser.readInt(8, "Cb-value");
                tValue = parser.readInt(8, "T-value");
            } else {
                yValue = parser.readInt(6, "Y-value") << 2;
                crValue = parser.readInt(4, "Cr-value") << 4;
                cbValue = parser.readInt(4, "Cb-value") << 4;
                tValue = parser.readInt(2, "T-value") << 6;
            }

            if (is2BitEntry) clut.setColor(2, entryId, yValue, cbValue, crValue, tValue);
            if (is4BitEntry) clut.setColor(4, entryId, yValue, cbValue, crValue, tValue);
            if (is8BitEntry) clut.setColor(8, entryId, yValue, cbValue, crValue, tValue);
        }
    }

    private void parseObjectDataSegment(BufferParser parser, int segmentLength) {
        int id = parser.readInt(16, "object_id");
        int versionNumber = parser.readInt(4, "object_version_number");
        int codingMethod = parser.readInt(2, "object_coding_method");
        boolean nonModifyingColour = parser.readBool("non_modifying_colour_flag");
        parser.skip(1, "reserved");

        GraphicalUnit graphicalUnit = getGraphicalUnit(id);
        if (graphicalUnit.versionNumber == versionNumber)
            return;

        graphicalUnit.versionNumber = versionNumber;
        graphicalUnit.type = codingMethod;
        graphicalUnit.colorKey = nonModifyingColour;
        if (codingMethod == 0) {
            long position = parser.getPosInBits();
            for (Region region : mRegions.values()) {
                // process based on the region buffer
                if (!region.hasPixelBuffer()) continue;
                region.fillIfNeeded(getClut(region.clutId));
                for (Region.GraphicalUnitComposition graphicalUnitComposition : region.graphicalUnitCompositions) {
                    if (graphicalUnitComposition.graphicalUnit != graphicalUnit)
                        continue;
                    mGraphicalContext.init(region, getClut(region.clutId));
                    parser.setPosInBits(position);
                    int topFieldDataBlockLength = parser.readInt(16, "top_field_data_block_length");
                    int bottomFieldDataBlockLength = parser.readInt(16, "bottom_field_data_block_length");

                    // top
                    int endPos = parser.getPosInBytes() + topFieldDataBlockLength;
                    mGraphicalContext.setOffset(
                            graphicalUnitComposition.x, graphicalUnitComposition.y);
                    mGraphicalContext.resetMapTables();
                    while (parser.getPosInBytes() < endPos) {
                        readPixelDataSubBlock(graphicalUnit, parser);
                    }

                    // bottom
                    if (bottomFieldDataBlockLength == 0) {
                        parser.setPosInBits(position + 32);
                        endPos = parser.getPosInBytes() + topFieldDataBlockLength;
                    } else {
                        endPos = parser.getPosInBytes() + bottomFieldDataBlockLength;
                    }
                    mGraphicalContext.setOffset(
                            graphicalUnitComposition.x, graphicalUnitComposition.y + 1);
                    mGraphicalContext.resetMapTables();
                    while (parser.getPosInBytes() < endPos) {
                        readPixelDataSubBlock(graphicalUnit, parser);
                    }
                }
            }
            // word align
            int deltaWord = (4 - parser.getPosInBytes() % 4);
            if (deltaWord < 4)
                parser.skip(deltaWord * 8, "wordaligned");
        }
        if (codingMethod == 1) {
            LogUtils.i("codingMethod characters never used, and then not implemented");
            int numberOfCodes = parser.readInt(8, "number of codes");
            char[] codes = new char[numberOfCodes];
            for (int i = 0; i < numberOfCodes; ++i) {
                codes[i] = (char) (parser.readInt(16, "character_code") & 0xFFFF);
            }
            graphicalUnit.codes = codes;
        }
    }

    private boolean read2bitPixelCode(GraphicalUnit graphicalUnit, BufferParser parser) {
        int nbPixels = 1;
        int clutEntry = parser.readInt(2, "2-bitpixel-code");
        if (clutEntry != 0) {
            nbPixels = 1;
        }
        if (clutEntry == 0) {
            boolean switch1 = parser.readBool("switch_1");
            if (switch1) {
                nbPixels = parser.readInt(3, "run_length_3-10") + 3;
                clutEntry = parser.readInt(2, "2-bitpixel-code");
            } else {
                boolean switch2 = parser.readBool("switch_2");
                if (!switch2) {
                    int switch3 = parser.readInt(2, "switch_3");
                    switch (switch3) {
                        case 0:
                            nbPixels = 0;
                            break;
                        case 1:
                            nbPixels = 2;
                            break;
                        case 2:
                            nbPixels = parser.readInt(4, "run_length_12-27") + 12;
                            clutEntry = parser.readInt(2, "2-bitpixel-code");
                            break;
                        case 3:
                            nbPixels = parser.readInt(8, "run_length_29-284") + 29;
                            clutEntry = parser.readInt(2, "2-bitpixel-code");
                            break;
                    }
                }
            }
        }

        mGraphicalContext.draw(graphicalUnit.colorKey, clutEntry, 2, nbPixels);

        return (nbPixels > 0);
    }

    private boolean read4bitPixelCode(GraphicalUnit graphicalUnit, BufferParser parser) {
        int nbPixels = 1;
        int clutEntry = parser.readInt(4, "4-bitpixel-code");
        if (clutEntry != 0) {
            nbPixels = 1;
        } else {
            int switch1 = parser.readInt(1, "switch_1");
            if (switch1 == 0) {
                nbPixels = parser.readInt(3, "run_length_3-9");
                if (nbPixels != 0)
                    nbPixels += 2;
            } else {
                int switch2 = parser.readInt(1, "switch_2");
                if (switch2 == 0) {
                    nbPixels = parser.readInt(2, "run_length_4-7") + 4;
                    clutEntry = parser.readInt(4, "4-bitpixel-code");
                } else {
                    int switch3 = parser.readInt(2, "switch_3");
                    switch (switch3) {
                        case 0:
                            nbPixels = 1;
                            break;
                        case 1:
                            nbPixels = 2;
                            break;
                        case 2:
                            nbPixels = parser.readInt(4, "run_length_9-24") + 9;
                            clutEntry = parser.readInt(4, "4-bitpixel-code");
                            break;
                        case 3:
                            nbPixels = parser.readInt(8, "run_length_25-280") + 25;
                            clutEntry = parser.readInt(4, "4-bitpixel-code");
                            break;
                    }
                }
            }
        }

        mGraphicalContext.draw(graphicalUnit.colorKey, clutEntry, 4, nbPixels);

        return (nbPixels > 0);
    }

    private boolean read8bitPixelCode(GraphicalUnit graphicalUnit, BufferParser parser) {
        int nbPixels = 1;
        int clutEntry = parser.readInt(8, "8-bitpixel-code");
        if (clutEntry != 0) {
            nbPixels = 1;
        }
        if (clutEntry == 0) {
            boolean switch1 = parser.readBool("switch_1");
            if (!switch1) {
                nbPixels = parser.readInt(7, "run_length_1-127");
            } else {
                nbPixels = parser.readInt(7, "run_length_3-127");
                clutEntry = parser.readInt(8, "8-bitpixel-code");
            }
        }

        mGraphicalContext.draw(graphicalUnit.colorKey, clutEntry, 8, nbPixels);

        return (nbPixels > 0);
    }

    private void overrideMapTable(BufferParser parser, int srcDepth, int dstDepth, String what) {
        int nbColors;
        switch (srcDepth) {
            case 2:
                nbColors = 4;
                break;
            case 4:
                nbColors = 16;
                break;
            default:
                return;
        }
        int[] mapTable = new int[nbColors];
        for (int i = 0; i < nbColors; ++i) {
            mapTable[i] = parser.readInt(srcDepth, what);
        }
        mGraphicalContext.setMapTable(srcDepth, dstDepth, mapTable);
    }

    private void readPixelDataSubBlock(GraphicalUnit graphicalUnit, BufferParser parser) {
        int dataType = parser.readInt(8, "data_type");

        switch (dataType) {
            case 0x10:
                while (read2bitPixelCode(graphicalUnit, parser)) ;
                if (parser.getPosInBits() % 8 != 0)
                    parser.setPosInBits((parser.getPosInBytes() + 1) * 8);
                break;
            case 0x11:
                while (read4bitPixelCode(graphicalUnit, parser)) ;
                if (parser.getPosInBits() % 8 != 0)
                    parser.setPosInBits((parser.getPosInBytes() + 1) * 8);
                break;
            case 0x12:
                while (read8bitPixelCode(graphicalUnit, parser)) ;
                break;
            case 0x20:
                overrideMapTable(parser, 2, 4, "2_to_4-bit_map-table");
                break;
            case 0x21:
                overrideMapTable(parser, 2, 8, "2_to_8-bit_map-table");
                break;
            case 0x22:
                overrideMapTable(parser, 4, 8, "4_to_8-bit_map-table");
                break;
            case 0xF0: // end of line
                mGraphicalContext.nextLine();
                break;
            default:
                LogUtils.w("dataType is reserved" + dataType);
                break;
        }
    }

    private void parseSegment(BufferParser parser) {

        int type = parser.readInt(8, "segment_type");
        int pageId = parser.readInt(16, "page_id");
        int segmentLength = parser.readInt(16, "segment_length");

        if (pageId != mPageId && pageId != mAncillaryPageId) {
            LogUtils.i("rejected segment with page : " + pageId);
            mMustStop = true;
            return;
        }
        if (segmentLength > parser.remaining()) {
            LogUtils.w("not enough data for segment type:" +type +"length = " +segmentLength);
            mMustStop = true;
            return;
        }

        int endPos = parser.getPosInBytes() + segmentLength;

        switch (type) {
            case SEGMENT_DISPLAY_DEFINITION:
                parseDisplayDefinitionSegment(parser);
                break;
            case SEGMENT_PAGE_COMPOSITION:
                parsePageCompositionSegment(parser, pageId, segmentLength);
                break;
            case SEGMENT_REGION_COMPOSITION:
                parseRegionCompositionSegment(parser, segmentLength);
                break;
            case SEGMENT_CLUT_DEFINITION:
                parseClutDefinitionSegment(parser, segmentLength);
                break;
            case SEGMENT_OBJECT_DATA:
                parseObjectDataSegment(parser, segmentLength);
                break;
            case SEGMENT_END_OF_DISPLAY_SET:
                mMustStop = true;
                mDisplayPage = true;
                break;
            case SEGMENT_DISPARITY_SIGNALING:
                LogUtils.i("SEGMENT_DISPARITY_SIGNALING");
            default:
                LogUtils.w("Unhandled segment type " + type);
                break;
        }
        parser.setPosInBits(endPos * 8);
    }

    public void setPageIds(int pageId, int ancillaryPageId) {
        mPageId = pageId;
        mAncillaryPageId = ancillaryPageId;
    }

    private void dumpDisplaySetInfos() {
        Log.i(TAG, String.format("DvbSubtitle : display [%dx%d]", mDisplayWidth, mDisplayHeight));
        if (mPage == null) {
            Log.i(TAG, "DvbSubtitle : no page defined");
        } else {
            String strPageState;
            switch (mPage.state) {
                case PAGE_STATE_NORMAL_CASE:
                    strPageState = "normal case";
                    break;
                case PAGE_STATE_ACQUISITION_POINT:
                    strPageState = "acquisition";
                    break;
                case PAGE_STATE_MODE_CHANGE:
                    strPageState = "mode change";
                    break;
                default:
                    strPageState = String.format(Locale.US, "page state unknown(%d)", mPage.state);
                    break;
            }
            Log.i(TAG, String.format("DvbSubtitle : page [id:%d, state:%s, version:%d, timeout:%d], nb_regions:%d",
                    mPage.id, strPageState, mPage.versionNumber, mPage.timeoutSec,
                    mPage.regionCompositions.size()));
            for (Page.RegionComposition regionComposition : mPage.regionCompositions) {
                Log.i(TAG, String.format("  Region %d [%d,%d %dx%d] depth=%d",
                        regionComposition.region.id,
                        regionComposition.x, regionComposition.y,
                        regionComposition.region.width, regionComposition.region.height,
                        regionComposition.region.depth));
                for (Region.GraphicalUnitComposition unitComposition : regionComposition.region.graphicalUnitCompositions) {
                    Log.i(TAG, String.format("     Unit %d [%d,%d] ",
                            unitComposition.graphicalUnit.id,
                            unitComposition.x, unitComposition.y));
                }
            }
        }
        Log.i(TAG, String.format("DvbSubtitle : regions:%d, objects:%d clut:%d", mRegions.size(), mGraphicalUnits.size(), mCluts.size()));
        Log.i(TAG, "DvbSubtitle : regions");
        for (Region region : mRegions.values()) {
            Log.i(TAG, String.format("   Region : id:%d, v:%d size:%dx%d, depth:%d, clutId:%d, nb_units:%d",
                    region.id, region.versionNumber, region.width, region.height, region.depth, region.clutId,
                    region.graphicalUnitCompositions.size()));
            for (Region.GraphicalUnitComposition graphicalUnitComposition : region.graphicalUnitCompositions) {
                Log.i(TAG, String.format("     Unit:%d, pos:%d,%d",
                        graphicalUnitComposition.graphicalUnit.id,
                        graphicalUnitComposition.x,
                        graphicalUnitComposition.y));
            }
        }
        Log.i(TAG, "DvbSubtitle : objects");
        for (GraphicalUnit unit : mGraphicalUnits.values()) {
            Log.i(TAG, String.format("   Object : id:%d, v:%d type:%d",
                    unit.id, unit.versionNumber, unit.type));
        }
        Log.i(TAG, "DvbSubtitle : clut");
        for (Clut clut : mCluts.values()) {
            Log.i(TAG, String.format("   Clut : id:%d, v:%d",
                    clut.id, clut.versionNumber));
        }
    }

    private Bitmap buildPage() {
        // dumpDisplaySetInfos();
        Bitmap bitmap;

        // get display info
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.setToDefaults();
        int x = 0;
        int y = 0;
        if (mDisplayHasWindow) {
            x = mDisplayWindowX;
            y = mDisplayWindowY;
            if ((x + mDisplayWindowWidth > mDisplayWidth) || (y + mDisplayWindowHeight > mDisplayHeight)) {
                LogUtils.w("suspicious window size [" + mDisplayWindowX + mDisplayWindowY
                        +mDisplayWindowWidth +"x" +mDisplayWindowHeight +"] display:[" + mDisplayWidth
                        + mDisplayHeight +"]");
            }
        }
        bitmap = Bitmap.createBitmap(metrics, mDisplayWidth, mDisplayHeight, Bitmap.Config.ARGB_8888);

        // draw page in bitmap
        mPage.draw(bitmap, x, y);

        return bitmap;
    }

    public void parse(InputBuffer inputBuffer) {
        mMustStop = false;
        mDisplayPage = false;

        BufferParser parser = new BufferParser();
        parser.setBytes(inputBuffer.buffer.array(), inputBuffer.offset, inputBuffer.size);
/*
        StringBuilder bufferString = new StringBuilder();
        for(byte ccByte :parser.getBytes()){
            bufferString.append(String.format("%02x", ccByte));
        }
 */
        Bitmap bitmap;

        int dataIdentifier = parser.readInt(8, "data_identifier");
        LogUtils.d("dataIdentifier = " + dataIdentifier);
        if (dataIdentifier != 0x20) {
            notifyError(String.format("unexpected data identifier (%x vs 0x20) in pes", dataIdentifier));
            notifyInputBuffer(inputBuffer);
            return;
        }
        int subtitleStreamId = parser.readInt(8, "subtitle_stream_id");
        LogUtils.d("subtitleStreamId = " + subtitleStreamId);
        if (subtitleStreamId != 0x0) {
            notifyError(String.format("unexpected stream id (%x vs 0x0) in pes", subtitleStreamId));
            notifyInputBuffer(inputBuffer);
            return;
        }

        while (parser.remaining() > 0 && !mMustStop) {
            int synchro = parser.readInt(8, "sync_byte");
            switch (synchro) {
                case START_SEGMENT_BYTE:
                    parseSegment(parser);
                    break;
                case END_PES_BYTE:
                    mMustStop = true;
                    break;
                default:
                    LogUtils.w("invalid pes, unexpected byte " +(synchro & 0xFF)
                     +"(must be start:" +START_SEGMENT_BYTE + "or end:" + END_PES_BYTE +")");
                    mMustStop = true;
                    break;
            }
        }

        if (mDisplayPage && mPage != null) {
            bitmap = buildPage();
            notifyOutputBuffer(bitmap, inputBuffer.presentationTimeUs, mPage.timeoutSec);
        }
        notifyInputBuffer(inputBuffer);
    }
}
