package com.prime.dtv.service.Table.Desciptor;

import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.service.Util.DVBString;

import java.util.ArrayList;
import java.util.List;

public class LinkageDescriptor extends DescBase{
    private static final String TAG = "LinkageDescriptor";

    public int TransportStreamId;
    public int OriginalNetworkId;
    public int ServiceId;
    public int LinkageType;
    private byte[] privateDataByte;
    public List<OUI> mOUI = new ArrayList<OUI>();
    public List<MultiSacnFreq> mMultiSacnFreq = new ArrayList<MultiSacnFreq>();
    public List<BatWhiteList> mBatWhiteList = new ArrayList<BatWhiteList>();
    public List<OUITBC> mOUITbc = new ArrayList<OUITBC>();
    public List<OUIDMG> mOUIDmg = new ArrayList<OUIDMG>();
    public List<WvCasUrlData> mWvCasUrlDataList = new ArrayList<>();


    public LinkageDescriptor(byte[] data, int lens) {
        Parsing(data,lens);
    }

    @Override
    public void Parsing(byte[] data, int lens) {
        int OUI_data_length;
        Tag = toUnsignedInt(data[0]);
        Length = toUnsignedInt(data[1]);
        if(lens == Length+2 && Length>0)
            DataExist = true;
        TransportStreamId = getInt(data,2,2,MASK_16BITS);
        OriginalNetworkId = getInt(data,4,2,MASK_16BITS);
        ServiceId =  getInt(data,6,2,MASK_16BITS);
        LinkageType = getInt(data,8,1,MASK_8BITS);
        Log.d(TAG,"LinkageType = " + LinkageType);
        if(LinkageType==0x09){ //System Software Update Service (TS 102 006)
            OUI_data_length = getInt(data,9,1,MASK_8BITS);
            int r =0;
            while (r<OUI_data_length) {
                final int oui = getInt(data,10+r, 3, MASK_24BITS);
                final int selectorLength= getInt(data, r+13, 1, MASK_8BITS);
                if(OUI_data_length < selectorLength+4)
                    break;
                List<OUIData> ouidataList = new ArrayList<OUIData>();
                int s=0;
                while (s<selectorLength) {
                    final int customerId = getInt(data, r+14+s,2,MASK_16BITS);
                    final int swid = getInt(data,r+14+2+s,2,MASK_16BITS);
                    final int force = getInt(data,r+14+3+s,1,MASK_8BITS);

                    final OUIData ouidata = new OUIData(customerId, swid, force);
                    ouidataList.add(ouidata);
                    s+=5;
                    if((s+5)>selectorLength)
                        break;
                }

                final OUI ouiEntry = new OUI(oui, ouidataList);
                mOUI.add(ouiEntry);
                r=r+4+selectorLength;
            }
        }
        else if(LinkageType == 0xD2){ //BAT whitelist for TBC
            final int BatNumber;
            final List<Integer> BatIdList=new ArrayList<Integer>();
            BatWhiteList batWhiteList;
            int i,BatId,r;
            //Log.d(TAG,"LinkageType = 0xD2 whitelist for TBC");
            BatNumber = getInt(data,9, 2, MASK_16BITS);
            //Log.d(TAG,"LinkageType = 0xD2, BatNumber = " + BatNumber);
            r=0;
            if(BatNumber > 0){
                for(i=0; i<BatNumber; i++) {
                    //Log.d(TAG,"Length =  " + Length + " r = " + r + " i = " + i);
                    BatId = getInt(data,9+2+(i*2), 2, MASK_16BITS);
                    //Log.d(TAG,"LinkageType = 0xD2, BatId = " + BatId);
                    BatIdList.add(BatId);
                    r=r+2;
                }
            }
            batWhiteList=new BatWhiteList(BatNumber,BatIdList);
            mBatWhiteList.add(batWhiteList);
        }
        else if(LinkageType == 0xB0){ //miltiple scanning Freq for TBC // leo 20230427 for tbc ota
            final int homeFreq ;
            final int candidateFreq;
            //Log.d(TAG,"LinkageType = 0xB0 miltiple scanning Freq for TBC");
            if((Length-7) > 0) {
                homeFreq = getInt(data, 9, 2, MASK_16BITS);
                candidateFreq = getInt(data, 9 + 2, 2, MASK_16BITS);
                MultiSacnFreq multiSacnFreq = new MultiSacnFreq(homeFreq, candidateFreq);
                //Log.d(TAG,"LinkageType = 0xB0, homeFreq = " + homeFreq);
                //Log.d(TAG,"LinkageType = 0xB0, candidateFreq = " + candidateFreq);
                mMultiSacnFreq.add(multiSacnFreq);
            }
        }
        else if(LinkageType == 0xB8){ // leo 20230427 for tbc ota
            OUI_data_length = getInt(data,9,1,MASK_8BITS);
            //Log.d(TAG,"LinkageType = 0xB8 ota for TBC");
            int r =0;
            while (r<OUI_data_length) {
                final int oui = getInt(data,10+r, 3, MASK_24BITS);
                final int selectorLength= getInt(data, r+13, 1, MASK_8BITS);
                //Log.d(TAG,"LinkageType = 0xB8, oui = " + oui);
                if(OUI_data_length < selectorLength+4)
                    break;
                List<OUIDataTBC> ouidatadmgList = new ArrayList<OUIDataTBC>();
                int s=0;
                while (s<selectorLength) {
                    final int HwVersion = getInt(data, r+14+s,2,MASK_16BITS);
                    final int SwVersion = getInt(data,r+14+2+s,2,MASK_16BITS);
                    final int DownloadPid = getInt(data,r+14+4+s,2,MASK_16BITS);
                    final int ZipCode = getInt(data,r+14+6+s,3,MASK_24BITS);
                    final int OtaType = getInt(data,r+14+9+s,1,MASK_8BITS);
                    final OUIDataTBC oUIDataTBC = new OUIDataTBC(HwVersion, SwVersion, DownloadPid, ZipCode, OtaType);

                    ouidatadmgList.add(oUIDataTBC);
                    s+=5;
                    if((s+5)>selectorLength)
                        break;
                }

                final OUITBC ouiEntry = new OUITBC(oui, ouidatadmgList);
                mOUITbc.add(ouiEntry);
                r=r+4+selectorLength;
            }
        }
        else if(LinkageType == 0xB9){ //eric lin 20240215 dmg ota
            OUI_data_length = getInt(data,9,1,MASK_8BITS);

            int r =0;
            while (r<OUI_data_length) {
                final int oui = getInt(data,10+r, 3, MASK_24BITS);
                final int selectorLength= getInt(data, r+13, 1, MASK_8BITS);

                if(OUI_data_length < selectorLength+4)
                    break;
                List<OUIDataDMG> ouidatadmgList = new ArrayList<OUIDataDMG>();
                int s=0;
                while (s<selectorLength) {
                    final int HwVersion = getInt(data, r+14+s,2,MASK_16BITS);
                    final int SwVersion = getInt(data,r+14+2+s,2,MASK_16BITS);
                    final int DownloadPid = getInt(data,r+14+4+s,2,MASK_16BITS);
                    final int ZipCode = getInt(data,r+14+6+s,3,MASK_24BITS);
                    final int OtaType = getInt(data,r+14+9+s,1,MASK_8BITS);
                    final int StartSn = getInt(data,r+14+10+s,4,MASK_16BITS);
                    final int EndSn = getInt(data,r+14+14+s,4,MASK_16BITS);

                    final OUIDataDMG oUIDataDMG = new OUIDataDMG(HwVersion, SwVersion, DownloadPid, ZipCode, OtaType, StartSn, EndSn);

                    ouidatadmgList.add(oUIDataDMG);
                    s+=5;
                    if((s+5)>selectorLength)
                        break;
                }

                final OUIDMG ouiEntry = new OUIDMG(oui, ouidatadmgList);
                mOUIDmg.add(ouiEntry);
                r=r+4+selectorLength;
            }
        }
        else if (LinkageType == 0xCA){// WV Provision server URL
            int tag1 = getInt(data,9,1,MASK_8BITS);
            int len1 = getInt(data,10,1,MASK_8BITS);
            int value1 = getInt(data,11,len1,MASK_32BITS);
            int tag2 = getInt(data,11+len1,1,MASK_8BITS);
            int len2 = getInt(data,12+len1,1,MASK_8BITS);
            String url = new DVBString(data, 12+len1+1, len2).toString();
            LogUtils.d("LinkageType 0xCA");
            LogUtils.d("Tag1 = "+tag1);
            LogUtils.d("value1 = "+value1);
            LogUtils.d("Tag2 = "+tag2);
            LogUtils.d("url = "+url);
            if(tag1 == 0xA0 && tag2 == 0xA1){
                mWvCasUrlDataList.add(new WvCasUrlData(value1, url));
            }
        }
        else
        {
            Log.w(TAG,"LinkageDescriptor, not implemented linkageType: "+LinkageType +"("+getLinkageTypeString(LinkageType));
        }
    }

