package com.prime.dtv.service.Table;


import static com.prime.dtv.service.Table.Desciptor.Descriptor.*;
import static com.prime.dtv.service.Table.StreamType.*;
import static com.prime.dtv.service.Util.Utils.*;
import static java.lang.Byte.toUnsignedInt;

import android.util.Log;

import com.prime.dtv.service.Table.Desciptor.*;
import com.prime.dtv.service.Table.Desciptor.DescBase;
import com.prime.dtv.service.Table.Desciptor.Descriptor;

import java.util.ArrayList;
import java.util.List;

public class PmtData extends TableData {
    private static final String TAG = "PmtData";
    private static int NUM_OF_MULTI_PMT = 3;
    public static int PMT_MAX_VIDEO_PID_NUM = 2;
    public static int PMT_MAX_AUDIO_PID_NUM = 20;
    public static int PMT_MAX_TELETEXT_PID_NUM = 2;

    private static int TTXT_MAX_LANG_NUM = (5+5);
    private static int SUBT_MAX_LANG_NUM = 12;

    private static int TELETEXT_NONE        = 0x00;
    private static int TELETEXT_NO_SUBTITLE = 0x01;
    private static int TELETEXT_SUBTITLE    = 0x02;
    private static int SUBTITLE_ONLY        = 0x03;//dvb_subtitle

    //private List<Program_map> mpmtlist = new ArrayList<Program_map>();
    private Program_map mProgramMap;
    private List<ElementaryStreamInformation> mEsilist = new ArrayList<ElementaryStreamInformation>();
    private Descriptor mprogram_info_Desc  = new Descriptor();
    private byte [] raw_data = null;
    private int raw_data_lens = 0;

    public class Program_map {
        private int PMTVersion;
        private int program_number;
        private int pcr_pid;
        private int[] video_pid = new int[PMT_MAX_VIDEO_PID_NUM];
        private int[] audio_pid = new int[PMT_MAX_AUDIO_PID_NUM];
        private int[] teletext_pid = new int[PMT_MAX_TELETEXT_PID_NUM];
        //private int[][] lang = new int[PMT_MAX_AUDIO_PID_NUM][4];
        private String[] iso639LanguageCode1 = new String[PMT_MAX_AUDIO_PID_NUM];
        private String[] iso639LanguageCode2 = new String[PMT_MAX_AUDIO_PID_NUM];
        private int[] video_stream_type = new int[PMT_MAX_VIDEO_PID_NUM];
        private int[] audio_stream_type = new int[PMT_MAX_AUDIO_PID_NUM];
        private int ca_flag;

        private int ac3_pid;
        private int ttxt_lang_num;
        private int ttxt_desc_number;
        private String[] ttxt_lang = new String[TTXT_MAX_LANG_NUM];
        private int[] ttxt_type = new int[TTXT_MAX_LANG_NUM];
        private int[] ttxt_magazine_number = new int[TTXT_MAX_LANG_NUM];
        private int[] ttxt_page_number = new int[TTXT_MAX_LANG_NUM];

        private int subtitle;//dvb_subtitle
        private int subt_lang_num;
        private String[] subt_lang = new String[SUBT_MAX_LANG_NUM];
        private int[] subt_stream_id = new int[SUBT_MAX_LANG_NUM];

        private int[] subtitling_type = new int[SUBT_MAX_LANG_NUM];

        private int[] subt_com_page_id = new int[SUBT_MAX_LANG_NUM];
        private int[] subt_anc_page_id = new int[SUBT_MAX_LANG_NUM];
        //        pmt_info					*table;
        //        struct program_map_table	*next;
        private int update_pid;
//        private int ca_system_id;
        private int sgt_pid;
		public List<EmergencyInformationDescriptor.EmergencyInformationSid> memergencyList = new ArrayList<>();
        private int caSystemID;
        private List<Integer> caSystemIdList = new ArrayList<>();
        private int caPID;
        private List<Integer> caPidList = new ArrayList<>();
        private byte[] privateDataByte;
        private List<byte[]> privateDataByteList = new ArrayList<>();
        private byte[] pmtDataByte;
        private int pmtDatalen;
        private int pmtPid;
        public int getPMTVersion(){
            return PMTVersion;
        }
        public void setPMTVersion(int version){
            PMTVersion = version;
        }
        public int getProgram_number() {
            return program_number;
        }

