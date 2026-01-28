package com.prime.dtv.service.subtitle.dvb;

import android.graphics.Bitmap;

import java.util.ArrayList;

class Page {

    static class RegionComposition {
        Region region;
        int x;
        int y;
    }

    int id;
    int versionNumber;
    int timeoutSec;
    int state;
    ArrayList<RegionComposition> regionCompositions;

    Page(int id) {
        this.id = id;
        this.regionCompositions = new ArrayList<>();
        this.versionNumber = -1;
    }

    void clearCompositions() {
        regionCompositions.clear();
    }

    void addRegionComposition(Region region, int x, int y) {
        RegionComposition composition = new RegionComposition();
        composition.region = region;
        composition.x = x;
        composition.y = y;
        regionCompositions.add(composition);
    }

    void draw(Bitmap bitmap, int offsetX, int offsetY) {
        for (RegionComposition regionComposition : regionCompositions) {
            Region region = regionComposition.region;
            if (region.pixels == null)
                continue;
            int x = offsetX + regionComposition.x;
            int y = offsetY + regionComposition.y;
            bitmap.setPixels(region.pixels, 0, region.width,
                    x, y, region.width, region.height);
        }
    }
}
