package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.dolphin.dtv.EnTableType;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.VMXProtectData;

import java.util.ArrayList;


public class VMXLocationDialog extends Dialog{
    private static final String TAG="VMXLocationDialog";
    Dialog mDialog = null;
    private Context mContext = null;
    private DTVActivity mDtv = null;
    private Resources mResouce = null;
    private TextView AreaText, RegionText;
    private Button okButton, cancelButton;
    Spinner provinceCity;
    SelectBoxView City;
    String[] cityList;
    int mFirst = 0, mSecond = 0, mThird = 0;


    class cityNode
    {
        int first_hex;
        int second_hex;
        int third_hex;
        String cityname;
    }
    ArrayList<cityNode> location_list = new ArrayList<cityNode>();


    public VMXLocationDialog(Context context, DTVActivity dtv) {
        super(context);
        mContext = context;
        mDtv = dtv;
        mResouce = mContext.getResources();
        mDialog = new Dialog(mContext, R.style.transparentDialog){//Scoty 20181218 add VMX EWBS mode
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {//key event
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        mDtv.SetProtectData(mFirst,mSecond,mThird);
                        mDtv.SaveTable(EnTableType.LOCATION);
                        mDialog.dismiss();
                        break;
                }
                return super.onKeyDown(keyCode, event);
            }
        };
        cityList = mResouce.getStringArray(R.array.STR_ARRAY_CITY_LIST);

        if(mDialog == null){
            return;
        }
        mDialog.setCanceledOnTouchOutside(true);
        //mDialog.show();
        new android.os.Handler().postDelayed(new Runnable() { // Edwin 20190509 fix dialog not focus
            @Override
            public void run () {
                mDialog.show();
            }
        }, 150);
        mDialog.setContentView(R.layout.vmx_location_dialog);
        Window window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        provinceCity = (Spinner) window.findViewById(R.id.provinceCityTXVSPINNER);
        AreaText = (TextView)  window.findViewById(R.id.wideAreaZoneValueTXV);
        RegionText = (TextView)  window.findViewById(R.id.regionValueTXV);
        okButton = (Button) window.findViewById(R.id.okBTN);
        cancelButton = (Button) window.findViewById(R.id.cancelBTN);
        AddAllCity();

        //for( int i = 0 ; i < location_list.size(); i++)
        //{
        //    Log.d(TAG, "VMXLocationDialog:  i =" + i + "   first = " + location_list.get(i).first_hex + "  second = " + location_list.get(i).second_hex  + "    name = " + location_list.get(i).cityname);
        //}

        City = new SelectBoxView(mContext, provinceCity, cityList);
        provinceCity.setOnItemSelectedListener(CityListener);

        int curLocation = 0;
        VMXProtectData data = mDtv.GetProtectData();
        cityNode curNode ;
        mFirst = data.getLocationFirst();
        mSecond = data.getLocationSecond();
        mThird = data.getLocationThird();
        Log.d(TAG, "onCreate:  mFirst = " + mFirst + "    mSecond =" + mSecond + "     mThird = " + mThird);
        for( int i = 0; i < location_list.size(); i++)
        {
            curNode = location_list.get(i);
            if(mFirst == curNode.first_hex && mSecond == curNode.second_hex && mThird ==curNode.third_hex) {
                curLocation = i;
                break;
            }
        }
        City.SetSelectedItemIndex(curLocation);