        public int getPcr_pid() {
            return pcr_pid;
        }

        public int getVideo_pid(int index) {
            return video_pid[index];
        }

        public int getAudio_pid(int index) {
            return audio_pid[index];
        }

        public int getTeletext_pid(int index) {
            return teletext_pid[index];
        }

        public int getVideo_stream_type(int index) {
            return video_stream_type[index];
        }

        public int getAudio_stream_type(int index) {
            return audio_stream_type[index];
        }

        public int getCa_flag() {
            return ca_flag;
        }

        public int getAc3_pid() {
            return ac3_pid;
        }

        public int getTtxt_lang_num() {
            return ttxt_lang_num;
        }


        public int getTtxt_type(int index) {
            return ttxt_type[index];
        }

        public int getTtxt_magazine_number(int index) {
            return ttxt_magazine_number[index];
        }

        public int getTtxt_page_number(int index) {
            return ttxt_page_number[index];
        }

        public int getSubtitle() {
            return subtitle;
        }

        public int getSubt_lang_num() {
            return subt_lang_num;
        }

        public int getSubt_stream_id(int index) {
            return subt_stream_id[index];
        }

        public int getSubtitling_type(int index) {
            return subtitling_type[index];
        }

        public int getSubt_com_page_id(int index) {
            return subt_com_page_id[index];
        }

        public int getSubt_anc_page_id(int index) {
            return subt_anc_page_id[index];
        }

        public int getUpdate_pid() {
            return update_pid;
        }

//        public int getCa_system_id() {
//            return ca_system_id;
//        }

        public int getSgt_pid() {
            return sgt_pid;
        }

        public String getIso639LanguageCode1(int index) {
            return iso639LanguageCode1[index];
        }
        public String getIso639LanguageCode2(int index) {
            return iso639LanguageCode2[index];
        }
        public String getsubt_lang(int index) {
            return subt_lang[index];
        }

        public String getTtxt_lang(int index) {
            return ttxt_lang[index];
        }

        public int getCaSystemID() {
            return caSystemID;
        }

        public List<Integer> getCaSystemIdList() {
            return caSystemIdList;
        }

        public int getCaPID() {
            return caPID;
        }

        public List<Integer> getCaPidList() {
            return caPidList;
        }

        public byte[] getPrivateDataByte() {
            return privateDataByte;
        }

        public List<byte[]> getPrivateDataByteList() {
            return privateDataByteList;
        }

        public byte[] getPmtDataByte() {
            return pmtDataByte;
        }

        public int getPmtDatalen() {
            return pmtDatalen;
        }

        public int getPmtPid() {
            return pmtPid;
        }

        public void setPmtPid(int pmtPid) {
            this.pmtPid = pmtPid;
        }

        public int getTtxt_desc_number() {
            return ttxt_desc_number;
        }
    }

    public void setProgrammapPid(int pmtPid) {
        mProgramMap.setPmtPid(pmtPid);
    }

    public class ElementaryStreamInformation {
        private int stream_type;
        private int	elementary_stream_pid;
        //private int[][] lang = new int[2][4];//lang[0] => Left ,lang[1]=>Right;
        private int ttype;
        private int ac3_flag;
        private int ttxt_lang_num;
        private int ttxt_desc_number;
        private String[] ttxt_lang = new String[TTXT_MAX_LANG_NUM];
        private int[] ttxt_type = new int[TTXT_MAX_LANG_NUM];
        private int[] ttxt_magazine_number = new int[TTXT_MAX_LANG_NUM];
        private int[] ttxt_page_number = new int[TTXT_MAX_LANG_NUM];
        private int subt_lang_num;
        /*
        private int[][] subt_lang = new int[SUBT_MAX_LANG_NUM][4];
        private int[] subt_stream_id = new int[SUBT_MAX_LANG_NUM];
        private int[] subtitling_type = new int[SUBT_MAX_LANG_NUM];
        private int[] subt_com_page_id = new int[SUBT_MAX_LANG_NUM];
        private int[] subt_anc_page_id = new int[SUBT_MAX_LANG_NUM];
         */
        private int tdata;
        private Descriptor mesiDescriptor  = new Descriptor();

