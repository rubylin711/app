package com.prime.dtv.service.subtitle.dvb;


import android.util.Log;

class Clut {

    private static final String TAG = "Clut";

    private static final int[] sDefault8bitClut = {
        /*        A R G B ,   A R G B ,   A R G B ,   A R G B  */
        /* 000 */ 0xFF000000, 0xBFFF0000, 0xBF00FF00, 0xBFFFFF00,
        /* 004 */ 0xBF0000FF, 0xBFFF00FF, 0xBF00FFFF, 0xBFFFFFFF,
        /* 008 */ 0x7F000000, 0x7F550000, 0x7F005500, 0x7F555500,
        /* 012 */ 0x7F000055, 0x7F550055, 0x7F005555, 0x7F555555,
        /* 016 */ 0x00AA0000, 0x00FF0000, 0x00AA5500, 0x00FF5500,
        /* 020 */ 0x00AA0055, 0x00FF0055, 0x00AA5555, 0x00FF5555,
        /* 024 */ 0x7FAA0000, 0x7FFF0000, 0x7FAA5500, 0x7FFF5500,
        /* 028 */ 0x7FAA0055, 0x7FFF0055, 0x7FAA5555, 0x7FFF5555,
        /* 032 */ 0x0000AA00, 0x0055AA00, 0x0000FF00, 0x0055FF00,
        /* 036 */ 0x0000AA55, 0x0055AA55, 0x0000FF55, 0x0055FF55,
        /* 040 */ 0x7F00AA00, 0x7F55AA00, 0x7F00FF00, 0x7F55FF00,
        /* 044 */ 0x7F00AA55, 0x7F55AA55, 0x7F00FF55, 0x7F55FF55,
        /* 048 */ 0x00AAAA00, 0x00FFAA00, 0x00AAFF00, 0x00FFFF00,
        /* 052 */ 0x00AAAA55, 0x00FFAA55, 0x00AAFF55, 0x00FFFF55,
        /* 056 */ 0x7FAAAA00, 0x7FFFAA00, 0x7FAAFF00, 0x7FFFFF00,
        /* 060 */ 0x7FAAAA55, 0x7FFFAA55, 0x7FAAFF55, 0x7FFFFF55,
        /* 064 */ 0x000000AA, 0x005500AA, 0x000055AA, 0x005555AA,
        /* 068 */ 0x000000FF, 0x005500FF, 0x000055FF, 0x005555FF,
        /* 072 */ 0x7F0000AA, 0x7F5500AA, 0x7F0055AA, 0x7F5555AA,
        /* 076 */ 0x7F0000FF, 0x7F5500FF, 0x7F0055FF, 0x7F5555FF,
        /* 080 */ 0x00AA00AA, 0x00FF00AA, 0x00AA55AA, 0x00FF55AA,
        /* 084 */ 0x00AA00FF, 0x00FF00FF, 0x00AA55FF, 0x00FF55FF,
        /* 088 */ 0x7FAA00AA, 0x7FFF00AA, 0x7FAA55AA, 0x7FFF55AA,
        /* 092 */ 0x7FAA00FF, 0x7FFF00FF, 0x7FAA55FF, 0x7FFF55FF,
        /* 096 */ 0x0000AAAA, 0x0055AAAA, 0x0000FFAA, 0x0055FFAA,
        /* 100 */ 0x0000AAFF, 0x0055AAFF, 0x0000FFFF, 0x0055FFFF,
        /* 104 */ 0x7F00AAAA, 0x7F55AAAA, 0x7F00FFAA, 0x7F55FFAA,
        /* 108 */ 0x7F00AAFF, 0x7F55AAFF, 0x7F00FFFF, 0x7F55FFFF,
        /* 112 */ 0x00AAAAAA, 0x00FFAAAA, 0x00AAFFAA, 0x00FFFFAA,
        /* 116 */ 0x00AAAAFF, 0x00FFAAFF, 0x00AAFFFF, 0x00FFFFFF,
        /* 120 */ 0x7FAAAAAA, 0x7FFFAAAA, 0x7FAAFFAA, 0x7FFFFFAA,
        /* 124 */ 0x7FAAAAFF, 0x7FFFAAFF, 0x7FAAFFFF, 0x7FFFFFFF,
        /* 128 */ 0x007F7F7F, 0x00AA7F7F, 0x007FAA7F, 0x00AAAA7F,
        /* 132 */ 0x007F7FAA, 0x00AA7FAA, 0x007FAAAA, 0x00AAAAAA,
        /* 136 */ 0x00000000, 0x002B0000, 0x00002B00, 0x002B2B00,
        /* 140 */ 0x0000002B, 0x002B002B, 0x00002B2B, 0x002B2B2B,
        /* 144 */ 0x00D47F7F, 0x00FF7F7F, 0x00D4AA7F, 0x00FFAA7F,
        /* 148 */ 0x00D47FAA, 0x00FF7FAA, 0x00D4AAAA, 0x00FFAAAA,
        /* 152 */ 0x00550000, 0x00800000, 0x00552B00, 0x00802B00,
        /* 156 */ 0x0055002B, 0x0080002B, 0x00552B2B, 0x00802B2B,
        /* 160 */ 0x007FD47F, 0x00AAD47F, 0x007FFF7F, 0x00AAFF7F,
        /* 164 */ 0x007FD4AA, 0x00AAD4AA, 0x007FFFAA, 0x00AAFFAA,
        /* 168 */ 0x00005500, 0x002B5500, 0x00008000, 0x002B8000,
        /* 172 */ 0x0000552B, 0x002B552B, 0x0000802B, 0x002B802B,
        /* 176 */ 0x00D4D47F, 0x00FFD47F, 0x00D4FF7F, 0x00FFFF7F,
        /* 180 */ 0x00D4D4AA, 0x00FFD4AA, 0x00D4FFAA, 0x00FFFFAA,
        /* 184 */ 0x00555500, 0x00805500, 0x00558000, 0x00808000,
        /* 188 */ 0x0055552B, 0x0080552B, 0x0055802B, 0x0080802B,
        /* 192 */ 0x007F7FD4, 0x00AA7FD4, 0x007FAAD4, 0x00AAAAD4,
        /* 196 */ 0x007F7FFF, 0x00AA7FFF, 0x007FAAFF, 0x00AAAAFF,
        /* 200 */ 0x00000055, 0x002B0055, 0x00002B55, 0x002B2B55,
        /* 204 */ 0x00000080, 0x002B0080, 0x00002B80, 0x002B2B80,
        /* 208 */ 0x00D47FD4, 0x00FF7FD4, 0x00D4AAD4, 0x00FFAAD4,
        /* 212 */ 0x00D47FFF, 0x00FF7FFF, 0x00D4AAFF, 0x00FFAAFF,
        /* 216 */ 0x00550055, 0x00800055, 0x00552B55, 0x00802B55,
        /* 220 */ 0x00550080, 0x00800080, 0x00552B80, 0x00802B80,
        /* 224 */ 0x007FD4D4, 0x00AAD4D4, 0x007FFFD4, 0x00AAFFD4,
        /* 228 */ 0x007FD4FF, 0x00AAD4FF, 0x007FFFFF, 0x00AAFFFF,
        /* 232 */ 0x00005555, 0x002B5555, 0x00008055, 0x002B8055,
        /* 236 */ 0x00005580, 0x002B5580, 0x00008080, 0x002B8080,
        /* 240 */ 0x00D4D4D4, 0x00FFD4D4, 0x00D4FFD4, 0x00FFFFD4,
        /* 244 */ 0x00D4D4FF, 0x00FFD4FF, 0x00D4FFFF, 0x00FFFFFF,
        /* 248 */ 0x00555555, 0x00805555, 0x00558055, 0x00808055,
        /* 252 */ 0x00555580, 0x00805580, 0x00558080, 0x00808080
    };

