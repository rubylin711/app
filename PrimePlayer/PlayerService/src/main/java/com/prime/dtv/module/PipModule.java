package com.prime.dtv.module;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.Surface;

import com.prime.dtv.PrimeDtvMediaPlayer;

public class PipModule {
    private static final String TAG = "PipModule";

    private static final int CMD_PIP_BASE = PrimeDtvMediaPlayer.CMD_Base + 0x1200;

    //PIP
    private static final int CMD_PIP_OPEN = CMD_PIP_BASE + 0x01;
    private static final int CMD_PIP_CLOSE = CMD_PIP_BASE + 0x02;
    private static final int CMD_PIP_START = CMD_PIP_BASE + 0x03;
    private static final int CMD_PIP_STOP = CMD_PIP_BASE + 0x04;
    private static final int CMD_PIP_SET_WINSIZE = CMD_PIP_BASE + 0x05;
    private static final int CMD_PIP_EXCHANGE = CMD_PIP_BASE + 0x06;

    private static final String SET_SURFACE = "com.prime.DTVPlayer.setSurface";//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk


    //PIP -start
    public int pip_open(int x, int y, int width, int height) {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIP_OPEN);
        request.writeInt(x);
        request.writeInt(y);
        request.writeInt(width);
        request.writeInt(height);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pip_close() {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIP_CLOSE);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pip_start(long channelId, int show) {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIP_START);
        request.writeInt((int) channelId);
        request.writeInt(show);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pip_stop() {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIP_STOP);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pip_set_window(int x, int y, int width, int height) {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIP_SET_WINSIZE);
        request.writeInt(x);
        request.writeInt(y);
        request.writeInt(width);
        request.writeInt(height);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int pip_exchange() {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIP_EXCHANGE);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }
    //PIP -end

    public void pip_mod_set_display(Context context, Surface surface, int type) // 1:TimeShift, 0:View //gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
        if (type == 0) // View
        {
            Log.d(TAG, "pip_mod_set_display: View");
            //commClearDisplay(surface);
            comm_set_display(context, surface, type);//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
        } else if (type == 1) // TimeShift
        {
            Log.d(TAG, "pip_mod_set_display: TimeShift surface = " + surface);
            //commClearDisplay(surface);
            comm_set_display(context, surface, type);//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
        }
    }

    // jim 2019/05/29 fix Android P set surface failed, using parcel to deliver Surface has some problem -s
    public void pip_mod_clear_display(Surface surface) {
        comm_clear_display(surface);
    }

    private int comm_clear_display(Surface surface) {
        //_ClearSideband(surface);
        return 0;
    }

    private int comm_set_display(Context context, Surface surface, int type)//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
//        int hwind = 0;
//        PrimeDtvMediaPlayer dtv = PrimeDtvMediaPlayer.getInstance();
//        if ( type == 0 )// Edwin 20181123 to set View's surface
//        {
//            hwind = dtv.commGetWindHandle();
//            Log.d(TAG, "commsetDisplay: commGetWindHandle = "+hwind);
//        }
//        else if ( type == 1 ) // Edwin 20181123 to set TimeShift's surface
//        {
//            hwind = dtv.commGetTimeshiftWindHandle();
//            Log.d(TAG, "commsetDisplay: commgetTimeshiftWindHandle = "+hwind);
//        }

// gary modify , need porting DoSideband for set surface
//        _DoSideband( surface, hwind ) ;
//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk-s
        Intent intent = new Intent();
        intent.setAction(SET_SURFACE);
        intent.setComponent(new ComponentName("com.prime.pesisystem", "com.prime.pesisystem.Receiver"));
//        intent.setPackage("com.prime.pesisystem");
        Bundle bundle = new Bundle();
//        Surface surface = mSurfaceView.getHolder().getSurface();
        bundle.putParcelable("Surface", (Parcelable) surface);
        if (surface != null) // jim 2020/12/16 add set surface to RTK_SerVideoSurfaceEx -s
            Log.d(TAG, "onReceive surface = " + surface.toString());
        bundle.putInt("is_pip", type);
        intent.putExtras(bundle);
        context.sendBroadcast(intent);
//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk-e
        return 0;
    }
    // jim 2019/05/29 fix Android P set surface failed, using parcel to deliver Surface has some problem -e
    /*
    private int commClearDisplay ( Surface surface )
    {
        Log.d( TAG, "clear" );
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString( DTV_INTERFACE_NAME );
        request.writeInt( CMD_COMM_ClearDisplay );
        surface.writeToParcel( request, 0 );
        invokeex( request, reply );

        request.recycle();
        reply.recycle();

        return 0;
    }
*/

    /*
    private int commsetDisplay(Surface surface, int type ) // 1:TimeShift, 0:Mpeg
    {
        Log.d(TAG, "setDisplay in");
        int ret = 0;
        int hwind = 0;
        if ( type == 0 )// Edwin 20181123 to set View's surface
        {
            hwind = commGetWindHandle();
            Log.d(TAG, "commsetDisplay: commGetWindHandle = "+hwind);
        }
        else if ( type == 1 ) // Edwin 20181123 to set TimeShift's surface
        {
            hwind = commgetTimeshiftWindHandle();
            Log.d(TAG, "commsetDisplay: commgetTimeshiftWindHandle = "+hwind);
        }
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        if (null != surface)
        {
            Log.d(TAG, "setDisplay");
            request.writeInt(CMD_COMM_SetDisplay);
            request.writeInt(hwind);
            surface.writeToParcel(request, 0);
            invokeex(request, reply);

            ret = reply.readInt();
        }
        else
        {
            Log.d(TAG, "setDisplay no surface");
        }

        request.recycle();
        reply.recycle();

        return ret;
    }
*/


}
