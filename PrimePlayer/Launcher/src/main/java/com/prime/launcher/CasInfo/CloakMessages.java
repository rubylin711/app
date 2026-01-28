package com.prime.launcher.CasInfo;

import android.content.Context;
import android.util.Log;

import com.prime.launcher.R;
import com.prime.datastructure.Ca.IrdetoErrorCode;

import java.util.HashMap;
import java.util.Map;

public class CloakMessages {
    private static final String TAG = "CloakMessages";
    private static final Map<String, Integer> MESSAGE_MAP = new HashMap<>();
    static {
        MESSAGE_MAP.put("E037-0", R.string.E037_0);
        MESSAGE_MAP.put("E038-0", R.string.E038_0);
        MESSAGE_MAP.put("E039-0", R.string.E039_0);
        MESSAGE_MAP.put("E040-0", R.string.E040_0);
        MESSAGE_MAP.put("E041-0", R.string.E041_0);
        MESSAGE_MAP.put("E042-0", R.string.E042_0);
        MESSAGE_MAP.put("E044-0", R.string.E044_0);
        MESSAGE_MAP.put("E045-0", R.string.E045_0);
        MESSAGE_MAP.put("E046-0", R.string.E046_0);
        MESSAGE_MAP.put("E047-0", R.string.E047_0);
        MESSAGE_MAP.put("E048-32", R.string.E048_32);
        MESSAGE_MAP.put("E049-0", R.string.E049_0);
        MESSAGE_MAP.put("E050-0", R.string.E050_0);
        MESSAGE_MAP.put("E052-32", R.string.E052_32);
        MESSAGE_MAP.put("E064-0", R.string.E064_0);
        MESSAGE_MAP.put("D029-0", R.string.D029_0);
        MESSAGE_MAP.put("D100-0", R.string.D100_0);
        MESSAGE_MAP.put("D101-0", R.string.D101_0);
        MESSAGE_MAP.put("D126-0", R.string.D126_0);
        MESSAGE_MAP.put("D127-0", R.string.D127_0);
        MESSAGE_MAP.put("I300-0", R.string.I300_0);
        MESSAGE_MAP.put("I301-0", R.string.I301_0);
        MESSAGE_MAP.put("I302-0", R.string.I302_0);
        MESSAGE_MAP.put("I303-0", R.string.I303_0);
        MESSAGE_MAP.put("I304-0", R.string.I304_0);
        MESSAGE_MAP.put("I305-0", R.string.I305_0);
        MESSAGE_MAP.put("I306-0", R.string.I306_0);
        MESSAGE_MAP.put("I307-0", R.string.I307_0);
        MESSAGE_MAP.put("I308-0", R.string.I308_0);
        MESSAGE_MAP.put("I309-0", R.string.I309_0);
        MESSAGE_MAP.put("I310-0", R.string.I310_0);
        MESSAGE_MAP.put("I311-0", R.string.I311_0);
        MESSAGE_MAP.put("I312-0", R.string.I312_0);
        MESSAGE_MAP.put("I313-0", R.string.I313_0);
        MESSAGE_MAP.put("I314-0", R.string.I314_0);
        MESSAGE_MAP.put("I315-0", R.string.I315_0);
        MESSAGE_MAP.put("I316-0", R.string.I316_0);
        MESSAGE_MAP.put("I317-0", R.string.I317_0);
        MESSAGE_MAP.put("I318-0", R.string.I318_0);
        MESSAGE_MAP.put("I319-0", R.string.I319_0);
        MESSAGE_MAP.put("I320-0", R.string.I320_0);
        MESSAGE_MAP.put("I321-0", R.string.I321_0);
        MESSAGE_MAP.put("I322-0", R.string.I322_0);
        MESSAGE_MAP.put("I323-0", R.string.I323_0);
        MESSAGE_MAP.put("I324-0", R.string.I324_0);
        MESSAGE_MAP.put("I325-0", R.string.I325_0);
        MESSAGE_MAP.put("I326-0", R.string.I326_0);
        MESSAGE_MAP.put("I327-0", R.string.I327_0);
        MESSAGE_MAP.put("I328-0", R.string.I328_0);
        MESSAGE_MAP.put("I329-0", R.string.I329_0);
        MESSAGE_MAP.put("I330-0", R.string.I330_0);
        MESSAGE_MAP.put("I331-0", R.string.I331_0);
        MESSAGE_MAP.put("I332-0", R.string.I332_0);
        MESSAGE_MAP.put("I333-0", R.string.I333_0);
        MESSAGE_MAP.put("I334-0", R.string.I334_0);
        MESSAGE_MAP.put("I335-0", R.string.I335_0);
        MESSAGE_MAP.put("I336-0", R.string.I336_0);
        MESSAGE_MAP.put("I337-0", R.string.I337_0);
        MESSAGE_MAP.put("I338-0", R.string.I338_0);
        MESSAGE_MAP.put("I339-0", R.string.I339_0);
        MESSAGE_MAP.put("I340-0", R.string.I340_0);
        MESSAGE_MAP.put("I341-0", R.string.I341_0);
        MESSAGE_MAP.put("I342-0", R.string.I342_0);
        MESSAGE_MAP.put("I101-0", R.string.I101_0);
        MESSAGE_MAP.put("I102-0", R.string.I102_0);
        MESSAGE_MAP.put("I103-0", R.string.I103_0);
        MESSAGE_MAP.put("I104-0", R.string.I104_0);
        MESSAGE_MAP.put("I106-0", R.string.I106_0);
        MESSAGE_MAP.put("E016-0", R.string.E016_0);
        MESSAGE_MAP.put("E017-0", R.string.E017_0);
        MESSAGE_MAP.put("E018-0", R.string.E018_0);
        MESSAGE_MAP.put("E030-0", R.string.E030_0);
        MESSAGE_MAP.put("E031-0", R.string.E031_0);
        MESSAGE_MAP.put("E032-0", R.string.E032_0);
        MESSAGE_MAP.put("E033-0", R.string.E033_0);
        MESSAGE_MAP.put("E055-0", R.string.E055_0);
        MESSAGE_MAP.put("E094-0", R.string.E094_0);
        MESSAGE_MAP.put("E111-0", R.string.E111_0);
        MESSAGE_MAP.put("E128-0", R.string.E128_0);
        MESSAGE_MAP.put("E129-0", R.string.E129_0);
        MESSAGE_MAP.put("E130-0", R.string.E130_0);
        MESSAGE_MAP.put("E131-0", R.string.E131_0);
        MESSAGE_MAP.put("E132-0", R.string.E132_0);
        MESSAGE_MAP.put("E133-0", R.string.E133_0);
        MESSAGE_MAP.put("E134-0", R.string.E134_0);
        MESSAGE_MAP.put("E135-0", R.string.E135_0);
        MESSAGE_MAP.put("E138-0", R.string.E138_0);
        MESSAGE_MAP.put("E139-0", R.string.E139_0);
        MESSAGE_MAP.put("E140-0", R.string.E140_0);
        MESSAGE_MAP.put("E150-0", R.string.E150_0);
        MESSAGE_MAP.put("E151-0", R.string.E151_0);
        MESSAGE_MAP.put("E157-0", R.string.E157_0);
        MESSAGE_MAP.put("E160-0", R.string.E160_0);
        MESSAGE_MAP.put("E161-0", R.string.E161_0);
        MESSAGE_MAP.put("E162-0", R.string.E162_0);
        MESSAGE_MAP.put("E163-0", R.string.E163_0);
        MESSAGE_MAP.put("E164-0", R.string.E164_0);
        MESSAGE_MAP.put("E165-0", R.string.E165_0);
        MESSAGE_MAP.put("E166-0", R.string.E166_0);
        MESSAGE_MAP.put("E167-0", R.string.E167_0);
        MESSAGE_MAP.put("E169-0", R.string.E169_0);
        MESSAGE_MAP.put("E170-0", R.string.E170_0);
        MESSAGE_MAP.put("E171-0", R.string.E171_0);
        MESSAGE_MAP.put("E172-0", R.string.E172_0);
        MESSAGE_MAP.put("E173-0", R.string.E173_0);
        MESSAGE_MAP.put("E174-0", R.string.E174_0);
        MESSAGE_MAP.put("E175-0", R.string.E175_0);
        MESSAGE_MAP.put("E176-0", R.string.E176_0);
        MESSAGE_MAP.put("E600-0", R.string.E600_0);
        MESSAGE_MAP.put("D000-4", R.string.D000_4);
        MESSAGE_MAP.put("D029-4", R.string.D029_4);
        MESSAGE_MAP.put("D100-5", R.string.D100_5);
        MESSAGE_MAP.put("D100-8", R.string.D100_8);
        MESSAGE_MAP.put("D101-6", R.string.D101_6);
        MESSAGE_MAP.put("D102-9", R.string.D102_9);
        MESSAGE_MAP.put("D105-9", R.string.D105_9);
        MESSAGE_MAP.put("D106-9", R.string.D106_9);
        MESSAGE_MAP.put("I007-4", R.string.I007_4);
        MESSAGE_MAP.put("I034-9", R.string.I034_9);
        MESSAGE_MAP.put("I055-4", R.string.I055_4);
        MESSAGE_MAP.put("I102-4", R.string.I102_4);
        MESSAGE_MAP.put("I139-4", R.string.I139_4);
        MESSAGE_MAP.put("E000-32", R.string.E000_32);
        MESSAGE_MAP.put("E004-4", R.string.E004_4);
        MESSAGE_MAP.put("E005-4", R.string.E005_4);
        MESSAGE_MAP.put("E006-4", R.string.E006_4);
        MESSAGE_MAP.put("E016-4", R.string.E016_4);
        MESSAGE_MAP.put("E017-13", R.string.E017_13);
        MESSAGE_MAP.put("E018-4", R.string.E018_4);
        MESSAGE_MAP.put("E019-4", R.string.E019_4);
        MESSAGE_MAP.put("E030-4", R.string.E030_4);
        MESSAGE_MAP.put("E032-4", R.string.E032_4);
        MESSAGE_MAP.put("E033-4", R.string.E033_4);
        MESSAGE_MAP.put("E038-32", R.string.E038_32);
        MESSAGE_MAP.put("E045-32", R.string.E045_32);
        MESSAGE_MAP.put("E080-35", R.string.E080_35);
        MESSAGE_MAP.put("E081-35", R.string.E081_35);
        MESSAGE_MAP.put("E101-4", R.string.E101_4);
        MESSAGE_MAP.put("E106-9", R.string.E106_9);
        MESSAGE_MAP.put("E107-4", R.string.E107_4);
        MESSAGE_MAP.put("E108-4", R.string.E108_4);
        MESSAGE_MAP.put("E109-4", R.string.E109_4);
        MESSAGE_MAP.put("E136-4", R.string.E136_4);
        MESSAGE_MAP.put("E137-4", R.string.E137_4);
        MESSAGE_MAP.put("E140-4", R.string.E140_4);
        MESSAGE_MAP.put("E141-4", R.string.E141_4);
        MESSAGE_MAP.put("E142-4", R.string.E142_4);
        MESSAGE_MAP.put("E144-4", R.string.E144_4);
        MESSAGE_MAP.put("E152-4", R.string.E152_4);
        MESSAGE_MAP.put("D500-0", R.string.D500_0);
        MESSAGE_MAP.put("I343-0", R.string.I343_0);
        MESSAGE_MAP.put("I344-0", R.string.I344_0);
        MESSAGE_MAP.put("I345-0", R.string.I345_0);
        MESSAGE_MAP.put("I346-0", R.string.I346_0);
        MESSAGE_MAP.put("I347-0", R.string.I347_0);
        MESSAGE_MAP.put("I348-0", R.string.I348_0);
        MESSAGE_MAP.put("I349-0", R.string.I349_0);
        MESSAGE_MAP.put("I350-0", R.string.I350_0);
        MESSAGE_MAP.put("I351-0", R.string.I351_0);
        MESSAGE_MAP.put("I352-0", R.string.I352_0);
        MESSAGE_MAP.put("I353-0", R.string.I353_0);
        MESSAGE_MAP.put("E015-0", R.string.E015_0);
        MESSAGE_MAP.put("E501-0", R.string.E501_0);
        MESSAGE_MAP.put("E502-0", R.string.E502_0);
        MESSAGE_MAP.put("E503-0", R.string.E503_0);
        MESSAGE_MAP.put("E504-0", R.string.E504_0);
        MESSAGE_MAP.put("E505-0", R.string.E505_0);
        MESSAGE_MAP.put("E506-0", R.string.E506_0);
        MESSAGE_MAP.put("E507-0", R.string.E507_0);
        MESSAGE_MAP.put("E508-0", R.string.E508_0);
        MESSAGE_MAP.put("E509-0", R.string.E509_0);
        MESSAGE_MAP.put("E510-0", R.string.E510_0);
        MESSAGE_MAP.put("E511-0", R.string.E511_0);
        MESSAGE_MAP.put("E512-0", R.string.E512_0);
        MESSAGE_MAP.put("E513-0", R.string.E513_0);
        MESSAGE_MAP.put("E514-0", R.string.E514_0);
        MESSAGE_MAP.put("E515-0", R.string.E515_0);
        MESSAGE_MAP.put("E516-0", R.string.E516_0);
        MESSAGE_MAP.put("E517-0", R.string.E517_0);
        MESSAGE_MAP.put("E518-0", R.string.E518_0);
        MESSAGE_MAP.put("E519-0", R.string.E519_0);
        MESSAGE_MAP.put("E520-0", R.string.E520_0);
        MESSAGE_MAP.put("E521-0", R.string.E521_0);
        MESSAGE_MAP.put("E522-0", R.string.E522_0);
        MESSAGE_MAP.put("E100-4", R.string.E100_4);
        MESSAGE_MAP.put("E118-4", R.string.E118_4);
        MESSAGE_MAP.put("E120-4", R.string.E120_4);
        MESSAGE_MAP.put("E133-4", R.string.E133_4);
        MESSAGE_MAP.put("E116-0", R.string.E116_0);
        MESSAGE_MAP.put("E202-0", R.string.E202_0);
        MESSAGE_MAP.put("E203-0", R.string.E203_0);
        MESSAGE_MAP.put("E204-0", R.string.E204_0);
        MESSAGE_MAP.put("E205-0", R.string.E205_0);
    }

