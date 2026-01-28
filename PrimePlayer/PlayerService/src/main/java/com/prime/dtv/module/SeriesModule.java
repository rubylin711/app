package com.prime.dtv.module;

import android.os.Parcel;
import android.os.SystemProperties;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.datastructure.sysdata.SeriesInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
            Log.d(TAG, "get_series => channelId = "+channelId);
            Log.d(TAG, "get_series: => series key = " + Arrays.toString(key));
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
        byte[] series_key = new byte[SeriesInfo.Series.MAX_SERIES_KEY_LENGTH];
        int number_of_episodes = 0;
        List<SeriesInfo.Episode> episodeList = series.getEpisodeList();
        try {
            reply.readByteArray(series_key);
        } catch (RuntimeException e) {
            Log.e(TAG, "init_series: read series key fail", e);
            Log.e(TAG, "init_series: check MAX_SERIES_KEY_LENGTH in dtvservice");
        }

        series.setSeriesKey(series_key);
        number_of_episodes = reply.readInt();
        if(DEBUG) {
            Log.d(TAG, "init_series: series key = " + Arrays.toString(series.getSeriesKey()));
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