        public List getIso639LanguageCodelist() {
            List<DescBase> descBase = mesiDescriptor.getDescriptorList(ISO639LANG_Desc);
            if(!descBase.isEmpty())
            {
                ISO639LanguageDescriptor tempDescriptor = (ISO639LanguageDescriptor) descBase.get(0);
                return tempDescriptor.getLanguageList();
            }
            return null;
        }

        public List<SubtitlingDescriptor.Subtitle> getsubtitleList() {
            List<DescBase> descBase = mesiDescriptor.getDescriptorList(SUBTITLE_DESC);
            if(!descBase.isEmpty())
            {
                SubtitlingDescriptor tempDescriptor = (SubtitlingDescriptor) descBase.get(0);
                return tempDescriptor.getSubtitleList();
            }
            return null;
        }
		

        public List getemergList() {
            List<DescBase> descBase = mesiDescriptor.getDescriptorList(EMERGENCY_INFORMATION_DESC);
            if(!descBase.isEmpty())
            {
                EmergencyInformationDescriptor tempDescriptor = (EmergencyInformationDescriptor) descBase.get(0);
                return tempDescriptor.getemergList();
            }
            return null;
        }
    }

    public List<ElementaryStreamInformation> getEsiList() {
        return mEsilist;
    }

//    public List<PmtData.Program_map> getpmts() {
//        return mpmtlist;
//    }
    public Program_map getProgramMap(){
        return mProgramMap;
    }


    public CADescriptor getcadesc() {
        List<DescBase> descBase = mprogram_info_Desc.getDescriptorList(CA_DESC);
        if (!descBase.isEmpty())
        {
            CADescriptor tempDescriptor = (CADescriptor) descBase.get(0);
            return tempDescriptor;
        }
        return null;
    }

//    public int getPmtDataTotalNum() {
//        if(mpmtlist != null)
//            return mpmtlist.size();
//        else
//            return 0;
//    }

