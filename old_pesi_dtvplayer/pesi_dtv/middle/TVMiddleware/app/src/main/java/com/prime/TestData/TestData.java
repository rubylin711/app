package com.prime.TestData;

import android.util.Log;
import com.prime.sysdata.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by Eric on 2017/11/20.
 */



public class TestData {
    private static final String TAG="TestData";
    private final int DVBT = 1;
    private final int DVBS = 2;
    private final int DVBC = 3;
    //private final int TestProgramNum = 14;
    private final int TestBookNum = 15;
    //private final int TestFavGroupNameNum=6;
    private final int TestAntNum=1;
    private final int TestSatNum=1;
    //private final int TestEPGEventNum = 100;
    private static int init_flag = 0;
    private static List<TpInfo> mTestDataTpInfo = null;    // johnny add 20171121
    private static List<ProgramInfo> mTestDataTvProgramInfo = new ArrayList<ProgramInfo>();
    private static List<ProgramInfo> mTestDataRadioProgramInfo = new ArrayList<ProgramInfo>();
    //private static List<AntInfo> mTestDataAntInfo = null;
    private static List<SatInfo> mTestDataSatInfo = null;
    private static List<BookInfo> mTestDataBookInfo = null;
    private static GposInfo mTestDataGposInfo = null;
    private static DefaultChannel mTestDataDefaultChannel = null;
    private static List<EPGEvent> mTestEPGEvent_PF = null;
    private static List<EPGEvent> mTestEPGEvent_Schedule = null;
    private static List<FavGroupName> mTestDataFavGroupName = new ArrayList<FavGroupName>();
    //private static List<FavInfo> mTestDataFavInfo = new ArrayList<FavInfo>();
    private static List<FavInfo> mTestDataTvFav1 = new ArrayList<FavInfo>();
    private static List<FavInfo> mTestDataTvFav2 = new ArrayList<FavInfo>();
    private static List<FavInfo> mTestDataTvFav3 = new ArrayList<FavInfo>();
    private static List<FavInfo> mTestDataTvFav4 = new ArrayList<FavInfo>();
    private static List<FavInfo> mTestDataTvFav5 = new ArrayList<FavInfo>();
    private static List<FavInfo> mTestDataTvFav6 = new ArrayList<FavInfo>();
    private static List<FavInfo> mTestDataRadioFav1 = new ArrayList<FavInfo>();
    private static List<FavInfo> mTestDataRadioFav2 = new ArrayList<FavInfo>();
    //private static List<List<FavInfo>> mTestDataFavInfo = new ArrayList<List<FavInfo>>();

    private int defCableTpInfo [][] =
    {
        {1, 58000, 6875, 5}, {2, 66000, 6875, 5},
        {3, 74000, 6875, 5}, {4, 82000, 6875, 5},
        {5, 90000, 6875, 5}, {6, 98000, 6875, 5},
        {7, 106000, 6875, 5}, {8, 114000, 6875, 5},
        {9, 122000, 6875, 5}, {10, 130000, 6875, 5},
        {11, 138000, 6875, 5}, {12, 146000, 6875, 5},
        {13, 154000, 6875, 5}, {14, 162000, 6875, 5},
        {15, 170000, 6875, 5}, {16, 178000, 6875, 5},
        {17, 186000, 6875, 5}, {18, 194000, 6875, 5},
        {19, 202000, 6875, 5}, {20, 210000, 6875, 5},
        {21, 218000, 6875, 5}, {22, 226000, 6875, 5},
        {23, 234000, 6875, 5}, {24, 242000, 6875, 5},
        {25, 250000, 6875, 5}, {26, 258000, 6875, 5},
        {27, 266000, 6875, 5}, {28, 274000, 6875, 5},
        {29, 282000, 6875, 5}, {30, 290000, 6875, 5},
        {31, 298000, 6875, 5}, {32, 306000, 6875, 5},
        {33, 314000, 6875, 5}, {34, 322000, 6875, 5},
        {35, 330000, 6875, 5}, {36, 338000, 6875, 5},
        {37, 346000, 6875, 5}, {38, 354000, 6875, 5},
        {39, 362000, 6875, 5}, {40, 370000, 6875, 5},
        {41, 378000, 6875, 5}, {42, 386000, 6875, 5},
        {43, 394000, 6875, 5}, {44, 402000, 6875, 5},
        {45, 410000, 6875, 5}, {46, 418000, 6875, 5},
        {47, 426000, 6875, 5}, {48, 434000, 6875, 5},
        {49, 442000, 6875, 5}, {50, 450000, 6875, 5},
        {51, 458000, 6875, 5}, {52, 466000, 6875, 5},
        {53, 474000, 6875, 5}, {54, 482000, 6875, 5},
        {55, 490000, 6875, 5}, {56, 498000, 6875, 5},
        {57, 506000, 6875, 5}, {58, 514000, 6875, 5},
        {59, 522000, 6875, 5}, {60, 530000, 6875, 5},
        {61, 538000, 6875, 5}, {62, 546000, 6875, 5},
        {63, 554000, 6875, 5}, {64, 562000, 6875, 5},
        {65, 570000, 6875, 5}, {66, 578000, 6875, 5},
        {67, 586000, 6875, 5}, {68, 594000, 6875, 5},
        {69, 602000, 6875, 5}, {70, 610000, 6875, 5},
        {71, 618000, 6875, 5}, {72, 626000, 6875, 5},
        {73, 634000, 6875, 5}, {74, 642000, 6875, 5},
        {75, 650000, 6875, 5}, {76, 658000, 6875, 5},
        {77, 666000, 6875, 5}, {78, 674000, 6875, 5},
        {79, 682000, 6875, 5}, {80, 690000, 6875, 5},
        {81, 698000, 6875, 5}, {82, 706000, 6875, 5},
        {83, 714000, 6875, 5}, {84, 722000, 6875, 5},
        {85, 730000, 6875, 5}, {86, 738000, 6875, 5},
        {87, 746000, 6875, 5}, {88, 754000, 6875, 5},
        {89, 762000, 6875, 5}, {90, 770000, 6875, 5},
        {91, 778000, 6875, 5}, {92, 786000, 6875, 5},
        {93, 794000, 6875, 5}, {94, 802000, 6875, 5},
        {95, 810000, 6875, 5}, {96, 818000, 6875, 5},
        {97, 826000, 6875, 5}, {98, 834000, 6875, 5},
        {99, 842000, 6875, 5}, {100, 850000, 6875, 5},
        {101, 858000, 6875, 5},
    };

    private int defISDBTTpInfo [][] =
    {
        {1, 473143, TpInfo.Terr.BAND_6MHZ}, {2, 478143, TpInfo.Terr.BAND_6MHZ},
        {3, 483143, TpInfo.Terr.BAND_6MHZ}, {4, 488143, TpInfo.Terr.BAND_6MHZ},
        {5, 493143, TpInfo.Terr.BAND_6MHZ}, {6, 498143, TpInfo.Terr.BAND_6MHZ},
        {7, 503143, TpInfo.Terr.BAND_6MHZ}, {8, 508143, TpInfo.Terr.BAND_6MHZ},
        {9, 513143, TpInfo.Terr.BAND_6MHZ}, {10, 518143, TpInfo.Terr.BAND_6MHZ},
        {11, 523143, TpInfo.Terr.BAND_6MHZ}, {12, 528143, TpInfo.Terr.BAND_6MHZ},
        {13, 533143, TpInfo.Terr.BAND_6MHZ}, {14, 538143, TpInfo.Terr.BAND_6MHZ},
        {15, 543143, TpInfo.Terr.BAND_6MHZ}, {16, 548143, TpInfo.Terr.BAND_6MHZ},
        {17, 553143, TpInfo.Terr.BAND_7MHZ}, {18, 558143, TpInfo.Terr.BAND_7MHZ},
        {19, 563143, TpInfo.Terr.BAND_8MHZ}, {20, 568143, TpInfo.Terr.BAND_8MHZ},
    };
    private int defTerrTpInfo [][]=
        {
            /*
            {1, 58000, TpInfo.Terr.BAND_8MHZ}, {2, 66000, TpInfo.Terr.BAND_8MHZ},
            {3, 74000, TpInfo.Terr.BAND_8MHZ}, {4, 82000, TpInfo.Terr.BAND_8MHZ},
            {5, 90000, TpInfo.Terr.BAND_8MHZ}, {6, 98000, TpInfo.Terr.BAND_8MHZ},
            {7, 106000, TpInfo.Terr.BAND_8MHZ}, {8, 114000, TpInfo.Terr.BAND_8MHZ},
            {9, 122000, TpInfo.Terr.BAND_8MHZ}, {10, 130000, TpInfo.Terr.BAND_8MHZ},
            */
            {5, 177, TpInfo.Terr.BAND_7MHZ}, {6, 184, TpInfo.Terr.BAND_7MHZ},
            {7, 191, TpInfo.Terr.BAND_7MHZ}, {8, 198, TpInfo.Terr.BAND_7MHZ},
            {9, 205, TpInfo.Terr.BAND_7MHZ}, {10, 212, TpInfo.Terr.BAND_7MHZ},
            {11, 219, TpInfo.Terr.BAND_7MHZ}, {12, 226, TpInfo.Terr.BAND_7MHZ},
            {21, 474, TpInfo.Terr.BAND_8MHZ}, {22, 482, TpInfo.Terr.BAND_8MHZ},
            {23, 490, TpInfo.Terr.BAND_8MHZ}, {24, 498, TpInfo.Terr.BAND_8MHZ},
            {25, 506, TpInfo.Terr.BAND_8MHZ}, {26, 514, TpInfo.Terr.BAND_8MHZ},
            {27, 522, TpInfo.Terr.BAND_8MHZ}, {28, 530, TpInfo.Terr.BAND_8MHZ},
            {29, 538, TpInfo.Terr.BAND_8MHZ}, {30, 546, TpInfo.Terr.BAND_8MHZ},
            {31, 554, TpInfo.Terr.BAND_8MHZ}, {32, 562, TpInfo.Terr.BAND_8MHZ},
            {33, 570, TpInfo.Terr.BAND_8MHZ}, {34, 578, TpInfo.Terr.BAND_8MHZ},
            {35, 586, TpInfo.Terr.BAND_8MHZ}, {36, 594, TpInfo.Terr.BAND_8MHZ},
            {37, 602, TpInfo.Terr.BAND_8MHZ}, {38, 610, TpInfo.Terr.BAND_8MHZ},
            {39, 618, TpInfo.Terr.BAND_8MHZ}, {40, 626, TpInfo.Terr.BAND_8MHZ},
            {41, 634, TpInfo.Terr.BAND_8MHZ}, {42, 642, TpInfo.Terr.BAND_8MHZ},
            {43, 650, TpInfo.Terr.BAND_8MHZ}, {44, 658, TpInfo.Terr.BAND_8MHZ},
            {45, 666, TpInfo.Terr.BAND_8MHZ}, {46, 674, TpInfo.Terr.BAND_8MHZ},
            {47, 682, TpInfo.Terr.BAND_8MHZ}, {48, 690, TpInfo.Terr.BAND_8MHZ},
            {49, 698, TpInfo.Terr.BAND_8MHZ}, {50, 706, TpInfo.Terr.BAND_8MHZ},
            {51, 714, TpInfo.Terr.BAND_8MHZ}, {52, 722, TpInfo.Terr.BAND_8MHZ},
            {53, 730, TpInfo.Terr.BAND_8MHZ}, {54, 738, TpInfo.Terr.BAND_8MHZ},
            {55, 746, TpInfo.Terr.BAND_8MHZ}, {56, 754, TpInfo.Terr.BAND_8MHZ},
            {57, 762, TpInfo.Terr.BAND_8MHZ}, {58, 770, TpInfo.Terr.BAND_8MHZ},
            {59, 778, TpInfo.Terr.BAND_8MHZ}, {60, 786, TpInfo.Terr.BAND_8MHZ},
            {61, 794, TpInfo.Terr.BAND_8MHZ}, {62, 802, TpInfo.Terr.BAND_8MHZ},
            {63, 810, TpInfo.Terr.BAND_8MHZ}, {64, 818, TpInfo.Terr.BAND_8MHZ},
            {65, 826, TpInfo.Terr.BAND_8MHZ}, {66, 834, TpInfo.Terr.BAND_8MHZ},
            {67, 842, TpInfo.Terr.BAND_8MHZ}, {68, 850, TpInfo.Terr.BAND_8MHZ},
            {69, 858, TpInfo.Terr.BAND_8MHZ},
        };
    private int defDVBSTpInfo [][] =
    {
        //Astra 19.2    satid, freq, polar, symbol
        {0,10714,TpInfo.Sat.POLAR_H,22000}, {0,10729,TpInfo.Sat.POLAR_V,22000},
        {0,10743,TpInfo.Sat.POLAR_H,22000}, {0,10758,TpInfo.Sat.POLAR_V,22000},
        {0,10773,TpInfo.Sat.POLAR_H,22000}, {0,10788,TpInfo.Sat.POLAR_V,22000},
        {0,10802,TpInfo.Sat.POLAR_H,22000}, {0,10817,TpInfo.Sat.POLAR_V,22000},
        {0,10832,TpInfo.Sat.POLAR_H,22000}, {0,10847,TpInfo.Sat.POLAR_V,22000},
        {0,10861,TpInfo.Sat.POLAR_H,22000}, {0,10876,TpInfo.Sat.POLAR_V,22000},
        {0,10891,TpInfo.Sat.POLAR_H,22000}, {0,10906,TpInfo.Sat.POLAR_V,22000},
        {0,10920,TpInfo.Sat.POLAR_H,22000}, {0,10935,TpInfo.Sat.POLAR_V,22000},
        {0,10964,TpInfo.Sat.POLAR_H,22000}, {0,10979,TpInfo.Sat.POLAR_V,22000},
        {0,10993,TpInfo.Sat.POLAR_H,22000}, {0,11008,TpInfo.Sat.POLAR_V,23500},
        {0,11023,TpInfo.Sat.POLAR_H,22000}, {0,11038,TpInfo.Sat.POLAR_V,22000},
        {0,11038,TpInfo.Sat.POLAR_V,23000}, {0,11052,TpInfo.Sat.POLAR_H,22000},
        {0,11067,TpInfo.Sat.POLAR_V,22000}, {0,11082,TpInfo.Sat.POLAR_H,22000},
        {0,11097,TpInfo.Sat.POLAR_V,22000}, {0,11111,TpInfo.Sat.POLAR_H,22000},
        {0,11112,TpInfo.Sat.POLAR_H,22000}, {0,11126,TpInfo.Sat.POLAR_V,22000},

        //Hotbird 13.0
        {1,10719,TpInfo.Sat.POLAR_V,27500}, {1,10722,TpInfo.Sat.POLAR_H,29900},
        {1,10758,TpInfo.Sat.POLAR_V,27500}, {1,10775,TpInfo.Sat.POLAR_H,29900},
        {1,10796,TpInfo.Sat.POLAR_V,27500}, {1,10815,TpInfo.Sat.POLAR_H,27500},
        {1,10834,TpInfo.Sat.POLAR_V,27500}, {1,10853,TpInfo.Sat.POLAR_H,29900},
        {1,10873,TpInfo.Sat.POLAR_V,27500}, {1,10892,TpInfo.Sat.POLAR_H,27500},

        //Astra 23.5
        {2,11719,TpInfo.Sat.POLAR_H,27500}, {2,11739,TpInfo.Sat.POLAR_V,27500},
        {2,11778,TpInfo.Sat.POLAR_V,27500}, {2,11797,TpInfo.Sat.POLAR_H,29500},
        {2,11817,TpInfo.Sat.POLAR_V,27500}, {2,11836,TpInfo.Sat.POLAR_H,27500},
        {2,11856,TpInfo.Sat.POLAR_V,27500}, {2,11875,TpInfo.Sat.POLAR_H,29900},
        {2,11895,TpInfo.Sat.POLAR_V,27500}, {2,11914,TpInfo.Sat.POLAR_H,29900},
    };