    private static final int[] sDefault4bitClut = {
        /*        A R G B ,   A R G B ,   A R G B ,   A R G B  */
        /* 000 */ 0xFF000000, 0x00FF0000, 0x0000FF00, 0x00FFFF00,
        /* 004 */ 0x000000FF, 0x00FF00FF, 0x0000FFFF, 0x00FFFFFF,
        /* 008 */ 0x00000000, 0x00800000, 0x00008000, 0x00808000,
        /* 012 */ 0x00000080, 0x00800080, 0x00008080, 0x00808080
    };

    private static final int[] sDefault2bitClut = {
            /*A R G B ,   A R G B ,   A R G B ,   A R G B  */
            0xFF000000, 0x00FFFFFF, 0x00000000, 0x00808080
    };


    int id;
    int versionNumber;
    int[] clut8bits;
    int[] clut4bits;
    int[] clut2bits;

    Clut(int id) {
        this.id = id;
        this.versionNumber = -1;
        this.clut8bits = sDefault8bitClut.clone();
        this.clut4bits = sDefault4bitClut.clone();
        this.clut2bits = sDefault2bitClut.clone();
    }

    int clamp(double value) {
        if (value < 0)
            return 0;
        if (value > 255)
            return 255;
        return (int) value;
    }

    int getColor(int index, int depth) {
        switch (depth) {
            case 2:
                return clut2bits[index];
            case 4:
                return clut4bits[index];
            case 8:
                return clut8bits[index];
            default:
                // return blue color to make sure that the error is visible
                return 0xFF0000FF;
        }
    }

