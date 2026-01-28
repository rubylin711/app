package com.prime.dtv.service.subtitle.dvb;

import java.util.Arrays;

class GraphicalContext {

    private static int[] sDefault2To4bitMapTable = {
            0, 0x7, 0x08, 0xF
    };

    private static int[] sDefault2To8bitMapTable = {
            0, 0x77, 0x88, 0xFF
    };

    private static int[] sDefault4To8bitMapTable = {
            0, 0x11, 0x22, 0x33,
            0x44, 0x55, 0x66, 0x77,
            0x88, 0x99, 0xAA, 0xBB,
            0xCC, 0xDD, 0xEE, 0xFF,
    };

    private Region region;
    private Clut clut;
    private int offsetX;
    private int offsetY;
    private int x;
    private int y;
    private int[] from2To4bitMapTable;
    private int[] from2To8bitMapTable;
    private int[] from4To8bitMapTable;

    GraphicalContext() {
        from2To4bitMapTable = sDefault2To4bitMapTable;
        from2To8bitMapTable = sDefault2To8bitMapTable;
        from4To8bitMapTable = sDefault4To8bitMapTable;
    }

    void setOffset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.x = offsetX;
        this.y = offsetY;
    }

    void init(Region region, Clut clut) {
        this.region = region;
        this.clut = clut;
    }

    void resetMapTables() {
        from2To4bitMapTable = sDefault2To4bitMapTable;
        from2To8bitMapTable = sDefault2To8bitMapTable;
        from4To8bitMapTable = sDefault4To8bitMapTable;
    }

    void setMapTable(int srcDepth, int dstDepth, int[] mapTable) {
        if (srcDepth == 2) {
            if (dstDepth == 4) {
                from2To4bitMapTable = mapTable;
            } else if (dstDepth == 8) {
                from2To8bitMapTable = mapTable;
            }
        } else if (srcDepth == 4) {
            from4To8bitMapTable = mapTable;
        }
    }

    void draw(boolean colorKey, int clutEntry, int clutDepth, int nbPixels) {
        if (colorKey && clutEntry == 1)
            return;
        if (nbPixels==0)
            return;

        // sanity check
        if (x + nbPixels + y * region.width >= region.pixels.length)
            return;

        // find color clut entry
        if (clutDepth==2 && region.depth==4)
            clutEntry = from2To4bitMapTable[clutEntry];
        else if (clutDepth==2 && region.depth==8)
            clutEntry = from2To8bitMapTable[clutEntry];
        else if (clutDepth==4 && region.depth==8)
            clutEntry = from4To8bitMapTable[clutEntry];
        int color = clut.getColor(clutEntry, region.depth);

        // copy pixels
        Arrays.fill(region.pixels, x + y * region.width, x + nbPixels + y * region.width, color);
        x += nbPixels;
    }

    void nextLine() {
        x = offsetX;
        y += 2;
    }
}
