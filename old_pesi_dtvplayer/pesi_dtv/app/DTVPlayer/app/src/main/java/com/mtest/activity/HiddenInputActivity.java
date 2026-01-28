package com.mtest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mtest.config.MtestConfig;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HiddenInputActivity extends DTVActivity {
    private static final String TAG = "HiddenInputActivity";

    public static final String KEY_RESULT_HIDDEN_INPUT = "result.hidden.input";

    private ListView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            String inputValue = ((TextView) view.findViewById(android.R.id.text1)).getText().toString();
            Log.d(TAG, "onItemClick: " + inputValue);

            switch (inputValue) {
                case MtestConfig.HIDDEN_INPUT_EMI:
                    Toast.makeText(getBaseContext(), inputValue, Toast.LENGTH_SHORT).show();
                    MtestConfig.switchHiddenFunctionEnable(getApplicationContext(), MtestConfig.KEY_EMI);
                    MtestConfig.restart(getApplicationContext());
                    setResult(RESULT_CANCELED);
                    break;
                case MtestConfig.HIDDEN_INPUT_STABLE_TEST_MODE: // Johnny 20190318 for stable test
                    Global_Variables globalVariables = (Global_Variables) getApplicationContext();
                    if (globalVariables.isStableTestEnabled()) {
                        globalVariables.setStableTestEnabled(false);
                        Toast.makeText(getBaseContext(), inputValue + " Stable Test Mode Disabled!", Toast.LENGTH_SHORT).show();
                    } else {
                        globalVariables.setStableTestEnabled(true);
                        Toast.makeText(getBaseContext(), inputValue + " Stable Test Mode Enabled!", Toast.LENGTH_SHORT).show();
                    }

                    setResult(RESULT_CANCELED);
                    break;
                default:
                    // hidden input not in 6152 handle by the caller activity
                    Intent intent = new Intent();
                    intent.putExtra(KEY_RESULT_HIDDEN_INPUT, inputValue);
                    // TODO: use new Activity Results API in the future?
                    setResult(RESULT_OK, intent);
                    break;
            }

            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden_input);


        initListView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    static class HiddenInput {
        String inputValue;
        String desc;

        HiddenInput(String inputValue, String desc) {
            this.inputValue = inputValue;
            this.desc = desc;
        }
    }

    private void initListView() {
        ListView listview = findViewById(R.id.listview);
        List<HashMap<String, String>> items = getDataList();

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                items,
                android.R.layout.simple_list_item_2,
                new String[] {"inputValue", "desc"},
                new int[] {android.R.id.text1, android.R.id.text2}) {
        };

        listview.setAdapter(adapter);
        listview.setOnItemClickListener(mOnItemClickListener);
    }

    private List<HashMap<String, String>> getDataList() {
        List<HashMap<String, String>> dataList = new ArrayList<>();
        List<HiddenInput> hiddenInputList = getHiddenInputList();

        for (HiddenInput hiddenInput : hiddenInputList) {
            HashMap<String, String> data = new HashMap<>();
            data.put("inputValue", hiddenInput.inputValue);
            data.put("desc", hiddenInput.desc);
            dataList.add(data);
        }

        return dataList;
    }

    /**
     *
     * Modify this function to edit items in listview
     */
    private List<HiddenInput> getHiddenInputList() {
        List<HiddenInput> hiddenInputList = new ArrayList<>();

        addHiddenInput(
                MtestConfig.HIDDEN_INPUT_HIDDEN_ACTIVITY,
                getString(R.string.str_hidden_input_desc_hidden_activity),
                hiddenInputList);
        addHiddenInput(
                MtestConfig.HIDDEN_INPUT_STABLE_TEST_MODE,
                getString(R.string.str_hidden_input_desc_stable_test_mode),
                hiddenInputList);
        addHiddenInput(
                MtestConfig.HIDDEN_INPUT_EMI,
                getString(R.string.str_hidden_input_desc_emi),
                hiddenInputList);
        addHiddenInput(
                MtestConfig.HIDDEN_INPUT_MTEST_ALL_PASS,
                getString(R.string.str_hidden_input_desc_mtest_all_pass),
                hiddenInputList);
        addHiddenInput(
                MtestConfig.HIDDEN_INPUT_MTEST_DISABLE_OPT,
                getString(R.string.str_hidden_input_desc_mtest_disable_opt),
                hiddenInputList);
        addHiddenInput(
                MtestConfig.HIDDEN_INPUT_CLEAR_WIFI,
                getString(R.string.str_hidden_input_desc_clear_wifi),
                hiddenInputList);
        addHiddenInput(
                MtestConfig.HIDDEN_INPUT_HW_TEST,
                getString(R.string.str_hidden_input_desc_hw_test),
                hiddenInputList);
        addHiddenInput(
                MtestConfig.HIDDEN_INPUT_HW_COPY_REPORT_FILE,
                getString(R.string.str_hidden_input_desc_hw_copy_report_file),
                hiddenInputList);
        addHiddenInput(
                MtestConfig.HIDDEN_INPUT_HW_RESET,
                getString(R.string.str_hidden_input_desc_hw_reset),
                hiddenInputList);
        addHiddenInput(
                MtestConfig.HIDDEN_INPUT_HW_DELETE_REPORT_FILE,
                getString(R.string.str_hidden_input_desc_hw_delete_report_file),
                hiddenInputList);
        addHiddenInput(
                MtestConfig.HIDDEN_INPUT_HW_SHOW_HELP,
                getString(R.string.str_hidden_input_desc_hw_show_help),
                hiddenInputList);
        addHiddenInput(
                MtestConfig.HIDDEN_INPUT_ALL_ITEM_SELECTABLE,
                getString(R.string.str_hidden_input_desc_all_item_selectable),
                hiddenInputList);
        addHiddenInput(
                MtestConfig.HIDDEN_INPUT_START_POWER_SAVING,
                getString(R.string.str_hidden_input_desc_start_power_saving),
                hiddenInputList);
        addHiddenInput(
                MtestConfig.HIDDEN_INPUT_SWITCH_LOCALE,
                getString(R.string.str_hidden_input_desc_switch_locale),
                hiddenInputList);
        addHiddenInput(
                MtestConfig.HIDDEN_INPUT_DURABILITY_TEST,
                getString(R.string.str_hidden_input_desc_durability_test),
                hiddenInputList);

        return hiddenInputList;
    }

    private void addHiddenInput(String inputValue, String desc, List<HiddenInput> hiddenInputList) {
        HiddenInput hiddenInput = new HiddenInput(inputValue, desc);
        hiddenInputList.add(hiddenInput);
    }
}
