package com.codemany.chinesecard;

import java.util.GregorianCalendar;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class QueryActivity extends Activity {
    private static final String TAG = "QueryActivity";

    // wi = 2(n-1)(mod 11)
    private final int[] wi = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1};
    // check code
    private final int[] vi = {1, 0, 'X', 9, 8, 7, 6, 5, 4, 3, 2};

    private EditText edtIdNum;
    private KeyboardView kbdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_query);

        edtIdNum = (EditText)findViewById(R.id.edt_id_num);

        kbdView = (KeyboardView)findViewById(R.id.kbd_view);
        kbdView.setKeyboard(new Keyboard(this, R.xml.qwerty));
        kbdView.setEnabled(true);
        kbdView.setPreviewEnabled(true);
        kbdView.setOnKeyboardActionListener(new OnKeyboardActionListener() {
            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                Log.d(TAG, "onKey? primaryCode=" + primaryCode);
                int n1 = 0;    // -1 count
                for (int keyCode : keyCodes) {
                    if (keyCode == -1) {
                        n1++;
                        continue;
                    }
                    Log.d(TAG, "keyCode=" + keyCode);
                }
                Log.d(TAG, "keyCode=-1 *" + n1);

                Editable editable = edtIdNum.getText();
                int start = edtIdNum.getSelectionStart();
                if (primaryCode == Keyboard.KEYCODE_CANCEL) {
                    hideKeyboard();
                } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
                    if (editable != null && editable.length() > 0 && start > 0) {
                        editable.delete(start - 1, start);
                    }
                } else if (primaryCode == 57419) {    // go left
                    if (start > 0) {
                        edtIdNum.setSelection(start - 1);
                    }
                } else if (primaryCode == 57421) {    // go right
                    if (start < edtIdNum.length()) {
                        edtIdNum.setSelection(start + 1);
                    }
                } else {
                    editable.insert(start, Character.toString((char)primaryCode));
                }
            }

            @Override
            public void onPress(int primaryCode) {
                Log.d(TAG, "onPress? primaryCode=" + primaryCode);
            }

            @Override
            public void onRelease(int primaryCode) {
                Log.d(TAG, "onRelease? primaryCode=" + primaryCode);
            }

            @Override
            public void onText(CharSequence text) {
                Log.d(TAG, "onText? \"" + text + "\"");
            }

            @Override
            public void swipeDown() {
                Log.d(TAG, "swipeDown");
            }

            @Override
            public void swipeLeft() {
                Log.d(TAG, "swipeLeft");
            }

            @Override
            public void swipeRight() {
                Log.d(TAG, "swipeRight");
            }

            @Override
            public void swipeUp() {
                Log.d(TAG, "swipeUp");
            }
        });

        edtIdNum.setInputType(InputType.TYPE_NULL);
        edtIdNum.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyboard();
            }
        });

        Button btnQuery = (Button)findViewById(R.id.btn_query);
        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearResultText();
                String number = getIdNumber();
                if (number.length() != 15 && number.length() != 18) {
                    showErrorMessage(R.string.error_must_be_fifteen_or_eighteen);
                    return;
                }

                if (number.length() == 15) {
                    number = upgradeIdNumber(number);
                }

                StringBuilder sb = new StringBuilder();
                sb.append(getString(R.string.result_query_prompt));
                sb.append("\n").append(getString(R.string.result_identity_number)).append(number);
                sb.append("\n").append(getString(R.string.address)).append(getAddress(number));
                sb.append("\n").append(getString(R.string.birth)).append(getBirth(number));
                sb.append("\n").append(getString(R.string.age)).append(getAge(number));
                sb.append("\n").append(getString(R.string.gender)).append(getGender(number));
                showResult(sb.toString());
            }
        });

        Button btnVerify = (Button)findViewById(R.id.btn_verify);
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearResultText();
                String number = getIdNumber();
                if (number.length() != 18) {
                    showErrorMessage(R.string.error_must_be_eighteen);
                    return;
                }

                String checkCode = getCheckCode(number.substring(0, 17));
                if (checkCode.equalsIgnoreCase(number.substring(17, 18))) {
                    showResult(getString(R.string.result_verify_success));
                } else {
                    showResult(getString(R.string.result_verify_failure) + checkCode);
                }
            }
        });

        Button btnUpgrade = (Button)findViewById(R.id.btn_upgrade);
        btnUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearResultText();
                String number = getIdNumber();
                if (number.length() != 15) {
                    showErrorMessage(R.string.error_must_be_fifteen);
                    return;
                }

                number = upgradeIdNumber(number);
                showResult(getString(R.string.result_upgrade_prefix) + number);
            }
        });

        Button btnClear = (Button)findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearResultText();
                edtIdNum.setText("");
            }
        });
    }

    private void hideKeyboard() {
        int visibility = kbdView.getVisibility();
        if (visibility == View.VISIBLE) {
            kbdView.setVisibility(View.INVISIBLE);
        }
    }

    private void showKeyboard() {
        int visibility = kbdView.getVisibility();
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            kbdView.setVisibility(View.VISIBLE);
        }
    }

    private void clearResultText() {
        TextView resultEdit = (TextView)findViewById(R.id.tv_result);
        resultEdit.setText("");
    }

    private String upgradeIdNumber(String number) {
        StringBuilder sb = new StringBuilder();
        sb.append(number.substring(0, 6));
        sb.append("19");
        sb.append(number.substring(6, 15));
        sb.append(getCheckCode(sb.toString()));
        return sb.toString();
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

    private String getIdNumber() {
        return edtIdNum.getText().toString().trim();
    }

    private void showResult(String message) {
        TextView tv = (TextView)findViewById(R.id.tv_result);
        tv.setText(message);
    }

    private void showErrorMessage(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
    }

    private String getGender(String number) {
        String gender = number.substring(14, 17);
        return Integer.parseInt(gender) % 2 == 0
            ? getString(R.string.female) : getString(R.string.male);
    }

    private int getAge(String number) {
        String birth = number.substring(6, 10);
        int start = Integer.parseInt(birth);
        GregorianCalendar calendar = new GregorianCalendar();
        int end = calendar.get(java.util.Calendar.YEAR);
        return end - start;
    }

    private String getBirth(String number) {
        String birth = number.substring(6, 14);
        String year = birth.substring(0, 4);
        String month = birth.substring(4, 6);
        String day = birth.substring(6, 8);
        return year + getString(R.string.year)
            + month + getString(R.string.month)
            + day + getString(R.string.day);
    }

    private String getAddress(String number) {
        StringBuilder sb = new StringBuilder();
        DBHelper helper = DBHelper.getInstance(this);
        sb.append(helper.getAddress(this, number.substring(0, 6)));
        helper.close();
        return sb.toString();
    }
}
