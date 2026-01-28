package com.prime.dtv.sysdata;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.prime.dmg.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.dmg.launcher.Utils.JsonParser.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class MusicInfo {
    private static final String TAG = "MusicInfo";

    @SerializedName("name")
    private String g_category_name;
    @SerializedName("servicelists")
    private ArrayList<Integer> g_service_id_list;
    @SerializedName("icon")
    private String g_url;

    public void set_url(String url) {
        g_url = url;
    }

    public String get_url() {
        return g_url;
    }

    public void set_category_name(String categoryName) {
        g_category_name = categoryName;
    }

    public String get_category_name() {
        return g_category_name;
    }

    public void set_service_id_list(ArrayList<Integer> serviceIdList) {
        g_service_id_list = serviceIdList;
    }

    public ArrayList<Integer> get_service_id_list() {
        return g_service_id_list;
    }

    public void to_string() {
        Log.d(TAG, "to_string: Category Name: = " + g_category_name + " Icon Url: = " + g_url + " Service List: = " + g_service_id_list);
    }

    public static List<MusicInfo> get_current_category(Context context) {
        String musicList = ACSDataProviderHelper.get_acs_provider_data(context, "music_category");
        //String musicList = "[\n" + "        {\n" + "            \"name\": \"華語音樂\",\n" + "            \"icon\": \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586330858497.png\",\n" + "            \"sort\": 1,\n" + "            \"servicelists\": [\n" + "                \"1534\",\n" + "                \"1535\",\n" + "                \"1536\",\n" + "                \"1543\"\n" + "            ]\n" + "        },\n" + "        {\n" + "            \"name\": \"西洋音樂\",\n" + "            \"icon\": \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586330890167.png\",\n" + "            \"sort\": 2,\n" + "            \"servicelists\": [\n" + "                \"1514\",\n" + "                \"1515\",\n" + "                \"1516\",\n" + "                \"1517\",\n" + "                \"1518\",\n" + "                \"1519\",\n" + "                \"1521\"\n" + "            ]\n" + "        },\n" + "        {\n" + "            \"name\": \"東洋音樂\",\n" + "            \"icon\": \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586330824653.png\",\n" + "            \"sort\": 3,\n" + "            \"servicelists\": [\n" + "                \"1525\",\n" + "                \"1526\",\n" + "                \"1527\"\n" + "            ]\n" + "        },\n" + "        {\n" + "            \"name\": \"沙發音樂\",\n" + "            \"icon\": \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586330938131.png\",\n" + "            \"sort\": 4,\n" + "            \"servicelists\": [\n" + "                \"1522\",\n" + "                \"1523\",\n" + "                \"1524\",\n" + "                \"1528\",\n" + "                \"1531\",\n" + "                \"1532\"\n" + "            ]\n" + "        },\n" + "        {\n" + "            \"name\": \"古典音樂\",\n" + "            \"icon\": \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586330777070.png\",\n" + "            \"sort\": 5,\n" + "            \"servicelists\": [\n" + "                \"1529\",\n" + "                \"1530\",\n" + "                \"1537\"\n" + "            ]\n" + "        },\n" + "        {\n" + "            \"name\": \"其他音樂\",\n" + "            \"icon\": \"https://acs-ota.tbcnet.net.tw/app_bd201/ico/1586331013427.png\",\n" + "            \"sort\": 6,\n" + "            \"servicelists\": [\n" + "                \"1520\",\n" + "                \"1533\",\n" + "                \"1538\",\n" + "                \"1539\",\n" + "                \"1540\",\n" + "                \"1541\",\n" + "                \"1542\"\n" + "            ]\n" + "        }\n" + "    ]";
        Log.d(TAG, "get_current_category: musicList = " + musicList);

        if (musicList == null)
            return new ArrayList<>();

        return JsonParser.parse_music_info(musicList);
    }
}