    // Constructor
    public TestData(int TunerType){
        if(init_flag == 0) {
            init_flag = 1;
            switch (TunerType) {
                case TpInfo.DVBC: {
                    //Antenna
                    //SetTestDataAntInfo(TunerType);
                    //print test ant info
                    //PrintTestDataAntInfo(TestAnt);

                    //Satellite
                    SetTestDataSatInfo(TunerType);
                    //print test sat info
                    //PrintTestDataSatInfo(TestSat);

                    //TP
                    SetTestDataTpInfo(TunerType);
                    //print test tp list
                    //PrintTestDataTpInfo(TunerType);

                    //ProgramInfo
                    SetTestDataProgramInfo(TunerType);
                    //print test program list
                    PrintTestDataProgramInfo(TunerType);

                    //BookInfo
                    SetTestDataBookInfo();

                    //GposInfo
                    SetTestDataGposInfo();

                    //EPGEvent
                    SetTestDataEPGEvent();

                    //FavGroupName
                    SetTestDataFavGroupName(TunerType);
                    //print test fav group name
                    PrintTestDataFavGroupName(TunerType);

                    //FavInfo
                    //SetTestDataFavInfo(TunerType);
                    //print test fav info
                    //PrintTestDataFavInfo(TunerType);

                    //set Default channel
                    SetTestDataDefaultChannel();

                }break;
                case TpInfo.DVBT:
                {
                    //Antenna
                    //SetTestDataAntInfo(TunerType);
                    //print test ant info
                    //PrintTestDataAntInfo(TestAnt);

                    //Satellite
                    SetTestDataSatInfo(TunerType);
                    //print test sat info
                    //PrintTestDataSatInfo(TestSat);

                    //TP
                    SetTestDataTpInfo(TunerType);
                    //print test tp list
                    //PrintTestDataTpInfo(TunerType);

                    //ProgramInfo
                    SetTestDataProgramInfo(TunerType);
                    //print test program list
                    PrintTestDataProgramInfo(TunerType);

                    //BookInfo
                    SetTestDataBookInfo();

                    //GposInfo
                    SetTestDataGposInfo();

                    //EPGEvent
                    SetTestDataEPGEvent();

                    //FavGroupName
                    SetTestDataFavGroupName(TunerType);
                    //print test fav group name
                    PrintTestDataFavGroupName(TunerType);

                    //set Default channel
                    SetTestDataDefaultChannel();
                }break;
                case TpInfo.ISDBT:
                {
                    //Antenna
                    //SetTestDataAntInfo(TunerType);
                    //print test ant info
                    //PrintTestDataAntInfo(TestAnt);

                    //Satellite
                    SetTestDataSatInfo(TunerType);
                    //print test sat info
                    //PrintTestDataSatInfo(TestSat);

                    //TP
                    SetTestDataTpInfo(TunerType);
                    //print test tp list
                    //PrintTestDataTpInfo(TunerType);

                    //ProgramInfo
                    SetTestDataProgramInfo(TunerType);
                    //print test program list
                    PrintTestDataProgramInfo(TunerType);

                    //BookInfo
                    SetTestDataBookInfo();

                    //GposInfo
                    SetTestDataGposInfo();

                    //EPGEvent
                    SetTestDataEPGEvent();

                    //FavGroupName
                    SetTestDataFavGroupName(TunerType);
                    //print test fav group name
                    PrintTestDataFavGroupName(TunerType);

                    //set Default channel
                    SetTestDataDefaultChannel();
                }break;
                case TpInfo.DVBS:
                {
                    //Antenna
                    //SetTestDataAntInfo(TunerType);
                    //print test ant info
                    //PrintTestDataAntInfo(TestAnt);

                    //Satellite
                    SetTestDataSatInfo(TunerType);
                    //print test sat info
                    //PrintTestDataSatInfo(TestSat);

                    //TP
                    SetTestDataTpInfo(TunerType);
                    //print test tp list
                    //PrintTestDataTpInfo(TunerType);

                    //ProgramInfo
                    SetTestDataProgramInfo(TunerType);
                    //print test program list
                    PrintTestDataProgramInfo(TunerType);

                    //BookInfo
                    SetTestDataBookInfo();

                    //GposInfo
                    SetTestDataGposInfo();

                    //EPGEvent
                    SetTestDataEPGEvent();

                    //FavGroupName
                    SetTestDataFavGroupName(TunerType);
                    //print test fav group name
                    PrintTestDataFavGroupName(TunerType);

                    //set Default channel
                    SetTestDataDefaultChannel();
                }break;
                default:
                    break;
            }
        }
    }


    void SetTestDataTpInfo(int TunerType)
    {
        mTestDataTpInfo = new ArrayList<>();
        switch (TunerType)
        {
            case TpInfo.DVBC: {

                int count = 0;
                //Log.d(TAG, "defCableTpInfo.length="+defCableTpInfo.length);
                for(int i = 0; i < defCableTpInfo.length ; i++, count++) {
                    TpInfo tpInfo = new TpInfo(TunerType);
                    tpInfo.setSatId(0);
                    tpInfo.setTpId(i);

                    //CableTp
                    tpInfo.CableTp.setChannel(defCableTpInfo[i][0]);
                    tpInfo.CableTp.setFreq(defCableTpInfo[i][1]);
                    tpInfo.CableTp.setSymbol(defCableTpInfo[i][2]);
                    tpInfo.CableTp.setQam(defCableTpInfo[i][3]);
                    mTestDataTpInfo.add(tpInfo);
                }

                // for ISDBT
                /*
                for(int i = 0 ; i < defISDBTTpInfo.length ; i++, count++) {
                    TpInfo tpInfo = new TpInfo(TpInfo.ISDBT);
                    tpInfo.setSatId(1);
                    tpInfo.setTpId(count);

                    //ISDBT
                    tpInfo.TerrTp.setChannel(defISDBTTpInfo[i][0]);
                    tpInfo.TerrTp.setFreq(defISDBTTpInfo[i][1]);
                    tpInfo.TerrTp.setBand(defISDBTTpInfo[i][2]);
                    mTestDataTpInfo.add(tpInfo);
                }
                */
            }break;
            case TpInfo.DVBT:
            {
                for(int i = 0; i < defTerrTpInfo.length ; i++) {
                    TpInfo tpInfo = new TpInfo(TunerType);
                    tpInfo.setSatId(0);
                    tpInfo.setTpId(i);
                    tpInfo.setTunerType(TunerType);
                    //TerrTp
                    tpInfo.TerrTp.setChannel(defTerrTpInfo[i][0]);
                    tpInfo.TerrTp.setFreq(defTerrTpInfo[i][1]);
                    tpInfo.TerrTp.setBand(defTerrTpInfo[i][2]);
                    mTestDataTpInfo.add(tpInfo);
                }
            }break;
            case TpInfo.ISDBT:
            {
                for(int i = 0 ; i < defISDBTTpInfo.length ; i++) {
                    TpInfo tpInfo = new TpInfo(TunerType);
                    tpInfo.setSatId(0);
                    tpInfo.setTpId(i);

                    //ISDBT
                    tpInfo.TerrTp.setChannel(defISDBTTpInfo[i][0]);
                    tpInfo.TerrTp.setFreq(defISDBTTpInfo[i][1]);
                    tpInfo.TerrTp.setBand(defISDBTTpInfo[i][2]);
                    mTestDataTpInfo.add(tpInfo);
                }
            }break;
            case TpInfo.DVBS:
            {
                for(int i = 0 ; i < defDVBSTpInfo.length ; i++) {
                    TpInfo tpInfo = new TpInfo(TpInfo.DVBS);
                    tpInfo.setTpId(i);
                    tpInfo.setSatId(defDVBSTpInfo[i][0]);
                    tpInfo.SatTp.setFreq(defDVBSTpInfo[i][1]);
                    tpInfo.SatTp.setPolar(defDVBSTpInfo[i][2]);
                    tpInfo.SatTp.setSymbol(defDVBSTpInfo[i][3]);
                    mTestDataTpInfo.add(tpInfo);
                }
            }break;
            default:
                break;
        }


    }

    void PrintTestDataTpInfo(int TunerType){
        switch (TunerType)
        {
            case TpInfo.DVBC: {

                //Log.d(TAG, "defCableTpInfo.length="+defCableTpInfo.length);
                for(int i = 0; i < mTestDataTpInfo.size() ; i++) {
                    Log.d(TAG, "PrintTestDataTpInfo:  index="+i+": SatId="+mTestDataTpInfo.get(i).getSatId()
                            +", TpId="+mTestDataTpInfo.get(i).getTpId()
                            +", TunerType="+mTestDataTpInfo.get(i).getTunerType()
                            +", Channel="+mTestDataTpInfo.get(i).CableTp.getChannel()
                            +", Freq="+mTestDataTpInfo.get(i).CableTp.getFreq()
                            +", Symbol="+mTestDataTpInfo.get(i).CableTp.getSymbol()
                            +", Qam="+mTestDataTpInfo.get(i).CableTp.getQam()
                    );
                }
            }break;
            case TpInfo.DVBT:
                Log.d(TAG, "defTerrTpInfo.length="+defTerrTpInfo.length);
                for(int i = 0; i < mTestDataTpInfo.size() ; i++) {
                    Log.d(TAG, "PrintTestDataTpInfo:  index="+i+": SatId="+mTestDataTpInfo.get(i).getSatId()
                            +", TpId="+mTestDataTpInfo.get(i).getTpId()
                            +", TunerType="+mTestDataTpInfo.get(i).getTunerType()
                            +", Channel="+mTestDataTpInfo.get(i).TerrTp.getChannel()
                            +", Freq="+mTestDataTpInfo.get(i).TerrTp.getFreq()
                            +", Bandwidth="+mTestDataTpInfo.get(i).TerrTp.getBand()
                    );
                }
                break;
            case TpInfo.DVBS:
                break;
            default:
                break;
        }
    }