    public class OUI {
        public int Oui;
        public List<OUIData> mOUIData = null;//new ArrayList<OUIData>();

        private OUI(int oui, List<OUIData> ouidataList) {
            super();
            this.Oui = oui;
            this.mOUIData = ouidataList;
        }
    }

    public class OUIData {
        public int CustomerId;
        public int Swid;
        public int Force;

        public OUIData(int customerid, int swid, int force) {
            CustomerId = customerid;
            Swid = swid;
            Force = force;
        }
    }

    public class MultiSacnFreq {
        public int HomeFreq;
        public int CandidateFreq;

        private MultiSacnFreq(int homeFreq, int candidateFreq) {
            super();
            this.HomeFreq = homeFreq;
            this.CandidateFreq = candidateFreq;
        }
    }

    public class BatWhiteList {
        public int BatNumber;
        public List<Integer> BatIdList=null;

        private BatWhiteList(int batNumber, List<Integer> batIdList) {
            super();
            this.BatNumber = batNumber;
            this.BatIdList = batIdList;
        }
    }

    public class OUITBC {
        public int Oui;
        public List<OUIDataTBC> mOUIDataTBC ;//= new ArrayList<OUIDataDMG>();

        private OUITBC(int oui, List<OUIDataTBC> ouidataList) {
            super();
            this.Oui = oui;
            this.mOUIDataTBC = ouidataList;
        }
    }
    public class OUIDataTBC {
        public int HwVersion; //2 bytes
        public int SwVersion; //2 bytes
        public int DownloadPid; //2 bytes
        public int ZipCode; //3 bytes
        public int OtaType; //1 bytes

