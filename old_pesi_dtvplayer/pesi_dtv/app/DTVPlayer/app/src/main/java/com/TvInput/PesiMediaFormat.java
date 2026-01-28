package com.TvInput;

public class PesiMediaFormat {
    public static final int NO_VALUE = -1;
    /**
        * The width of the video in pixels, or {@link #NO_VALUE} if unknown or not applicable.
        */
    public int width;
    /**
       * The height of the video in pixels, or {@link #NO_VALUE} if unknown or not applicable.
       */
    public int height;

// Audio specific.
    /**
        * The number of audio channels, or {@link #NO_VALUE} if unknown or not applicable.
        */
    public int channelCount;
    /**
        * The audio sampling rate in Hz, or {@link #NO_VALUE} if unknown or not applicable.
        */
    public int sampleRate;

    /**
       * The language of the track, or null if unknown or not applicable.
       */
    public String language;

    public void setWidth(int width){
        this.width = width;
    }
    public int getWidth(){
        return this.width;
    }
    public void setHeight(int height){
        this.height = height;
    }
    public int getHeight(){
        return this.height;
    }

    public void setChannelCount(int channelCount){
        this.channelCount = channelCount;
    }
    public int getChannelCount(){
        return this.channelCount;
    }
    public void setSampleRate(int sampleRate){
        this.sampleRate = sampleRate;
    }
    public int getSampleRate(){
        return this.sampleRate;
    }

    public void setLanguage(String language){
        this.language = language;
    }
    public String getLanguage(){
        return this.language;
    }

}
