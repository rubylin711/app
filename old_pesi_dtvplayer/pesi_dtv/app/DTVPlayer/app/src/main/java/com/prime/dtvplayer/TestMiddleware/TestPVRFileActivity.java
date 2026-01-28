package com.prime.dtvplayer.TestMiddleware;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dolphin.dtv.PvrFileInfo;
import com.mtest.utils.PesiStorageHelper;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestPVRFileActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();

    private static String DEFAULT_RECORD_TESTFILE = Environment.getExternalStorageDirectory().getPath() + "/test.ts";
    private static String DEFAULT_RECORD_TESTFILE_RENAME = DEFAULT_RECORD_TESTFILE + "_rename.ts";

    private ActivityHelpView help;
    private TextView result;

    final int mTestTotalFuncCount = 4;    // 4 PVR_File functions
    private int mPosition;  // position of testMidMain

    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    private Toast mToast;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_pvr_file);

        List<Object> volList = getVolumes();
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);
        for ( Object vol : volList )
        {
            String devType = pesiStorageHelper.getDevType(vol);
            String internalPath = pesiStorageHelper.getInternalPath(vol);
            if ( (devType != null) && (!devType.equals("")) && (internalPath != null))
            {
                Log.d(TAG, "usb path use = " + internalPath);
                DEFAULT_RECORD_TESTFILE = internalPath+"/test.ts";
                DEFAULT_RECORD_TESTFILE_RENAME = internalPath + "/test_rename.ts";
            }
        }

        Log.d(TAG, "onCreate: DEFAULT_RECORD_TESTFILE = "+DEFAULT_RECORD_TESTFILE);
        Log.d(TAG, "onCreate: DEFAULT_RECORD_TESTFILE_RENAME = "+DEFAULT_RECORD_TESTFILE_RENAME);
        File testFile = new File(DEFAULT_RECORD_TESTFILE);
        File testFileRename = new File(DEFAULT_RECORD_TESTFILE_RENAME);
        if (!testFile.exists())
        {
            ShowToast("No TestFile, Record in PVR_Record First !");
            finish();
        }
        else if (testFileRename.exists())
        {
            PvrFileRemove(DEFAULT_RECORD_TESTFILE_RENAME); // remove renamed file if exist
            ShowToast(DEFAULT_RECORD_TESTFILE_RENAME + " removed!");
        }

        Init();
    }

    @Override
    public void onBackPressed() {

        TestMidMain tm = new TestMidMain();
        // if all funcs are tested, set checked = true
        if ( mTestedFuncSet.size() == mTestTotalFuncCount )
        {
            int result = 0;
            tm.getTestInfoByIndex(mPosition).setChecked(true);

            // send error item to testMidMain
            for (int index : mErrorIndexSet) {
                result = tm.bitwiseLeftShift(result, index, false);    //fail item
            }

            tm.getTestInfoByIndex(mPosition).setResult(result);
        }

        super.onBackPressed();
    }

    @Override
    public  void onDestroy() {
        super.onDestroy();
    }

    public void PvrFileRemove_OnClick(View view)
    {
        final int btnIndex = 0; // first btn
        result.setText(null);

        try
        {
            int ret = PvrFileRemove(DEFAULT_RECORD_TESTFILE);
            if (ret == 0)
            {
                File file = new File(DEFAULT_RECORD_TESTFILE);
                if (!file.exists())
                {
                    TestPass(view, "PvrFileRemove , FullName = " + DEFAULT_RECORD_TESTFILE);
                }
                else
                {
                    GotError(view, "Fail : File still exits!", btnIndex);
                }
            }
            else
            {
                GotError(view, "Fail : PvrFileRemove return " + ret, btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    public void PvrFileRename_OnClick(View view)
    {
        final int btnIndex = 1;
//        String oldName = "/storage/sda1/REC/aaaa.ts";
//        String newName = "/storage/sda1/REC/bbbb.ts";
        result.setText(null);

        String oldName = DEFAULT_RECORD_TESTFILE;
        String newName = DEFAULT_RECORD_TESTFILE_RENAME;

        try
        {
            Log.d(TAG, "PvrFileRename_OnClick: " + oldName);
            Log.d(TAG, "PvrFileRename_OnClick: " + newName);
            int ret = PvrFileRename(oldName, newName);
            if (ret == 0)
            {
                File file = new File(newName);
                if (file.exists())
                {
                    TestPass(view, "PvrFileRename , newName = " + newName);
                }
                else
                {
                    GotError(view, "Fail : File of newName not exits!", btnIndex);
                }
            }
            else
            {
                GotError(view, "Fail : PvrFileRename return " + ret, btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    public void PvrFileGetDuration_OnClick(View view)
    {
        final int btnIndex = 2;
        result.setText(null);
        try
        {
            int ret = PvrFileGetDuration(DEFAULT_RECORD_TESTFILE);
            if (ret != -1)
            {
                TestPass(view, "PvrFileGetDuration  = " + ret);
            }
            else
            {
                GotError(view, "Fail : PvrFileGetDuration return -1", btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    public void PvrFileGetSize_OnClick(View view)
    {
        final int btnIndex = 3;
        result.setText(null);
        try
        {
            long ret = PvrFileGetSize(DEFAULT_RECORD_TESTFILE);
            if (ret != -1)
            {
                TestPass(view, "PvrFileGetSize = " + ret);
            }
            else
            {
                GotError(view, "Fail : PvrFileGetSize return -1", btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    public void PvrFileGetAllInfo_OnClick(View view)
    {
        final int btnIndex = 4;

        try
        {
            PvrFileInfo fileInfo;
            fileInfo = PvrFileGetAllInfo( DEFAULT_RECORD_TESTFILE );
            if (fileInfo != null)
            {
                String Info = "PvrFileGetAllInfo () :\nstartTimeInMs = " + fileInfo.startTimeInMs +"\nendTimeInMs = " + fileInfo.endTimeInMs +
                        "\ndurationInMs =" + fileInfo.durationInMs + "\nfileSize = " + fileInfo.fileSize + "\nserviceType = " + fileInfo.serviceType +
                        "\nchannelLock = " + fileInfo.channelLock + "\nparentalRate = " + fileInfo.parentalRate;
                result.setText(Info);
                TestPass(view, "PvrFileGetAllInfo_OnClick pass !!!\n ");
            }
            else
            {
                GotError(view, "Fail : PvrFileGetAllInfo_OnClick Fail !!!", btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    public void PvrFileGetExtraInfo_OnClick(View view)
    {
        final int btnIndex = 5;

        try
        {
            PvrFileInfo fileInfo;
            fileInfo = PvrFileGetExtraInfo( DEFAULT_RECORD_TESTFILE );
            if (fileInfo != null)
            {
                String Info = "PvrFileGetExtraInfo () :\nchannelName = " + fileInfo.channelName +"\nyear = " + fileInfo.year +
                        "\nmonth =" + fileInfo.month + "\ndate = " + fileInfo.date + "\nweek = " + fileInfo.week +
                        "\nhour = " + fileInfo.hour + "\nminute = " + fileInfo.minute + "\nsecond = " + fileInfo.second;
                result.setText(Info);
                TestPass(view, "PvrFileGetExtraInfo pass !!!\n ");
            }
            else
            {
                GotError(view, "Fail : PvrFileGetExtraInfo Fail !!!", btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    public void pvrFileGetEpgInfo_OnClick(View view)
    {
        final int btnIndex = 6;

        try
        {
            PvrFileInfo fileInfo;
            fileInfo = pvrFileGetEpgInfo( DEFAULT_RECORD_TESTFILE , 1); // 1 : present
            if (fileInfo != null)
            {
                String Info = "PvrFileGetExtraInfo () :\neventName = " + fileInfo.eventName +
                        "\nshortEvent = " + fileInfo.shortEvent +"\nextendedText = " + fileInfo.extendedText +
                        "\n\nlanguageCode = " + fileInfo.languageCode + "   extendedLanguageCode = " + fileInfo.extendedLanguageCode +
                        "\nyear = " + fileInfo.year + "    month = " + fileInfo.month + "  date =  " + fileInfo.date +
                        "\nhour = " + fileInfo.hour + "   minute = " + fileInfo.minute + "   second = " + fileInfo.second + "  week = " + fileInfo.week +
                        "\nyearEnd = " + fileInfo.yearEnd + "   monthEnd = " + fileInfo.monthEnd + "   dateEnd = " + fileInfo.dateEnd +
                        "\nhourEnd = " + fileInfo.hourEnd + "   monthEnd = " + fileInfo.monthEnd + "   secondEnd = " + fileInfo.secondEnd + "   weekEnd = " + fileInfo.weekEnd +
                        "\n\nparentalRate = " + fileInfo.parentalRate +
                        "\neventNameCharCode = " + fileInfo.eventNameCharCode +
                        "\n shortEventCharCode = " + fileInfo.shortEventCharCode +
                        "\nrecordTimeStamp =  " + fileInfo.recordTimeStamp;
                result.setText(Info);
                Log.d(TAG, "pvrFileGetEpgInfo_OnClick:  Info = " + Info);
                TestPass(view, "pvrFileGetEpgInfo pass !!!\n ");
            }
            else
            {
                GotError(view, "Fail : pvrFileGetEpgInfo Fail !!!", btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    public void pvrTotalRecordFileOpen_OnClick(View view)
    {
        final int btnIndex = 7;

        try
        {
            String path = Environment.getExternalStorageDirectory().getPath();
            Log.d(TAG, "pvrTotalRecordFileOpen_OnClick:  path = " + path + "/");
            int ret = pvrTotalRecordFileOpen(path); // 1 : present
            Log.d(TAG, "pvrTotalRecordFileOpen_OnClick:  ret = " + ret);
            if (ret != 0)
            {
                TestPass(view, "pvrTotalRecordFileOpen pass !!!   total = "+ ret);
            }
            else
            {
                GotError(view, "Fail : pvrTotalRecordFileOpen  Fail !!!", btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    public void pvrTotalRecordFileGet_OnClick(View view)
    {
        final int btnIndex = 8;

        try
        {
            String path = Environment.getExternalStorageDirectory().getPath();
            List<PvrFileInfo> fileList = new ArrayList<PvrFileInfo>();
            int total = pvrTotalRecordFileOpen(path);
            fileList = pvrTotalRecordFileGet( 0, total);
            if (fileList != null && fileList.size() != 0)
            {
                String Info = "";
                for(int i = 0 ; i < fileList.size(); i++)
                {
                    Info = Info + fileList.get(i).realFileName + "\n";
                }
                result.setText(Info);
                TestPass(view, "pvrFileGetEpgInfo ==> total =  !!!\n " + total);
            }
            else
            {
                GotError(view, "Fail : pvrFileGetEpgInfo  Fail !!!", btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    public void pvrTotalRecordFileSort_OnClick(View view)
    {
        final int btnIndex = 9;

        try
        {
            String path = Environment.getExternalStorageDirectory().getPath();
            List<PvrFileInfo> fileList = new ArrayList<PvrFileInfo>();
            int total = pvrTotalRecordFileOpen(path);

            String origin = "";
            String SortByChannelName = "";
            String SortByDateTime = "";

            fileList = pvrTotalRecordFileGet( 0, total);
            for(int i = 0 ; i < fileList.size(); i++)
                origin = origin + fileList.get(i).realFileName + "\n";


            int ret = PvrTotalRecordFileSort( 0 ); // PVR_SORT_BY_CHNAME = 0 ,PVR_SORT_BY_DATETIME = 1
            fileList = PvrTotalRecordFileGet( 0, total );
            if (ret != -1)
            {
                for(int i = 0 ; i < fileList.size(); i++)
                    SortByChannelName = SortByChannelName + fileList.get(i).realFileName + "\n";
            }
            else
            {
                GotError(view, "Fail : pvrFileGetEpgInfo  Fail !!!", btnIndex);
            }


            ret = PvrTotalRecordFileSort( 1 ); // PVR_SORT_BY_CHNAME = 0 ,PVR_SORT_BY_DATETIME = 1
            fileList = PvrTotalRecordFileGet( 0, total );
            if(ret != -1)
            {
                for(int i = 0 ; i < fileList.size(); i++)
                    SortByDateTime = SortByDateTime + fileList.get(i).realFileName + "\n";
                TestPass(view, "pvrFileGetEpgInfo ==> total =  !!!\n " + total);
            }
            else
            {
                GotError(view, "Fail : pvrFileGetEpgInfo  Fail !!!", btnIndex);
            }

            String Info = "Original :\n" + origin + "\nSort by Channel Name\n" + SortByChannelName + "\nSort by Date Time :\n" + SortByDateTime;
            result.setText(Info);
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    private void Init()
    {
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_TESTPVR_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_TESTPVR_LAYOUT_HELP);
        result = (TextView) findViewById(R.id.resultTXV);
        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TestMiddlewareMain > TestPVR_File");

        // init help
        help.setHelpInfoText(null, null);
        help.setHelpRedText(null);
        help.setHelpGreenText(null);
        help.setHelpBlueText(null);
        help.setHelpYellowText(null);
    }

    private void GotError(View view, String errorMsg, int btnIndex)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, errorMsg );
        button.setTextColor(0xFFFF0000);    // red
        mErrorIndexSet.add(btnIndex);
        mTestedFuncSet.add(view.getId());
    }

    private void TestPass(View view, String msg)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, msg );
        button.setTextColor(0xFF00FF00);    // green
        mTestedFuncSet.add(view.getId());
    }

    private void ShowToast(String string)
    {
        if (mToast == null)
        {
            mToast = Toast.makeText(this, string, Toast.LENGTH_SHORT);
        }
        else
        {
            mToast.setText(string);
        }

        mToast.show();
    }
}