        public OUIDataTBC(int hwVersion, int swVersion, int downloadPid,int zipCode, int otaType) {
            HwVersion = hwVersion;
            SwVersion = swVersion;
            DownloadPid = downloadPid;
            ZipCode = zipCode;
            OtaType = otaType;
        }
    }

    public class OUIDMG {
        public int Oui;
        public List<OUIDataDMG> mOUIDataDMG ;//= new ArrayList<OUIDataDMG>();

        private OUIDMG(int oui, List<OUIDataDMG> ouidataList) {
            super();
            this.Oui = oui;
            this.mOUIDataDMG = ouidataList;
        }
    }

    public class WvCasUrlData{
        public int mStbUpdateTime;
        public String mUrl;

        public WvCasUrlData(int time, String url){
            mStbUpdateTime = time;
            mUrl = url;
        }
    }

    public class OUIDataDMG {
        public int HwVersion; //2 bytes
        public int SwVersion; //2 bytes
        public int DownloadPid; //2 bytes
        public int ZipCode; //3 bytes
        public int OtaType; //1 bytes
        public int StartSn; //4 bytes
        public int EndSn; //4 bytes

        public OUIDataDMG(int hwVersion, int swVersion, int downloadPid,int zipCode, int otaType, int startSn, int endSn) {
            HwVersion = hwVersion;
            SwVersion = swVersion;
            DownloadPid = downloadPid;
            ZipCode = zipCode;
            OtaType = otaType;
            StartSn = startSn;
            EndSn = endSn;
        }
    }

    public static String getLinkageTypeString(final int linkageType) {
        switch (linkageType) {
            case 0x00 : return "reserved for future use";
            case 0x01 : return "information service";
            case 0x02 : return "EPG service";
            case 0x03 : return "CA replacement service";
            case 0x04 : return "TS containing complete Network/Bouquet SI";
            case 0x05 : return "service replacement service";
            case 0x06 : return "data broadcast service";
            case 0x07 : return "RCS Map";
            case 0x08 : return "mobile hand-over";
            case 0x09 : return "System Software Update Service";
            case 0x0A : return "TS containing SSU BAT or NIT";
            case 0x0B : return "IP/MAC Notification Service";
            case 0x0C : return "TS containing INT BAT or NIT";
            case 0x0D : return "event linkage";

            case 0x20 : return "downloadable font info linkage";

            case 0x81 : return "user defined: (linkage to NorDig bootloader)";
            case 0x82 : return "user defined: (NorDig Simulcast replacement service/linkage to Ziggo software update)"; // or NorDig Simulcast replacement service.

            case 0x88 : // fall through
            case 0x89 : // fall through
            case 0x8A : return "user defined: (M7 Fastscan Home TP location descriptor)";
            case 0x8D : return "user defined: (M7 Fastscan ONT location location descriptor)";

            case 0xA0 : return "user defined: link to OpenTV VOD service (YOUSEE)";  // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
            case 0xA6 : return "user defined: link to OpenTV ITV service (YOUSEE)";  // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
            case 0xA7 : return "user defined: link to WEB service (YOUSEE)";  // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf

            case 0xFF : return "reserved for future use";

            default:
                if((0x0E<=linkageType)&&(linkageType<=0x1F )){return "extended event linkage";}
                if((0x0D<=linkageType)&&(linkageType<=0x7F )){return "reserved for future use";}
                if((0x80<=linkageType)&&(linkageType<=0xFE )){return "user defined";}

                return "Illegal value";

        }
    }


}