    public static String getMessage(Context context, String code) {
        String index = code.trim();
        String detail = "";
        int res_id = MESSAGE_MAP.getOrDefault(index, 0);
//        Log.d(TAG,"getMessage = res_id "+res_id);
        if(res_id != 0) {
            detail = context.getString(res_id);
        }

        //Log.d(TAG,"detail.length() = "+detail.length());
        if(detail.length() > 0) {
            return index + " " + detail;
        }
        else
            return index;
    }

    public static boolean is_general_banner_msg(int error_flag, String error_code) {
        boolean ret = false;
        String general_error_msg[]=
        {
            //4.3 table 10:Status codes that cause an error banner to be displayed immediately
            "E016-0",
            "E017-0",
            "E018-0",
            "E030-0",
            "E031-0",
            "E032-0",
            "E033-0",
            "E042-32",
            "E044-32",
            "E048-32", //Lily 20161006 add no signal msg
            "E055-0",
            "E094-0",
            "D101-0",
            "E101-0",
            "E102-0",
            "E103-0",
            "E106-0",
            "E111-0",
            "E128-0",
            "E129-0",
            "E130-0",
            "E131-0",
            "E132-0",
            "E133-0",
            "E134-0",
            "E135-0",
            "E139-0",
            "E140-0",
            "E501-0",
            "E502-0",
            "E503-0",
            "E504-0",
            "E505-0",
            "E506-0",
            "E507-0",
            "E508-0",
            "E509-0",
            "E510-0",
            "E511-0",
            "E512-0",
            "E513-0",
            "E514-0",
            "E515-0",
            "E516-0",
            "E517-0",
            "E518-0",
            "E519-0",
            "E520-0",
            "E521-0",
            "E522-0",
            "E600-0",
            //5.2.1 table 17:New Error Banner for Unified Client
            "I007-4",
            "I055-4",
            "I102-4",
            "I139-4",
            "E000-32",
            "E004-4",
            "E005-4",
            "E006-4",
            "E016-4",
            "E017-13",
            "E018-4",
            "E030-4",
            "E032-4",
            "E033-4",
            "E038-32",
            "E045-32",
            "E080-35",
            "E081-35",
            "E100-4",
            "E101-4",
            "E106-9",
            "E107-4",
            "E108-4",
            "E109-4",
            "E118-4",
            "E120-4",
            "E133-4",
            "E136-4",
            "E137-4",
            "E140-4",
            "E141-4",
            "E142-4",
            "E144-4",
            "E152-4",
            "MAX"
        };

        for(String s : general_error_msg) {
            if(s.equals(error_code)) {
                ret = true;
                Log.d(TAG,error_code+" is general banner msg");
                break;
            }
        }
        if(!error_code.isEmpty()) {
            char firstChar = error_code.charAt(0);
            if (!ret && (error_flag == IrdetoErrorCode.CLEAN_E48_52 || error_flag == IrdetoErrorCode.SET_E52 || error_flag == IrdetoErrorCode.SET_E48 ||
                    (firstChar != 'E' && firstChar != 'e'))) {
                ret = true;
                Log.d(TAG, error_code + " is general banner msg");
            }
        }
        else {
            Log.d(TAG, "error_flag["+error_flag + "] is general banner msg");
            ret = true;
        }


        return ret;
    }
}
