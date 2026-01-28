package com.prime.datastructure.sysdata;

/**
 * Created by jim_huang on 2018/7/25.
 */

public class ResolutionInfo {
    private int Format;
    private String resolution;

    public ResolutionInfo( int Format, String resolution) {
        this.Format = Format;
        this.resolution = resolution;
    }

    public int getFormat() {
        return Format;
    }

    public void setFormat(int Format) {
        this.Format = Format;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    //to display object as a string in spinner
    @Override
    public String toString() {
        return resolution;
    }
}