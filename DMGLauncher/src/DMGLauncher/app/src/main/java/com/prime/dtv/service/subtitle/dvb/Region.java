package com.prime.dtv.service.subtitle.dvb;

import java.util.ArrayList;
import java.util.Arrays;

class Region {

    static class GraphicalUnitComposition {

        static final int GRAPHICAL_UNIT_BITMAP = 0;
        static final int GRAPHICAL_UNIT_TEXT = 1;

        GraphicalUnit graphicalUnit;
        int type;
        int x;
        int y;

        // for text
        int foreground;
        int background;
    }

    int id;
    int versionNumber;
    boolean needFill;
    int width;
    int height;
    int depth;
    int clutId;
    int backgroundColor;
    int pixels[];

    ArrayList<GraphicalUnitComposition> graphicalUnitCompositions;

    Region(int id) {
        this.id = id;
        this.graphicalUnitCompositions = new ArrayList<>();
        this.versionNumber = -1;
    }

    void addBitmap(GraphicalUnit unit, int x, int y) {
        GraphicalUnitComposition composition = new GraphicalUnitComposition();
        composition.graphicalUnit = unit;
        composition.type = GraphicalUnitComposition.GRAPHICAL_UNIT_BITMAP;
        composition.x = x;
        composition.y = y;
        graphicalUnitCompositions.add(composition);
    }

    void addText(GraphicalUnit unit, int x, int y, int foreground, int background) {
        GraphicalUnitComposition composition = new GraphicalUnitComposition();
        composition.graphicalUnit = unit;
        composition.type = GraphicalUnitComposition.GRAPHICAL_UNIT_TEXT;
        composition.x = x;
        composition.y = y;
        composition.foreground = foreground;
        composition.background = background;
        graphicalUnitCompositions.add(composition);
    }

    void createBuffer() {
        pixels = new int[width * height];
    }

    void fillIfNeeded(Clut clut) {
        if (!needFill)
            return;

        int color = clut.getColor(backgroundColor, depth);
        Arrays.fill(pixels, 0, pixels.length, color);
        needFill = false;
    }

    boolean hasPixelBuffer() {
        return pixels != null;
    }
}