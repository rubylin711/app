package com.TvInput;

import android.net.Uri;
import androidx.tvprovider.media.tv.TvContractCompat;

import com.google.android.media.tv.companionlibrary.model.InternalProviderData;

public class TIFChannelData {
    final static String[] projection = {
            TvContractCompat.Channels._ID,
            TvContractCompat.Channels.COLUMN_INPUT_ID,
            TvContractCompat.Channels.COLUMN_DISPLAY_NUMBER,
            TvContractCompat.Channels.COLUMN_DISPLAY_NAME,
            TvContractCompat.Channels.COLUMN_DESCRIPTION,
            TvContractCompat.Channels.COLUMN_SERVICE_ID,
            TvContractCompat.Channels.COLUMN_ORIGINAL_NETWORK_ID,
            TvContractCompat.Channels.COLUMN_TRANSPORT_STREAM_ID,
            TvContractCompat.Channels.COLUMN_SERVICE_TYPE,
            TvContractCompat.Channels.COLUMN_TYPE,
            TvContractCompat.Channels.COLUMN_INTERNAL_PROVIDER_DATA,
    };

    private Uri channelUri ;

    private long channelId ;
    private String inputId ;
    private String displayNumber ;
    private String displayName ;
    private String descprition ;
    private long serviceId ;
    private long onId ;
    private long tsId ;
    private String serviceType;
    private String type;
    private InternalProviderData internalProviderData;

    public void setChannelUri( Uri value ) { channelUri = value ; }
    public void setChannelId( long value ) { channelId = value ; }
    public void setInputId( String value ) { inputId = value ; }
    public void setDisplayNumber( String value ) { displayNumber = value ; }
    public void setDisplayName( String value ) { displayName = value ; }
    public void setDescprition( String value ) { descprition = value ; }
    public void setServiceId( long value ) { serviceId = value ; }
    public void setOnId( long value ) { onId = value ; }
    public void setTsId( long value ) { tsId = value ; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public void setType(String type) { this.type = type; }
    public void setInternalProviderData(InternalProviderData internalProviderData) { this.internalProviderData = internalProviderData; };

    public Uri getChannelUri() { return channelUri ; }
    public long getChannelId() { return channelId ; }
    public String getInputId() { return inputId ; }
    public String getDisplayNumber() { return displayNumber ; }
    public String getDisplayName() { return displayName ; }
    public String getDescprition() { return descprition ; }
    public long getServiceId() { return serviceId ; }
    public long getOnId() { return onId ; }
    public long getTsId() { return tsId ; }
    public String getServiceType() { return serviceType; }
    public String getType() { return type; }
    public InternalProviderData getInternalProviderData() { return internalProviderData; }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("{")
                .append("\n channelUri : ").append(channelUri)
                .append("\n channelId : ").append(channelId)
                .append("\n inputId : ").append(inputId)
                .append("\n displayNumber : ").append(displayNumber)
                .append("\n displayName : ").append(displayName)
                .append("\n descprition : ").append(descprition)
                .append("\n serviceId : ").append(serviceId)
                .append("\n onId : ").append(onId)
                .append("\n tsId : ").append(tsId)
                .append("\n serviceType : ").append(serviceType)
                .append("\n type : ").append(type)
                .append("\n }").toString();
    }





}