    void setColor(int depth, int index, int yValue, int cbValue, int crValue, int tValue) {
        // according to NOTE 1, in section 7.2.4 - Y_value : CLUT definition segment
        // 0 for means : full transparence
        //
        // equation to convert yuv to rgb are
        // r = y +1.403v
        // g = y -0.344u -0.714v
        // b = y + 1.770v
        // with r,g,b,y in range [0, 1]
        // and u,v in range [-0.5, 0.5]
        //
        // according to ITU-R BT.601
        // range of y is [16-235]
        // range of cb and cr is [16,239]
        // that's explain for instance the first equation
        // r = (y-16)*256/220 + 1.403*(cr-128)*256/224;
        // ...
        // and why we clamp
        //
        // PS: I have not found in specs if y must be between [16-235] or [1-220] (and same
        // for cb,cr). But it seems that range [16-235] gives better results, although I found
        // 1 service (test stream built with BBC contents) with cb, cr = 0
        //


        int argb;
        if (yValue == 0) {
            argb = 0;
        } else {
            if ((yValue<16 || yValue>235) ||
                    (cbValue<16 || cbValue>239) ||
                    (crValue<16 || crValue>239)) {
                Log.i(TAG, String.format("y,cb,cr is out of bounds y:%d[16, 235], cb:%d[16-239], cr:%d[16-239]",
                        yValue, cbValue, crValue));
            }

            double yFactor, crFactor, cbFactor;
            double r, g, b;
            yFactor = 1.164 * (yValue - 16);
            crFactor = (crValue - 128);
            cbFactor = (cbValue - 128);
            r = yFactor + (1.596 * crFactor);
            g = yFactor - (0.392 * cbFactor) - (0.813 * crFactor);
            b = yFactor + (2.017 * cbFactor);

            argb = (clamp(255 - tValue) << 24) |
                    (clamp(r) << 16) |
                    (clamp(g) << 8) |
                    (clamp(b));
        }

        switch (depth) {
            case 2:
                clut2bits[index] = argb;
                break;
            case 4:
                clut4bits[index] = argb;
                break;
            case 8:
                clut8bits[index] = argb;
                break;
            default:
                break;
        }
    }
}
