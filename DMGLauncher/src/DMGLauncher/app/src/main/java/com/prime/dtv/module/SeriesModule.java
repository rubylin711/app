package com.prime.dtv.module;

import android.os.Parcel;
import android.os.SystemProperties;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.dtv.sysdata.SeriesInfo;
import com.prime.dtv.utils.LogUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SeriesModule {
    private static final String TAG = "SeriesModule";

    private static boolean DEBUG = SystemProperties.getBoolean("persist.sys.prime.debug.series_modules", false);

    private static final int CMD_Series_Base = PrimeDtvMediaPlayer.CMD_Base + 0x1800;

    private static final int CMD_SERIES_ADD = CMD_Series_Base + 0x01;
    private static final int CMD_SERIES_DELETE = CMD_Series_Base + 0x02;
    private static final int CMD_SERIES_GET_SERIES = CMD_Series_Base + 0x03;
    private static final int CMD_SERIES_GET_SERIES_LIST = CMD_Series_Base + 0x04;
    private static final int CMD_SERIES_GET_ALL = CMD_Series_Base + 0x05;
    private static final int CMD_SERIES_SAVE = CMD_Series_Base + 0x06;

    private static final int MAX_DATA_ONE_CMD = 50;

    private List<SeriesInfo> g_SeriesInfo = new ArrayList<>();

    public int add_series(long channelId, byte[] key){
        int ret = -1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SERIES_ADD);
        request.writeInt((int) channelId);
        request.writeByteArray(key);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if(DEBUG) {
            Log.d(TAG,"add_series => channelId = "+channelId+" key = "+key);
            Log.d(TAG, " ret = "+ret);
        }

        return ret;
    }

    public int delete_series(long channelId, byte[] key){
        int ret = -1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SERIES_DELETE);
        request.writeInt((int) channelId);
        request.writeByteArray(key);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if(DEBUG) {
            Log.d(TAG,"delete_series => channelId = "+channelId+" key = "+key);
            Log.d(TAG, " ret = "+ret);
        }
        return ret;
    }

    public List<SeriesInfo> get_all_series_data(){
        int ret = -1;
        List<SeriesInfo> seriesInfoList = new ArrayList<>();
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if(DEBUG){
            Log.d(TAG, "get_all_series_data");
        }
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SERIES_GET_ALL);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS){
            int num_of_programe = reply.readInt();
            if(DEBUG){
                Log.d(TAG, "num_of_programe = "+num_of_programe);
            }
            for(int i = 0 ; i< num_of_programe ; i++){
                SeriesInfo seriesInfo = new SeriesInfo();
                int ch_id = reply.readInt();
                int num_of_series = reply.readInt();
                seriesInfo.setChannelId(ch_id);
                if(DEBUG){
                    Log.d(TAG, "********************************");
                    Log.d(TAG, " channel id = "+ch_id);
                    Log.d(TAG, " num_of_series = "+num_of_series);
                }
                for(int j=0 ; j<num_of_series ; j++){
                    init_series_info(seriesInfo, reply);
                }
                seriesInfoList.add(seriesInfo);
            }
        }

        return seriesInfoList;
    }

    public SeriesInfo get_series_info(long channelId){
        SeriesInfo seriesInfo = null;
        int ret = -1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SERIES_GET_SERIES_LIST);
        request.writeInt((int) channelId);

        if(DEBUG){
            Log.d(TAG,"get_series => channelId = "+channelId);
        }
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS){
            seriesInfo = new SeriesInfo();
            List<SeriesInfo.Series> seriesList = new ArrayList<>();
            int id = reply.readInt();
            int num_of_series = reply.readInt();
            seriesInfo.setChannelId(id);
            if(DEBUG){
                Log.d(TAG, "********************************");
                Log.d(TAG, " channel id = "+id);
                Log.d(TAG, " num_of_series = "+num_of_series);
            }
            for(int i=0 ; i<num_of_series ; i++){
                Log.d(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                init_series_info(seriesInfo, reply);
            }
        }

        return seriesInfo;
    }

    public SeriesInfo.Series get_series(long channelId, byte[] key){
        int ret = -1;
        SeriesInfo.Series series = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SERIES_GET_SERIES);
        request.writeInt((int) channelId);
        request.writeByteArray(key);
        if(DEBUG) {
            Log.d(TAG,"get_series => channelId = "+channelId);
            Log.d(TAG,"get_series => key = "+(int)key[0]+" "+(int)key[1]+" "+(int)key[2]+" "+(int)key[3]+" "+(int)key[4]+" "+(int)key[5]+" "+(int)key[6]+" "+(int)key[7]+" "+(int)key[8]+" "+(int)key[9]);
            Log.d(TAG,"get_series => key = "+(int)key[70]+" "+(int)key[71]+" "+(int)key[72]+" "+(int)key[73]+" "+(int)key[74]+" "+(int)key[75]+" "+(int)key[76]+" "+(int)key[77]+" "+(int)key[78]+" "+(int)key[79]);
        }
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if(ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS){
            series = new SeriesInfo.Series();
            init_series(series, reply);
        }
        if(DEBUG) {
            Log.d(TAG, " ret = "+ret);
        }
        return series;
    }

    public int save_series(){
        if(DEBUG) {
            Log.d(TAG,"save_series");
        }
        return PrimeDtvMediaPlayer.excute_command(CMD_SERIES_SAVE);
    }

    private void init_series_info(SeriesInfo seriesinfo, Parcel reply){
        List<SeriesInfo.Series> seriesList = seriesinfo.getSeriesList();
        SeriesInfo.Series series = new SeriesInfo.Series();
        init_series(series, reply);
        seriesList.add(series);
    }

    private void init_series(SeriesInfo.Series series, Parcel reply){
        byte[] series_key = new byte[80];
        int number_of_episodes = 0;
        List<SeriesInfo.Episode> episodeList = series.getEpisodeList();
        reply.readByteArray(series_key);
        series.setSeriesKey(series_key);
        number_of_episodes = reply.readInt();
        if(DEBUG) {
            Log.d(TAG,"get_series => key = "+(int)series_key[0]+" "+(int)series_key[1]+" "+(int)series_key[2]+" "+(int)series_key[3]+" "+(int)series_key[4]+" "+(int)series_key[5]+" "+(int)series_key[6]+" "+(int)series_key[7]+" "+(int)series_key[8]+" "+(int)series_key[9]);
            Log.d(TAG,"get_series => key = "+(int)series_key[70]+" "+(int)series_key[71]+" "+(int)series_key[72]+" "+(int)series_key[73]+" "+(int)series_key[74]+" "+(int)series_key[75]+" "+(int)series_key[76]+" "+(int)series_key[77]+" "+(int)series_key[78]+" "+(int)series_key[79]);
            Log.d(TAG, "number_of_episodes = " + number_of_episodes);
        }
        for(int i=0; i<number_of_episodes ; i++){
            int episode_key = reply.readInt();
            int episode_status = reply.readInt();
            boolean episode_last = reply.readBoolean();
            int year = reply.readInt();
            int month = reply.readInt();
            int date = reply.readInt();
            int hour = reply.readInt();
            int min = reply.readInt();
            LocalDateTime localDateTime = LocalDateTime.of(year, month, date, hour, min);
            int duration = reply.readInt();
            String event_name = reply.readString();
            if(DEBUG){
                Log.d(TAG, "Episode "+i+" ===============================");
                Log.d(TAG, "episode_key = "+episode_key);
                Log.d(TAG, "episode_status = "+episode_status);
                Log.d(TAG, "episode_last = "+episode_last);
                Log.d(TAG, "localDateTime = "+localDateTime);
                Log.d(TAG, "duration = "+duration);
                Log.d(TAG, "event_name = "+event_name);
            }
            episodeList.add(new SeriesInfo.Episode(episode_key,episode_status, episode_last, localDateTime, duration, event_name));
        }

    }

}
