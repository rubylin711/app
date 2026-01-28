package com.prime.dtv.service.subtitle.dvb;

class GraphicalUnit {
    int id;
    int versionNumber;
    int type;
    boolean colorKey;
    char[] codes;

    GraphicalUnit(int id) {
        this.id = id;
        versionNumber = -1;
    }
}
