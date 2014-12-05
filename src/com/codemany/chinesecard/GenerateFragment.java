package com.codemany.chinesecard;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class GenerateFragment extends Fragment {
    // wi = 2(n-1)(mod 11)
    private final int[] wi = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1};
    // check code
    private final int[] vi = {1, 0, 'X', 9, 8, 7, 6, 5, 4, 3, 2};

    private Button mBirth;
    private int mYear;
    private int mMonth;
    private int mDay;
    private String mCode;

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_generate, container, false);

        loadSpinner();
        initBirth();

        Button btn = (Button)rootView.findViewById(R.id.btn_generate);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                generateIdNumber();
            }
        });

        return rootView;
    }

    private void generateIdNumber() {
        StringBuilder sb = new StringBuilder();
        sb.append(mCode);
        sb.append(String.format("%d%02d%02d", mYear, mMonth + 1, mDay));

        int rand = (int)(Math.random() * 999);
        RadioGroup group = (RadioGroup)rootView.findViewById(R.id.radio_gender);
        int checkedId = group.getCheckedRadioButtonId();
        if ((checkedId == R.id.male && rand % 2 == 0)
                || (checkedId == R.id.female && rand % 2 != 0)) {
            rand++;
        }
        sb.append(String.format("%03d", rand));
        sb.append(getCheckCode(sb.toString()));

        EditText edit = (EditText)rootView.findViewById(R.id.edt_id_num);
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
        mBirth = (Button)rootView.findViewById(R.id.btn_birth);
        mBirth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DatePickerDialogFragment();
                dialogFragment.show(getActivity().getSupportFragmentManager(), "DatePicker");
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

    class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new DatePickerDialog(
                    getActivity(),
                    this,
                    mYear,
                    mMonth,
                    mDay);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateBirth();
        }
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

    private void loadSpinner() {
        Spinner sprProvince = (Spinner)rootView.findViewById(R.id.spr_province);
        sprProvince.setPrompt(getString(R.string.prompt_province));
        sprProvince.setAdapter(getSpinnerAdapterByParentCode(999999));
        sprProvince.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner sprCity = (Spinner)rootView.findViewById(R.id.spr_city);
                sprCity.setPrompt(getString(R.string.prompt_city));
                sprCity.setAdapter(getSpinnerAdapterByParentCode(id));
                sprCity.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Spinner sprCounty = (Spinner)rootView.findViewById(R.id.spr_county);
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
        DBHelper helper = DBHelper.getInstance(getActivity());
        SpinnerAdapter adapter = helper.getListByParentCode(getActivity(), String.valueOf(code));
        helper.close();
        return adapter;
    }
}
