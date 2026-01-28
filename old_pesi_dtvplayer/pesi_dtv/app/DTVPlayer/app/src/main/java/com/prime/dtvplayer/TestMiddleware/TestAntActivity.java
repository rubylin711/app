/*
package com.prime.dtvplayer.TestMiddleware;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.sysdata.AntInfo;
import com.prime.sysdata.TpInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestAntActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();

    private ActivityHelpView help;
    private TextView txvInput;
    private TextView txvOutput;

    final int mTestDataIntCount = 15;  // 15 Ant elements have int value
    final int mTestTotalFuncCount = 5;    // 5 Ant functions
    private int mPosition;  // position of testMidMain

    private List<String> mAntElements;
    private List<Integer> mTestDataIntIn;
    private List<Integer> mTestDataIntOut;

    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    int tunerType = TpInfo.DVBC;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_ant);

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

    public void BtnAntInfoGet_OnClick(View view)
    {

        List<AntInfo> store = AntInfoGetList();
        final int btnIndex = 0; // it is first button

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        try
        {
            // test1 -start
            strInput.append("Input Test1 : \n");
            strOutput.append("Output Test1 : \n");
            mTestDataIntIn = new ArrayList<>();
            mTestDataIntOut = new ArrayList<>();

            // add test data to mTestDataIn
            for ( int i = 0 ; i < mTestDataIntCount ; i++ )
            {
                mTestDataIntIn.add(i+1);
            }

            // save test data to DB
            AntInfo antInfo = new AntInfo();
            antInfo.setAntId(mTestDataIntIn.get(0));
            antInfo.setSatId(mTestDataIntIn.get(1));
            antInfo.setLnb1(mTestDataIntIn.get(2));
            antInfo.setLnb2(mTestDataIntIn.get(3));
            antInfo.setLnbType(mTestDataIntIn.get(4));
            antInfo.setDiseqcType(mTestDataIntIn.get(5));
            antInfo.setDiseqcUse(mTestDataIntIn.get(6));
            antInfo.setDiseqc(mTestDataIntIn.get(7));
            antInfo.setTone22kUse(mTestDataIntIn.get(8));
            antInfo.setTone22k(mTestDataIntIn.get(9));
            antInfo.setV012Use(mTestDataIntIn.get(10));
            antInfo.setV012(mTestDataIntIn.get(11));
            antInfo.setV1418Use(mTestDataIntIn.get(12));
            antInfo.setV1418(mTestDataIntIn.get(13));
            antInfo.setCku(mTestDataIntIn.get(14));
            AntInfoSave(antInfo);

            // get test data from DB
            antInfo = SatInfoGetList(tunerType);
            antInfo = AntInfoGet(mTestDataIntIn.get(0));
            if ( antInfo == null )
            {
                GotError(view, "Got null AntInfo", btnIndex, store);
                return;
            }

            // add data we got from DB to mTestDataOut
            mTestDataIntOut.add(antInfo.getAntId());
            mTestDataIntOut.add(antInfo.getSatId());
            mTestDataIntOut.add(antInfo.getLnb1());
            mTestDataIntOut.add(antInfo.getLnb2());
            mTestDataIntOut.add(antInfo.getLnbType());
            mTestDataIntOut.add(antInfo.getDiseqcType());
            mTestDataIntOut.add(antInfo.getDiseqcUse());
            mTestDataIntOut.add(antInfo.getDiseqc());
            mTestDataIntOut.add(antInfo.getTone22kUse());
            mTestDataIntOut.add(antInfo.getTone22k());
            mTestDataIntOut.add(antInfo.getV012Use());
            mTestDataIntOut.add(antInfo.getV012());
            mTestDataIntOut.add(antInfo.getV1418Use());
            mTestDataIntOut.add(antInfo.getV1418());
            mTestDataIntOut.add(antInfo.getCku());

            // set textview text
            int totalCount = 0;
            for ( int i = 0 ; i < mTestDataIntCount ; i++, totalCount++ )
            {
                strInput.append(mAntElements.get(totalCount)).append(" : ").append(mTestDataIntIn.get(i)).append("\n");
                strOutput.append(mAntElements.get(totalCount)).append(" : ").append(mTestDataIntOut.get(i)).append("\n");
            }

            // compare mTestDataIn and mTestDataOut
            for ( int i = 0 ; i < mTestDataIntCount ; i++ )
            {
                int in = mTestDataIntIn.get(i);
                int out = mTestDataIntOut.get(i);
                if ( in != out )
                {
                    String msg = "Error : " + mAntElements.get(i) + "\n" + "Expected : " + in + ", Got : " + out;
                    GotError(view, msg, btnIndex, store);
                    return;
                }
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end

            // test2 -start
            strInput.append("Input Test2 : \n");
            strOutput.append("Output Test2 : \n");

            // get AntInfo when there is no specific ID
            AntInfoDelete(Integer.MAX_VALUE);
            strInput.append("AntInfoDelete(Integer.MAX_VALUE)\n");
            strOutput.append("\n");

            antInfo = AntInfoGet(Integer.MAX_VALUE);
            strInput.append("AntInfoGet(Integer.MAX_VALUE)\n");
            strOutput.append("antInfo = ").append(antInfo).append("\n");

            // we should get null
            if (antInfo != null)
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got AntInfo when the specific ID does not exit", btnIndex, store);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test2 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, store);
            return;
        }

        // test pass
        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "AntInfoGet() Pass!", store);
    }

    public void BtnAntInfoGetList_OnClick(View view)
    {
        List<AntInfo> store = AntInfoGetList();
        final int btnIndex = 1;

        final int testSize = 5;

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        try
        {
            // test1 -start
            strInput.append("Input Test1 : \n");
            strOutput.append("Output Test1 : \n");
            List<AntInfo> antInfoList = new ArrayList<>();

            // add testSize antInfo
            for ( int i = 0 ; i < testSize ; i++ )
            {
                AntInfo antInfo = new AntInfo();
                antInfo.setAntId(Integer.MAX_VALUE-i);
                antInfoList.add(antInfo);
            }

            AntInfoSaveList(antInfoList);
            strInput.append("AntInfoSaveList() : Size = ").append(antInfoList.size()).append("\n");
            strOutput.append("\n");

            // get size
            int size =  AntInfoGetList().size();
            strInput.append("AntInfoGetList()\n");
            strOutput.append("AntInfoGetList() : Size = ").append(size).append("\n");

            // check size should be testSize
            if ( size != testSize )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Wrong size of AntInfoList ", btnIndex, store);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 - end

            // test2 -start
            strInput.append("Input Test2 : \n");
            strOutput.append("Output Test2 : \n");

            // clear antInfo
            int count = AntInfoGetList().size();
            List<Integer> idList = new ArrayList<>();
            for ( int i = 0 ; i < count ; i++ )
            {
                idList.add(AntInfoGetList().get(i).getAntId());
            }

            for ( int i = 0 ; i < idList.size() ; i++ )
            {
                AntInfoDelete(idList.get(i));
            }

            strInput.append("AntInfoDelete() : Del All\n");
            strOutput.append("\n");

            // getlist when empty
            antInfoList = AntInfoGetList();
            strInput.append("antInfoList = AntInfoGetList()\n");
            strOutput.append("antInfoList = ").append(antInfoList).append("\n");

            // antInfoList should be null when empty
            if ( antInfoList != null )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "GetList  is not null when there is no AntInfo", btnIndex, store);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test2 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);

            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, store);
            return;
        }

        // test pass
        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "AntInfoGetList() Pass!", store);
    }

    public void BtnAntInfoSave_OnClick(View view)
    {
        List<AntInfo> store = AntInfoGetList();
        final int btnIndex = 2;

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        try
        {
            // test1 -start
            strInput.append("Input Test1 : \n");
            strOutput.append("Output Test1 : \n");
            mTestDataIntIn = new ArrayList<>();
            mTestDataIntOut = new ArrayList<>();

            // ensure this antid does not exist
            AntInfoDelete(1);

            // add test data to mTestDataIn
            for ( int i = 0 ; i < mTestDataIntCount ; i++ )
            {
                mTestDataIntIn.add(i+1);
            }

            // save test data to DB
            AntInfo antInfo = new AntInfo();
            antInfo.setAntId(mTestDataIntIn.get(0));
            antInfo.setSatId(mTestDataIntIn.get(1));
            antInfo.setLnb1(mTestDataIntIn.get(2));
            antInfo.setLnb2(mTestDataIntIn.get(3));
            antInfo.setLnbType(mTestDataIntIn.get(4));
            antInfo.setDiseqcType(mTestDataIntIn.get(5));
            antInfo.setDiseqcUse(mTestDataIntIn.get(6));
            antInfo.setDiseqc(mTestDataIntIn.get(7));
            antInfo.setTone22kUse(mTestDataIntIn.get(8));
            antInfo.setTone22k(mTestDataIntIn.get(9));
            antInfo.setV012Use(mTestDataIntIn.get(10));
            antInfo.setV012(mTestDataIntIn.get(11));
            antInfo.setV1418Use(mTestDataIntIn.get(12));
            antInfo.setV1418(mTestDataIntIn.get(13));
            antInfo.setCku(mTestDataIntIn.get(14));
            AntInfoSave(antInfo);

            // get test data from DB
            antInfo = AntInfoGet(mTestDataIntIn.get(0));
            if ( antInfo == null )
            {
                GotError(view, "Got null AntInfo", btnIndex, store);
                return;
            }

            // add data we got from DB to mTestDataOut
            mTestDataIntOut.add(antInfo.getAntId());
            mTestDataIntOut.add(antInfo.getSatId());
            mTestDataIntOut.add(antInfo.getLnb1());
            mTestDataIntOut.add(antInfo.getLnb2());
            mTestDataIntOut.add(antInfo.getLnbType());
            mTestDataIntOut.add(antInfo.getDiseqcType());
            mTestDataIntOut.add(antInfo.getDiseqcUse());
            mTestDataIntOut.add(antInfo.getDiseqc());
            mTestDataIntOut.add(antInfo.getTone22kUse());
            mTestDataIntOut.add(antInfo.getTone22k());
            mTestDataIntOut.add(antInfo.getV012Use());
            mTestDataIntOut.add(antInfo.getV012());
            mTestDataIntOut.add(antInfo.getV1418Use());
            mTestDataIntOut.add(antInfo.getV1418());
            mTestDataIntOut.add(antInfo.getCku());

            // set textview text
            int totalCount = 0;
            for ( int i = 0 ; i < mTestDataIntCount ; i++, totalCount++ )
            {
                strInput.append(mAntElements.get(totalCount)).append(" : ").append(mTestDataIntIn.get(i)).append("\n");
                strOutput.append(mAntElements.get(totalCount)).append(" : ").append(mTestDataIntOut.get(i)).append("\n");
            }

            // compare mTestDataIn and mTestDataOut
            for ( int i = 0 ; i < mTestDataIntCount ; i++ )
            {
                int in = mTestDataIntIn.get(i);
                int out = mTestDataIntOut.get(i);
                if ( in != out )
                {
                    ShowResultOnTXV(strInput.toString(), txvInput);
                    ShowResultOnTXV(strOutput.toString(), txvOutput);
                    String msg = "Error : " + mAntElements.get(i) + "\n" + "Expected : " + in + ", Got : " + out;
                    GotError(view, msg, btnIndex, store);
                    return;
                }
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end

            // test2 -start
            strInput.append("Input Test2 : \n");
            strOutput.append("Output Test2 : \n");
            mTestDataIntIn = new ArrayList<>();
            mTestDataIntOut = new ArrayList<>();

            // add test data to mTestDataIn, keep id to test1 id
            mTestDataIntIn.add(1);

            // change other elements' value
            for ( int i = 1 ; i < mTestDataIntCount ; i++ )
            {
                mTestDataIntIn.add(i+5);
            }

            // save test data to DB
            antInfo = new AntInfo();
            antInfo.setAntId(mTestDataIntIn.get(0));
            antInfo.setSatId(mTestDataIntIn.get(1));
            antInfo.setLnb1(mTestDataIntIn.get(2));
            antInfo.setLnb2(mTestDataIntIn.get(3));
            antInfo.setLnbType(mTestDataIntIn.get(4));
            antInfo.setDiseqcType(mTestDataIntIn.get(5));
            antInfo.setDiseqcUse(mTestDataIntIn.get(6));
            antInfo.setDiseqc(mTestDataIntIn.get(7));
            antInfo.setTone22kUse(mTestDataIntIn.get(8));
            antInfo.setTone22k(mTestDataIntIn.get(9));
            antInfo.setV012Use(mTestDataIntIn.get(10));
            antInfo.setV012(mTestDataIntIn.get(11));
            antInfo.setV1418Use(mTestDataIntIn.get(12));
            antInfo.setV1418(mTestDataIntIn.get(13));
            antInfo.setCku(mTestDataIntIn.get(14));
            AntInfoSave(antInfo);

            // get test data from DB
            antInfo = AntInfoGet(mTestDataIntIn.get(0));
            if ( antInfo == null )
            {
                GotError(view, "Got null AntInfo", btnIndex, store);
                return;
            }

            // add data we got from DB to mTestDataOut
            mTestDataIntOut.add(antInfo.getAntId());
            mTestDataIntOut.add(antInfo.getSatId());
            mTestDataIntOut.add(antInfo.getLnb1());
            mTestDataIntOut.add(antInfo.getLnb2());
            mTestDataIntOut.add(antInfo.getLnbType());
            mTestDataIntOut.add(antInfo.getDiseqcType());
            mTestDataIntOut.add(antInfo.getDiseqcUse());
            mTestDataIntOut.add(antInfo.getDiseqc());
            mTestDataIntOut.add(antInfo.getTone22kUse());
            mTestDataIntOut.add(antInfo.getTone22k());
            mTestDataIntOut.add(antInfo.getV012Use());
            mTestDataIntOut.add(antInfo.getV012());
            mTestDataIntOut.add(antInfo.getV1418Use());
            mTestDataIntOut.add(antInfo.getV1418());
            mTestDataIntOut.add(antInfo.getCku());

            // set textview text
            totalCount = 0;
            for ( int i = 0 ; i < mTestDataIntCount ; i++, totalCount++ )
            {
                strInput.append(mAntElements.get(totalCount)).append(" : ").append(mTestDataIntIn.get(i)).append("\n");
                strOutput.append(mAntElements.get(totalCount)).append(" : ").append(mTestDataIntOut.get(i)).append("\n");
            }

            // compare mTestDataIn and mTestDataOut
            for ( int i = 0 ; i < mTestDataIntCount ; i++ )
            {
                int in = mTestDataIntIn.get(i);
                int out = mTestDataIntOut.get(i);
                if ( in != out )
                {
                    ShowResultOnTXV(strInput.toString(), txvInput);
                    ShowResultOnTXV(strOutput.toString(), txvOutput);
                    String msg = "Error : " + mAntElements.get(i) + "\n" + "Expected : " + in + ", Got : " + out;
                    GotError(view, msg, btnIndex, store);
                    return;
                }
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test2 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, store);
            return;
        }

        // test pass
        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "AntInfoSave() Pass!", store);

    }

    public void BtnAntInfoSaveList_OnClick(View view)
    {
        List<AntInfo> store = AntInfoGetList();
        final int btnIndex = 3;

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        try
        {
            // test1 -start
            strInput.append("Input Test1 : \n");
            strOutput.append("Output Test1 : \n");
            int saveListSize = 5;
            List<AntInfo> antInfoList = new ArrayList<>();
            for ( int i = 0 ; i < saveListSize ; i++ )
            {
                AntInfo antInfo = new AntInfo();
                antInfo.setAntId(i);
                antInfoList.add(antInfo);
            }

            AntInfoSaveList(antInfoList);
            strInput.append("AntInfoSaveList() : Size = ").append(antInfoList.size()).append("\n");
            strOutput.append("\n");

            antInfoList = AntInfoGetList();
            if ( antInfoList == null )
            {
                GotError(view, "Got null AntInfoList", btnIndex, store);
                return;
            }

            strInput.append("AntInfoGetList()\n");
            strOutput.append("antInfoList : Size = ").append(antInfoList.size()).append("\n");

            if ( saveListSize != antInfoList.size() )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Wrong size of AntInfoList after AntInfoSaveList", btnIndex, store);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end

            // test2 -start
            strInput.append("Input Test2 : \n");
            strOutput.append("Output Test2 : \n");

            // add 15 antinfo, 5 are added in test1
            saveListSize = 15;
            for ( int i = antInfoList.size() ; i < saveListSize ; i++ )
            {
                AntInfo antInfo = new AntInfo();
                antInfo.setAntId(i);
                antInfoList.add(antInfo);
            }

            AntInfoSaveList(antInfoList);
            strInput.append("AntInfoSaveList() : Size = ").append(antInfoList.size()).append("\n");
            strOutput.append("\n");

            antInfoList = AntInfoGetList();
            if ( antInfoList == null )
            {
                GotError(view, "Got null AntInfoList", btnIndex, store);
                return;
            }

            strInput.append("AntInfoGetList()\n");
            strOutput.append("antInfoList : Size = ").append(antInfoList.size()).append("\n");

            if ( saveListSize != antInfoList.size() )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Wrong size of AntInfoList after AntInfoSaveList", btnIndex, store);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test2 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, store);
            return;
        }

        // test pass
        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "AntInfoSaveList() Pass!", store);
    }

    public void BtnAntInfoDelete_OnClick(View view)
    {
        List<AntInfo> store = AntInfoGetList();
        final int btnIndex = 4;

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        try
        {
            // test1 -start
            strInput.append("Input Test1 : \n");
            strOutput.append("Output Test1 : \n");

            // save an antinfo first
            AntInfo antInfo = new AntInfo();
            antInfo.setAntId(Integer.MAX_VALUE);
            AntInfoSave(antInfo);
            strInput.append("AntInfoSave(Integer.MAX_VALUE)\n");
            strOutput.append("\n");

            // record size -> del -> record size
            int sizeBefore = AntInfoGetList().size();
            strInput.append("AntInfoGetList()\n");
            strOutput.append("Size = ").append(sizeBefore).append("\n");

            AntInfoDelete(Integer.MAX_VALUE);
            strInput.append("AntInfoDelete(Integer.MAX_VALUE)\n");
            strOutput.append("\n");

            int sizeAfter = AntInfoGetList().size();
            strInput.append("AntInfoGetList()\n");
            strOutput.append("Size = ").append(sizeAfter).append("\n");

            // check size
            if ( sizeAfter != sizeBefore-1 )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Wrong size of AntInfoList after AntInfoDelete", btnIndex, store);
                return;
            }

            // list should not have same antid
            List<AntInfo> antInfoList = AntInfoGetList();
            for ( int i = 0 ; i < antInfoList.size() ; i++ )
            {
                if ( antInfoList.get(i).getAntId() == Integer.MAX_VALUE )
                {
                    ShowResultOnTXV(strInput.toString(), txvInput);
                    ShowResultOnTXV(strOutput.toString(), txvOutput);
                    GotError(view, "AntInfo still exist after AntInfoDelete", btnIndex, store);
                    return;
                }
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end

            // test2 -start
            strInput.append("Input Test2 : \n");
            strOutput.append("Output Test2 : \n");

            // Del when no specific antinfoid exist, size should not change
            sizeBefore = AntInfoGetList().size();
            strInput.append("AntInfoGetList()\n");
            strOutput.append("Size = ").append(sizeBefore).append("\n");

            AntInfoDelete(Integer.MAX_VALUE);
            strInput.append("AntInfoDelete(Integer.MAX_VALUE)\n");
            strOutput.append("\n");

            sizeAfter = AntInfoGetList().size();
            strInput.append("AntInfoGetList()\n");
            strOutput.append("Size = ").append(sizeAfter).append("\n");

            if ( sizeBefore != sizeAfter )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Wrong size after delete non exist AntInfoId", btnIndex, store);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test2 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, store);
            return;
        }

        // test pass
        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "AntInfoDelete() Pass!", store);
    }

    private void Init()
    {
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_TESTANT_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_TESTANT_LAYOUT_HELP);
        txvInput = (TextView) findViewById(R.id.ID_TESTANT_TXV_INPUT);
        txvOutput = (TextView) findViewById(R.id.ID_TESTANT_TXV_OUTPUT);

        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TestMiddlewareMain > TestAnt");

        // init help
        help.setHelpInfoText(null, null);
        help.setHelpRedText(null);
        help.setHelpGreenText(null);
        help.setHelpBlueText(null);
        help.setHelpYellowText(null);

        // init ant elements, for error message
        mAntElements = new ArrayList<>();
        mAntElements.add("AntId");
        mAntElements.add("SatId");
        mAntElements.add("Lnb1");
        mAntElements.add("Lnb2");
        mAntElements.add("LnbType");
        mAntElements.add("DiseqcType");
        mAntElements.add("DiseqcUse");
        mAntElements.add("Diseqc");
        mAntElements.add("Tone22kUse");
        mAntElements.add("Tone22k");
        mAntElements.add("V012Use");
        mAntElements.add("V012");
        mAntElements.add("V1418Use");
        mAntElements.add("V1418");
        mAntElements.add("Cku");
    }

    private void GotError(View view, String errorMsg, int btnIndex, List<AntInfo> store)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, errorMsg );
        button.setTextColor(0xFFFF0000);    // red
        mErrorIndexSet.add(btnIndex);
        mTestedFuncSet.add(view.getId());

        if ( store != null )
        {
            AntInfoSaveList(store);
        }
    }

    private void TestPass(View view, String msg, List<AntInfo> store)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, msg );
        button.setTextColor(0xFF00FF00);    // green
        mTestedFuncSet.add(view.getId());

        if ( store != null )
        {
            AntInfoSaveList(store);
        }
    }

    // show result(string) on textview
    private void ShowResultOnTXV(String result, TextView textView)
    {
        textView.setText(result);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.scrollTo(0,0);
    }
}
*/
