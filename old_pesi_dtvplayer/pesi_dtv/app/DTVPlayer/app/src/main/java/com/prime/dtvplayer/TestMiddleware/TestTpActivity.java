package com.prime.dtvplayer.TestMiddleware;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.MiscDefine;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.Sysdata.TpInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestTpActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();

    private ActivityHelpView help;
    private TextView txvInput;
    private TextView txvOutput;

    final int mTestTotalFuncCount = 6;    // 6 Tp functions
    private int mPosition;  // position of testMidMain

    private List<String> mTpElements;

    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_tp);

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

    public void BtnTpInfoGet_OnClick(View view)
    {
        Log.d(TAG, "BtnTpInfoGet_OnClick: ");

        List<List<TpInfo>> stores = new ArrayList<>();
        List<TpInfo> storeDVBC = TpInfoGetList(TpInfo.DVBC);
        List<TpInfo> storeDVBS = TpInfoGetList(TpInfo.DVBS);
        List<TpInfo> storeDVBT = TpInfoGetList(TpInfo.DVBT);
        stores.add(storeDVBC);
        stores.add(storeDVBS);
        stores.add(storeDVBT);

        final int btnIndex = 0; // it is first button

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);

        final int testSatId = SatInfoGetList(TpInfo.DVBC).get(0).getSatId();
        try
        {
            // test1 -start
            strInput.append("Input Test1 : \n");
            strOutput.append("Output Test1 : \n");

            // save test data to DB
            TpInfo tpIn = new TpInfo(TpInfo.DVBC);
            tpIn.setSatId(testSatId);
            tpIn.setTuner_id(0);
            tpIn.CableTp.setFreq(11111);
            tpIn.CableTp.setSymbol(22222);
            tpIn.CableTp.setQam(3);
            TpInfoAdd(tpIn);

            // get test data from DB
            TpInfo tpOut = TpInfoGet(tpIn.getTpId());  // tpId is set after TpInfoAdd
            if ( tpOut == null )
            {
                GotError(view, "Got null TpInfo", btnIndex, stores);
                return;
            }

            // set textview text
            strInput.append("TpID = ").append(tpIn.getTpId()).append("\n");
            strOutput.append("TpID = ").append(tpOut.getTpId()).append("\n");
            strInput.append("SatID = ").append(tpIn.getSatId()).append("\n");
            strOutput.append("SatID = ").append(tpOut.getSatId()).append("\n");
            strInput.append("TunerID = ").append(tpIn.getTuner_id()).append("\n");
            strOutput.append("TunerID = ").append(tpOut.getTuner_id()).append("\n");
            strInput.append("TunerType = ").append(tpIn.getTunerType()).append("\n");
            strOutput.append("TunerType = ").append(tpOut.getTunerType()).append("\n");
            strInput.append("Freq = ").append(tpIn.CableTp.getFreq()).append("\n");
            strOutput.append("Freq = ").append(tpOut.CableTp.getFreq()).append("\n");
            strInput.append("Symbol = ").append(tpIn.CableTp.getSymbol()).append("\n");
            strOutput.append("Symbol = ").append(tpOut.CableTp.getSymbol()).append("\n");
            strInput.append("Qam = ").append(tpIn.CableTp.getQam()).append("\n");
            strOutput.append("Qam = ").append(tpOut.CableTp.getQam()).append("\n");

            // compare TpIn and TpOut
            if ( !tpIn.ToString().equals(tpOut.ToString()) )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                String msg = "TpInfoIn is different form TpInfoOut";
                GotError(view, msg, btnIndex, stores);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, stores);
            return;
        }

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "TpInfoGet() Pass!", stores);
    }

    public void BtnTpInfoGetList_OnClick(View view)
    {
        List<List<TpInfo>> stores = new ArrayList<>();
        List<TpInfo> storeDVBC = TpInfoGetList(TpInfo.DVBC);
        List<TpInfo> storeDVBS = TpInfoGetList(TpInfo.DVBS);
        List<TpInfo> storeDVBT = TpInfoGetList(TpInfo.DVBT);
        stores.add(storeDVBC);
        stores.add(storeDVBS);
        stores.add(storeDVBT);

        final int btnIndex = 1;

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        try
        {
            // test1 -start
            strInput.append("Input Test1 : \n");
            strOutput.append("Output Test1 : \n");

            // get size
            List<TpInfo> tpInfoList = TpInfoGetList(TpInfo.DVBC);
            int sizeAfterDVBC = tpInfoList == null ? 0 : tpInfoList.size();
            strInput.append("TpInfoGetList(DVBC)\n");
            strOutput.append("TpInfoGetList(DVBC) : Size = ").append(sizeAfterDVBC).append("\n");
//            int sizeDVBS =  TpInfoGetList(TpInfo.DVBS).size();
//            strInput.append("TpInfoGetList(DVBS)\n");
//            strOutput.append("TpInfoGetList(DVBS) : Size = ").append(sizeDVBS).append("\n");
//            int sizeDVBT =  TpInfoGetList(TpInfo.DVBT).size();
//            strInput.append("TpInfoGetList(DVBT)\n");
//            strOutput.append("TpInfoGetList(DVBT) : Size = ").append(sizeDVBT).append("\n");

            if (tpInfoList == null)
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                String msg = "Got null TpInfoList";
                GotError(view, msg, btnIndex, stores);
                return;
            }

            for (int i = 0 ; i < tpInfoList.size() ; i++)
            {
                // set textview text
                strInput.append("TpList: ").append(i).append("\n");
                strOutput.append("TpID = ").append(tpInfoList.get(i).getTpId()).append("\n");
                strInput.append("\n");
                strOutput.append("SatID = ").append(tpInfoList.get(i).getSatId()).append("\n");
                strInput.append("\n");
                strOutput.append("TunerID = ").append(tpInfoList.get(i).getTuner_id()).append("\n");
                strInput.append("\n");
                strOutput.append("TunerType = ").append(tpInfoList.get(i).getTunerType()).append("\n");
                strInput.append("\n");
                strOutput.append("Freq = ").append(tpInfoList.get(i).CableTp.getFreq()).append("\n");
                strInput.append("\n");
                strOutput.append("Symbol = ").append(tpInfoList.get(i).CableTp.getSymbol()).append("\n");
                strInput.append("\n\n");
                strOutput.append("Qam = ").append(tpInfoList.get(i).CableTp.getQam()).append("\n\n");
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 - end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, stores);
            return;
        }

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "TpInfoGetList() Pass!", stores);
    }

    public void BtnTpInfoGetListBySatId_OnClick(View view)
    {
        List<List<TpInfo>> stores = new ArrayList<>();
        List<TpInfo> storeDVBC = TpInfoGetList(TpInfo.DVBC);
        List<TpInfo> storeDVBS = TpInfoGetList(TpInfo.DVBS);
        List<TpInfo> storeDVBT = TpInfoGetList(TpInfo.DVBT);
        stores.add(storeDVBC);
        stores.add(storeDVBS);
        stores.add(storeDVBT);

        final int btnIndex = 2;

        final int testSatId = SatInfoGetList(TpInfo.DVBC).get(0).getSatId();

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        try
        {
            // test1 -start
            strInput.append("Input Test1 : SatId = ").append(testSatId).append("\n");
            strOutput.append("Output Test1 : \n");

            // get size
            List<TpInfo> tpInfoList = TpInfoGetListBySatId(testSatId);
            int size = tpInfoList == null ? 0 : tpInfoList.size();
            strInput.append("TpInfoGetListBySatId()\n");
            strOutput.append("TpInfoGetListBySatId() : Size = ").append(size).append("\n");

            if ( tpInfoList == null )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got null TpInfoList", btnIndex, stores);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 - end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, stores);
            return;
        }

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "TpInfoGetListBySatId() Pass!", stores);
    }

    public void BtnTpInfoAddAndUpdate_OnClick(View view)
    {
        List<List<TpInfo>> stores = new ArrayList<>();
        List<TpInfo> storeDVBC = TpInfoGetList(TpInfo.DVBC);
        List<TpInfo> storeDVBS = TpInfoGetList(TpInfo.DVBS);
        List<TpInfo> storeDVBT = TpInfoGetList(TpInfo.DVBT);
        stores.add(storeDVBC);
        stores.add(storeDVBS);
        stores.add(storeDVBT);

        final int btnIndex = 3;

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);

        final int testSatId = SatInfoGetList(TpInfo.DVBC).get(0).getSatId();
        int testDVBCFreq = 87000;
        int testDVBCSymbol = 4444;
        int testDVBCQam = 3;
        try
        {
            // test1 Add -start
            strInput.append("Input Test1 : Add()\n");
            strOutput.append("Output Test1 : Add()\n");

            // add TpIn in DB
            TpInfo tpIn = new TpInfo(TpInfo.DVBC);
            tpIn.setSatId(testSatId);
            tpIn.setTuner_id(0);
            tpIn.CableTp.setFreq(testDVBCFreq);
            tpIn.CableTp.setSymbol(testDVBCSymbol);
            tpIn.CableTp.setQam(testDVBCQam);
            TpInfoAdd(tpIn);

            // Got TpOut
            TpInfo tpOut = TpInfoGet(tpIn.getTpId());
            if ( tpOut == null )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got null TpInfo", btnIndex, stores);
                return;
            }

            // set textview text
            strInput.append("TpID = ").append(tpIn.getTpId()).append("\n");
            strOutput.append("TpID = ").append(tpOut.getTpId()).append("\n");
            strInput.append("SatID = ").append(tpIn.getSatId()).append("\n");
            strOutput.append("SatID = ").append(tpOut.getSatId()).append("\n");
            strInput.append("TunerID = ").append(tpIn.getTuner_id()).append("\n");
            strOutput.append("TunerID = ").append(tpOut.getTuner_id()).append("\n");
            strInput.append("TunerType = ").append(tpIn.getTunerType()).append("\n");
            strOutput.append("TunerType = ").append(tpOut.getTunerType()).append("\n");
            strInput.append("Freq = ").append(tpIn.CableTp.getFreq()).append("\n");
            strOutput.append("Freq = ").append(tpOut.CableTp.getFreq()).append("\n");
            strInput.append("Symbol = ").append(tpIn.CableTp.getSymbol()).append("\n");
            strOutput.append("Symbol = ").append(tpOut.CableTp.getSymbol()).append("\n");
            strInput.append("Qam = ").append(tpIn.CableTp.getQam()).append("\n");
            strOutput.append("Qam = ").append(tpOut.CableTp.getQam()).append("\n");

            // compare TpIn and TpOut
            if ( !tpIn.ToString().equals(tpOut.ToString()) )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                String msg = "TpInfoIn is different form TpInfoOut";
                GotError(view, msg, btnIndex, stores);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 - end

            // test2 Update -start
            strInput.append("Input Test2 : Update()\n");
            strOutput.append("Output Test2 : Update()\n");

            // update TpIn in DB
            tpIn.CableTp.setFreq(testDVBCFreq + 123);
            tpIn.CableTp.setSymbol(testDVBCSymbol + 456);
            tpIn.CableTp.setQam(testDVBCQam+1);
            TpInfoUpdate(tpIn);

            // Got TpOut
            tpOut = TpInfoGet(tpIn.getTpId());
            if ( tpOut == null )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got null TpInfo", btnIndex, stores);
                return;
            }

            // set textview text
            strInput.append("TpID = ").append(tpIn.getTpId()).append("\n");
            strOutput.append("TpID = ").append(tpOut.getTpId()).append("\n");
            strInput.append("SatID = ").append(tpIn.getSatId()).append("\n");
            strOutput.append("SatID = ").append(tpOut.getSatId()).append("\n");
            strInput.append("TunerID = ").append(tpIn.getTuner_id()).append("\n");
            strOutput.append("TunerID = ").append(tpOut.getTuner_id()).append("\n");
            strInput.append("TunerType = ").append(tpIn.getTunerType()).append("\n");
            strOutput.append("TunerType = ").append(tpOut.getTunerType()).append("\n");
            strInput.append("Freq = ").append(tpIn.CableTp.getFreq()).append("\n");
            strOutput.append("Freq = ").append(tpOut.CableTp.getFreq()).append("\n");
            strInput.append("Symbol = ").append(tpIn.CableTp.getSymbol()).append("\n");
            strOutput.append("Symbol = ").append(tpOut.CableTp.getSymbol()).append("\n");
            strInput.append("Qam = ").append(tpIn.CableTp.getQam()).append("\n");
            strOutput.append("Qam = ").append(tpOut.CableTp.getQam()).append("\n");

            // compare TpIn and TpOut
            if ( !tpIn.ToString().equals(tpOut.ToString()) )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                String msg = "TpInfoIn is different form TpInfoOut";
                GotError(view, msg, btnIndex, stores);
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
            GotError(view, errorMsg, btnIndex, stores);
            return;
        }

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "TpInfoAdd/Update() Pass!", stores);
    }

    // TpSaveList can only do multi update now, cant add and del
    public void BtnTpInfoSaveList_OnClick(View view)
    {
        List<List<TpInfo>> stores = new ArrayList<>();
        List<TpInfo> storeDVBC = TpInfoGetList(TpInfo.DVBC);
        List<TpInfo> storeDVBS = TpInfoGetList(TpInfo.DVBS);
        List<TpInfo> storeDVBT = TpInfoGetList(TpInfo.DVBT);
        stores.add(storeDVBC);
        stores.add(storeDVBS);
        stores.add(storeDVBT);

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
            List<TpInfo> tpInfoListIn = TpInfoGetList(TpInfo.DVBC);

            if (tpInfoListIn == null)
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got null TpInfoList", btnIndex, stores);
                return;
            }

            // multi update
            for (int i = 0 ; i < tpInfoListIn.size() ; i++)
            {
                tpInfoListIn.get(i).CableTp.setFreq(i*10000%60000);
                tpInfoListIn.get(i).CableTp.setSymbol(i*10000%60000 + i);
                tpInfoListIn.get(i).CableTp.setQam(i%6);
            }

            TpInfoUpdateList(tpInfoListIn);
            strInput.append("TpInfoSaveList(DVBC) : Size = ").append(tpInfoListIn.size()).append("\n");
            strOutput.append("\n");

            List<TpInfo> tpInfoListOut = TpInfoGetList(TpInfo.DVBC);
            if ( tpInfoListOut == null )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got null TpInfoList", btnIndex, stores);
                return;
            }

            strInput.append("TpInfoGetList(DVBC)\n");
            strOutput.append("tpInfoList : Size = ").append(tpInfoListOut.size()).append("\n");

            // check size
            if ( tpInfoListOut.size() != tpInfoListIn.size() )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Wrong size of TpInfoList after TpInfoSaveList", btnIndex, stores);
                return;
            }

            // show content of tplistIn and tplistOut
            for (int i = 0 ; i < tpInfoListIn.size() ; i++)
            {
                // set textview text
                strInput.append("TpID = ").append(tpInfoListIn.get(i).getTpId()).append("\n");
                strOutput.append("TpID = ").append(tpInfoListOut.get(i).getTpId()).append("\n");
                strInput.append("SatID = ").append(tpInfoListIn.get(i).getSatId()).append("\n");
                strOutput.append("SatID = ").append(tpInfoListOut.get(i).getSatId()).append("\n");
                strInput.append("TunerID = ").append(tpInfoListIn.get(i).getTuner_id()).append("\n");
                strOutput.append("TunerID = ").append(tpInfoListOut.get(i).getTuner_id()).append("\n");
                strInput.append("TunerType = ").append(tpInfoListIn.get(i).getTunerType()).append("\n");
                strOutput.append("TunerType = ").append(tpInfoListOut.get(i).getTunerType()).append("\n");
                strInput.append("Freq = ").append(tpInfoListIn.get(i).CableTp.getFreq()).append("\n");
                strOutput.append("Freq = ").append(tpInfoListOut.get(i).CableTp.getFreq()).append("\n");
                strInput.append("Symbol = ").append(tpInfoListIn.get(i).CableTp.getSymbol()).append("\n");
                strOutput.append("Symbol = ").append(tpInfoListOut.get(i).CableTp.getSymbol()).append("\n");
                strInput.append("Qam = ").append(tpInfoListIn.get(i).CableTp.getQam()).append("\n");
                strOutput.append("Qam = ").append(tpInfoListOut.get(i).CableTp.getQam()).append("\n");

//                strInput.append(tpInfoListIn.get(i).ToString()).append("\n");
//                strOutput.append(tpInfoListOut.get(i).ToString()).append("\n");

                strInput.append("\n");
                strOutput.append("\n");
            }

            // check content
            for (int i = 0 ; i < tpInfoListIn.size() ; i++)
            {
                if (!tpInfoListIn.get(i).ToString().equals(tpInfoListOut.get(i).ToString()))
                {
                    ShowResultOnTXV(strInput.toString(), txvInput);
                    ShowResultOnTXV(strOutput.toString(), txvOutput);
                    GotError(view, "Content does not match, pos = " + i, btnIndex, stores);
                    return;
                }
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end
        }
        catch (Exception e)
        {
            ShowResultOnTXV(strInput.toString(), txvInput);
            ShowResultOnTXV(strOutput.toString(), txvOutput);
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex, stores);
            return;
        }

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "TpInfoSaveList() Pass!", stores);
//        TestPass(view, "Skip Test", stores);
    }

    public void BtnTpInfoDelete_OnClick(View view)
    {
        List<List<TpInfo>> stores = new ArrayList<>();
        List<TpInfo> storeDVBC = TpInfoGetList(TpInfo.DVBC);
        List<TpInfo> storeDVBS = TpInfoGetList(TpInfo.DVBS);
        List<TpInfo> storeDVBT = TpInfoGetList(TpInfo.DVBT);
        stores.add(storeDVBC);
        stores.add(storeDVBS);
        stores.add(storeDVBT);

        final int btnIndex = 5;

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        List<TpInfo> tpInfoList;
        try
        {
            // test1 -start
            strInput.append("Input Test1 : \n");
            strOutput.append("Output Test1 : \n");

            // save an Tpinfo first
            TpInfo tpInfo = new TpInfo(TpInfo.DVBC);
            int satId = SatInfoGetList(TpInfo.DVBC).get(0).getSatId();
            tpInfo.setSatId(satId);
            TpInfoAdd(tpInfo);
            strInput.append("TpInfoAdd()\n");
            strOutput.append("\n");

            // record size -> del -> record size
            tpInfoList = TpInfoGetList(TpInfo.DVBC);
            int sizeBefore = tpInfoList == null ? 0 : tpInfoList.size();
            strInput.append("TpInfoGetList()\n");
            strOutput.append("Size = ").append(sizeBefore).append("\n");

            TpInfoDelete(tpInfo.getTpId());
            strInput.append("TpInfoDelete()\n");
            strOutput.append("\n");

            tpInfoList = TpInfoGetList(TpInfo.DVBC);
            int sizeAfter = tpInfoList == null ? 0 : tpInfoList.size();
            strInput.append("TpInfoGetList()\n");
            strOutput.append("Size = ").append(sizeAfter).append("\n");

            // check size
            if ( sizeAfter != sizeBefore-1 )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Wrong size of TpInfoList after TpInfoDelete", btnIndex, stores);
                return;
            }

            // list should not have same tpid
//            tpInfoList = TpInfoGetList(TpInfo.DVBC);
//            for ( int i = 0 ; tpInfoList != null && i < tpInfoList.size() ; i++ )
//            {
//                if ( tpInfoList.get(i).getSatId() == tpInfo.getTpId() )
//                {
//                    ShowResultOnTXV(strInput.toString(), txvInput);
//                    ShowResultOnTXV(strOutput.toString(), txvOutput);
//                    GotError(view, "TpInfo still exist after TpInfoDelete", btnIndex, stores);
//                    return;
//                }
//            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end

            // test2 -start
            strInput.append("Input Test2 : \n");
            strOutput.append("Output Test2 : \n");

            // Del when no specific tpinfoid exist, size should not change
            tpInfoList = TpInfoGetList(TpInfo.DVBC);
            sizeBefore = tpInfoList == null ? 0 : tpInfoList.size();
            strInput.append("TpInfoGetList()\n");
            strOutput.append("Size = ").append(sizeBefore).append("\n");

            TpInfoDelete(Integer.MAX_VALUE);
            strInput.append("TpInfoDelete(Integer.MAX_VALUE)\n");
            strOutput.append("\n");

            tpInfoList = TpInfoGetList(TpInfo.DVBC);
            sizeAfter = tpInfoList == null ? 0 : tpInfoList.size();
            strInput.append("TpInfoGetList()\n");
            strOutput.append("Size = ").append(sizeAfter).append("\n");

            if ( sizeBefore != sizeAfter )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Wrong size after delete non exist SatInfoId", btnIndex, stores);
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
            GotError(view, errorMsg, btnIndex, stores);
            return;
        }

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "TpInfoDelete() Pass!", stores);
    }

    private void Init()
    {
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_TESTTP_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_TESTTP_LAYOUT_HELP);
        txvInput = (TextView) findViewById(R.id.ID_TESTTP_TXV_INPUT);
        txvOutput = (TextView) findViewById(R.id.ID_TESTTP_TXV_OUTPUT);

        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TvMiddlewareMain > TestTp");

        // init help
        help.setHelpInfoText(null, null);
        help.setHelpRedText(null);
        help.setHelpGreenText(null);
        help.setHelpBlueText(null);
        help.setHelpYellowText(null);

        // init tp elements, for error message
        mTpElements = new ArrayList<>();
        mTpElements.add("TunerType");
        mTpElements.add("TpId");
        mTpElements.add("SatId");
        mTpElements.add("network_id");
        mTpElements.add("transport_id");
        mTpElements.add("orignal_network_id");
        mTpElements.add("tuner_id");
    }

    private void GotError(View view, String errorMsg, int btnIndex, List<List<TpInfo>> stores)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, errorMsg );
        button.setTextColor(0xFFFF0000);    // red
        mErrorIndexSet.add(btnIndex);
        mTestedFuncSet.add(view.getId());

        List<TpInfo> allTps = new ArrayList<>();
        for ( int i = 0 ; i < stores.size() ; i++ )
        {
            List<TpInfo> tpInfoList = stores.get(i);
            if ( tpInfoList != null )
            {
                allTps.addAll(tpInfoList);
            }
        }

//        TpInfoSaveList(allTps);
    }

    private void TestPass(View view, String msg, List<List<TpInfo>> stores)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, msg );
        button.setTextColor(0xFF00FF00);    // green
        mTestedFuncSet.add(view.getId());

        List<TpInfo> allTps = new ArrayList<>();
        for ( int i = 0 ; i < stores.size() ; i++ )
        {
            List<TpInfo> tpInfoList = stores.get(i);
            if ( tpInfoList != null )
            {
                allTps.addAll(tpInfoList);
            }
        }

//        TpInfoSaveList(allTps);
    }

    // show result(string) on textview
    private void ShowResultOnTXV(String result, TextView textView)
    {
        textView.setText(result);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.scrollTo(0,0);
    }
}