    void SetTestDataProgramInfo(int TunerType)
    {
        ProgramInfo programInfo=null;
        switch (TunerType)
        {
            case TpInfo.DVBC:
            case TpInfo.ISDBT:
            {
                //Program 0
                programInfo = new ProgramInfo();
                programInfo.setServiceId(1010);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(1);
                programInfo.setDisplayName("SVT1");
                //programInfo.setLCN(1);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(76);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(1019);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(1021);
                programInfo.setOriginalNetworkId(8945);
                programInfo.setNetworkId(12660);
                programInfo.pVideo.setPID(1019);
                programInfo.pVideo.setCodec(0);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //TestProgramList[i].Audio[0] = TestProgramList[i].new Audio();   // johnny add 20171122
                //AudioList.clear();
                //ProgramInfo.AudioInfo audio = new ProgramInfo.AudioInfo(1018, 1, "", "");
                //AudioList.add(new ProgramInfo.AudioInfo(1028, 1, "", ""));
                //TestProgramList[i].pAudios = AudioList;
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(1018, 0, "eng", "fre"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(500, 1, "swe", "eng"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                //Teletext
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 1, "swe", 1, 0));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 2, "swe", 0x6, 0x91));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 5, "swe", 0x7, 0x91));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 2, "swe", 0x6, 0x91));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 2, "swe", 0x6, 0x91));
                //Subtitle
                programInfo.pSubtitle.add(new ProgramInfo.SubtitleInfo(
                        0x3f7, "swe", 2, 2));
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 1
                programInfo = new ProgramInfo();
                programInfo.setServiceId(5100);
                programInfo.setType(ProgramInfo.ALL_RADIO_TYPE);
                programInfo.setDisplayNum(2);
                programInfo.setDisplayName("TestRadio1");
                //programInfo.setLCN(2);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(76);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(400);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(1021);
                programInfo.setOriginalNetworkId(8945);
                programInfo.setNetworkId(12660);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(400);
                programInfo.pVideo.setCodec(1);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //AudioList.clear();
                //ProgramInfo.AudioInfo audio = new ProgramInfo.AudioInfo(1028, 1, "", "");
                //AudioList.add(new ProgramInfo.AudioInfo(1028, 1, "", ""));
                //programInfo.pAudios = AudioList;
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(410, 1, "fre", "swe"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(501, 0, "swe", "eng"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataRadioProgramInfo.add(programInfo);

                //Program 2
                programInfo = new ProgramInfo();
                programInfo.setServiceId(5060);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(3);
                programInfo.setDisplayName("SVT2 med ABC");
                //programInfo.setLCN(3);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(76);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(1029);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(1021);
                programInfo.setOriginalNetworkId(8945);
                programInfo.setNetworkId(12660);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(1029);
                programInfo.pVideo.setCodec(2);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //AudioList.clear();
                //ProgramInfo.AudioInfo audio = new ProgramInfo.AudioInfo(1028, 1, "", "");
                //AudioList.add(new ProgramInfo.AudioInfo(1028, 1, "", ""));
                //programInfo.pAudios = AudioList;
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(1028, 1, "eng", "fre"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(502, 0, "fre", "eng"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(503, 1, "swe", "fre"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                //Teletext
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        0x400, 1, "fre", 1, 0));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        0x400, 2, "fre", 0x6, 0x92));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        0x400, 5, "fre", 0x7, 0x92));
                //Subtitle
                programInfo.pSubtitle.add(new ProgramInfo.SubtitleInfo(
                        0x3f8, "fre", 3, 3));
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 3
                programInfo = new ProgramInfo();
                programInfo.setServiceId(5101);
                programInfo.setType(ProgramInfo.ALL_RADIO_TYPE);
                programInfo.setDisplayNum(4);
                programInfo.setDisplayName("TestRadio2");
                //programInfo.setLCN(4);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(76);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(404);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(1021);
                programInfo.setOriginalNetworkId(8945);
                programInfo.setNetworkId(12660);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(401);
                programInfo.pVideo.setCodec(0);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //AudioList.clear();
                //ProgramInfo.AudioInfo audio = new ProgramInfo.AudioInfo(1028, 1, "", "");
                //AudioList.add(new ProgramInfo.AudioInfo(1028, 1, "", ""));
                //programInfo.pAudios = AudioList;
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(411, 1, "fre", "eng"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(504, 0, "swe", "fre"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataRadioProgramInfo.add(programInfo);

                //Program 4
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(880);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(97);
                programInfo.setDisplayName("SVT Extra");
                //programInfo.setLCN(97);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(76);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(8191);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(1021);
                programInfo.setOriginalNetworkId(8945);
                programInfo.setNetworkId(12660);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(889);
                programInfo.pVideo.setCodec(1);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //AudioList.clear();
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //AudioList.add(new ProgramInfo.AudioInfo(888, 1, "", ""));
                //programInfo.pAudios = AudioList;
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(1028, 1, "eng", "fre"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(505, 0, "swe", "eng"));
                //programInfo.Audio[0].setPID(888);
                //programInfo.Audio[0].setCodec(1);
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 5
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(870);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(98);
                programInfo.setDisplayName("Barnkanalen");
                //programInfo.setLCN(98);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(76);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(879);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(1021);
                programInfo.setOriginalNetworkId(8945);
                programInfo.setNetworkId(12660);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(879);
                programInfo.pVideo.setCodec(1);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(878);
                //programInfo.Audio[0].setCodec(1);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(878, 1, "fre", "eng"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(506, 0, "swe", "fre"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 6
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(5170);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(99);
                programInfo.setDisplayName("24 ABC");
                //programInfo.setLCN(99);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(76);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(1249);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(1021);
                programInfo.setOriginalNetworkId(8945);
                programInfo.setNetworkId(12660);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(1249);
                programInfo.pVideo.setCodec(1);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(1248);
                //programInfo.Audio[0].setCodec(1);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(1248, 1, "swe", "eng"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(507, 0, "eng", "fre"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 7
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(5102);
                programInfo.setType(ProgramInfo.ALL_RADIO_TYPE);
                programInfo.setDisplayNum(301);
                programInfo.setDisplayName("TestRadio3");
                //programInfo.setLCN(301);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(77);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(402);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(67);
                programInfo.setOriginalNetworkId(88);
                programInfo.setNetworkId(88);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(402);
                programInfo.pVideo.setCodec(0);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(8191);
                //programInfo.Audio[0].setCodec(0);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(412, 0, "", ""));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(508, 1, "eng", "fre"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataRadioProgramInfo.add(programInfo);

                //Program 8
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(6716);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(100);
                programInfo.setDisplayName("RS208");
                //programInfo.setLCN(100);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(77);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(8191);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(67);
                programInfo.setOriginalNetworkId(88);
                programInfo.setNetworkId(88);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(8191);
                programInfo.pVideo.setCodec(0);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(8191);
                //programInfo.Audio[0].setCodec(0);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(8191, 0, "", ""));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(509, 1, "fre", "eng"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 9
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(6717);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(101);
                programInfo.setDisplayName("RS209");
                //programInfo.setLCN(101);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(77);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(8191);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(67);
                programInfo.setOriginalNetworkId(88);
                programInfo.setNetworkId(88);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(8191);
                programInfo.pVideo.setCodec(0);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(8191);
                //programInfo.Audio[0].setCodec(0);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(8191, 0, "", ""));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(510, 1, "fre", "eng"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 10
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(6718);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(102);
                programInfo.setDisplayName("TGN2");
                //programInfo.setLCN(102);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(1);
                programInfo.setTpId(77);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(519);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(67);
                programInfo.setOriginalNetworkId(88);
                programInfo.setNetworkId(88);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(519);
                programInfo.pVideo.setCodec(2);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(720);
                //programInfo.Audio[0].setCodec(1);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(720, 1, "eng", "fre"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(511, 0, "fre", "eng"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                //Teletext
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 1, "eng", 1, 0));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 2, "eng", 0x6, 0x91));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 5, "eng", 0x7, 0x91));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 2, "eng", 0x6, 0x91));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 2, "eng", 0x6, 0x91));
                //Subtitle
                programInfo.pSubtitle.add(new ProgramInfo.SubtitleInfo(
                        0x3f7, "eng", 2, 2));
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 11
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(6719);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(103);
                programInfo.setDisplayName("TGN");
                //programInfo.setLCN(103);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(77);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(520);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(67);
                programInfo.setOriginalNetworkId(88);
                programInfo.setNetworkId(88);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(520);
                programInfo.pVideo.setCodec(0);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(730);
                //programInfo.Audio[0].setCodec(1);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(730, 1, "fre", "swe"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(512, 0, "fre", "eng"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                //Teletext
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        0x400, 1, "fre", 1, 0));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        0x400, 2, "fre", 0x6, 0x92));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        0x400, 5, "fre", 0x7, 0x92));
                //Subtitle
                programInfo.pSubtitle.add(new ProgramInfo.SubtitleInfo(
                        0x3f8, "fre", 3, 3));
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 12
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(6720);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(104);
                programInfo.setDisplayName("ETV");
                //programInfo.setLCN(104);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(1);
                programInfo.setTpId(77);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(521);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(67);
                programInfo.setOriginalNetworkId(88);
                programInfo.setNetworkId(88);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(521);
                programInfo.pVideo.setCodec(1);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(740);
                //programInfo.Audio[0].setCodec(1);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(740, 1, "", ""));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(513, 0, "fre", "eng"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 13
                programInfo = new ProgramInfo();
                programInfo.setServiceId(5103);
                programInfo.setType(ProgramInfo.ALL_RADIO_TYPE);
                programInfo.setDisplayNum(302);
                programInfo.setDisplayName("TestRadio4");
                //programInfo.setLCN(302);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(77);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(403);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(67);
                programInfo.setOriginalNetworkId(88);
                programInfo.setNetworkId(88);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(403);
                programInfo.pVideo.setCodec(1);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(740);
                //programInfo.Audio[0].setCodec(1);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(413, 0, "", ""));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(514, 1, "fre", "eng"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataRadioProgramInfo.add(programInfo);

            }break;            
            case TpInfo.DVBS:
            {
                //Program 0
                programInfo = new ProgramInfo();
                programInfo.setServiceId(1010);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(1);
                programInfo.setDisplayName("SVT1");
                //programInfo.setLCN(1);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(76);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(1019);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(1021);
                programInfo.setOriginalNetworkId(8945);
                programInfo.setNetworkId(12660);
                programInfo.pVideo.setPID(1019);
                programInfo.pVideo.setCodec(0);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //TestProgramList[i].Audio[0] = TestProgramList[i].new Audio();   // johnny add 20171122
                //AudioList.clear();
                //ProgramInfo.AudioInfo audio = new ProgramInfo.AudioInfo(1018, 1, "", "");
                //AudioList.add(new ProgramInfo.AudioInfo(1028, 1, "", ""));
                //TestProgramList[i].pAudios = AudioList;
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(1018, 0, "eng", "fre"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(500, 1, "swe", "eng"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                //Teletext
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 1, "swe", 1, 0));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 2, "swe", 0x6, 0x91));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 5, "swe", 0x7, 0x91));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 2, "swe", 0x6, 0x91));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 2, "swe", 0x6, 0x91));
                //Subtitle
                programInfo.pSubtitle.add(new ProgramInfo.SubtitleInfo(
                        0x3f7, "swe", 2, 2));
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 1
                programInfo = new ProgramInfo();
                programInfo.setServiceId(5100);
                programInfo.setType(ProgramInfo.ALL_RADIO_TYPE);
                programInfo.setDisplayNum(2);
                programInfo.setDisplayName("TestRadio1");
                //programInfo.setLCN(2);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(76);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(400);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(1021);
                programInfo.setOriginalNetworkId(8945);
                programInfo.setNetworkId(12660);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(400);
                programInfo.pVideo.setCodec(1);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //AudioList.clear();
                //ProgramInfo.AudioInfo audio = new ProgramInfo.AudioInfo(1028, 1, "", "");
                //AudioList.add(new ProgramInfo.AudioInfo(1028, 1, "", ""));
                //programInfo.pAudios = AudioList;
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(410, 1, "fre", "swe"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(501, 0, "swe", "eng"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataRadioProgramInfo.add(programInfo);

                //Program 2
                programInfo = new ProgramInfo();
                programInfo.setServiceId(5060);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(3);
                programInfo.setDisplayName("SVT2 med ABC");
                //programInfo.setLCN(3);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(76);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(1029);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(1021);
                programInfo.setOriginalNetworkId(8945);
                programInfo.setNetworkId(12660);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(1029);
                programInfo.pVideo.setCodec(2);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //AudioList.clear();
                //ProgramInfo.AudioInfo audio = new ProgramInfo.AudioInfo(1028, 1, "", "");
                //AudioList.add(new ProgramInfo.AudioInfo(1028, 1, "", ""));
                //programInfo.pAudios = AudioList;
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(1028, 1, "eng", "fre"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(502, 0, "fre", "eng"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(503, 1, "swe", "fre"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                //Teletext
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        0x400, 1, "fre", 1, 0));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        0x400, 2, "fre", 0x6, 0x92));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        0x400, 5, "fre", 0x7, 0x92));
                //Subtitle
                programInfo.pSubtitle.add(new ProgramInfo.SubtitleInfo(
                        0x3f8, "fre", 3, 3));
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 3
                programInfo = new ProgramInfo();
                programInfo.setServiceId(5101);
                programInfo.setType(ProgramInfo.ALL_RADIO_TYPE);
                programInfo.setDisplayNum(4);
                programInfo.setDisplayName("TestRadio2");
                //programInfo.setLCN(4);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(76);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(404);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(1021);
                programInfo.setOriginalNetworkId(8945);
                programInfo.setNetworkId(12660);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(401);
                programInfo.pVideo.setCodec(0);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //AudioList.clear();
                //ProgramInfo.AudioInfo audio = new ProgramInfo.AudioInfo(1028, 1, "", "");
                //AudioList.add(new ProgramInfo.AudioInfo(1028, 1, "", ""));
                //programInfo.pAudios = AudioList;
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(411, 1, "fre", "eng"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(504, 0, "swe", "fre"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataRadioProgramInfo.add(programInfo);

                //Program 4
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(880);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(97);
                programInfo.setDisplayName("SVT Extra");
                //programInfo.setLCN(97);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(76);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(8191);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(1021);
                programInfo.setOriginalNetworkId(8945);
                programInfo.setNetworkId(12660);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(889);
                programInfo.pVideo.setCodec(1);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //AudioList.clear();
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //AudioList.add(new ProgramInfo.AudioInfo(888, 1, "", ""));
                //programInfo.pAudios = AudioList;
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(1028, 1, "eng", "fre"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(505, 0, "swe", "eng"));
                //programInfo.Audio[0].setPID(888);
                //programInfo.Audio[0].setCodec(1);
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 5
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(870);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(98);
                programInfo.setDisplayName("Barnkanalen");
                //programInfo.setLCN(98);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(76);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(879);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(1021);
                programInfo.setOriginalNetworkId(8945);
                programInfo.setNetworkId(12660);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(879);
                programInfo.pVideo.setCodec(1);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(878);
                //programInfo.Audio[0].setCodec(1);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(878, 1, "fre", "eng"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(506, 0, "swe", "fre"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 6
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(5170);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(99);
                programInfo.setDisplayName("24 ABC");
                //programInfo.setLCN(99);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(76);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(1249);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(1021);
                programInfo.setOriginalNetworkId(8945);
                programInfo.setNetworkId(12660);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(1249);
                programInfo.pVideo.setCodec(1);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(1248);
                //programInfo.Audio[0].setCodec(1);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(1248, 1, "swe", "eng"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(507, 0, "eng", "fre"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 7
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(5102);
                programInfo.setType(ProgramInfo.ALL_RADIO_TYPE);
                programInfo.setDisplayNum(301);
                programInfo.setDisplayName("TestRadio3");
                //programInfo.setLCN(301);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(77);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(402);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(67);
                programInfo.setOriginalNetworkId(88);
                programInfo.setNetworkId(88);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(402);
                programInfo.pVideo.setCodec(0);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(8191);
                //programInfo.Audio[0].setCodec(0);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(412, 0, "", ""));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(508, 1, "eng", "fre"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataRadioProgramInfo.add(programInfo);

                //Program 8
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(6716);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(100);
                programInfo.setDisplayName("RS208");
                //programInfo.setLCN(100);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(77);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(8191);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(67);
                programInfo.setOriginalNetworkId(88);
                programInfo.setNetworkId(88);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(8191);
                programInfo.pVideo.setCodec(0);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(8191);
                //programInfo.Audio[0].setCodec(0);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(8191, 0, "", ""));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(509, 1, "fre", "eng"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 9
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(6717);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(101);
                programInfo.setDisplayName("RS209");
                //programInfo.setLCN(101);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(77);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(8191);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(67);
                programInfo.setOriginalNetworkId(88);
                programInfo.setNetworkId(88);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(8191);
                programInfo.pVideo.setCodec(0);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(8191);
                //programInfo.Audio[0].setCodec(0);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(8191, 0, "", ""));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(510, 1, "fre", "eng"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 10
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(6718);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(102);
                programInfo.setDisplayName("TGN2");
                //programInfo.setLCN(102);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(1);
                programInfo.setTpId(77);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(519);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(67);
                programInfo.setOriginalNetworkId(88);
                programInfo.setNetworkId(88);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(519);
                programInfo.pVideo.setCodec(2);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(720);
                //programInfo.Audio[0].setCodec(1);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(720, 1, "eng", "fre"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(511, 0, "fre", "eng"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                //Teletext
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 1, "eng", 1, 0));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 2, "eng", 0x6, 0x91));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 5, "eng", 0x7, 0x91));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 2, "eng", 0x6, 0x91));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        1014, 2, "eng", 0x6, 0x91));
                //Subtitle
                programInfo.pSubtitle.add(new ProgramInfo.SubtitleInfo(
                        0x3f7, "eng", 2, 2));
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 11
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(6719);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(103);
                programInfo.setDisplayName("TGN");
                //programInfo.setLCN(103);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(77);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(520);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(67);
                programInfo.setOriginalNetworkId(88);
                programInfo.setNetworkId(88);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(520);
                programInfo.pVideo.setCodec(0);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(730);
                //programInfo.Audio[0].setCodec(1);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(730, 1, "fre", "swe"));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(512, 0, "fre", "eng"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                //Teletext
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        0x400, 1, "fre", 1, 0));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        0x400, 2, "fre", 0x6, 0x92));
                programInfo.pTeletext.add(new ProgramInfo.TeletextInfo(
                        0x400, 5, "fre", 0x7, 0x92));
                //Subtitle
                programInfo.pSubtitle.add(new ProgramInfo.SubtitleInfo(
                        0x3f8, "fre", 3, 3));
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 12
                programInfo = new ProgramInfo(); // johnny add 20171121
                programInfo.setServiceId(6720);
                programInfo.setType(ProgramInfo.PROGRAM_TV);
                programInfo.setDisplayNum(104);
                programInfo.setDisplayName("ETV");
                //programInfo.setLCN(104);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(1);
                programInfo.setTpId(77);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(521);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(67);
                programInfo.setOriginalNetworkId(88);
                programInfo.setNetworkId(88);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(521);
                programInfo.pVideo.setCodec(1);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(740);
                //programInfo.Audio[0].setCodec(1);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(740, 1, "", ""));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(513, 0, "fre", "eng"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataTvProgramInfo.add(programInfo);

                //Program 13
                programInfo = new ProgramInfo();
                programInfo.setServiceId(5103);
                programInfo.setType(ProgramInfo.ALL_RADIO_TYPE);
                programInfo.setDisplayNum(302);
                programInfo.setDisplayName("TestRadio4");
                //programInfo.setLCN(302);
                programInfo.setLock(0);
                programInfo.setSkip(0);
                programInfo.setCA(0);
                programInfo.setTpId(77);
                programInfo.setSatId(0);
                programInfo.setAntId(0);
                programInfo.setPcr(403);
                //public ProgramInfo.Teletext Teletext;
                programInfo.setTransportStreamId(67);
                programInfo.setOriginalNetworkId(88);
                programInfo.setNetworkId(88);
                //programInfo.Video = programInfo.new Video();  // johnny add 20171122
                programInfo.pVideo.setPID(403);
                programInfo.pVideo.setCodec(1);
                //public ProgramInfo.Audio[] Audio = new ProgramInfo.Audio[NUMBER_OF_AUDIO_IN_SIL];
                //programInfo.Audio[0] = programInfo.new Audio();   // johnny add 20171122
                //programInfo.Audio[0].setPID(740);
                //programInfo.Audio[0].setCodec(1);
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(413, 0, "", ""));
                programInfo.pAudios.add(new ProgramInfo.AudioInfo(514, 1, "fre", "eng"));
                programInfo.setAudioLRSelected(0);
                programInfo.setAudioSelected(0);
                //public ProgramInfo.Subtitle Subtitle;
                programInfo.setPmtVersion(0);
                programInfo.setTunerId(0);
                programInfo.setChannelId((programInfo.getTpId() << 16) | programInfo.getServiceId());
                mTestDataRadioProgramInfo.add(programInfo);
            }break;
            default:
                break;
        }
    }


    void PrintTestDataProgramInfo(int TunerType){

        switch (TunerType)
        {
            case TpInfo.DVBC: {
                int i=0,j=0;

                //Log.d(TAG, "defCableTpInfo.length="+defCableTpInfo.length);
                for(i=0; i < mTestDataTvProgramInfo.size() ; i++) {
                    Log.d(TAG, "PrintTestDataTvProgramInfo:  index="+i+": type="+mTestDataTvProgramInfo.get(i).getType()
                            +", ServiceId="+mTestDataTvProgramInfo.get(i).getServiceId()
                            +", ChNum="+mTestDataTvProgramInfo.get(i).getDisplayNum()
                            +", ChName="+mTestDataTvProgramInfo.get(i).getDisplayName()
                            +", TpId="+mTestDataTvProgramInfo.get(i).getTpId()
                    );
                }
                for(i=0; i < mTestDataRadioProgramInfo.size() ; i++) {
                    Log.d(TAG, "mTestDataRadioProgramInfo:  index="+i+": type="+mTestDataRadioProgramInfo.get(i).getType()
                            +", ServiceId="+mTestDataRadioProgramInfo.get(i).getServiceId()
                            +", ChNum="+mTestDataRadioProgramInfo.get(i).getDisplayNum()
                            +", ChName="+mTestDataRadioProgramInfo.get(i).getDisplayName()
                            +", TpId="+mTestDataRadioProgramInfo.get(i).getTpId()
                    );
                }
            }break;
            case TpInfo.DVBT:
                break;
            case TpInfo.DVBS:
                break;
            default:
                break;
        }
    }

    void SetTestDataSatInfo(int TunerType)
    {
        mTestDataSatInfo = new ArrayList<>();
        switch (TunerType)
        {
            case TpInfo.DVBC: {
                SatInfo satInfo = new SatInfo();
                satInfo.setSatId(0);
                satInfo.setSatName("DVBC Sat");
                satInfo.setTpNum(defCableTpInfo.length);
                satInfo.setAngle(0);
                satInfo.setLocation(0);
                satInfo.setPostionIndex(0);

                satInfo.Antenna.setLnb1(0);
                satInfo.Antenna.setLnb2(0);
                satInfo.Antenna.setLnbType(0);
                satInfo.Antenna.setDiseqcType(0);
                satInfo.Antenna.setDiseqcUse(0);
                satInfo.Antenna.setDiseqc(0);
                satInfo.Antenna.setTone22kUse(0);
                satInfo.Antenna.setTone22k(0);
                satInfo.Antenna.setV012Use(0);
                satInfo.Antenna.setV012(0);
                satInfo.Antenna.setV1418Use(0);
                satInfo.Antenna.setV1418(0);
                satInfo.Antenna.setCku(0);

                mTestDataSatInfo.add(satInfo);
                
            }break;
            case TpInfo.DVBT:
            {
                for ( int i = 0 ; i < TestSatNum ; i++ ) {
                    SatInfo satInfo = new SatInfo();
                    satInfo.setSatId(i);
                    satInfo.setSatName("DVBT Sat");
                    satInfo.setTpNum(defTerrTpInfo.length);
                satInfo.setAngle(0);
                satInfo.setLocation(0);
                satInfo.setPostionIndex(0);

                    List<Integer> tps = new ArrayList<>();
                    for (int j = 0; j < defTerrTpInfo.length; j++)
                    {
                        tps.add(j);
                    }

                    satInfo.setTps(tps);

                satInfo.Antenna.setLnb1(0);
                satInfo.Antenna.setLnb2(0);
                satInfo.Antenna.setLnbType(0);
                satInfo.Antenna.setDiseqcType(0);
                satInfo.Antenna.setDiseqcUse(0);
                satInfo.Antenna.setDiseqc(0);
                satInfo.Antenna.setTone22kUse(0);
                satInfo.Antenna.setTone22k(0);
                satInfo.Antenna.setV012Use(0);
                satInfo.Antenna.setV012(0);
                satInfo.Antenna.setV1418Use(0);
                satInfo.Antenna.setV1418(0);
                satInfo.Antenna.setCku(0);

                mTestDataSatInfo.add(satInfo);
                }
            }break;
            case TpInfo.ISDBT:
            {                 
                // for ISDBT
                    SatInfo satInfo = new SatInfo();
                satInfo = new SatInfo();
                satInfo.setSatId(1);
                satInfo.setSatName("ISDBT Sat");
                satInfo.setTpNum(defISDBTTpInfo.length);
                    satInfo.setAngle(0);
                    satInfo.setLocation(0);
                    satInfo.setPostionIndex(0);

                    satInfo.Antenna.setLnb1(0);
                    satInfo.Antenna.setLnb2(0);
                    satInfo.Antenna.setLnbType(0);
                    satInfo.Antenna.setDiseqcType(0);
                    satInfo.Antenna.setDiseqcUse(0);
                    satInfo.Antenna.setDiseqc(0);
                    satInfo.Antenna.setTone22kUse(0);
                    satInfo.Antenna.setTone22k(0);
                    satInfo.Antenna.setV012Use(0);
                    satInfo.Antenna.setV012(0);
                    satInfo.Antenna.setV1418Use(0);
                    satInfo.Antenna.setV1418(0);
                    satInfo.Antenna.setCku(0);

                    mTestDataSatInfo.add(satInfo);
            }break; 
            case TpInfo.DVBS:
            {
                SatInfo satInfo = new SatInfo();
                satInfo.setSatId(0);
                satInfo.setSatName("Astra");
                satInfo.setTpNum(defDVBSTpInfo.length);
                satInfo.setAngle((float)19.2);
                satInfo.setLocation(1);
                satInfo.setPostionIndex(0);
                satInfo.Antenna.setLnb1(0);
                satInfo.Antenna.setLnb2(0);
                satInfo.Antenna.setLnbType(0);
                satInfo.Antenna.setDiseqcType(0);
                satInfo.Antenna.setDiseqcUse(0);
                satInfo.Antenna.setDiseqc(0);
                satInfo.Antenna.setTone22kUse(0);
                satInfo.Antenna.setTone22k(0);
                satInfo.Antenna.setV012Use(0);
                satInfo.Antenna.setV012(0);
                satInfo.Antenna.setV1418Use(0);
                satInfo.Antenna.setV1418(0);
                satInfo.Antenna.setCku(0);
                mTestDataSatInfo.add(satInfo);

                satInfo = new SatInfo();
                satInfo.setSatId(1);
                satInfo.setSatName("Hot Bird");
                satInfo.setTpNum(defDVBSTpInfo.length);
                satInfo.setAngle((float)13.0);
                satInfo.setLocation(2);
                satInfo.setPostionIndex(0);
                satInfo.Antenna.setLnb1(0);
                satInfo.Antenna.setLnb2(0);
                satInfo.Antenna.setLnbType(1);
                satInfo.Antenna.setDiseqcType(1);
                satInfo.Antenna.setDiseqcUse(0);
                satInfo.Antenna.setDiseqc(1);
                satInfo.Antenna.setTone22kUse(0);
                satInfo.Antenna.setTone22k(1);
                satInfo.Antenna.setV012Use(0);
                satInfo.Antenna.setV012(0);
                satInfo.Antenna.setV1418Use(0);
                satInfo.Antenna.setV1418(0);
                satInfo.Antenna.setCku(0);
                mTestDataSatInfo.add(satInfo);

                satInfo = new SatInfo();
                satInfo.setSatId(2);
                satInfo.setSatName("Astra 3A");
                satInfo.setTpNum(defDVBSTpInfo.length);
                satInfo.setAngle((float)23.5);
                satInfo.setLocation(3);
                satInfo.setPostionIndex(0);
                satInfo.Antenna.setLnb1(0);
                satInfo.Antenna.setLnb2(0);
                satInfo.Antenna.setLnbType(2);
                satInfo.Antenna.setDiseqcType(0);
                satInfo.Antenna.setDiseqcUse(0);
                satInfo.Antenna.setDiseqc(2);
                satInfo.Antenna.setTone22kUse(0);
                satInfo.Antenna.setTone22k(2);
                satInfo.Antenna.setV012Use(0);
                satInfo.Antenna.setV012(0);
                satInfo.Antenna.setV1418Use(0);
                satInfo.Antenna.setV1418(0);
                satInfo.Antenna.setCku(0);
                mTestDataSatInfo.add(satInfo);
            }break;
            default:
                break;
        }
    }

    void PrintTestDataSatInfo(){
        for(int i = 0; i< mTestDataSatInfo.size(); i++)
        {
            Log.d(TAG, "PrintTestDataSatInfo:  index="+i
                    +": SatId="+mTestDataSatInfo.get(i).getSatId()
                    +", SatName="+mTestDataSatInfo.get(i).getSatName()
                    +", TpNum="+mTestDataSatInfo.get(i).getTpNum()
                    +", Angle="+mTestDataSatInfo.get(i).getAngle()
                    +", Location="+mTestDataSatInfo.get(i).getLocation()
                    +", PostionIndex="+mTestDataSatInfo.get(i).getPostionIndex()
            );
            for(int j = 0; j < mTestDataSatInfo.get(i).getTps().size(); j++){
                Log.d(TAG, "["+j
                        +"]="+mTestDataSatInfo.get(i).getTps().get(j));
            }
        }
    }

    /*void SetTestDataAntInfo(int TunerType)
    {
        mTestDataAntInfo = new ArrayList<>();
        switch (TunerType)
        {
            case TpInfo.DVBC: {

                AntInfo antInfo = new AntInfo();
                antInfo.setAntId(0);
                antInfo.setSatId(0);
                antInfo.setLnb1(0);
                antInfo.setLnb2(0);
                antInfo.setLnbType(0);
                antInfo.setDiseqcType(0);
                antInfo.setDiseqcUse(0);
                antInfo.setDiseqc(0);
                antInfo.setTone22kUse(0);
                antInfo.setTone22k(0);
                antInfo.setV012Use(0);
                antInfo.setV012(0);
                antInfo.setV1418Use(0);
                antInfo.setV1418(0);
                antInfo.setCku(0);
                mTestDataAntInfo.add(antInfo);

            }break;
            case TpInfo.DVBT:
            {
                for(int i = 0; i < TestAntNum ; i++)
                {
                AntInfo antInfo = new AntInfo();
                antInfo.setAntId(i);
                antInfo.setSatId(0);
                antInfo.setLnb1(0);
                antInfo.setLnb2(0);
                antInfo.setLnbType(0);
                antInfo.setDiseqcType(0);
                antInfo.setDiseqcUse(0);
                antInfo.setDiseqc(0);
                antInfo.setTone22kUse(0);
                antInfo.setTone22k(0);
                antInfo.setV012Use(0);
                antInfo.setV012(0);
                antInfo.setV1418Use(0);
                antInfo.setV1418(0);
                antInfo.setCku(0);

                mTestDataAntInfo.add(antInfo);
                }
            }break;
            case TpInfo.ISDBT:
            {

            }break;
            case TpInfo.DVBS:
            {
                AntInfo antInfo = new AntInfo();
                antInfo.setAntId(0);
                antInfo.setSatId(0);
                antInfo.setLnb1(0);
                antInfo.setLnb2(0);
                antInfo.setLnbType(0);
                antInfo.setDiseqcType(0);
                antInfo.setDiseqcUse(0);
                antInfo.setDiseqc(0);
                antInfo.setTone22kUse(0);
                antInfo.setTone22k(0);
                antInfo.setV012Use(0);
                antInfo.setV012(0);
                antInfo.setV1418Use(0);
                antInfo.setV1418(0);
                antInfo.setCku(0);
                mTestDataAntInfo.add(antInfo);

                antInfo = new AntInfo();
                antInfo.setAntId(1);
                antInfo.setSatId(1);
                antInfo.setLnb1(0);
                antInfo.setLnb2(0);
                antInfo.setLnbType(1);
                antInfo.setDiseqcType(1);
                antInfo.setDiseqcUse(0);
                antInfo.setDiseqc(1);
                antInfo.setTone22kUse(0);
                antInfo.setTone22k(1);
                antInfo.setV012Use(0);
                antInfo.setV012(0);
                antInfo.setV1418Use(0);
                antInfo.setV1418(0);
                antInfo.setCku(0);
                mTestDataAntInfo.add(antInfo);

                antInfo = new AntInfo();
                antInfo.setAntId(2);
                antInfo.setSatId(2);
                antInfo.setLnb1(0);
                antInfo.setLnb2(0);
                antInfo.setLnbType(2);
                antInfo.setDiseqcType(0);
                antInfo.setDiseqcUse(0);
                antInfo.setDiseqc(2);
                antInfo.setTone22kUse(0);
                antInfo.setTone22k(2);
                antInfo.setV012Use(0);
                antInfo.setV012(0);
                antInfo.setV1418Use(0);
                antInfo.setV1418(0);
                antInfo.setCku(0);
                mTestDataAntInfo.add(antInfo);
            }break;
            default:
                break;
        }
    }*/

    /*void PrintTestDataAntInfo()
    {
        for(int i=0; i< mTestDataAntInfo.size(); i++)
        {
            Log.d(TAG, "PrintTestDataAntInfo:  index="+i
                            +": AntId="+mTestDataAntInfo.get(i).getAntId()
                            +", SatId="+mTestDataAntInfo.get(i).getSatId()
                            +": Lnb1="+mTestDataAntInfo.get(i).getLnb1()
                            +", Lnb2="+mTestDataAntInfo.get(i).getLnb2()
                            +": LnbType="+mTestDataAntInfo.get(i).getLnbType()
                            +", DiseqcType="+mTestDataAntInfo.get(i).getDiseqcType()
                            +": DiseqcUse="+mTestDataAntInfo.get(i).getDiseqcUse()
                            +", Diseqc="+mTestDataAntInfo.get(i).getDiseqc()
                            +": Tone22kUse="+mTestDataAntInfo.get(i).getTone22kUse()
                            +", Tone22k="+mTestDataAntInfo.get(i).getTone22k()
                            +": V012Use="+mTestDataAntInfo.get(i).getV012Use()
                            +", V012="+mTestDataAntInfo.get(i).getV012()
                            +": V1418Use="+mTestDataAntInfo.get(i).getV1418Use()
                            +", V1418="+mTestDataAntInfo.get(i).getV1418()
                            +": Cku="+mTestDataAntInfo.get(i).getCku()
            );
        }
    }*/

    private void SetTestDataBookInfo()
    {
        final int CHTYPE_NUM = 2;
//        final int ChType_ALLRADIOCH = 7;
//        final int ChType_ALLTVCH = 0;
        final int BOOKTYPE_NUM = 2;
//        final int BookType_TURN_ON = 0;
//        final int BookType_RECORD = 1;
        final  int BOOKCYCLE_NUM = 5;
//        final int BookCycle_ONETIME = 0;
//        final int BookCycle_DAILY = 1;
//        final int BookCycle_WEEKLY = 2;
//        final int BookCycle_WEEKEND = 3;
//        final int BookCycle_WEEKDAYS = 4;
        final int ENABLE_NUM = 2;
//        final int Enable_YES = 0;
//        final int Enable_NO = 1;
        int count = 0;

        mTestDataBookInfo = new ArrayList<>();
        if(!mTestDataTvProgramInfo.isEmpty()) {
            int tvProgramCount = mTestDataTvProgramInfo.size();
            for (int i = 0; i < 10; i++, count++) {  // 10 TV program book
                BookInfo bookInfo = new BookInfo();
                bookInfo.setBookId(i);
                bookInfo.setChannelId(mTestDataTvProgramInfo.get(i%tvProgramCount).getChannelId());
                bookInfo.setGroupType(mTestDataTvProgramInfo.get(i%tvProgramCount).getType());
                bookInfo.setEventName("TV book " + i);
                bookInfo.setBookType(i % BOOKTYPE_NUM);
                bookInfo.setBookCycle(i % BOOKCYCLE_NUM);
                bookInfo.setYear(2017);
                bookInfo.setMonth(i % 12 + 1);  // 1~12
                bookInfo.setDate(i % 30 + 1);   // 1~30
                bookInfo.setWeek(i % 7 + 1);    // 1~7
                bookInfo.setStartTime(i % 24 * 100 + i % 60);   // 0~2359
                bookInfo.setDuration(i % 24 * 100 + (i+1) % 60);    // 1~2359
                bookInfo.setEnable(1);

                mTestDataBookInfo.add(bookInfo);
            }
        }

        if (!mTestDataRadioProgramInfo.isEmpty())
        {
            int radioProgramCount = mTestDataRadioProgramInfo.size();
            for (int i = count; i < 15; i++, count++) {  // 5 Radio program book
                BookInfo bookInfo = new BookInfo();
                bookInfo.setBookId(i);
                bookInfo.setChannelId(mTestDataRadioProgramInfo.get(i%radioProgramCount).getChannelId());
                bookInfo.setGroupType(mTestDataRadioProgramInfo.get(i%radioProgramCount).getType());
                bookInfo.setEventName("Radio book " + i);
                bookInfo.setBookType(i % BOOKTYPE_NUM);
                bookInfo.setBookCycle(i % BOOKCYCLE_NUM);
                bookInfo.setYear(2017);
                bookInfo.setMonth(i % 12 + 1);  // 1~12
                bookInfo.setDate(i % 30 + 1);   // 1~30
                bookInfo.setWeek(i % 7 + 1);    // 1~7
                bookInfo.setStartTime(i % 24 * 100 + i % 60);   // 0~2359
                bookInfo.setDuration(i % 24 * 100 + (i+1) % 60);    // 1~2359
                bookInfo.setEnable(1);

                mTestDataBookInfo.add(bookInfo);
            }
        }

    }

    private void SetTestDataGposInfo()
    {
        mTestDataGposInfo = new GposInfo();

        mTestDataGposInfo.setDBVersion(1);
        mTestDataGposInfo.setCurChannelId(mTestDataTvProgramInfo.get(0).getChannelId());
        mTestDataGposInfo.setCurGroupType(ProgramInfo.ALL_TV_TYPE);
        mTestDataGposInfo.setPasswordValue(0);
        mTestDataGposInfo.setParentalRate(0);
        mTestDataGposInfo.setParentalLockOnOff(0);
        mTestDataGposInfo.setInstallLockOnOff(0);
        mTestDataGposInfo.setBoxPowerStatus(0);
        mTestDataGposInfo.setStartOnChannelId(0);
        mTestDataGposInfo.setStartOnChType(0);
        mTestDataGposInfo.setVolume(7);
        mTestDataGposInfo.setAudioStereo(0);
        mTestDataGposInfo.setPalStandard(0);
        mTestDataGposInfo.setMonitorType(0);
        mTestDataGposInfo.setAutoRegionTimeOffset(0);
        mTestDataGposInfo.setRegionTimeOffset((float) 0);
        mTestDataGposInfo.setRegionSummerTime(0);
        mTestDataGposInfo.setLnbPower(0);
        mTestDataGposInfo.setScreen16x9(0);
        mTestDataGposInfo.setConversion(0);
        mTestDataGposInfo.setResolution(0);
        mTestDataGposInfo.setOSDLanguage("eng");
        mTestDataGposInfo.setSearchProgramType(0);
        mTestDataGposInfo.setSearchMode(0);
        mTestDataGposInfo.setAudioLanguageSelection(0,"eng");
        mTestDataGposInfo.setAudioLanguageSelection(1,"eng");
        mTestDataGposInfo.setSubtitleLanguageSelection(0,"eng");
        mTestDataGposInfo.setSubtitleLanguageSelection(1,"eng");
        mTestDataGposInfo.setSortByLcn(0);
        mTestDataGposInfo.setOSDTransparency(0);
        mTestDataGposInfo.setBannerTimeout(5);
        mTestDataGposInfo.setStandbyMode(0);
        mTestDataGposInfo.setHardHearing(0);
        mTestDataGposInfo.setAutoStandbyTime(0);
        mTestDataGposInfo.setDolbyMode(0);
        mTestDataGposInfo.setHDCPOnOff(0);
        mTestDataGposInfo.setDeepSleepMode(0);
        mTestDataGposInfo.setGoBackStandby(0);
        mTestDataGposInfo.setSubtitleOnOff(0);
    }

    /*private long GetLongByYMD( int year, int month, int day )
    {
        String strYMD;
        SimpleDateFormat format;
        Date date = null;

        strYMD = String.format("%d/%02d/%02d", year, month, day);
        format = new SimpleDateFormat("yyyy/MM/dd");

        try {
            date = format.parse(strYMD);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return date.getTime();
    }*/

    public static long GetLongByHMS( int hour, int min, int sec )
    {
        /*String strHMS;
        SimpleDateFormat format;
        Date date = null;

        strHMS = String.format("%02d:%02d:%02d", hour, min, sec);
        format = new SimpleDateFormat("HH:mm:ss");

        try {
            date = format.parse(strHMS);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return date.getTime();*/

        String strLong = String.format(Locale.US, "0x%02d%02d%02d", hour, min, sec);
        return Long.decode(strLong);
    }

    private static int[] GetHMSByLong( long timeUtcL )
    {
        int[] hms = new int[3];

        String strHexTimeL = Long.toHexString(timeUtcL);
        String strHexTimeLPad0 = "000000".substring(0, 6-strHexTimeL.length()) + strHexTimeL;   // Pad 0
        int timeLHour = Integer.parseInt(strHexTimeLPad0.substring(0, 2));
        int timeLMin = Integer.parseInt(strHexTimeLPad0.substring(2, 4));
        int timeLSec = Integer.parseInt(strHexTimeLPad0.substring(4, 6));

        hms[0] = timeLHour;
        hms[1] = timeLMin;
        hms[2] = timeLSec;

        return hms;
    }

    public static void AddEPGEvent(List<EPGEvent> list, int durationH, int durationM, int durationS, int epgType) // Add EPGEvent by pre EPGEvent
    {
        EPGEvent epgEvent;

        if ( list == null || list.isEmpty() )
        {
            return;
        }

        int prePos = list.size()-1;
        int preSid = list.get(prePos).getSid();
        int preNid = list.get(prePos).getOriginalNetworkId();
        int preStreamid = list.get(prePos).getTransportStreamId();
        int preEventid = list.get(prePos).getEventId();
        int preTableid = list.get(prePos).getTableId();
        String preLangCodec = list.get(prePos).getEventNameLangCodec();

        // Long->HexStr("0xXXXX")->Int
        // preTimeM
        long preTimeM = list.get(prePos).getStartTimeUtcM();
        //String strPreTimeM = String.format("0x%s", Long.toHexString(preTimeM));
        //int intPreTimeM = Integer.decode(strPreTimeM);

        // Long->HexStr("ABCDEF")->Int  (Hour = AB, Min = CD, Sec = EF)
        // preTimeL
        long preTimeL = list.get(prePos).getStartTimeUtcL();
        int[] preTimeLHMS = GetHMSByLong(preTimeL);
        int preTimeLHour = preTimeLHMS[0];
        int preTimeLMin = preTimeLHMS[1];
        int preTimeLSec = preTimeLHMS[2];

        // Long->HexStr("ABCDEF")->Int  (Hour = AB, Min = CD, Sec = EF)
        // preDuration
        long preDuration = list.get(prePos).getDuration();
        int[] preDurationHMS = GetHMSByLong(preDuration);
        int preDurationHour = preDurationHMS[0];
        int preDurationMin = preDurationHMS[1];
        int preDurationSec = preDurationHMS[2];

        // Current EPGEvent startTimeUtcL (Sec)
        // If over 60s add 1 min
        int timeUtcLSec = preTimeLSec + preDurationSec;
        int timeUtcLSecAddtoMin = timeUtcLSec / 60;
        timeUtcLSec = timeUtcLSec % 60;

        // Current EPGEvent startTimeUtcL (Min)
        // If over 60m add 1 hour
        int timeUtcLMin = preTimeLMin + preDurationMin + timeUtcLSecAddtoMin;
        int timeUtcLMinAddtoHour = timeUtcLMin / 60;
        timeUtcLMin = timeUtcLMin % 60;

        // Current EPGEvent startTimeUtcL (Hour)
        // If over 24h add 1 day
        int timeUtcLHour = preTimeLHour + preDurationHour + timeUtcLMinAddtoHour;
        int timeUtcLHourAddtoDay = timeUtcLHour / 24;
        timeUtcLHour = timeUtcLHour % 24;

        // Current EPGEvent startTimeUtcM
        //int timeUtcM = intPreTimeM + timeUtcLHourAddtoDay;
        long timeUtcM = preTimeM + (long)timeUtcLHourAddtoDay;

        // Int->HexStr("0xXXXX")
        //String strTimeUtcM = String.format("0x%s", Integer.toHexString(timeUtcM));
        //String strTimeUtcL = String.format("0x%02d%02d%02d", (timeUtcLHour+17)%24, timeUtcLMin%60, timeUtcLSec%60);
        //String strDuration = String.format("0x%02d%02d%02d", (durationH+17)%24, durationM%60, durationS%60);

        epgEvent = new EPGEvent();
        epgEvent.setSid(preSid);
        epgEvent.setOriginalNetworkId(preNid);
        epgEvent.setTransportStreamId(preStreamid);
        epgEvent.setEventId(++preEventid);  // eventId = preEventId++
        epgEvent.setTableId(preTableid);
        epgEvent.setEventType(epgType);
        epgEvent.setEventName("Event Name " + epgEvent.getEventId());
        epgEvent.setEventNameLangCodec(preLangCodec);
        //epgEvent.setStartTimeUtcM( Long.decode(strTimeUtcM) );  // HexStr("0xXXXX")->Long
        epgEvent.setStartTimeUtcM( timeUtcM );
        epgEvent.setStartTimeUtcL( GetLongByHMS(timeUtcLHour, timeUtcLMin, timeUtcLSec) );
        epgEvent.setDuration( GetLongByHMS(durationH, durationM, durationS) );
        epgEvent.setParentalRate(0);
        epgEvent.setShortEvent("Short Event " + epgEvent.getEventId());
        epgEvent.setShortEventLangCodec(preLangCodec);
        epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
        epgEvent.setExtendedEventLangCodec(preLangCodec);
        list.add( epgEvent );

    }

    private void SetTestDataEPGEvent()
    {
        EPGEvent epgEvent;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
        long utcM = 0xe1f1;
        Date beginDate = new Date();
        Date nowDate = new Date();
        int daysAfter;

        mTestEPGEvent_PF = new ArrayList<>();
        mTestEPGEvent_Schedule = new ArrayList<>();

        try {
            beginDate = sdf.parse("2017/03/29");
            nowDate =  sdf.parse(sdf.format(nowDate));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // set utcM to now
        daysAfter = (int) ((nowDate.getTime()-beginDate.getTime()) /1000/60/60/24);
        utcM += daysAfter;

        // PF Start TableId = 0x4e(78) ServiceCount:2, TotalEPG:4
        // Service 1 (SID:1010, Present:07:00:00~07:05:00, Follow:07:05:00~08:05:00)
        epgEvent = new EPGEvent();
        epgEvent.setSid(1010);
        epgEvent.setOriginalNetworkId(8945);
        epgEvent.setTransportStreamId(1021);
        epgEvent.setEventId(0);
        epgEvent.setTableId(78);
        epgEvent.setEventType(EPGEvent.EPG_TYPE_PRESENT);
        epgEvent.setEventName("Event Name " + epgEvent.getEventId());
        epgEvent.setEventNameLangCodec("eng");
        epgEvent.setStartTimeUtcM( utcM );
        epgEvent.setStartTimeUtcL( GetLongByHMS(7, 0, 0) );
        epgEvent.setDuration( GetLongByHMS(0, 5, 0) );
        epgEvent.setParentalRate(0);
        epgEvent.setShortEvent("Short Event " + epgEvent.getEventId());
        epgEvent.setShortEventLangCodec("eng");
        epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
        epgEvent.setExtendedEventLangCodec("eng");
        mTestEPGEvent_PF.add( epgEvent );
        AddEPGEvent(mTestEPGEvent_PF, 1, 0, 0, EPGEvent.EPG_TYPE_FOLLOW);

        // Service 2 (SID:5060, Present:07:30:00~07:35:00, Follow:07:35:00~08:35:00)
        epgEvent = new EPGEvent();
        epgEvent.setSid(5060);
        epgEvent.setOriginalNetworkId(8945);
        epgEvent.setTransportStreamId(1021);
        epgEvent.setEventId(10);
        epgEvent.setTableId(78);
        epgEvent.setEventType(EPGEvent.EPG_TYPE_PRESENT);
        epgEvent.setEventName("Event Name " + epgEvent.getEventId());
        epgEvent.setEventNameLangCodec("eng");
        epgEvent.setStartTimeUtcM( utcM );
        epgEvent.setStartTimeUtcL( GetLongByHMS(7, 30, 0) );
        epgEvent.setDuration( GetLongByHMS(0, 5, 0) );
        epgEvent.setParentalRate(0);
        epgEvent.setShortEvent("Short Event " + epgEvent.getEventId());
        epgEvent.setShortEventLangCodec("eng");
        epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
        epgEvent.setExtendedEventLangCodec("eng");
        mTestEPGEvent_PF.add( epgEvent );
        AddEPGEvent(mTestEPGEvent_PF, 1, 0, 0, EPGEvent.EPG_TYPE_FOLLOW);

        // Service 3 will update by message

        // Service 4 (SID:870, tsid:1021, netid:8945)
        epgEvent = new EPGEvent();
        epgEvent.setSid(870);
        epgEvent.setOriginalNetworkId(8945);
        epgEvent.setTransportStreamId(1021);
        epgEvent.setEventId(20);
        epgEvent.setTableId(78);
        epgEvent.setEventType(EPGEvent.EPG_TYPE_PRESENT);
        epgEvent.setEventName("Event Name " + epgEvent.getEventId());
        epgEvent.setEventNameLangCodec("eng");
        epgEvent.setStartTimeUtcM( utcM );
        epgEvent.setStartTimeUtcL( GetLongByHMS(5, 30, 0) );
        epgEvent.setDuration( GetLongByHMS(0, 5, 0) );
        epgEvent.setParentalRate(0);
        epgEvent.setShortEvent("Short Event " + epgEvent.getEventId());
        epgEvent.setShortEventLangCodec("eng");
        epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
        epgEvent.setExtendedEventLangCodec("eng");
        mTestEPGEvent_PF.add( epgEvent );
        AddEPGEvent(mTestEPGEvent_PF, 1, 0, 0, EPGEvent.EPG_TYPE_FOLLOW);

        // Service 5 (SID:5170, tsid:1021, netid:8945)
        epgEvent = new EPGEvent();
        epgEvent.setSid(5170);
        epgEvent.setOriginalNetworkId(8945);
        epgEvent.setTransportStreamId(1021);
        epgEvent.setEventId(30);
        epgEvent.setTableId(78);
        epgEvent.setEventType(EPGEvent.EPG_TYPE_PRESENT);
        epgEvent.setEventName("Event Name " + epgEvent.getEventId());
        epgEvent.setEventNameLangCodec("eng");
        epgEvent.setStartTimeUtcM( utcM );
        epgEvent.setStartTimeUtcL( GetLongByHMS(6, 30, 0) );
        epgEvent.setDuration( GetLongByHMS(0, 5, 0) );
        epgEvent.setParentalRate(0);
        epgEvent.setShortEvent("Short Event " + epgEvent.getEventId());
        epgEvent.setShortEventLangCodec("eng");
        epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
        epgEvent.setExtendedEventLangCodec("eng");
        mTestEPGEvent_PF.add( epgEvent );
        AddEPGEvent(mTestEPGEvent_PF, 1, 0, 0, EPGEvent.EPG_TYPE_FOLLOW);

        // Service 6 (SID:6716, tsid:67, netid:88)
        epgEvent = new EPGEvent();
        epgEvent.setSid(6716);
        epgEvent.setOriginalNetworkId(88);
        epgEvent.setTransportStreamId(67);
        epgEvent.setEventId(40);
        epgEvent.setTableId(78);
        epgEvent.setEventType(EPGEvent.EPG_TYPE_PRESENT);
        epgEvent.setEventName("Event Name " + epgEvent.getEventId());
        epgEvent.setEventNameLangCodec("eng");
        epgEvent.setStartTimeUtcM( utcM );
        epgEvent.setStartTimeUtcL( GetLongByHMS(7, 30, 0) );
        epgEvent.setDuration( GetLongByHMS(0, 5, 0) );
        epgEvent.setParentalRate(0);
        epgEvent.setShortEvent("Short Event " + epgEvent.getEventId());
        epgEvent.setShortEventLangCodec("eng");
        epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
        epgEvent.setExtendedEventLangCodec("eng");
        mTestEPGEvent_PF.add( epgEvent );
        AddEPGEvent(mTestEPGEvent_PF, 1, 0, 0, EPGEvent.EPG_TYPE_FOLLOW);

        // Service 7 (SID:6717, tsid:67, netid:88)
        epgEvent = new EPGEvent();
        epgEvent.setSid(6717);
        epgEvent.setOriginalNetworkId(88);
        epgEvent.setTransportStreamId(67);
        epgEvent.setEventId(50);
        epgEvent.setTableId(78);
        epgEvent.setEventType(EPGEvent.EPG_TYPE_PRESENT);
        epgEvent.setEventName("Event Name " + epgEvent.getEventId());
        epgEvent.setEventNameLangCodec("eng");
        epgEvent.setStartTimeUtcM( utcM );
        epgEvent.setStartTimeUtcL( GetLongByHMS(6, 30, 0) );
        epgEvent.setDuration( GetLongByHMS(0, 5, 0) );
        epgEvent.setParentalRate(0);
        epgEvent.setShortEvent("Short Event " + epgEvent.getEventId());
        epgEvent.setShortEventLangCodec("eng");
        epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
        epgEvent.setExtendedEventLangCodec("eng");
        mTestEPGEvent_PF.add( epgEvent );
        AddEPGEvent(mTestEPGEvent_PF, 1, 0, 0, EPGEvent.EPG_TYPE_FOLLOW);

        // Service 8 (SID:6718, tsid:67, netid:88)
        epgEvent = new EPGEvent();
        epgEvent.setSid(6718);
        epgEvent.setOriginalNetworkId(88);
        epgEvent.setTransportStreamId(67);
        epgEvent.setEventId(50);
        epgEvent.setTableId(78);
        epgEvent.setEventType(EPGEvent.EPG_TYPE_PRESENT);
        epgEvent.setEventName("Event Name " + epgEvent.getEventId());
        epgEvent.setEventNameLangCodec("eng");
        epgEvent.setStartTimeUtcM( utcM );
        epgEvent.setStartTimeUtcL( GetLongByHMS(5, 30, 0) );
        epgEvent.setDuration( GetLongByHMS(0, 5, 0) );
        epgEvent.setParentalRate(0);
        epgEvent.setShortEvent("Short Event " + epgEvent.getEventId());
        epgEvent.setShortEventLangCodec("eng");
        epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
        epgEvent.setExtendedEventLangCodec("eng");
        mTestEPGEvent_PF.add( epgEvent );
        AddEPGEvent(mTestEPGEvent_PF, 1, 0, 0, EPGEvent.EPG_TYPE_FOLLOW);
        // PF End

        // Schedule Start  TableId = 0x50(80) ServiceCount:2, TotalEPG:100
        // Service 1 (First two EPGs are as PF, TotalEPG:50)
        epgEvent = new EPGEvent();
        epgEvent.setSid(1010);
        epgEvent.setOriginalNetworkId(8945);
        epgEvent.setTransportStreamId(1021);
        epgEvent.setEventId(64479);
        epgEvent.setTableId(80);
        epgEvent.setEventType(EPGEvent.EPG_TYPE_SCHEDULE);
        epgEvent.setEventName("Event Name " + epgEvent.getEventId());
        epgEvent.setEventNameLangCodec("eng");
        epgEvent.setStartTimeUtcM( utcM );
        epgEvent.setStartTimeUtcL( GetLongByHMS(7, 0, 0) );
        epgEvent.setDuration( GetLongByHMS(0, 5, 0) );
        epgEvent.setParentalRate(0);
        epgEvent.setShortEvent("Short Event " + epgEvent.getEventId());
        epgEvent.setShortEventLangCodec("eng");
        epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
        epgEvent.setExtendedEventLangCodec("eng");
        mTestEPGEvent_Schedule.add( epgEvent );
        AddEPGEvent(mTestEPGEvent_Schedule, 1, 0, 0, EPGEvent.EPG_TYPE_SCHEDULE);

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 2, 0, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 0, 30, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 0, 0, 30, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 1, 20, 40, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 8 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 1, 45, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        // Service 2 (First two EPGs are as PF, TotalEPG:50)
        epgEvent = new EPGEvent();
        epgEvent.setSid(5060);
        epgEvent.setOriginalNetworkId(8945);
        epgEvent.setTransportStreamId(1021);
        epgEvent.setEventId(65020);
        epgEvent.setTableId(80);
        epgEvent.setEventType(EPGEvent.EPG_TYPE_SCHEDULE);
        epgEvent.setEventName("Event Name " + epgEvent.getEventId());
        epgEvent.setEventNameLangCodec("eng");
        epgEvent.setStartTimeUtcM( utcM );
        epgEvent.setStartTimeUtcL( GetLongByHMS(7, 30, 0) );
        epgEvent.setDuration( GetLongByHMS(0, 5, 0) );
        epgEvent.setParentalRate(0);
        epgEvent.setShortEvent("Short Event " + epgEvent.getEventId());
        epgEvent.setShortEventLangCodec("eng");
        epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
        epgEvent.setExtendedEventLangCodec("eng");
        mTestEPGEvent_Schedule.add( epgEvent );
        AddEPGEvent(mTestEPGEvent_Schedule, 1, 0, 0, EPGEvent.EPG_TYPE_SCHEDULE);

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 2, 0, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 0, 30, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 0, 0, 30, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 1, 20, 40, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 8 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 1, 45, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        // Service 4 (First two EPGs are as PF, TotalEPG:25)
        epgEvent = new EPGEvent();
        epgEvent.setSid(870);
        epgEvent.setOriginalNetworkId(8945);
        epgEvent.setTransportStreamId(1021);
        epgEvent.setEventId(66020);
        epgEvent.setTableId(80);
        epgEvent.setEventType(EPGEvent.EPG_TYPE_SCHEDULE);
        epgEvent.setEventName("Event Name " + epgEvent.getEventId());
        epgEvent.setEventNameLangCodec("eng");
        epgEvent.setStartTimeUtcM( utcM );
        epgEvent.setStartTimeUtcL( GetLongByHMS(5, 30, 0) );
        epgEvent.setDuration( GetLongByHMS(0, 5, 0) );
        epgEvent.setParentalRate(0);
        epgEvent.setShortEvent("Short Event " + epgEvent.getEventId());
        epgEvent.setShortEventLangCodec("eng");
        epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
        epgEvent.setExtendedEventLangCodec("eng");
        mTestEPGEvent_Schedule.add( epgEvent );
        AddEPGEvent(mTestEPGEvent_Schedule, 1, 0, 0, EPGEvent.EPG_TYPE_SCHEDULE);

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 2, 0, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 0, 30, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 0, 5, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 1, 20, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 8 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 1, 45, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        // delete epg every two epgs from back
        for ( int i = 0 ; i < 50 ; i++)
        {
            if (i % 2 == 0)
            {
                mTestEPGEvent_Schedule.remove(mTestEPGEvent_Schedule.size()-1-i);
            }
        }

        // Service 5 (First two EPGs are as PF, TotalEPG:25)
        epgEvent = new EPGEvent();
        epgEvent.setSid(5170);
        epgEvent.setOriginalNetworkId(8945);
        epgEvent.setTransportStreamId(1021);
        epgEvent.setEventId(66520);
        epgEvent.setTableId(80);
        epgEvent.setEventType(EPGEvent.EPG_TYPE_SCHEDULE);
        epgEvent.setEventName("Event Name " + epgEvent.getEventId());
        epgEvent.setEventNameLangCodec("eng");
        epgEvent.setStartTimeUtcM( utcM );
        epgEvent.setStartTimeUtcL( GetLongByHMS(6, 30, 0) );
        epgEvent.setDuration( GetLongByHMS(0, 5, 0) );
        epgEvent.setParentalRate(0);
        epgEvent.setShortEvent("Short Event " + epgEvent.getEventId());
        epgEvent.setShortEventLangCodec("eng");
        epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
        epgEvent.setExtendedEventLangCodec("eng");
        mTestEPGEvent_Schedule.add( epgEvent );
        AddEPGEvent(mTestEPGEvent_Schedule, 1, 0, 0, EPGEvent.EPG_TYPE_SCHEDULE);

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 2, 0, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 0, 30, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 0, 5, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 1, 20, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 8 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 1, 45, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        // delete epg every two epgs from back
        for ( int i = 0 ; i < 50 ; i++)
        {
            if (i % 2 == 0)
            {
                mTestEPGEvent_Schedule.remove(mTestEPGEvent_Schedule.size()-1-i);
            }
        }

        // Service 6 (First two EPGs are as PF, TotalEPG:25)
        epgEvent = new EPGEvent();
        epgEvent.setSid(6716);
        epgEvent.setOriginalNetworkId(88);
        epgEvent.setTransportStreamId(67);
        epgEvent.setEventId(67020);
        epgEvent.setTableId(80);
        epgEvent.setEventType(EPGEvent.EPG_TYPE_SCHEDULE);
        epgEvent.setEventName("Event Name " + epgEvent.getEventId());
        epgEvent.setEventNameLangCodec("eng");
        epgEvent.setStartTimeUtcM( utcM );
        epgEvent.setStartTimeUtcL( GetLongByHMS(7, 30, 0) );
        epgEvent.setDuration( GetLongByHMS(0, 5, 0) );
        epgEvent.setParentalRate(0);
        epgEvent.setShortEvent("Short Event " + epgEvent.getEventId());
        epgEvent.setShortEventLangCodec("eng");
        epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
        epgEvent.setExtendedEventLangCodec("eng");
        mTestEPGEvent_Schedule.add( epgEvent );
        AddEPGEvent(mTestEPGEvent_Schedule, 1, 0, 0, EPGEvent.EPG_TYPE_SCHEDULE);

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 2, 0, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 0, 30, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 0, 5, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 1, 20, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 8 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 1, 45, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        // delete epg every two epgs from back
        for ( int i = 0 ; i < 50 ; i++)
        {
            if (i % 2 == 0)
            {
                mTestEPGEvent_Schedule.remove(mTestEPGEvent_Schedule.size()-1-i);
            }
        }

        // Service 7 (First two EPGs are as PF, TotalEPG:25)
        epgEvent = new EPGEvent();
        epgEvent.setSid(6717);
        epgEvent.setOriginalNetworkId(88);
        epgEvent.setTransportStreamId(67);
        epgEvent.setEventId(67520);
        epgEvent.setTableId(80);
        epgEvent.setEventType(EPGEvent.EPG_TYPE_SCHEDULE);
        epgEvent.setEventName("Event Name " + epgEvent.getEventId());
        epgEvent.setEventNameLangCodec("eng");
        epgEvent.setStartTimeUtcM( utcM );
        epgEvent.setStartTimeUtcL( GetLongByHMS(6, 30, 0) );
        epgEvent.setDuration( GetLongByHMS(0, 5, 0) );
        epgEvent.setParentalRate(0);
        epgEvent.setShortEvent("Short Event " + epgEvent.getEventId());
        epgEvent.setShortEventLangCodec("eng");
        epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
        epgEvent.setExtendedEventLangCodec("eng");
        mTestEPGEvent_Schedule.add( epgEvent );
        AddEPGEvent(mTestEPGEvent_Schedule, 1, 0, 0, EPGEvent.EPG_TYPE_SCHEDULE);

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 2, 0, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 0, 30, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 0, 5, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 1, 20, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 8 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 1, 45, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        // delete epg every two epgs from back
        for ( int i = 0 ; i < 50 ; i++)
        {
            if (i % 2 == 0)
            {
                mTestEPGEvent_Schedule.remove(mTestEPGEvent_Schedule.size()-1-i);
            }
        }

        // Service 8 (First two EPGs are as PF, TotalEPG:25)
        epgEvent = new EPGEvent();
        epgEvent.setSid(6718);
        epgEvent.setOriginalNetworkId(88);
        epgEvent.setTransportStreamId(67);
        epgEvent.setEventId(68020);
        epgEvent.setTableId(80);
        epgEvent.setEventType(EPGEvent.EPG_TYPE_SCHEDULE);
        epgEvent.setEventName("Event Name " + epgEvent.getEventId());
        epgEvent.setEventNameLangCodec("eng");
        epgEvent.setStartTimeUtcM( utcM );
        epgEvent.setStartTimeUtcL( GetLongByHMS(5, 30, 0) );
        epgEvent.setDuration( GetLongByHMS(0, 5, 0) );
        epgEvent.setParentalRate(0);
        epgEvent.setShortEvent("Short Event " + epgEvent.getEventId());
        epgEvent.setShortEventLangCodec("eng");
        epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
        epgEvent.setExtendedEventLangCodec("eng");
        mTestEPGEvent_Schedule.add( epgEvent );
        AddEPGEvent(mTestEPGEvent_Schedule, 1, 0, 0, EPGEvent.EPG_TYPE_SCHEDULE);

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 2, 0, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 0, 30, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 0, 5, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 10 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 1, 20, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        for ( int i = 0 ; i < 8 ; i++)
        {
            AddEPGEvent(mTestEPGEvent_Schedule, 1, 45, 0, EPGEvent.EPG_TYPE_SCHEDULE);
        }

        // delete epg every two epgs from back
        for ( int i = 0 ; i < 50 ; i++)
        {
            if (i % 2 == 0)
            {
                mTestEPGEvent_Schedule.remove(mTestEPGEvent_Schedule.size()-1-i);
            }
        }
        // Schedule End

    }

    void SetTestDataFavGroupName(int TunerType)
    {
        switch (TunerType)
        {
            case DVBC: {
                String str=null;
                    mTestDataFavGroupName.add(new FavGroupName(0, "All TV"));
                    for(int i=1; i<=FavGroupName.TV_FAV_NUM; i++)
                        mTestDataFavGroupName.add(new FavGroupName(i, String.format("TV Favorite %d",i)));
                    mTestDataFavGroupName.add(new FavGroupName(7, "All Radio"));
                    for(int i=1; i<=FavGroupName.RADIO_FAV_NUM; i++)
                        mTestDataFavGroupName.add(new FavGroupName(7+i, String.format("Radio Favorite %d",i)));
            }break;
            case DVBT:{
                String str=null;
                mTestDataFavGroupName.add(new FavGroupName(0, "All TV"));
                for(int i=1; i<=FavGroupName.TV_FAV_NUM; i++)
                    mTestDataFavGroupName.add(new FavGroupName(i, String.format("TV Favorite %d",i)));
                mTestDataFavGroupName.add(new FavGroupName(7, "All Radio"));
                for(int i=1; i<=FavGroupName.RADIO_FAV_NUM; i++)
                    mTestDataFavGroupName.add(new FavGroupName(7+i, String.format("Radio Favorite %d",i)));
            }break;
            case TpInfo.ISDBT:
            {
                String str=null;
                mTestDataFavGroupName.add(new FavGroupName(0, "All TV"));
                for(int i=1; i<=FavGroupName.TV_FAV_NUM; i++)
                    mTestDataFavGroupName.add(new FavGroupName(i, String.format("TV Favorite %d",i)));
                mTestDataFavGroupName.add(new FavGroupName(7, "All Radio"));
                for(int i=1; i<=FavGroupName.RADIO_FAV_NUM; i++)
                    mTestDataFavGroupName.add(new FavGroupName(7+i, String.format("Radio Favorite %d",i)));
            }break;
            case DVBS:
            {
                String str=null;
                mTestDataFavGroupName.add(new FavGroupName(0, "All TV"));
                for(int i=1; i<=FavGroupName.TV_FAV_NUM; i++)
                    mTestDataFavGroupName.add(new FavGroupName(i, String.format("TV Favorite %d",i)));
                mTestDataFavGroupName.add(new FavGroupName(7, "All Radio"));
                for(int i=1; i<=FavGroupName.RADIO_FAV_NUM; i++)
                    mTestDataFavGroupName.add(new FavGroupName(7+i, String.format("Radio Favorite %d",i)));
            }break;
            default:
                break;
        }    
    }

    void PrintTestDataFavGroupName(int TunerType)
    {
        switch (TunerType)
        {
            case DVBC: {
                if(mTestDataFavGroupName != null) {
                    Log.d(TAG, "PrintTestDataFavGroupName:  size=" + mTestDataFavGroupName.size());
                    for (int i = 0; i < mTestDataFavGroupName.size(); i++) {
                        Log.d(TAG, "PrintTestDataFavGroupName:  index=" + i
                                + ", groupType=" + mTestDataFavGroupName.get(i).getGroupType()
                                + ", groupName=" + mTestDataFavGroupName.get(i).getGroupName()
                        );
                    }
                }
            }break;
            case DVBT:
                break;
            case DVBS:
                break;
            default:
                break;
        }
    }

    void SetTestDataDefaultChannel()
    {
        mTestDataDefaultChannel = new DefaultChannel();
        mTestDataDefaultChannel.setChanneId(mTestDataTvProgramInfo.get(0).getChannelId());
        mTestDataDefaultChannel.setGroupType(ProgramInfo.ALL_TV_TYPE);
    }

    /*
    void SetTestDataFavInfo(int TunerType)
    {
        switch (TunerType)
        {
            case DVBC: {
                Log.d(TAG, "SetTestDataFavInfo("+TunerType+")");
                if(mTestDataProgramInfo.isEmpty() != true) {
                    for (int i = 0; i < mTestDataProgramInfo.size() - 1; i++) {
                        Log.d(TAG, "FavId=" + (i / 6)
                                + ", s_id=" + mTestDataProgramInfo.get(i).getChannelId()
                                + ", on_id=" + mTestDataProgramInfo.get(i).getOriginalNetworkId()
                                + ", ts_id=" + mTestDataProgramInfo.get(i).getTransportStreamId()
                                + ", mode=" + ((i % 6)+1)
                        );
                        mTestDataFavInfo.add(new FavInfo((i / 6),
                                mTestDataProgramInfo.get(i).getChannelId(),
                                mTestDataProgramInfo.get(i).getOriginalNetworkId(),
                                mTestDataProgramInfo.get(i).getTransportStreamId(),
                                ((i % 6)+1)));
                    }
                }
            }break;
            case DVBT:
                break;
            case DVBS:
                break;
            default:
                break;
        }
    }

    void PrintTestDataFavInfo(int TunerType)
    {
        switch (TunerType)
        {
            case DVBC: {
                if(mTestDataFavInfo.isEmpty() != true) {
                    for (int i = 0; i < mTestDataFavInfo.size(); i++) {
                        Log.d(TAG, "PrintTestDataFavInfo:  index=" + i
                                + ": FavMode=" + mTestDataFavInfo.get(i).getFavMode()
                                + ", FavId=" + mTestDataFavInfo.get(i).getFavNum()
                                + ": ServiceId=" + mTestDataFavInfo.get(i).getChannelId()
                                + ", TransportStreamId=" + mTestDataFavInfo.get(i).getTransportStreamId()
                                + ", OriginalNetworkId=" + mTestDataFavInfo.get(i).getOriginalNetworkId()
                        );
                    }
                }
            }break;
            case DVBT:
                break;
            case DVBS:
                break;
            default:
                break;
        }
    }
    */


    public List<EPGEvent> GetTestDatEpgEventList_PF()
    {
        if ( mTestEPGEvent_PF == null )
        {
            return null;
        }

        return mTestEPGEvent_PF;
    }

    public List<EPGEvent> GetTestDatEpgEventList_Schedule()
    {
        if ( mTestEPGEvent_Schedule == null )
        {
            return null;
        }

        return mTestEPGEvent_Schedule;
    }


    public GposInfo GetTestDatGposInfo()
    {
        if ( mTestDataGposInfo == null )
        {
            return  null;
        }

        return mTestDataGposInfo;
    }

    public List<BookInfo> GetTestDatBookInfoList()
    {
        if ( mTestDataBookInfo == null )
        {
            return null;
        }

        return mTestDataBookInfo;
    }

    // johnny 20171121 add
    public List<TpInfo> GetTestDatTpInfoList()
    {
        if ( mTestDataTpInfo == null )
        {
            return null;
        }

        return mTestDataTpInfo;
    }

    public List<ProgramInfo> GetTestDataProgramInfoArray(int type)
    {
        if(type == ProgramInfo.ALL_TV_TYPE) {
            if (mTestDataTvProgramInfo.isEmpty())
                return null;
            else{
                return mTestDataTvProgramInfo;
            }
        }
        else if(type == ProgramInfo.ALL_RADIO_TYPE) {
            if (mTestDataRadioProgramInfo.isEmpty())
                return null;
            else{
                return mTestDataRadioProgramInfo;
            }
        }else
            return null;
    }

    public List<ProgramInfo> GetTestDataProgramInforSave(int type)
    {
        if(type == ProgramInfo.ALL_TV_TYPE) {
            return mTestDataTvProgramInfo;
        }else if(type == ProgramInfo.ALL_RADIO_TYPE) {
            return mTestDataRadioProgramInfo;
        }
        return null;
    }

    public List<ProgramInfo> GetTestDataTvProgramList()
    {
        return mTestDataTvProgramInfo;
    }

    public List<ProgramInfo> GetTestDataRadioProgramList()
    {
        return mTestDataRadioProgramInfo;
    }

    /*public List<AntInfo> GetTestDatAntInfoList()
    {
        if ( mTestDataAntInfo == null )
        {
            return null;
        }

        return mTestDataAntInfo;
    }*/

    public List<SatInfo> GetTestDatSatInfoList()
    {
        if ( mTestDataSatInfo == null )
        {
            return null;
        }

        return mTestDataSatInfo;
    }

    public List<FavGroupName> GetTestDataFavGroupNameArray()
    {
        if (mTestDataFavGroupName.isEmpty())
            return null;
        else{
            return mTestDataFavGroupName;
        }
    }

    public List<FavInfo> GetTestDataFavInfoArray(int favMode)
    {
        if(favMode == ProgramInfo.TV_FAV1_TYPE && !mTestDataTvFav1.isEmpty())
            return mTestDataTvFav1;
        else if(favMode == ProgramInfo.TV_FAV2_TYPE && !mTestDataTvFav2.isEmpty())
            return mTestDataTvFav2;
        else if(favMode == ProgramInfo.TV_FAV3_TYPE && !mTestDataTvFav3.isEmpty())
            return mTestDataTvFav3;
        else if(favMode == ProgramInfo.TV_FAV4_TYPE && !mTestDataTvFav4.isEmpty())
            return mTestDataTvFav4;
        else if(favMode == ProgramInfo.TV_FAV5_TYPE && !mTestDataTvFav5.isEmpty())
            return mTestDataTvFav5;
        else if(favMode == ProgramInfo.TV_FAV6_TYPE && !mTestDataTvFav6.isEmpty())
            return mTestDataTvFav6;
        else if(favMode == ProgramInfo.RADIO_FAV1_TYPE && !mTestDataRadioFav1.isEmpty())
            return mTestDataRadioFav1;
        else if(favMode == ProgramInfo.RADIO_FAV2_TYPE && !mTestDataRadioFav2.isEmpty())
            return mTestDataRadioFav2;
        else
            return null;
    }

    public List<FavInfo> GetTestDataFavInfoArrayForSave(int favMode)
    {
        if(favMode == ProgramInfo.TV_FAV1_TYPE)
            return mTestDataTvFav1;
        else if(favMode == ProgramInfo.TV_FAV2_TYPE)
            return mTestDataTvFav2;
        else if(favMode == ProgramInfo.TV_FAV3_TYPE)
            return mTestDataTvFav3;
        else if(favMode == ProgramInfo.TV_FAV4_TYPE)
            return mTestDataTvFav4;
        else if(favMode == ProgramInfo.TV_FAV5_TYPE)
            return mTestDataTvFav5;
        else if(favMode == ProgramInfo.TV_FAV6_TYPE)
            return mTestDataTvFav6;
        else if(favMode == ProgramInfo.RADIO_FAV1_TYPE)
            return mTestDataRadioFav1;
        else if(favMode == ProgramInfo.RADIO_FAV2_TYPE)
            return mTestDataRadioFav2;
        else
            return null;
    }

    public static List<FavInfo> FavInfoCloneList(List<FavInfo> dogList) {
        List<FavInfo> clonedList = new ArrayList<FavInfo>(dogList.size());
        for (FavInfo dog : dogList) {
            clonedList.add(new FavInfo(dog));
        }
        return clonedList;
    }

    public void TestDataFavInfoListSave(int favMode, List<FavInfo> favInfo)
    {
        switch(favMode)
        {
            case ProgramInfo.TV_FAV1_TYPE: {
                if (!mTestDataTvFav1.isEmpty())
                    mTestDataTvFav1.clear();
                mTestDataTvFav1 = FavInfoCloneList(favInfo);
            }break;
            case ProgramInfo.TV_FAV2_TYPE: {
                if (!mTestDataTvFav2.isEmpty())
                    mTestDataTvFav2.clear();
                mTestDataTvFav2 = FavInfoCloneList(favInfo);
            }break;
            case ProgramInfo.TV_FAV3_TYPE: {
                if (!mTestDataTvFav3.isEmpty())
                    mTestDataTvFav3.clear();
                mTestDataTvFav3 = FavInfoCloneList(favInfo);
            }break;
            case ProgramInfo.TV_FAV4_TYPE: {
                if (!mTestDataTvFav4.isEmpty())
                    mTestDataTvFav4.clear();
                mTestDataTvFav4 = FavInfoCloneList(favInfo);
            }break;
            case ProgramInfo.TV_FAV5_TYPE: {
                if (!mTestDataTvFav5.isEmpty())
                    mTestDataTvFav5.clear();
                mTestDataTvFav5 = FavInfoCloneList(favInfo);
            }break;
            case ProgramInfo.TV_FAV6_TYPE: {
                if (!mTestDataTvFav6.isEmpty())
                    mTestDataTvFav6.clear();
                mTestDataTvFav6 = FavInfoCloneList(favInfo);
            }break;
            case ProgramInfo.RADIO_FAV1_TYPE: {
                if (!mTestDataRadioFav1.isEmpty())
                    mTestDataRadioFav1.clear();
                mTestDataRadioFav1 = FavInfoCloneList(favInfo);
            }break;
            case ProgramInfo.RADIO_FAV2_TYPE: {
                if (!mTestDataRadioFav2.isEmpty())
                    mTestDataRadioFav2.clear();
                mTestDataRadioFav2 = FavInfoCloneList(favInfo);
            }break;
        }
    }

    public static List<ProgramInfo> ProgramInfoCloneList(List<ProgramInfo> dogList) {
        List<ProgramInfo> clonedList = new ArrayList<ProgramInfo>(dogList.size());
        for (ProgramInfo dog : dogList) {
            clonedList.add(new ProgramInfo(dog));
        }
        return clonedList;
    }

    public void TestDataProgramInfoListSave(int type, List<ProgramInfo> programInfo){
        if(type == ProgramInfo.ALL_TV_TYPE){
            if (!mTestDataTvProgramInfo.isEmpty())
                mTestDataTvProgramInfo.clear();
            mTestDataTvProgramInfo = ProgramInfoCloneList(programInfo);
            }else if(type == ProgramInfo.ALL_RADIO_TYPE){
            if (!mTestDataRadioProgramInfo.isEmpty())
                mTestDataRadioProgramInfo.clear();
            mTestDataRadioProgramInfo = ProgramInfoCloneList(programInfo);
        }
    }

    public int GetTestDataDefaultChannelChanneId() {
        return mTestDataDefaultChannel.getChanneId();
    }

    public void SetTestDataDefaultChannelChanneId(int channeId) {
        mTestDataDefaultChannel.setChanneId(channeId);
    }

    public int GetTestDataDefaultChannelGroupType() {
        return mTestDataDefaultChannel.getGroupType();
    }

    public void SetTestDataDefaultChannelGroupType(int groupType) {
        mTestDataDefaultChannel.setGroupType(groupType);
    }

    public DefaultChannel GetTestDataDefaultChannel()
    {
        return mTestDataDefaultChannel;
    }

    /*
    public void TestDataProgramInfoListSave(List<ProgramInfo> programInfo)
    {
        int i=0,j=0;
        boolean find_new = false;
        if (mTestDataProgramInfo.isEmpty() != true ) {
            for(i=0; i<programInfo.size(); i++) {
                find_new = true;
                for(j=0; j<mTestDataProgramInfo.size(); j++) {
                    if (programInfo.get(i).getChannelId() == mTestDataProgramInfo.get(j).getServiceId()
                            && programInfo.get(i).getOriginalNetworkId() ==  mTestDataProgramInfo.get(j).getOriginalNetworkId()
                            && programInfo.get(i).getTransportStreamId() == mTestDataProgramInfo.get(j).getTransportStreamId()){
                        mTestDataProgramInfo.set(j, programInfo.get(i));//update
                        find_new = false;
                        break;
                    }
    }
                if(find_new == true)
                    mTestDataProgramInfo.add(programInfo.get(i));//add
            }
        }
    }
    */
}