    public void buildesiList(byte[] data, int offset, int lens) {
        int i=0, j=0, ttxt_desc_number, ttxt_lang_number, subt_lang_number;
        int	es_info_length;
        int array_shift;
        int desc_length;
        for (i = offset; i < lens;) {
            ttxt_lang_number = 0;
            ttxt_desc_number = 0;
            ElementaryStreamInformation esiData = new ElementaryStreamInformation();
            esiData.stream_type = toUnsignedInt(data[i]);
            esiData.elementary_stream_pid = (256 * (toUnsignedInt(data[i+1]) & 0x1f)) + toUnsignedInt(data[i+2]);
            //Log.d(TAG,"stream_type =" + esiData.stream_type +" elementary_stream_pid =" + esiData.elementary_stream_pid );

            if((esiData.stream_type == 0x80)||(esiData.stream_type == 0x82)||(esiData.stream_type == 0x84))//eric lin 20110617 add irdeto dl flag,-s
            {
                esiData.tdata =  0x80;
                //TABLE_TRACE_INFO("Parse esi SGT Pid = %x\n",new_node->elementary_stream_pid);
            }//eric lin 20110617 add irdeto dl flag,-e
            es_info_length = (256 * (toUnsignedInt(data[i+3]) & 0xf)) + toUnsignedInt(data[i+4]);
            array_shift = i+5;
            for(j=0; j<es_info_length; j+=toUnsignedInt(data[array_shift+j+1])+2){
                //Log.d(TAG,"esi_desciptor [" + toUnsignedInt(data[array_shift+j]) + "]");
                switch (toUnsignedInt(data[array_shift + j])) {
                    case TELETEXT_DESC:
                    {
                        int l, des_length;
                        int ttxt_type;
                        int ttxt_shift;

                        des_length = toUnsignedInt(data[array_shift+j+1]);
                        ttxt_shift = array_shift + j+ 2;
                        //TABLE_TRACE_INFO("\n TELETEXT_DESCRIPTOR des_length = [%d]",des_length);
                        if(ttxt_lang_number >9)//ethan 2008/3/13 09:59�W�� because we only save 10 teletext sub descriptor
                            break;
                        ttxt_desc_number++;
                        if(des_length == 0)//Vincent 20031020 ���ᶶ��bug
                            esiData.ttype = TELETEXT_SUBTITLE; //denny20030611 for Tursat1c teletext error
                        for(l=0; l < des_length; l+= 5)
                        {
                            ttxt_type = (toUnsignedInt(data[ttxt_shift+l+3]) & 0xff) >> 3;

                            //TABLE_TRACE_INFO("\n teletext_type = 0x%x",(temp_pointer[l + 3] & 0xf8) >> 3);
                            switch(ttxt_type)
                            {
                                case 0x01://Init Teletext Page
                                {
                                    //Log.d(TAG, "Init Teletext Page");
                                }
                                case 0x02://Teletext Subtitle page
                                {
                                    esiData.ttxt_lang[ttxt_lang_number] = getISO8859_1String(data, ttxt_shift+l, 3);
                                    esiData.ttxt_type[ttxt_lang_number] = ttxt_type;
                                    esiData.ttxt_magazine_number[ttxt_lang_number] = toUnsignedInt(data[ttxt_shift+l+3]) & 0x07;
                                    esiData.ttxt_page_number[ttxt_lang_number] = toUnsignedInt(data[ttxt_shift+l+4]);
                                    //								TABLE_TRACE_INFO("\n ISO_LANG = [%s]",new_node->ttxt_lang[ttxt_lang_number]);
                                    //								TABLE_TRACE_INFO("\n teletext_magazine_number = 0x%x",new_node->ttxt_magazine_number[ttxt_lang_number]);
                                    //								TABLE_TRACE_INFO("\n teletexttext_page_number = 0x%x",new_node->ttxt_page_number[ttxt_lang_number]);

                                    ttxt_lang_number ++;

                                    esiData.ttxt_lang_num = ttxt_lang_number;
                                    esiData.ttype = TELETEXT_SUBTITLE;
                                }
                                break;
                                default:
                                {
                                    if(esiData.ttype!=TELETEXT_SUBTITLE)
                                        esiData.ttype = TELETEXT_NO_SUBTITLE;
                                }
                                break;
                            }
                            if(ttxt_lang_number > TTXT_MAX_LANG_NUM-1)
                                break;
                            //						TABLE_TRACE_INFO("\n %x-----------------%d-------------------------------",new_node,ttxt_lang_number);
                        }
                        //TABLE_TRACE_INFO("\n111111111111111111111111 new_node->ttxt_lang_num[%d]",new_node->ttxt_lang_num);
                        //TABLE_TRACE_INFO("\n222222222222222222222222 new_node->elementary_stream_pid[%x]",new_node->elementary_stream_pid);
                    }
                    break;
                    case SUBTITLE_DESC://SUBTITLE_DESCRIPTOR
                    {
                        esiData.ttype = SUBTITLE_ONLY;//SUBTITLE_ONLY
                    }
                    break;
                    case 0x6a:
                        esiData.ac3_flag = 1;
                        break;
                    case 0x7a://eric lin 20090220 support DD+
                        esiData.ac3_flag = 2;
                        break;
                }
                desc_length = toUnsignedInt(data[array_shift + j + 1]);
                byte[] tempData = new byte[desc_length + 2];
                System.arraycopy(data, array_shift+j, tempData, 0, desc_length + 2);
                esiData.mesiDescriptor.ParsingDescriptor(tempData, desc_length + 2);

            }
            esiData.ttxt_desc_number = ttxt_desc_number;
            i += es_info_length + 5;
            mEsilist.add(esiData);
        }

    }

