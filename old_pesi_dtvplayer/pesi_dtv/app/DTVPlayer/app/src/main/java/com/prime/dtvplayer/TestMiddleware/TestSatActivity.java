package com.prime.dtvplayer.TestMiddleware;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dolphin.dtv.EnNetworkType;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.Sysdata.SatInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestSatActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();

    private ActivityHelpView help;
    private TextView txvInput;
    private TextView txvOutput;

    final int mTestTotalFuncCount = 5;    // 5 Sat functions
    private int mPosition;  // position of testMidMain

    private List<String> mSatElements;

    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    int tunerType = TpInfo.DVBC;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_sat);

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

    public void BtnSatInfoGet_OnClick(View view)
    {
        List<SatInfo> store = SatInfoGetList(TpInfo.DVBS);
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

            SatInfo satInfoIn = new SatInfo();
            satInfoIn.setSatName("test sat");
            satInfoIn.setTunerType(TpInfo.DVBS); // can only add DVBS type
            satInfoIn.setAngle(10);
//            satInfo.setLocation(mTestDataIntIn.get(3));
            satInfoIn.setPostionIndex(1);
            satInfoIn.Antenna.setLnb1(111);
            satInfoIn.Antenna.setLnb2(222);
            satInfoIn.Antenna.setLnbType(3);
            satInfoIn.Antenna.setCku(244);

            // add test data to DB
            SatInfoAdd(satInfoIn);

            // get test data from DB
            SatInfo satInfoOut = SatInfoGet(satInfoIn.getSatId());
            if ( satInfoOut == null )
            {
                GotError(view, "Got null SatInfo", btnIndex, store);
                return;
            }

            // set textview text
            strInput.append("SatId = ").append(satInfoIn.getSatId()).append("\n");
            strOutput.append("SatId = ").append(satInfoOut.getSatId()).append("\n");
            strInput.append("SatName = ").append(satInfoIn.getSatName()).append("\n");
            strOutput.append("SatName = ").append(satInfoOut.getSatName()).append("\n");
            strInput.append("TunerType = ").append(satInfoIn.getTunerType()).append("\n");
            strOutput.append("TunerType = ").append(satInfoOut.getTunerType()).append("\n");
            strInput.append("Angle = ").append(satInfoIn.getAngle()).append("\n");
            strOutput.append("Angle = ").append(satInfoOut.getAngle()).append("\n");
            strInput.append("Location = ").append(satInfoIn.getLocation()).append("\n");
            strOutput.append("Location = ").append(satInfoOut.getLocation()).append("\n");
            strInput.append("PostionIndex = ").append(satInfoIn.getPostionIndex()).append("\n");
            strOutput.append("PostionIndex = ").append(satInfoOut.getPostionIndex()).append("\n");
            strInput.append("TpNum = ").append(satInfoIn.getTpNum()).append("\n");
            strOutput.append("TpNum = ").append(satInfoOut.getTpNum()).append("\n");

            strInput.append("LnbType = ").append(satInfoIn.Antenna.getLnbType()).append("\n");
            strOutput.append("LnbType = ").append(satInfoOut.Antenna.getLnbType()).append("\n");
            strInput.append("Lnb1 = ").append(satInfoIn.Antenna.getLnb1()).append("\n");
            strOutput.append("Lnb1 = ").append(satInfoOut.Antenna.getLnb1()).append("\n");
            strInput.append("Lnb2 = ").append(satInfoIn.Antenna.getLnb2()).append("\n");
            strOutput.append("Lnb2 = ").append(satInfoOut.Antenna.getLnb2()).append("\n");
            strInput.append("Cku = ").append(satInfoIn.Antenna.getCku()).append("\n");
            strOutput.append("Cku = ").append(satInfoOut.Antenna.getCku()).append("\n");

            /*strInput.append(satInfoIn.ToString()).append("\n");
            strOutput.append(satInfoOut.ToString()).append("\n");*/

            // compare
            if ( !satInfoIn.ToString().equals(satInfoOut.ToString()) )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                String msg = "SatInfoIn is different from SatInfoOut";
                GotError(view, msg, btnIndex, store);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end

            // test2 -start
            strInput.append("Input Test2 : \n");
            strOutput.append("Output Test2 : \n");

            // get SatInfo when there is no specific ID
            SatInfoDelete(Integer.MAX_VALUE);
            strInput.append("SatInfoDelete(Integer.MAX_VALUE)\n");
            strOutput.append("\n");
            satInfoOut = SatInfoGet(Integer.MAX_VALUE);
            strInput.append("SatInfoGet(Integer.MAX_VALUE)\n");
            strOutput.append("satInfo = ").append(satInfoOut).append("\n");

            // we should get null
            if (satInfoOut != null)
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got SatInfo when the specific ID does not exit", btnIndex, store);
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

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "SatInfoGet() Pass!", store);
    }

    public void BtnSatInfoGetList_OnClick(View view)
    {
        List<SatInfo> store = SatInfoGetList(tunerType);
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

            // get sizeAfter
            List<SatInfo> satInfoList = SatInfoGetList(tunerType);
            int size = satInfoList == null ? 0 : satInfoList.size();
            strInput.append("SatInfoGetList()\n");
            strOutput.append("SatInfoGetList() : Size = ").append(size).append("\n");

            for ( int i = 0 ; i < size ; i++)
            {
                strInput.append("\n");
                strOutput.append("\n");

                strInput.append("satInfoList[ ").append(i).append(" ]\n");
                strOutput.append("SatId = ").append(satInfoList.get(i).getSatId()).append("\n");
                strInput.append("\n");
                strOutput.append("SatName = ").append(satInfoList.get(i).getSatName()).append("\n");
                strInput.append("\n");
                strOutput.append("TunerType = ").append(satInfoList.get(i).getTunerType()).append("\n");
                strInput.append("\n");
                strOutput.append("PosIndex = ").append(satInfoList.get(i).getPostionIndex()).append("\n");
                strInput.append("\n");
                strOutput.append("Loc = ").append(satInfoList.get(i).getLocation()).append("\n");
                strInput.append("\n");
                strOutput.append("Angle = ").append(satInfoList.get(i).getAngle()).append("\n");
                strInput.append("\n");
                strOutput.append("TpNum = ").append(satInfoList.get(i).getTpNum()).append("\n");

                strInput.append("\n");
                strOutput.append("LnbType = ").append(satInfoList.get(i).Antenna.getLnbType()).append("\n");
                strInput.append("\n");
                strOutput.append("Lnb1 = ").append(satInfoList.get(i).Antenna.getLnb1()).append("\n");
                strInput.append("\n");
                strOutput.append("Lnb2 = ").append(satInfoList.get(i).Antenna.getLnb2()).append("\n");
                strInput.append("\n");
                strOutput.append("Cku = ").append(satInfoList.get(i).Antenna.getCku()).append("\n");
                strInput.append("\n");
                strOutput.append("Tone22k = ").append(satInfoList.get(i).Antenna.getTone22k()).append("\n");
                strInput.append("\n");
                strOutput.append("DiseqcType = ").append(satInfoList.get(i).Antenna.getDiseqcType()).append("\n");
                strInput.append("\n");
                strOutput.append("Diseqc = ").append(satInfoList.get(i).Antenna.getDiseqc()).append("\n");
            }

            if (satInfoList == null || satInfoList.isEmpty())
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got empty SatInfoList", btnIndex, store);
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
            GotError(view, errorMsg, btnIndex, store);

            return;
        }

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "SatInfoGetList() Pass!", store);
    }

    public void BtnSatInfoAddAndUpdate_OnClick(View view)
    {
        List<SatInfo> store = SatInfoGetList(TpInfo.DVBS);
        final int btnIndex = 2;

        StringBuilder strInput = new StringBuilder();
        StringBuilder strOutput = new StringBuilder();
        ShowResultOnTXV("", txvInput);
        ShowResultOnTXV("", txvOutput);
        try
        {
            // test1 add -start, cant add DVBC sat so use DVBS instead
            strInput.append("Input Test1 : Add()\n");
            strOutput.append("Output Test1 : Add()\n");

            // add sat to DB
            SatInfo satInfoIn = new SatInfo();
            satInfoIn.setSatName("test sat dvbs");
            satInfoIn.setTunerType(TpInfo.DVBS); // can only add DVBS type
            satInfoIn.setAngle((float)47.5);
//            satInfo.setLocation(mTestDataIntIn.get(3));
            satInfoIn.setPostionIndex(2);
            satInfoIn.Antenna.setLnb1(999);
            satInfoIn.Antenna.setLnb2(888);
            satInfoIn.Antenna.setLnbType(7);
            satInfoIn.Antenna.setCku(244);
            //satInfoIn.Antenna.setDiseqcUse(0);
            satInfoIn.Antenna.setDiseqc(0);
            satInfoIn.Antenna.setDiseqcType(SatInfo.DISEQC_TYPE_OFF);

            SatInfoAdd(satInfoIn);

            // get sat from DB
            SatInfo satInfoOut = SatInfoGet(satInfoIn.getSatId());
            if ( satInfoOut == null )
            {
                GotError(view, "Got null SatInfo", btnIndex, store);
                return;
            }

            // set textview text
            strInput.append("SatId = ").append(satInfoIn.getSatId()).append("\n");
            strOutput.append("SatId = ").append(satInfoOut.getSatId()).append("\n");
            strInput.append("SatName = ").append(satInfoIn.getSatName()).append("\n");
            strOutput.append("SatName = ").append(satInfoOut.getSatName()).append("\n");
            strInput.append("TunerType = ").append(satInfoIn.getTunerType()).append("\n");
            strOutput.append("TunerType = ").append(satInfoOut.getTunerType()).append("\n");
            strInput.append("Angle = ").append(satInfoIn.getAngle()).append("\n");
            strOutput.append("Angle = ").append(satInfoOut.getAngle()).append("\n");
            strInput.append("Location = ").append(satInfoIn.getLocation()).append("\n");
            strOutput.append("Location = ").append(satInfoOut.getLocation()).append("\n");
            strInput.append("PostionIndex = ").append(satInfoIn.getPostionIndex()).append("\n");
            strOutput.append("PostionIndex = ").append(satInfoOut.getPostionIndex()).append("\n");
            strInput.append("TpNum = ").append(satInfoIn.getTpNum()).append("\n");
            strOutput.append("TpNum = ").append(satInfoOut.getTpNum()).append("\n");

            strInput.append("LnbType = ").append(satInfoIn.Antenna.getLnbType()).append("\n");
            strOutput.append("LnbType = ").append(satInfoOut.Antenna.getLnbType()).append("\n");
            strInput.append("Lnb1 = ").append(satInfoIn.Antenna.getLnb1()).append("\n");
            strOutput.append("Lnb1 = ").append(satInfoOut.Antenna.getLnb1()).append("\n");
            strInput.append("Lnb2 = ").append(satInfoIn.Antenna.getLnb2()).append("\n");
            strOutput.append("Lnb2 = ").append(satInfoOut.Antenna.getLnb2()).append("\n");
            strInput.append("Cku = ").append(satInfoIn.Antenna.getCku()).append("\n");
            strOutput.append("Cku = ").append(satInfoOut.Antenna.getCku()).append("\n");
            //strInput.append("DiseqcUse = ").append(satInfoIn.Antenna.getDiseqcUse()).append("\n");
            //strOutput.append("DiseqcUse = ").append(satInfoOut.Antenna.getDiseqcUse()).append("\n");
            strInput.append("Diseqc = ").append(satInfoIn.Antenna.getDiseqc()).append("\n");
            strOutput.append("Diseqc = ").append(satInfoOut.Antenna.getDiseqc()).append("\n");
            strInput.append("DiseqcType = ").append(satInfoIn.Antenna.getDiseqcType()).append("\n");
            strOutput.append("DiseqcType = ").append(satInfoOut.Antenna.getDiseqcType()).append("\n");

            /*strInput.append("string").append(satInfoIn.ToString()).append("\n");
            strOutput.append("string").append(satInfoOut.ToString()).append("\n");*/

            // compare SatIn and SatOut
            if ( !satInfoIn.ToString().equals(satInfoOut.ToString()) )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                String msg = "SatInfoIn is different from SatInfoOut";
                GotError(view, msg, btnIndex, store);
                return;
            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end

            // test2 Update -start
            strInput.append("Input Test2 : Update()\n");
            strOutput.append("Output Test2 : Update()\n");

            // update SatIn in DB
            satInfoIn.setSatName("test update sat");

            satInfoIn.setPostionIndex((satInfoIn.getPostionIndex()+1)%32);
            satInfoIn.setAngle((satInfoIn.getAngle()+10)%360);
            satInfoIn.Antenna.setLnbType(satInfoIn.Antenna.getLnbType()+1);
            satInfoIn.Antenna.setLnb1(satInfoIn.Antenna.getLnb1()+100);
            satInfoIn.Antenna.setLnb2(satInfoIn.Antenna.getLnb2()+100);
            satInfoIn.Antenna.setCku(satInfoIn.Antenna.getCku()+2);
            //satInfoIn.Antenna.setDiseqcUse(1);
            satInfoIn.Antenna.setDiseqc(2);
            satInfoIn.Antenna.setDiseqcType(SatInfo.DISEQC_TYPE_1_0);

            SatInfoUpdate(satInfoIn);

            // Got SatOut
            satInfoOut = SatInfoGet(satInfoIn.getSatId());
            if ( satInfoOut == null )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got null SatInfo", btnIndex, store);
                return;
            }

            // set textview text
            strInput.append("SatId = ").append(satInfoIn.getSatId()).append("\n");
            strOutput.append("SatId = ").append(satInfoOut.getSatId()).append("\n");
            strInput.append("SatName = ").append(satInfoIn.getSatName()).append("\n");
            strOutput.append("SatName = ").append(satInfoOut.getSatName()).append("\n");
            strInput.append("TunerType = ").append(satInfoIn.getTunerType()).append("\n");
            strOutput.append("TunerType = ").append(satInfoOut.getTunerType()).append("\n");
            strInput.append("Angle = ").append(satInfoIn.getAngle()).append("\n");
            strOutput.append("Angle = ").append(satInfoOut.getAngle()).append("\n");
            strInput.append("Location = ").append(satInfoIn.getLocation()).append("\n");
            strOutput.append("Location = ").append(satInfoOut.getLocation()).append("\n");
            strInput.append("PostionIndex = ").append(satInfoIn.getPostionIndex()).append("\n");
            strOutput.append("PostionIndex = ").append(satInfoOut.getPostionIndex()).append("\n");
            strInput.append("TpNum = ").append(satInfoIn.getTpNum()).append("\n");
            strOutput.append("TpNum = ").append(satInfoOut.getTpNum()).append("\n");

            strInput.append("LnbType = ").append(satInfoIn.Antenna.getLnbType()).append("\n");
            strOutput.append("LnbType = ").append(satInfoOut.Antenna.getLnbType()).append("\n");
            strInput.append("Lnb1 = ").append(satInfoIn.Antenna.getLnb1()).append("\n");
            strOutput.append("Lnb1 = ").append(satInfoOut.Antenna.getLnb1()).append("\n");
            strInput.append("Lnb2 = ").append(satInfoIn.Antenna.getLnb2()).append("\n");
            strOutput.append("Lnb2 = ").append(satInfoOut.Antenna.getLnb2()).append("\n");
            strInput.append("Cku = ").append(satInfoIn.Antenna.getCku()).append("\n");
            strOutput.append("Cku = ").append(satInfoOut.Antenna.getCku()).append("\n");
            //strInput.append("DiseqcUse = ").append(satInfoIn.Antenna.getDiseqcUse()).append("\n");
            //strOutput.append("DiseqcUse = ").append(satInfoOut.Antenna.getDiseqcUse()).append("\n");
            strInput.append("Diseqc = ").append(satInfoIn.Antenna.getDiseqc()).append("\n");
            strOutput.append("Diseqc = ").append(satInfoOut.Antenna.getDiseqc()).append("\n");
            strInput.append("DiseqcType = ").append(satInfoIn.Antenna.getDiseqcType()).append("\n");
            strOutput.append("DiseqcType = ").append(satInfoOut.Antenna.getDiseqcType()).append("\n");

            /*strInput.append(satInfoIn.ToString()).append("\n");
            strOutput.append(satInfoOut.ToString()).append("\n");*/

            // compare SatIn and SatOut
            if ( !satInfoIn.ToString().equals(satInfoOut.ToString()) )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                String msg = "SatInfoIn is different from SatInfoOut";
                GotError(view, msg, btnIndex, store);
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

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "SatInfoAdd/Update() Pass!", store);
    }

    // SatSaveList can only do multi update now, cant add and del
    public void BtnSatInfoSaveList_OnClick(View view)
    {
        List<SatInfo> store = SatInfoGetList(TpInfo.DVBS);
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
            List<SatInfo> satInfoListIn = SatInfoGetList(TpInfo.DVBS);

            if (satInfoListIn == null)
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got null SatInfoList", btnIndex, store);
                return;
            }

            // multi update
            for (int i = 0 ; i < satInfoListIn.size() ; i++)
            {
                satInfoListIn.get(i).setSatName("test savelist sat" + i);
//                satInfoListIn.get(i).setTunerType(tunerType);

                satInfoListIn.get(i).setAngle(i*10%360);
//                satInfoListIn.get(i).setLocation(mTestDataIntIn.get(3));
                satInfoListIn.get(i).setPostionIndex(i%4);
                satInfoListIn.get(i).Antenna.setLnb1(i*100%1000);
                satInfoListIn.get(i).Antenna.setLnb2(i*100%1000);
                satInfoListIn.get(i).Antenna.setLnbType(i%10);
                satInfoListIn.get(i).Antenna.setCku(i*50%250);
            }

            SatInfoUpdateList(satInfoListIn);
            strInput.append("SatInfoSaveList() : Size = ").append(satInfoListIn.size()).append("\n");
            strOutput.append("\n");

            List<SatInfo> satInfoListOut = SatInfoGetList(TpInfo.DVBS);
            if ( satInfoListOut == null )
            {
                GotError(view, "Got null SatInfoList", btnIndex, store);
                return;
            }

            strInput.append("SatInfoGetList(DVBS)\n");
            strOutput.append("satInfoList : Size = ").append(satInfoListOut.size()).append("\n");

            // check size
            if ( satInfoListOut.size() != satInfoListIn.size() )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Wrong size of SatInfoList after SatInfoSaveList", btnIndex, store);
                return;
            }

            // show content of satlistIn and satlistOut
            for (int i = 0 ; i < satInfoListIn.size() ; i++)
            {
                // set textview text
                strInput.append("SatId = ").append(satInfoListIn.get(i).getSatId()).append("\n");
                strOutput.append("SatId = ").append(satInfoListOut.get(i).getSatId()).append("\n");
                strInput.append("SatName = ").append(satInfoListIn.get(i).getSatName()).append("\n");
                strOutput.append("SatName = ").append(satInfoListOut.get(i).getSatName()).append("\n");
                strInput.append("TunerType = ").append(satInfoListIn.get(i).getTunerType()).append("\n");
                strOutput.append("TunerType = ").append(satInfoListOut.get(i).getTunerType()).append("\n");
                strInput.append("Angle = ").append(satInfoListIn.get(i).getAngle()).append("\n");
                strOutput.append("Angle = ").append(satInfoListOut.get(i).getAngle()).append("\n");
                strInput.append("Location = ").append(satInfoListIn.get(i).getLocation()).append("\n");
                strOutput.append("Location = ").append(satInfoListOut.get(i).getLocation()).append("\n");
                strInput.append("PostionIndex = ").append(satInfoListIn.get(i).getPostionIndex()).append("\n");
                strOutput.append("PostionIndex = ").append(satInfoListOut.get(i).getPostionIndex()).append("\n");
                strInput.append("TpNum = ").append(satInfoListIn.get(i).getTpNum()).append("\n");
                strOutput.append("TpNum = ").append(satInfoListOut.get(i).getTpNum()).append("\n");

                strInput.append("LnbType = ").append(satInfoListIn.get(i).Antenna.getLnbType()).append("\n");
                strOutput.append("LnbType = ").append(satInfoListOut.get(i).Antenna.getLnbType()).append("\n");
                strInput.append("Lnb1 = ").append(satInfoListIn.get(i).Antenna.getLnb1()).append("\n");
                strOutput.append("Lnb1 = ").append(satInfoListOut.get(i).Antenna.getLnb1()).append("\n");
                strInput.append("Lnb2 = ").append(satInfoListIn.get(i).Antenna.getLnb2()).append("\n");
                strOutput.append("Lnb2 = ").append(satInfoListOut.get(i).Antenna.getLnb2()).append("\n");
                strInput.append("Cku = ").append(satInfoListIn.get(i).Antenna.getCku()).append("\n");
                strOutput.append("Cku = ").append(satInfoListOut.get(i).Antenna.getCku()).append("\n");
                //strInput.append("DiseqcUse = ").append(satInfoListIn.get(i).Antenna.getDiseqcUse()).append("\n");
                //strOutput.append("DiseqcUse = ").append(satInfoListOut.get(i).Antenna.getDiseqcUse()).append("\n");
                strInput.append("Diseqc = ").append(satInfoListIn.get(i).Antenna.getDiseqc()).append("\n");
                strOutput.append("Diseqc = ").append(satInfoListOut.get(i).Antenna.getDiseqc()).append("\n");
                strInput.append("DiseqcType = ").append(satInfoListIn.get(i).Antenna.getDiseqcType()).append("\n");
                strOutput.append("DiseqcType = ").append(satInfoListOut.get(i).Antenna.getDiseqcType()).append("\n");

                strInput.append(satInfoListIn.get(i).ToString()).append("\n");
                strOutput.append(satInfoListOut.get(i).ToString()).append("\n");

                strInput.append("\n");
                strOutput.append("\n");
            }

            // check content
            for (int i = 0 ; i < satInfoListIn.size() ; i++)
            {
                if (!satInfoListIn.get(i).ToString().equals(satInfoListOut.get(i).ToString()))
                {
                    ShowResultOnTXV(strInput.toString(), txvInput);
                    ShowResultOnTXV(strOutput.toString(), txvOutput);
                    GotError(view, "Content does not match, pos = " + i, btnIndex, store);
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
            GotError(view, errorMsg, btnIndex, store);
            return;
        }

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "SatInfoSaveList() Pass!", store);
//        TestPass(view, "Skip Test", store);
    }

    public void BtnSatInfoDelete_OnClick(View view)
    {
        List<SatInfo> store = SatInfoGetList(TpInfo.DVBS);
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

            // save a SatInfo first
            SatInfo satInfo = new SatInfo();
            satInfo.setSatName("test del sat");
            satInfo.setTunerType(TpInfo.DVBS); // can only add DVBS type
            satInfo.setAngle(10);

//            satInfo.setLocation(mTestDataIntIn.get(3));
            satInfo.setPostionIndex(3);
            satInfo.Antenna.setLnb1(123);
            satInfo.Antenna.setLnb2(456);
            satInfo.Antenna.setLnbType(4);
            satInfo.Antenna.setCku(128);
            SatInfoAdd(satInfo);
            strInput.append("SatInfoAdd()\n");
            strOutput.append("\n");

            // record size -> del -> record size
            List<SatInfo> satInfoList = SatInfoGetList(TpInfo.DVBS);
            int sizeBefore = satInfoList == null ? 0 : satInfoList.size();
            strInput.append("SatInfoGetList(DVBS)\n");
            strOutput.append("Size = ").append(sizeBefore).append("\n");
            if (sizeBefore == 0)
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Got Empty SatInfoList", btnIndex, store);
                return;
            }

            SatInfoDelete(satInfo.getSatId());
            strInput.append("SatInfoDelete()\n");
            strOutput.append("\n");

            satInfoList = SatInfoGetList(TpInfo.DVBS);
            int sizeAfter = satInfoList == null ? 0 : satInfoList.size();
            strInput.append("SatInfoGetList(DVBS)\n");
            strOutput.append("Size = ").append(sizeAfter).append("\n");

            // check size
            if ( sizeAfter != sizeBefore-1 )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Wrong size of SatInfoList after SatInfoDelete", btnIndex, store);
                return;
            }

            // list should not have same satid
