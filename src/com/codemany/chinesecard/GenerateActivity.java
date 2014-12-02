package com.codemany.chinesecard;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class GenerateActivity extends Activity {
    private static final int DATE_DIALOG_ID = 0;

    // wi = 2(n-1)(mod 11)
    private final int[] wi = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1};
    // check code
    private final int[] vi = {1, 0, 'X', 9, 8, 7, 6, 5, 4, 3, 2};

    private Button mBirth;
    private int mYear;
    private int mMonth;
    private int mDay;
    private String mCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_generate);

        loadSpinner();
        initBirth();

        Button btn = (Button)findViewById(R.id.btn_generate);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                generateIdNumber();
            }
        });
    }

    private void generateIdNumber() {
        StringBuilder sb = new StringBuilder();
        sb.append(mCode);
        sb.append(String.format("%d%02d%02d", mYear, mMonth + 1, mDay));

        int rand = (int)(Math.random() * 999);
        RadioGroup group = (RadioGroup)findViewById(R.id.radio_gender);
        int checkedId = group.getCheckedRadioButtonId();
        if ((checkedId == R.id.male && rand % 2 == 0)
                || (checkedId == R.id.female && rand % 2 != 0)) {
            rand++;
        }
        sb.append(String.format("%03d", rand));
        sb.append(getCheckCode(sb.toString()));

        EditText edit = (EditText)findViewById(R.id.edt_id_num);
        edit.setText(sb);
        edit.setKeyListener(null);
    }

    private String getCheckCode(String number) {
        int remaining = 0;
        if (number.length() == 18) {
          return number.substring(17, 18);
        }

        int sum = 0;
        int ai = 0;
        for (int i = 0; i < 17; i++) {
            String k = number.substring(i, i + 1);
            ai = Integer.parseInt(k);
            sum += wi[i] * ai;
        }
        remaining = sum % 11;
        return remaining == 2 ? "X" : String.valueOf(vi[remaining]);
    }

    private void initBirth() {
        mBirth = (Button)findViewById(R.id.btn_birth);
        mBirth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        // get the current date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        // display the current date (this method is below)
        updateBirth();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(
                    this,
                    mDateSetListener,
                    mYear,
                    mMonth,
                    mDay);
        }
        return null;
    }

    // updates the date in the TextView
    private void updateBirth() {
        StringBuilder sb = new StringBuilder();
        sb.append(mYear).append("-");
        // Month is 0 based so add 1
        sb.append(mMonth + 1).append("-");
        sb.append(mDay);
        mBirth.setText(sb);
    }

    // the callback received when the user "sets" the date in the dialog
    private OnDateSetListener mDateSetListener = new OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateBirth();
        }
    };

    private void loadSpinner() {
        Spinner sprProvince = (Spinner)findViewById(R.id.spr_province);
        sprProvince.setPrompt(getString(R.string.prompt_province));
        sprProvince.setAdapter(getSpinnerAdapterByParentCode(999999));
        sprProvince.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner sprCity = (Spinner)findViewById(R.id.spr_city);
                sprCity.setPrompt(getString(R.string.prompt_city));
                sprCity.setAdapter(getSpinnerAdapterByParentCode(id));
                sprCity.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Spinner sprCounty = (Spinner)findViewById(R.id.spr_county);
                        sprCounty.setPrompt(getString(R.string.prompt_county));
                        sprCounty.setAdapter(getSpinnerAdapterByParentCode(id));
                        sprCounty.setOnItemSelectedListener(new OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                mCode = String.valueOf(id);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private SpinnerAdapter getSpinnerAdapterByParentCode(long code) {
        DBHelper helper = DBHelper.getInstance(this);
        SpinnerAdapter adapter = helper.getListByParentCode(this, String.valueOf(code));
        helper.close();
        return adapter;
    }
}
