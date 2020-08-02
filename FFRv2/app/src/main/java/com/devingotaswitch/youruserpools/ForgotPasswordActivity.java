package com.devingotaswitch.youruserpools;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.devingotaswitch.ffrv2.R;


public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText passwordInput;
    private EditText codeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit(null, null);
            }
        });

        TextView main_title = findViewById(R.id.forgot_password_toolbar_title);
        main_title.setText("Forgot password");

        init();
    }

    public void forgotPassword(View view) {
        getCode();
    }

    private void init(){

        Bundle extras = getIntent().getExtras();
        if (extras !=null) {
            if (extras.containsKey("destination")) {
                String dest = extras.getString("destination");
                String delMed = extras.getString("deliveryMed");
                TextView message = findViewById(R.id.textViewForgotPasswordMessage);
                String textToDisplay = "Code to set a new password was sent to " + dest + " via "+delMed;
                message.setText(textToDisplay);
            }
        }

        passwordInput = findViewById(R.id.editTextForgotPasswordPass);
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = findViewById(R.id.textViewForgotPasswordUserIdLabel);
                    label.setText(passwordInput.getHint());
                    passwordInput.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = findViewById(R.id.textViewForgotPasswordUserIdMessage);
                label.setText(" ");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = findViewById(R.id.textViewForgotPasswordUserIdLabel);
                    label.setText("");
                }
            }
        });

        codeInput = findViewById(R.id.editTextForgotPasswordCode);
        codeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = findViewById(R.id.textViewForgotPasswordCodeLabel);
                    label.setText(codeInput.getHint());
                    codeInput.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = findViewById(R.id.textViewForgotPasswordCodeMessage);
                label.setText(" ");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = findViewById(R.id.textViewForgotPasswordCodeLabel);
                    label.setText("");
                }
            }
        });
    }

    private void getCode() {
        String newPassword = passwordInput.getText().toString();

        if (newPassword == null || newPassword.length() < 1) {
            TextView label = findViewById(R.id.textViewForgotPasswordUserIdMessage);
            label.setText(passwordInput.getHint() + " cannot be empty");
            passwordInput.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }

        String verCode = codeInput.getText().toString();

        if (verCode == null || verCode.length() < 1) {
            TextView label = findViewById(R.id.textViewForgotPasswordCodeMessage);
            label.setText(codeInput.getHint() + " cannot be empty");
            codeInput.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }
        exit(newPassword, verCode);
    }

    private void exit(String newPass, String code) {
        Intent intent = new Intent();
        if(newPass == null || code == null) {
            newPass = "";
            code = "";
        }
        intent.putExtra("newPass", newPass);
        intent.putExtra("code", code);
        setResult(RESULT_OK, intent);
        finish();
    }
}