    public void buildpmtList(byte[] data, int pcr_pid, int program_number, int ca_flag){
        int i=0, j=0, k=0;
        final Program_map pmt_data = new Program_map();
        List<ElementaryStreamInformation> esilist = getEsiList();

        pmt_data.PMTVersion = (toUnsignedInt(data[5]) & 0x3e) >> 1;
        pmt_data.pcr_pid = pcr_pid;
        pmt_data.program_number = program_number;
        pmt_data.ca_flag = ca_flag;
        pmt_data.subtitle = 0;
        pmt_data.subt_lang_num = 0;
        pmt_data.pmtDatalen = getInt(data,1,2, 0x0FFF)+3;
        pmt_data.pmtDataByte = new byte[pmt_data.pmtDatalen];
        System.arraycopy(data, 0, pmt_data.pmtDataByte, 0, pmt_data.pmtDatalen);
        //Log.d(TAG,"buildpmtList  esilist size = "+ esilist.size());
        for(ElementaryStreamInformation temp_e : esilist){
            //TABLE_TRACE_INFO("temp_e->stream_type = %d ( PID[%d] ) \n",temp_e->stream_type,temp_e->elementary_stream_pid);
            //Log.d(TAG,"buildpmtList  stream_type = "+ temp_e.stream_type);
            //Log.d(TAG,"buildpmtList  elementary_stream_pid = "+ temp_e.elementary_stream_pid);
            switch(temp_e.stream_type){
                case STREAM_AAC_AUDIO ://Justin 20080612 add for AAC
                case STREAM_HEAAC_AUDIO ://Justin 20080612 add for HEAAC
                case STREAM_DDPLUS_AUDIO://Jimmy-2009.02.17, add audio stream type DDPLUS
                case STREAM_MPEG1_AUDIO :
                case STREAM_MPEG2_AUDIO :
                    if (i < PMT_MAX_AUDIO_PID_NUM)
                    {
                        pmt_data.audio_pid[i] = temp_e.elementary_stream_pid;
                        pmt_data.audio_stream_type[i] = temp_e.stream_type;
                        List<ISO639LanguageDescriptor.Language> templanguageList = temp_e.getIso639LanguageCodelist();
                        if(templanguageList != null) {
                            pmt_data.iso639LanguageCode1[i] = templanguageList.get(0).getIso639LanguageCode();
                            //Log.d(TAG,"LanguageCode1 = "+ pmt_data.iso639LanguageCode1[i] );
                            if (templanguageList.size() > 1)
                                pmt_data.iso639LanguageCode2[i] = templanguageList.get(1).getIso639LanguageCode();
                        }
                        //TABLE_TRACE_INFO("audio_pid [%x]\n", temp_e->elementary_stream_pid);
                        //TABLE_TRACE_INFO("stream_type [%x]\n", temp_e->stream_type);
                        //TABLE_TRACE_INFO("lang [%s]\n", temp_e->lang);
                        i++;
                    }
                    break;
                case STREAM_AVS_VIDEO:
                case STREAM_MPEG4_VIDEO:
                case STREAM_HEVC_VIDEO:
                case STREAM_MPEG4_H264_VIDEO://denny20060407 when search the channel of H264, the channel will be sort as Radio.//centaur20060417
                case STREAM_MPEG1_VIDEO :
                case STREAM_MPEG2_VIDEO :
                    if (j < PMT_MAX_VIDEO_PID_NUM)
                    {

                        pmt_data.video_pid[j] = temp_e.elementary_stream_pid;
                        pmt_data.video_stream_type[j] = temp_e.stream_type;
                        //TABLE_TRACE_INFO("new_node->video_pid[%d] = 0x%x\n", j,new_node->video_pid[j]);
                        //TABLE_TRACE_INFO("new_node->video_stream_type[%d] = 0x%x\n",j, new_node->video_stream_type[j]);
                        j++;
                        //TABLE_TRACE_INFO("video_pid [%x]\n", temp_e->elementary_stream_pid);
                        //TABLE_TRACE_INFO("video_stream_type [%x]\n", temp_e->stream_type);
                        //TABLE_TRACE_INFO("\n **** link_esi_to_pmt()... video%d[%x] ", j, new_node->video_pid[j - 1]);
                    }
                    break;
                case STREAM_TELETEXT :
                    if(temp_e.ac3_flag > 0)
                    {
                        pmt_data.audio_pid[i] = temp_e.elementary_stream_pid;
                        if(temp_e.ac3_flag ==1)//eric lin 20090220 support DD+
                            pmt_data.audio_stream_type[i] = STREAM_AC3_AUDIO;
                        else if(temp_e.ac3_flag ==2)//eric lin 20090220 support DD+
                            pmt_data.audio_stream_type[i] = STREAM_DDPLUS_AUDIO;
                        List<ISO639LanguageDescriptor.Language> templanguageList = temp_e.getIso639LanguageCodelist();
                        if(templanguageList != null)
                            pmt_data.iso639LanguageCode1[i] = templanguageList.get(0).getIso639LanguageCode();
                        //TABLE_TRACE_INFO("audio_pid [%x]\n", new_node->audio_pid[i]);
                        //TABLE_TRACE_INFO("stream_type [%x]\n", new_node->audio_stream_type[i]);
                        //TABLE_TRACE_INFO("lang [%s]\n", new_node->lang[i]);
                        i++;	//Eric_Lin 20081121 fix AC3 not in Audio list, unmark
                    }
                    else if(temp_e.ttype == SUBTITLE_ONLY) //dvb_subtitle
                    {
                        int index;
                        if (pmt_data.subt_lang_num>=SUBT_MAX_LANG_NUM)
                            break;
                        List<SubtitlingDescriptor.Subtitle> tempsubtList = temp_e.getsubtitleList();
                        if(tempsubtList != null) {
                            for (index = 0; index < /*temp_e.subt_lang_num*/tempsubtList.size(); index++) {
                                if (index >= SUBT_MAX_LANG_NUM)
                                    break;
                                pmt_data.subt_stream_id[(int) pmt_data.subt_lang_num] = temp_e.elementary_stream_pid;
                                pmt_data.subt_lang[pmt_data.subt_lang_num] = tempsubtList.get(index).getIso639LanguageCode();
                                pmt_data.subtitling_type[pmt_data.subt_lang_num] = tempsubtList.get(index).getSubtitlingType();
                                pmt_data.subt_com_page_id[pmt_data.subt_lang_num] = tempsubtList.get(index).getCompositionPageId();
                                pmt_data.subt_anc_page_id[pmt_data.subt_lang_num] = tempsubtList.get(index).getAncillaryPageId();
                                pmt_data.subt_lang_num++;//johnny20060113 modify parsing esi about dvb subtitle
                            }
                            pmt_data.subtitle = 1;
                        }
                        break;
                    }
                    else if(k < PMT_MAX_TELETEXT_PID_NUM)
                    {
                        if(temp_e.ttype == TELETEXT_NO_SUBTITLE)
                        {
                            pmt_data.teletext_pid[k++] = temp_e.elementary_stream_pid;
                            //printf("elementary_stream_pid [0x%lx]", temp_e->elementary_stream_pid);
                        }
                        else if(temp_e.ttype == TELETEXT_SUBTITLE)
                        {
                            int teletext_subtitle_cnt = 0;//for teletext init page
                            pmt_data.teletext_pid[k++] = temp_e.elementary_stream_pid;
                            //						TABLE_TRACE_INFO("\nnew_node->teletext_pid[%x]",new_node->teletext_pid[0]);
                            //TABLE_TRACE_INFO("\n QQQQQQ11 ...prg=0x%04x, ttxtpid=0x%04x, tag=0x%02x ", new_node->program_number, temp_e->elementary_stream_pid, temp_e->ttype);
                            //						TABLE_TRACE_INFO("\n **** link_esi_to_pmt()... teletext%d[%x] ", k, new_node->teletext_pid[k - 1]);
                            pmt_data.ttxt_lang_num = temp_e.ttxt_lang_num;

                            if(k==1)
                            {
                                //							TABLE_TRACE_INFO("\ntemp_e->ttxt_lang_num[%d]",temp_e->ttxt_lang_num);
                                for(int ttxt_lang = 0; ttxt_lang < temp_e.ttxt_lang_num; ttxt_lang ++)
                                {
                                    pmt_data.ttxt_lang[ttxt_lang] = temp_e.ttxt_lang[ttxt_lang];
                                    pmt_data.ttxt_type[ttxt_lang] = temp_e.ttxt_type[ttxt_lang];//for teletext init page
                                    pmt_data.ttxt_magazine_number[ttxt_lang] = temp_e.ttxt_magazine_number[ttxt_lang];
                                    pmt_data.ttxt_page_number[ttxt_lang] = temp_e.ttxt_page_number[ttxt_lang];
                                    //								TABLE_TRACE_INFO("\n -----%d-------- %s --------------",ttxt_lang,new_node->ttxt_lang[ttxt_lang]);
                                    if(temp_e.ttxt_type[ttxt_lang] == 0x02)//for teletext init page
                                        teletext_subtitle_cnt++;
                                    if(ttxt_lang > TTXT_MAX_LANG_NUM-1)
                                        break;
                                }
                            }
                            pmt_data.ttxt_lang_num = teletext_subtitle_cnt;//for teletext init page
                            pmt_data.ttxt_desc_number = temp_e.ttxt_desc_number;
                        }
                    }
                    break;
                default:
                    break;

            }
            if (temp_e.tdata == 0x66)
            {
                if(temp_e.stream_type == 0x0B)
                {
                    //Log.d(TAG, "link_esi_to_pmt get unt pid = " + temp_e.elementary_stream_pid);
                    pmt_data.audio_pid[1] = temp_e.elementary_stream_pid;// UNT PID
                }
                //Log.d(TAG, "link_esi_to_pmt get dsmcc pid");
                pmt_data.update_pid = temp_e.elementary_stream_pid;// DSMCC PID
            }

            if (temp_e.tdata == 0x80) //eric lin 20110617 add irdeto dl flag
            {
                pmt_data.update_pid = temp_e.elementary_stream_pid;
            }

            List<EmergencyInformationDescriptor.EmergencyInformationSid> tempemergList = temp_e.getemergList();
            if(tempemergList != null)
                pmt_data.memergencyList.addAll(tempemergList);

            // ca from program stream(in video or audio section of pmt program)
            // e.g. google wvcas stream(bbb_1080p_30fps_mp3_enc_xxx.ts)
            List<DescBase> caDescriptors = temp_e.mesiDescriptor.getDescriptorList(CA_DESC);
            for (DescBase descriptor : caDescriptors) {
                CADescriptor caDescriptor = (CADescriptor) descriptor;
                pmt_data.caSystemIdList.add(caDescriptor.getCaSystemID());
                pmt_data.caPidList.add(caDescriptor.getCaPID());
                pmt_data.privateDataByteList.add(caDescriptor.getPrivateDataByte());
            }
        }

        // ca from program (in pmt program section of pmt table)
        // e.g. alti wvcas stream
        List<DescBase> caDescriptors = mprogram_info_Desc.getDescriptorList(CA_DESC);
        for (DescBase descriptor : caDescriptors) {
            CADescriptor caDescriptor = (CADescriptor) descriptor;
            pmt_data.caSystemIdList.add(caDescriptor.getCaSystemID());
            pmt_data.caPidList.add(caDescriptor.getCaPID());
            pmt_data.privateDataByteList.add(caDescriptor.getPrivateDataByte());
        }

        mProgramMap = pmt_data;
        //mpmtlist.add(pmt_data);

    }