//            satInfoList = SatInfoGetList(tunerType);
//            for ( int i = 0 ; satInfoList != null && i < satInfoList.size() ; i++ )
//            {
//                if ( satInfoList.get(i).getSatId() == satID )
//                {
//                    ShowResultOnTXV(strInput.toString(), txvInput);
//                    ShowResultOnTXV(strOutput.toString(), txvOutput);
//                    GotError(view, "SatInfo still exist after SatInfoDelete", btnIndex, store);
//                    return;
//                }
//            }

            strInput.append("\n");
            strOutput.append("\n");
            // test1 -end

            // test2 -start
            strInput.append("Input Test2 : \n");
            strOutput.append("Output Test2 : \n");

            // Del when no specific satinfoid exist, size should not change
            satInfoList = SatInfoGetList(TpInfo.DVBS);
            sizeBefore = satInfoList == null ? 0 : satInfoList.size();
            strInput.append("SatInfoGetList(DVBS)\n");
            strOutput.append("Size = ").append(sizeBefore).append("\n");

            SatInfoDelete(Integer.MAX_VALUE);
            strInput.append("SatInfoDelete(Integer.MAX_VALUE)\n");
            strOutput.append("\n");

            satInfoList = SatInfoGetList(TpInfo.DVBS);
            sizeAfter = satInfoList == null ? 0 : satInfoList.size();
            strInput.append("SatInfoGetList(DVBS)\n");
            strOutput.append("Size = ").append(sizeAfter).append("\n");

            if ( sizeBefore != sizeAfter )
            {
                ShowResultOnTXV(strInput.toString(), txvInput);
                ShowResultOnTXV(strOutput.toString(), txvOutput);
                GotError(view, "Wrong size after delete non exist SatInfoId", btnIndex, store);
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

        ShowResultOnTXV(strInput.toString(), txvInput);
        ShowResultOnTXV(strOutput.toString(), txvOutput);
        TestPass(view, "SatInfoDelete() Pass!", store);
    }

    private void Init()
    {
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_TESTSAT_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_TESTSAT_LAYOUT_HELP);
        txvInput = (TextView) findViewById(R.id.ID_TESTSAT_TXV_INPUT);
        txvOutput = (TextView) findViewById(R.id.ID_TESTSAT_TXV_OUTPUT);

        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TestMiddlewareMain > TestSat");

        // init help
        help.setHelpInfoText(null, null);
        help.setHelpRedText(null);
        help.setHelpGreenText(null);
        help.setHelpBlueText(null);
        help.setHelpYellowText(null);

        // init sat elements, for error message
        mSatElements = new ArrayList<>();
        mSatElements.add("SatId");
        mSatElements.add("TpNum");
        mSatElements.add("Angle");
        mSatElements.add("Location");
        mSatElements.add("PostionIndex");
        mSatElements.add("Tps");
        mSatElements.add("Ant Lnb1");
        mSatElements.add("Ant Lnb2");
        mSatElements.add("Ant LnbType");
        mSatElements.add("Ant DiseqcType");
        mSatElements.add("Ant DiseqcUse");
        mSatElements.add("Ant Diseqc");
        mSatElements.add("Ant Tone22kUse");
        mSatElements.add("Ant Tone22k");
        mSatElements.add("Ant V012Use");
        mSatElements.add("Ant V012");
        mSatElements.add("Ant V1418Use");
        mSatElements.add("Ant V1418");
        mSatElements.add("Ant Cku");
        mSatElements.add("SatName");
    }

    private void GotError(View view, String errorMsg, int btnIndex, List<SatInfo> store)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, errorMsg );
        button.setTextColor(0xFFFF0000);    // red
        mErrorIndexSet.add(btnIndex);
        mTestedFuncSet.add(view.getId());

        if ( store != null )
        {
//            SatInfoSaveList(store);
        }

    }

    private void TestPass(View view, String msg, List<SatInfo> store)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, msg );
        button.setTextColor(0xFF00FF00);    // green
        mTestedFuncSet.add(view.getId());

        if ( store != null )
        {
//            SatInfoSaveList(store);
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