        okButton.setOnClickListener(OKClickListener);
        //cancelButton.setOnClickListener(CancelClickListener);//Scoty 20181218 add VMX EWBS mode

    }

    private AdapterView.OnItemSelectedListener CityListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView,view,position,id);
            cityNode curcity = location_list.get(position);
            mFirst = curcity.first_hex;
            mSecond = curcity.second_hex;
            mThird = curcity.third_hex;
            if(location_list != null && position < location_list.size())
            {
                Log.d(TAG, "onItemSelected:  firstHext = " + curcity.first_hex + "   secondHex = " + curcity.second_hex + "   thirdHex = " + curcity.third_hex );
                String Area = getLoactionAreaStr(curcity.first_hex);
                String Region = getLoactionRegionStr(curcity.first_hex, curcity.second_hex);
                AreaText.setText(Area);
                RegionText.setText(Region);
            }
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };


    public void AddCity(int firstHex, int secondHex, int thirdHex, String cityname)
    {
        cityNode node = new cityNode();
        node.first_hex = firstHex;
        node.second_hex = secondHex;
        node.third_hex = thirdHex;
        node.cityname = cityname;
        location_list.add(node);
    }

    public void AddAllCity()
    {
        AddCity(0x02, 0x02, 0x01, "Abra");
        AddCity(0x04, 0x05, 0x01, "Agusan del Norte");
        AddCity(0x04, 0x05, 0x02, "Agusan del Sur");
        AddCity(0x03, 0x01, 0x01, "Aklan");
        AddCity(0x02, 0x08, 0x01, "Albay");
        AddCity(0x02, 0x05, 0x01, "Angeles");
        AddCity(0x03, 0x01, 0x02, "Antique");
        AddCity(0x02, 0x02, 0x02, "Apayao");
        AddCity(0x02, 0x05, 0x02, "Aurora");
        AddCity(0x03, 0x01, 0x03, "Bacolod");
        AddCity(0x02, 0x02, 0x03, "Baguio");
        AddCity(0x04, 0x06, 0x01, "Basilan");
        AddCity(0x02, 0x05, 0x03, "Bataan");
        AddCity(0x02, 0x04, 0x01, "Batanes");
        AddCity(0x02, 0x06, 0x01, "Batangas");
        AddCity(0x02, 0x02, 0x04, "Benguet");
        AddCity(0x03, 0x03, 0x01, "Biliran");
        AddCity(0x03, 0x02, 0x01, "Bohol");
        AddCity(0x04, 0x02, 0x01, "Bukidnon");
        AddCity(0x02, 0x05, 0x04, "Bulacan");
        AddCity(0x04, 0x05, 0x03, "Butuan");
        AddCity(0x02, 0x04, 0x02, "Cagayan");
        AddCity(0x04, 0x02, 0x02, "Cagayan de Oro");
        AddCity(0x02, 0x01, 0x01, "Caloocan");
        AddCity(0x02, 0x08, 0x03, "Camarines Norte");
        AddCity(0x02, 0x08, 0x02, "Camarines Sur");
        AddCity(0x04, 0x02, 0x03, "Camiguin");
        AddCity(0x03, 0x01, 0x04, "Capiz");
        AddCity(0x02, 0x08, 0x04, "Catanduanes");
        AddCity(0x02, 0x06, 0x02, "Cavite");
        AddCity(0x03, 0x02, 0x02, "Cebu");
        AddCity(0x03, 0x02, 0x03, "Cebu City");
        AddCity(0x04, 0x03, 0x01, "Compostela Valley");
        AddCity(0x04, 0x04, 0x01, "Cotabato");
        AddCity(0x04, 0x04, 0x02, "Cotabato City");
        AddCity(0x04, 0x03, 0x02, "Davao City");
        AddCity(0x04, 0x03, 0x03, "Davao del Norte");
        AddCity(0x04, 0x03, 0x04, "Davao del Sur");
        AddCity(0x04, 0x03, 0x05, "Davao Oriental");
        AddCity(0x04, 0x05, 0x04, "Dinagat Islands");
        AddCity(0x03, 0x03, 0x02, "Eastern Samar");
        AddCity(0x04, 0x04, 0x03, "General Santos");
        AddCity(0x03, 0x01, 0x05, "Guimaras");
        AddCity(0x02, 0x02, 0x05, "Ifugao");
        AddCity(0x04, 0x02, 0x04, "Iligan");
        AddCity(0x02, 0x03, 0x01, "Ilocos Norte");
        AddCity(0x02, 0x03, 0x02, "Ilocos Sur");
        AddCity(0x03, 0x01, 0x06, "Iloilo");
        AddCity(0x03, 0x01, 0x07, "Iloilo City");
        AddCity(0x02, 0x04, 0x03, "Isabela");
        AddCity(0x04, 0x01, 0x01, "Isabela City");
        AddCity(0x02, 0x02, 0x06, "Kalinga");
        AddCity(0x02, 0x03, 0x03, "La Union");
        AddCity(0x02, 0x06, 0x03, "Laguna");
        AddCity(0x04, 0x02, 0x05, "Lanao del Norte");
        AddCity(0x04, 0x06, 0x02, "Lanao del Sur");
        AddCity(0x03, 0x02, 0x04, "Lapu Lapu");
        AddCity(0x02, 0x01, 0x02,"Las Piñas");  //Las Pi\xf1as
        AddCity(0x03, 0x03, 0x03, "Leyte");
        AddCity(0x02, 0x06, 0x04, "Lucena");
        AddCity(0x04, 0x06, 0x03, "Maguindanao");
        AddCity(0x02, 0x01, 0x03, "Makati");
        AddCity(0x02, 0x01, 0x04, "Malabon");
        AddCity(0x02, 0x01, 0x05, "Mandaluyong");
        AddCity(0x02, 0x01, 0x06, "Manila");
        AddCity(0x02, 0x01, 0x07, "Marikina");
        AddCity(0x02, 0x07, 0x01, "Marinduque");
        AddCity(0x02, 0x08, 0x05, "Masbate");
        AddCity(0x04, 0x02, 0x06, "Misamis Occidental");
        AddCity(0x04, 0x02, 0x07, "Misamis Oriental");
        AddCity(0x02, 0x02, 0x07, "Mt. Province");
        AddCity(0x02, 0x01, 0x08, "Muntinlupa");
        AddCity(0x02, 0x08, 0x06, "Naga");
        AddCity(0x02, 0x01, 0x09, "Navotas");
        AddCity(0x03, 0x01, 0x08, "Negros Occidental");
        AddCity(0x03, 0x02, 0x05, "Negros Oriental");
        AddCity(0x03, 0x03, 0x04, "Northern Samar");
        AddCity(0x02, 0x05, 0x05, "Nueva Ecija");
        AddCity(0x02, 0x04, 0x04, "Nueva Vizcaya");
        AddCity(0x02, 0x07, 0x02, "Occidental Mindoro");
        AddCity(0x02, 0x05, 0x06, "Olongapo");
        AddCity(0x02, 0x07, 0x03, "Oriental Mindoro");
        AddCity(0x03, 0x03, 0x05, "Ormoc");
        AddCity(0x02, 0x07, 0x04, "Palawan");
        AddCity(0x02, 0x05, 0x07, "Pampanga");
        AddCity(0x02, 0x03, 0x04, "Pangasinan");
        AddCity(0x02, 0x01, 0x0a, "Parañaque");
        AddCity(0x02, 0x01, 0x0b, "Pasay");
        AddCity(0x02, 0x01, 0x0c, "Pasig");
        AddCity(0x02, 0x01, 0x0d, "Pateros");
        AddCity(0x02, 0x07, 0x05, "Puerto Princesa");
        AddCity(0x02, 0x06, 0x05, "Quezon");
        AddCity(0x02, 0x01, 0x0e, "Quezon City");
        AddCity(0x02, 0x04, 0x05, "Quirino");
        AddCity(0x02, 0x06, 0x06, "Rizal");
        AddCity(0x02, 0x07, 0x06, "Romblon");
        AddCity(0x03, 0x03, 0x06, "Samar");
        AddCity(0x02, 0x01, 0x0f, "San Juan");
        AddCity(0x02, 0x04, 0x06, "Santiago");
        AddCity(0x04, 0x04, 0x04, "Saranggani");
        AddCity(0x03, 0x02, 0x06, "Siquijor");
        AddCity(0x02, 0x08, 0x07, "Sorsogon");
        AddCity(0x04, 0x04, 0x05, "South Cotabato");
        AddCity(0x03, 0x03, 0x07, "Southern Leyte");
        AddCity(0x04, 0x04, 0x06, "Sultan Kudarat");
        AddCity(0x04, 0x06, 0x04, "Sulu");
        AddCity(0x04, 0x05, 0x05, "Surigao del Norte");
        AddCity(0x04, 0x05, 0x06, "Surigao del Sur");
        AddCity(0x03, 0x03, 0x08, "Tacloban");
        AddCity(0x02, 0x01, 0x10, "Taguig");
        AddCity(0x02, 0x05, 0x08, "Tarlac");
        AddCity(0x04, 0x06, 0x05, "Tawi-Tawi");
        AddCity(0x02, 0x01, 0x11, "Valenzuela");
        AddCity(0x02, 0x05, 0x09, "Zambales");
        AddCity(0x04, 0x01, 0x02, "Zamboanga City");
        AddCity(0x04, 0x01, 0x03, "Zamboanga del Norte");
        AddCity(0x04, 0x01, 0x04, "Zamboanga del Sur");
        AddCity(0x04, 0x01, 0x05, "Zamboanga Sibugay"); //118
        AddCity(0x01, 0x00, 0x00, "All citys of the Philippines");
        AddCity(0x02, 0x00, 0x00, "All citys of LUZON");
        AddCity(0x02, 0x01, 0x00, "All citys of NCR");
        AddCity(0x02, 0x02, 0x00, "All cities of CAR");
        AddCity(0x02, 0x03, 0x00, "All cities of llocos Region");
        AddCity(0x02, 0x04, 0x00, "All cities of Cagayan Valley");
        AddCity(0x02, 0x05, 0x00, "All cities of Central Luzon");
        AddCity(0x02, 0x06, 0x00, "All cities of CALABARZON");
        AddCity(0x02, 0x07, 0x00, "All cities of MIMAROPA");
        AddCity(0x02, 0x08, 0x00, "All cities of Bicol Region");
        AddCity(0x03, 0x00, 0x00, "All citys of VISAYAS");
        AddCity(0x03, 0x01, 0x00, "All cities of Western Visayas");
        AddCity(0x03, 0x02, 0x00, "All cities of Central Visayas");
        AddCity(0x03, 0x03, 0x00, "All cities of Eastern Visayas");
        AddCity(0x04, 0x00, 0x00, "All cities of MINDANAO");
        AddCity(0x04, 0x01, 0x00, "All cities of Zamboanga Peninsula");
        AddCity(0x04, 0x02, 0x00, "All cities of Northern Mindanao");
        AddCity(0x04, 0x03, 0x00, "All cities of Davao Region");
        AddCity(0x04, 0x04, 0x00, "All cities of SOCCSKSARGEN");
        AddCity(0x04, 0x05, 0x00, "All cities of CARAGA");
        AddCity(0x04, 0x06, 0x00, "All cities of REGION ARMM");
        //AddCity(0x00, 0x00, 0x00, "Undefined");//Scoty 20181218 add VMX EWBS mode
    }

    private String getLoactionAreaStr(int firstHex)
    {
        int i;
        cityNode city = new cityNode();
        for(i=0; i <location_list.size(); i++)
        {
            city = location_list.get(i);
            if(city.first_hex == firstHex && city.second_hex == 0 && city.third_hex == 0)
            {
                String str[] = city.cityname.split(" ");
                for( int j = 0; j < str.length; j++)
                    Log.d(TAG, "getLoactionAreaStr:  str  " + j + "   ==>" + str[j]);
                if(str.length >= 3)
                    return str[3];
                else
                    return city.cityname;
            }
        }
        return mResouce.getString(R.string.STR_ERROR);
    }
    private String getLoactionRegionStr(int firstHex, int secondHex)
    {
        int i;
        cityNode city = new cityNode();
        for(i=0; i <location_list.size(); i++)
        {
            city = location_list.get(i);
            if(city.first_hex == firstHex && city.second_hex == secondHex && city.third_hex == 0)
            {
                String str[] = city.cityname.split(" ");
                for( int j = 0; j < str.length; j++)
                    Log.d(TAG, "getLoactionAreaStr:  str  " + j + "   ==>" + str[j]);
                if(str.length >= 3)
                    return str[3];
                else
                    return city.cityname;
            }
        }
        return mResouce.getString(R.string.STR_ERROR);
    }

    private View.OnClickListener OKClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mDtv.SetProtectData(mFirst,mSecond,mThird);
            mDtv.SaveTable(EnTableType.LOCATION);
            mDialog.dismiss();
        }
    };
//Scoty 20181218 add VMX EWBS mode
//    private View.OnClickListener CancelClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            mDialog.dismiss();
//        }
//    };
}
