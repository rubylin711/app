package com.prime.dtvplayer.Activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.SelectBoxView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeAdjustActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();
    private ActivityTitleView title;
    private ActivityHelpView helpView;
    private Spinner SpinnerAutoTimeSetting;
    private Spinner SpinnerGmtoffset;
    private Spinner SpinnerSummerTime;
    private SelectBoxView SelAutoTimeSetting;
    private SelectBoxView SelGmtoffset;
    private SelectBoxView SelSummerTime;
    private TextView TxvDate;
    TextView TxvCurrentTime=null;
    TextView TxvRegion=null;

    private int myear, premyear;
    private int mmonth, premmonth;
    private int mday, premday;
    private int mhourOfDay, premhourOfDay;
    private int mminute, premminute;
    private int dateModifyFlag=0;
    private int timeModifyFlag=0;
    private int mTimeZone=0;//eric lin 20180628 modify TimeAdjust
    private int mSummerTime=0;//eric lin 20180628 modify TimeAdjust

    public enum TIMEZONE_SECONDS{//eric lin 20180628 modify TimeAdjust
        MINUS_1200(-43200),
        MINUS_1130(-41400),
        MINUS_1100(-39600),
        MINUS_1030(-37800),
        MINUS_1000(-36000),
        MINUS_0930(-34200),
        MINUS_0900(-32400),
        MINUS_0830(-30600),
        MINUS_0800(-28800),
        MINUS_0730(-27000),
        MINUS_0700(-25200),
        MINUS_0630(-23400),
        MINUS_0600(-21600),
        MINUS_0530(-19800),
        MINUS_0500(-18000),
        MINUS_0430(-16200),
        MINUS_0400(-14400),
        MINUS_0330(-12600),
        MINUS_0300(-10800),
        MINUS_0230(-9000),
        MINUS_0200(-7200),
        MINUS_0130(-5400),
        MINUS_0100(-3600),
        MINUS_0030(-1800),
        GMT_0000(0),

        PLUS_0030(1800),
        PLUS_0100(3600),
        PLUS_0130(5400),
        PLUS_0200(7200),
        PLUS_0230(9000),
        PLUS_0300(10800),
        PLUS_0330(12600),
        PLUS_0400(14400),
        PLUS_0430(16200),
        PLUS_0500(18000),
        PLUS_0530(19800),
        PLUS_0600(21600),
        PLUS_0630(23400),
        PLUS_0700(25200),
        PLUS_0730(27000),
        PLUS_0800(28800),
        PLUS_0830(30600),
        PLUS_0900(32400),
        PLUS_0930(34200),
        PLUS_1000(36000),
        PLUS_1030(37800),
        PLUS_1100(39600),
        PLUS_1130(41400),
        PLUS_1200(43200);

        private int value;

        private TIMEZONE_SECONDS(int value){
            this.value = value;
        }

        public static TIMEZONE_SECONDS BySeconds(int sec) {
            for (TIMEZONE_SECONDS e : TIMEZONE_SECONDS.values()) {
                if (e.value == sec) {
                    return e;
                }
            }
            return null;
        }
        public int getValue() {
            return this.value;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_adjust);

        InitTitleHelp();
        InitItems();

        showTimeZone(TmGetCurrentTimeZone());
        showSummerTime();
        //show current date
        Date date = GetLocalTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd",Locale.getDefault());
        String today = sdf.format(date);
        TxvDate.setText(today);

        //show current time
        sdf = new SimpleDateFormat("HH:mm",Locale.getDefault());
        String time = sdf.format(date);
        TxvCurrentTime.setText(time);
        //Log.d(TAG,"onCreate: currentTime="+time);

        //set pre time
        premyear = date.getYear();
        premmonth = date.getMonth();
        premday = date.getDate();
        premhourOfDay = date.getHours();
        premminute = date.getMinutes();
        //Log.d(TAG, "onCreate: pre time="+premyear+"/"+premmonth+"/"+premday+" "+premhourOfDay+":"+premminute);
    }

    @Override
    public void onBackPressed() {
        int saveFlag=0;
        int year=0, month=0, day=0, hourOfDay=0, minute=0;

        if(TmGetSettingTDTStatus() == 0 && (dateModifyFlag==1 || timeModifyFlag==1)){
            if(dateModifyFlag == 1 && timeModifyFlag==0){
                //Log.d(TAG, "onBackPressed: dateModifyFlag == 1 && timeModifyFlag==0");
                if(myear- 1900 != premyear || mmonth != premmonth || premday!= mday){
                    saveFlag = 1;
                    year = myear;
                    month = mmonth;
                    day = mday;
                    hourOfDay = premhourOfDay;
                    minute = premminute;
                }
            }else if(dateModifyFlag == 0 && timeModifyFlag==1){
                //Log.d(TAG, "onBackPressed: dateModifyFlag == 0 && timeModifyFlag==1"+", premhourOfDay="+premhourOfDay+", mhourOfDay="+mhourOfDay+", premminute="+premminute+", mminute="+mminute);
                if(premhourOfDay != mhourOfDay || premminute != mminute) {
                    saveFlag = 1;
                    year = premyear+1900;
                    month = premmonth;
                    day = premday;
                    hourOfDay = mhourOfDay;
                    minute = mminute;
                }
            }else {//dateModifyFlag == 1 && timeModifyFlag==1
                //Log.d(TAG, "onBackPressed: dateModifyFlag == 1 && timeModifyFlag==1");
                saveFlag = 1;
                year = myear;
                month = mmonth;
                day = mday;
                hourOfDay = mhourOfDay;
                minute = mminute;
            }
            if(saveFlag == 1){
                long loffset=0;//eric lin 20180628 modify TimeAdjust
                //Log.d(TAG, "onBackPressed: saveFlag=1, save time="+year+"/"+month+"/"+day+" "+hourOfDay+":"+minute);
//                Log.d(TAG, "onBackPressed: myear="+myear+", premyear="+premyear
//                        +", mmonth="+mmonth+", premmonth="+premmonth+", premday="+premday+", mday="+mday
//                        +", premhourOfDay="+mhourOfDay+", premminute="+mminute);
                Date tmpDate = null;
//                int offset = 0;
                tmpDate = new Date(year - 1900, month, day, hourOfDay, minute, 0);

//                java.util.Calendar ca = java.util.Calendar.getInstance();
//                int zone = ca.get(java.util.Calendar.ZONE_OFFSET);
//                //Log.d(TAG,"new TimePickerDialog: zone="+zone);
//                offset = zone;
                //Log.d(TAG, "onBackPressed: saveFlag=1, save time="+year+"/"+month+"/"+day+" "+hourOfDay+":"+minute);

                //eric lin 20180628 modify TimeAdjust,-start
                if(mSummerTime==1)
                    loffset += 60*60;//1hr
                if(mTimeZone < 0)
                    loffset -= Math.abs(mTimeZone);
                else if((mTimeZone > 0))
                    loffset += Math.abs(mTimeZone);
                loffset = loffset*1000;
                //Log.d(TAG, "onBackPressed: tmpDate.getTime()="+tmpDate.getTime()+", mTimeZone="+mTimeZone+", mSummerTime="+mSummerTime+", offset="+offset);
                tmpDate.setTime(tmpDate.getTime()-loffset);//tmpDate.setTime(tmpDate.getTime() + offset);
                //eric lin 20180628 modify TimeAdjust,-end
                TmSetDateTime(tmpDate);
            }
        }
        super.onBackPressed();
    }

    private void showTimeZone(int seconds) {
        int position =0;
        TIMEZONE_SECONDS select = TIMEZONE_SECONDS.BySeconds(seconds);//eric lin 20180628 modify TimeAdjust
        //Log.d(TAG, "showTimeZone: select="+select);
        switch(select){
            case MINUS_1200://-12:00
                position = 0;
                break;
            case MINUS_1130://-11:30
                position = 1;
                break;
            case MINUS_1100://-11:00
                position = 2;
                break;
            case MINUS_1030://-10:30
                position = 3;
                break;
            case MINUS_1000://-10:00
                position = 4;
                break;
            case MINUS_0930://-9:30
                position = 5;
                break;
            case MINUS_0900://-9:00
                position = 6;
                break;
            case MINUS_0830://-8:30
                position = 7;
                break;
            case MINUS_0800://-8:00
                position = 8;
                break;
            case MINUS_0730://-7:30
                position = 9;
                break;
            case MINUS_0700://-7:00
                position = 10;
                break;
            case MINUS_0630://-6:30
                position = 11;
                break;
            case MINUS_0600://-6:00
                position = 12;
                break;
            case MINUS_0530://-5:30
                position = 13;
                break;
            case MINUS_0500://-5:00
                position = 14;
                break;
            case MINUS_0430://-4:30
                position = 15;
                break;
            case MINUS_0400://-4:00
                position = 16;
                break;
            case MINUS_0330://-3:30
                position = 17;
                break;
            case MINUS_0300://-3:00
                position = 18;
                break;
            case MINUS_0230://-2:30
                position = 19;
                break;
            case MINUS_0200://-2:00
                position = 20;
                break;
            case MINUS_0130://-1:30
                position = 21;
                break;
            case MINUS_0100://-1:00
                position = 22;
                break;
            case MINUS_0030://-0:30
                position = 23;
                break;
            case GMT_0000://0:00
                position = 24;
                break;
            case PLUS_0030://+0:30
                position = 25;
                break;
            case PLUS_0100://+1:00
                position = 26;
                break;
            case PLUS_0130://+1:30
                position = 27;
                break;
            case PLUS_0200://+2:00
                position = 28;
                break;
            case PLUS_0230://+2:30
                position = 29;
                break;
            case PLUS_0300://+3:00
                position = 30;
                break;
            case PLUS_0330://+3:30
                position = 31;
                break;
            case PLUS_0400://+4:00
                position = 32;
                break;
            case PLUS_0430://+4:30
                position = 33;
                break;
            case PLUS_0500://+5:00
                position = 34;
                break;
            case PLUS_0530://+5:30
                position = 35;
                break;
            case PLUS_0600://+6:00
                position = 36;
                break;
            case PLUS_0630://+6:30
                position = 37;
                break;
            case PLUS_0700://+7:00
                position = 38;
                break;
            case PLUS_0730://+7:30
                position = 39;
                break;
            case PLUS_0800://+8:00
                position = 40;
                break;
            case PLUS_0830://+8:30
                position = 41;
                break;
            case PLUS_0900://+9:00
                position = 42;
                break;
            case PLUS_0930://+9:30
                position = 43;
                break;
            case PLUS_1000://+10:00
                position = 44;
                break;
            case PLUS_1030://+10:30
                position = 45;
                break;
            case PLUS_1100://+11:00
                position = 46;
                break;
            case PLUS_1130://+11:30
                position = 47;
                break;
            case PLUS_1200://+12:00
                position = 48;
                break;
            default:
                position=24;
                break;
        }
        SpinnerGmtoffset.setSelection(position);

        mTimeZone = seconds;//eric lin 20180628 modify TimeAdjust
        String[] strTimeRegions = getResources().getStringArray(R.array.STR_TIME_REGION_OPTION);
        TxvRegion.setText(strTimeRegions[position]);

    }
    private void setTimeZone(int position) {
        int seconds;
        switch(position){
            case 0://-12:00
                seconds = TIMEZONE_SECONDS.MINUS_1200.getValue();
                break;
            case 1://-11:30
                seconds = TIMEZONE_SECONDS.MINUS_1130.getValue();
                break;
            case 2://-11:00
                seconds = TIMEZONE_SECONDS.MINUS_1100.getValue();
                break;
            case 3://-10:30
                seconds = TIMEZONE_SECONDS.MINUS_1030.getValue();
                break;
            case 4://-10:00
                seconds = TIMEZONE_SECONDS.MINUS_1000.getValue();
                break;
            case 5://-9:30
                seconds = TIMEZONE_SECONDS.MINUS_0930.getValue();
                break;
            case 6://-9:00
                seconds = TIMEZONE_SECONDS.MINUS_0900.getValue();
                break;
            case 7://-8:30
                seconds = TIMEZONE_SECONDS.MINUS_0830.getValue();
                break;
            case 8://-8:00
                seconds = TIMEZONE_SECONDS.MINUS_0800.getValue();
                break;
            case 9://-7:30
                seconds = TIMEZONE_SECONDS.MINUS_0730.getValue();
                break;
            case 10://-7:00
                seconds = TIMEZONE_SECONDS.MINUS_0700.getValue();
                break;
            case 11://-6:30
                seconds = TIMEZONE_SECONDS.MINUS_0630.getValue();
                break;
            case 12://-6:00
                seconds = TIMEZONE_SECONDS.MINUS_0600.getValue();
                break;
            case 13://-5:30
                seconds = TIMEZONE_SECONDS.MINUS_0530.getValue();
                break;
            case 14://-5:00
                seconds = TIMEZONE_SECONDS.MINUS_0500.getValue();
                break;
            case 15://-4:30
                seconds = TIMEZONE_SECONDS.MINUS_0430.getValue();
                break;
            case 16://-4:00
                seconds = TIMEZONE_SECONDS.MINUS_0400.getValue();
                break;
            case 17://-3:30
                seconds = TIMEZONE_SECONDS.MINUS_0330.getValue();
                break;
            case 18://-3:00
                seconds = TIMEZONE_SECONDS.MINUS_0300.getValue();
                break;
            case 19://-2:30
                seconds = TIMEZONE_SECONDS.MINUS_0230.getValue();
                break;
            case 20://-2:00
                seconds = TIMEZONE_SECONDS.MINUS_0200.getValue();
                break;
            case 21://-1:30
                seconds = TIMEZONE_SECONDS.MINUS_0130.getValue();
                break;
            case 22://-1:00
                seconds = TIMEZONE_SECONDS.MINUS_0100.getValue();
                break;
            case 23://-0:30
                seconds = TIMEZONE_SECONDS.MINUS_0030.getValue();
                break;
            case 24://0:00
                seconds = TIMEZONE_SECONDS.GMT_0000.getValue();
                break;
            case 25://+0:30
                seconds = TIMEZONE_SECONDS.PLUS_0030.getValue();
                break;
            case 26://+1:00
                seconds = TIMEZONE_SECONDS.PLUS_0100.getValue();
                break;
            case 27://+1:30
                seconds = TIMEZONE_SECONDS.PLUS_0130.getValue();
                break;
            case 28://+2:00
                seconds = TIMEZONE_SECONDS.PLUS_0200.getValue();
                break;
            case 29://+2:30
                seconds = TIMEZONE_SECONDS.PLUS_0230.getValue();
                break;
            case 30://+3:00
                seconds = TIMEZONE_SECONDS.PLUS_0300.getValue();
                break;
            case 31://+3:30
                seconds = TIMEZONE_SECONDS.PLUS_0330.getValue();
                break;
            case 32://+4:00
                seconds = TIMEZONE_SECONDS.PLUS_0400.getValue();
                break;
            case 33://+4:30
                seconds = TIMEZONE_SECONDS.PLUS_0430.getValue();
                break;
            case 34://+5:00
                seconds = TIMEZONE_SECONDS.PLUS_0500.getValue();
                break;
            case 35://+5:30
                seconds = TIMEZONE_SECONDS.PLUS_0530.getValue();
                break;
            case 36://+6:00
                seconds = TIMEZONE_SECONDS.PLUS_0600.getValue();
                break;
            case 37://+6:30
                seconds = TIMEZONE_SECONDS.PLUS_0630.getValue();
                break;
            case 38://+7:00
                seconds = TIMEZONE_SECONDS.PLUS_0700.getValue();
                break;
            case 39://+7:30
                seconds = TIMEZONE_SECONDS.PLUS_0730.getValue();
                break;
            case 40://+8:00
                seconds = TIMEZONE_SECONDS.PLUS_0800.getValue();
                break;
            case 41://+8:30
                seconds = TIMEZONE_SECONDS.PLUS_0830.getValue();
                break;
            case 42://+9:00
                seconds = TIMEZONE_SECONDS.PLUS_0900.getValue();
                break;
            case 43://+9:30
                seconds = TIMEZONE_SECONDS.PLUS_0930.getValue();
                break;
            case 44://+10:00
                seconds = TIMEZONE_SECONDS.PLUS_1000.getValue();
                break;
            case 45://+10:30
                seconds = TIMEZONE_SECONDS.PLUS_1030.getValue();
                break;
            case 46://+11:00
                seconds = TIMEZONE_SECONDS.PLUS_1100.getValue();
                break;
            case 47://+11:30
                seconds = TIMEZONE_SECONDS.PLUS_1130.getValue();
                break;
            case 48://+12:00
                seconds = TIMEZONE_SECONDS.PLUS_1200.getValue();
                break;
            default:
                seconds = TIMEZONE_SECONDS.GMT_0000.getValue();
                break;
        }
        mTimeZone = seconds;//eric lin 20180628 modify TimeAdjust
        TmSetTimeZone(seconds);
    }
    private void showSummerTime() {
        int daylightOnOff = TmGetDaylightSaving();
        mSummerTime = daylightOnOff;//eric lin 20180628 modify TimeAdjust
        if(daylightOnOff == 0)
            SpinnerSummerTime.setSelection(0);
        else if(daylightOnOff == 1)
            SpinnerSummerTime.setSelection(1);
        else {
            Log.d(TAG, "showSummerTime: daylightOnOff=" + daylightOnOff + ", ....fail");
        }
    }
    private AdapterView.OnItemSelectedListener SummerTimeListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView,view,position,id);
            mSummerTime = position;//eric lin 20180628 modify TimeAdjust
            if(position==0){//off
                TmSetDaylightSaving(0);
            }else if(position==1){//on
                TmSetDaylightSaving(1);
            }
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };
    private AdapterView.OnItemSelectedListener GmtoffsetListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView,view,position,id);
            String[] strTimeRegions = getResources().getStringArray(R.array.STR_TIME_REGION_OPTION);

            //Log.d(TAG, "position =  " + position);
            setTimeZone(position);
            TxvRegion.setText(strTimeRegions[position]);
            //Log.d(TAG, "(3)TmGetCurrentTimeZone()="+TmGetCurrentTimeZone());
            Log.d(TAG, "onItemSelected:    position = " + position);
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };
    private AdapterView.OnItemSelectedListener AutoTimeSettingListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView,view,position,id);
            String str;
            //Log.d(TAG, "position =  " + position);
            if(position==0){//off
                setOtherItemsFocus(true);
                TmSetSettingTDTStatus(0);
            }else if(position==1){//on
                setOtherItemsFocus(false);
                TmSetSettingTDTStatus(1);
            }
            Log.d(TAG, "onItemSelected:    position = " + position);
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };
    private void setOtherItemsFocus(boolean focus){
        if(focus) {
            SpinnerGmtoffset.setFocusable(true);
            setSinnerFoucsTextColor(SpinnerGmtoffset.getSelectedView());
            SpinnerGmtoffset.setBackgroundResource(R.drawable.selectbox);//eric lin test
            SpinnerSummerTime.setFocusable(true);
            setSinnerFoucsTextColor(SpinnerSummerTime.getSelectedView());
            SpinnerSummerTime.setBackgroundResource(R.drawable.selectbox);//eric lin test
            TxvDate.setFocusable(true);
            TxvCurrentTime.setFocusable(true);

            // Johnny 20181219 for mouse control -s
            SpinnerGmtoffset.setEnabled(true);
            SpinnerGmtoffset.setFocusableInTouchMode(true);
            SpinnerSummerTime.setEnabled(true);
            SpinnerSummerTime.setFocusableInTouchMode(true);
            TxvDate.setEnabled(true);
            TxvDate.setFocusableInTouchMode(true);
            TxvCurrentTime.setEnabled(true);
            TxvCurrentTime.setFocusableInTouchMode(true);
            // Johnny 20181219 for mouse control -e
        }
        else {
            SpinnerGmtoffset.setFocusable(false);
            setSinnerUnfoucsTextColor(SpinnerGmtoffset.getSelectedView());
            SpinnerGmtoffset.setBackgroundResource(R.drawable.selectboxgrayarrow);//eric lin test
            SpinnerSummerTime.setFocusable(false);
            setSinnerUnfoucsTextColor(SpinnerSummerTime.getSelectedView());
            SpinnerSummerTime.setBackgroundResource(R.drawable.selectboxgrayarrow);//eric lin test
            TxvDate.setFocusable(false);
            TxvCurrentTime.setFocusable(false);

            // Johnny 20181219 for mouse control -s
            SpinnerGmtoffset.setEnabled(false);
            SpinnerSummerTime.setEnabled(false);
            TxvDate.setEnabled(false);
            TxvCurrentTime.setEnabled(false);
            // Johnny 20181219 for mouse control -e
        }
    }
    private void setSinnerUnfoucsTextColor(View v) {
        TextView txt = (TextView) v.findViewById(R.id.view_text1);
        txt.setTextColor(getColor(R.color.colorGray));
    }
    private void setSinnerFoucsTextColor(View v) {
        TextView txt = (TextView) v.findViewById(R.id.view_text1);
        txt.setTextColor(Color.WHITE);
    }

    private void InitTitleHelp() {
        title = (ActivityTitleView) findViewById(R.id.timeAdjustTitleLayout);
        title.setTitleView(getString(R.string.STR_TIME_ADJUST_TITLE));
        helpView = (ActivityHelpView) findViewById(R.id.timeAdjustHelpViewLayout);
        helpView.resetHelp(1,0,null);
        helpView.resetHelp(2,0,null);
        helpView.resetHelp(3,0,null);
        helpView.resetHelp(4,0,null);
        helpView.setHelpInfoTextBySplit(getString(R.string.STR_PARENT_HELP_INFO));
    }

    private void InitItems() {
        //Spinners
        SpinnerAutoTimeSetting = (Spinner) findViewById(R.id.autoTimeSettingSPIN);
        SpinnerGmtoffset = (Spinner) findViewById(R.id.gmtoffsetSPIN);
        SpinnerSummerTime = (Spinner) findViewById(R.id.summerTimeSPIN);
        Resources res = getResources();
        SelAutoTimeSetting = new SelectBoxView(this, SpinnerAutoTimeSetting,
                res.getStringArray(R.array.STR_AUTO_TIME_SETTING_OPTION));
        SelGmtoffset = new SelectBoxView(this, SpinnerGmtoffset,
                res.getStringArray(R.array.STR_GTM_OFFSET_OPTION));
        SelSummerTime = new SelectBoxView(this, SpinnerSummerTime,
                res.getStringArray(R.array.STR_SUMMER_TIME_OPTION));
        int curAutoSetting = TmGetSettingTDTStatus();
        //Log.d(TAG, "InitItems: curAutoSetting="+curAutoSetting);
        if(curAutoSetting==0) //off
            SpinnerAutoTimeSetting.setSelection(0);
        else //on
            SpinnerAutoTimeSetting.setSelection(1);


        //Current Date textview
        TxvDate = (TextView) findViewById(R.id.currentdateTXV);
        //date calendar
        TxvDate.setOnClickListener(
            new View.OnClickListener(){
                public void onClick(View v) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                    String dateString = TxvDate.getText().toString();
                    Date select_date = GetLocalTime();
                    try {
                        select_date = sdf.parse(dateString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    final Calendar calendar = Calendar.getInstance();
                    calendar.setTime(select_date);
                    myear = calendar.get(Calendar.YEAR);
                    mmonth = calendar.get(Calendar.MONTH);
                    mday = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(TimeAdjustActivity.this, R.style.DatePickerDialogStyle, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            //Log.d(TAG," year " + year + " month " + month + " dayOfMonth " + dayOfMonth);
                            myear = year;
                            mmonth = month;
                            mday = dayOfMonth;
                            TxvDate.setText(String
                                    .valueOf(year)
                                    .concat("/")
                                    .concat(String.format(Locale.getDefault(), "%02d", month+1))
                                    .concat("/")
                                    .concat(String.format(Locale.getDefault(), "%02d", dayOfMonth))
                            );
                            dateModifyFlag = 1;
                        }

                    }, myear, mmonth, mday);

                    datePickerDialog.show();
                }
            }
        );

        //Current Time textview
        TxvCurrentTime = (TextView) findViewById(R.id.currentTimeTXV);
        TxvCurrentTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String timeString = TxvCurrentTime.getText().toString();
                Date select_date = GetLocalTime();
                try {
                    select_date = sdf.parse(timeString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(select_date);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                //Log.d(TAG,"TxvCurrentTime.setOnClickListener: hour="+hour+", minute="+minute );
                TimePickerDialog timePickerDialog = new TimePickerDialog(TimeAdjustActivity.this, R.style.TimePickerDialogStyle, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        //Log.d(TAG,"new TimePickerDialog: selectedHour="+selectedHour+", selectedMinute="+selectedMinute );
                        mhourOfDay = selectedHour;
                        mminute = selectedMinute;
                        TxvCurrentTime.setText( String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
                        timeModifyFlag = 1;
                    }
                }, hour, minute, true);
                timePickerDialog.show();
            }
        });

        //Region textview
        TxvRegion = (TextView) findViewById(R.id.timeRegionTV);

        //Auto Time Setting's selectedListener
        SpinnerAutoTimeSetting.setOnItemSelectedListener(AutoTimeSettingListener);

        //GMT Offset's selectedListener
        SpinnerGmtoffset.setOnItemSelectedListener(GmtoffsetListener);

        //Summer Time's selectedListener
        SpinnerSummerTime.setOnItemSelectedListener(SummerTimeListener);
    }
}