    public byte[] getRaw_data(){
        if(raw_data != null){
           return raw_data;
        }
        return null;
    }

    @Override
    public void parsing(byte[] data, int lens) {
        int	section_length, program_info_length;
        int i;
        int desc_length;
        int pcr_pid, program_number, ca_flag=0;
        try {
            raw_data = new byte[lens];
            raw_data_lens = lens;
            System.arraycopy(data, 0, raw_data, 0, lens);

            pcr_pid = getInt(data, 8, 2, 0x1FFF);
            program_number = getInt(data, 3, 2, MASK_16BITS);

            if (data[7] == 0) {
                /* if this is the first section of the pmt table */
                ca_flag = 0;
            }

            section_length = getInt(data, 1, 2, 0x0FFF);
            program_info_length = getInt(data, 10, 2, 0x0FFF);
            Log.d(TAG, "section_length = " + section_length + " program_info_length [" + program_info_length + "]");
            for (i = 0; i < program_info_length; i += (toUnsignedInt(data[12 + i + 1]) + 2)) {
                if (data[12 + i] == 9) {
                    ca_flag = 1;
                }
                Log.d(TAG, "TAG [" + toUnsignedInt(data[12 + i]) + "]");
                desc_length = toUnsignedInt(data[12 + i + 1]);
                Log.d(TAG, "desc_length [" + desc_length + "]");
                byte[] Data = new byte[desc_length + 2];
                System.arraycopy(data, 12 + i, Data, 0, desc_length + 2);
                if (data[12 + i] == EMERGENCY_INFORMATION_DESC) {
                    ElementaryStreamInformation esiData = new ElementaryStreamInformation();
                    esiData.mesiDescriptor.ParsingDescriptor(Data, desc_length + 2);
                    mEsilist.add(esiData);
                } else {
                    mprogram_info_Desc.ParsingDescriptor(Data, desc_length + 2);
                }
            }

            buildesiList(data, 12 + program_info_length, section_length - 1);

            buildpmtList(data, pcr_pid, program_number, ca_flag);
        }
        catch (Exception e){
            Log.e(TAG, "e = "+e);
            e.printStackTrace();
        }
    }

}
