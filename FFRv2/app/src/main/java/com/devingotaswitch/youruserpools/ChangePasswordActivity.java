package com.devingotaswitch.youruserpools;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.andrognito.flashbar.Flashbar;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.utils.FlashbarFactory;
import com.devingotaswitch.utils.GeneralUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText currPassword;
    private EditText newPassword;
    private Button changeButton;
    private AlertDialog userDialog;
    private AlertDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbarChangePass);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView main_title = findViewById(R.id.change_password_toolbar_title);
        main_title.setText("Change password");

        init();

    }

    private void init() {
        currPassword = findViewById(R.id.editTextChangePassCurrPass);
        currPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() == 0) {
                    TextView label = findViewById(R.id.textViewChangePassCurrPassLabel);
                    label.setText(currPassword.getHint());
                    currPassword.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = findViewById(R.id.textViewChangePassCurrPassMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) {
                    TextView label = findViewById(R.id.textViewChangePassCurrPassLabel);
                    label.setText("");
                }
            }
        });


        newPassword = findViewById(R.id.editTextChangePassNewPass);
        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() == 0) {
                    TextView label = findViewById(R.id.textViewChangePassNewPassLabel);
                    label.setText(newPassword.getHint());
                    newPassword.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = findViewById(R.id.textViewChangePassNewPassMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) {
                    TextView label = findViewById(R.id.textViewChangePassNewPassLabel);
                    label.setText("");
                }
            }
        });

        changeButton = findViewById(R.id.change_pass_button);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
    }

    private void changePassword() {
        String cPass = currPassword.getText().toString();

        if(cPass == null || cPass.length() < 1) {
            TextView label = findViewById(R.id.textViewChangePassCurrPassMessage);
            label.setText(currPassword.getHint()+" cannot be empty");
            currPassword.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }

        String nPass = newPassword.getText().toString();

        if(nPass == null || nPass.length() < 1) {
            TextView label = findViewById(R.id.textViewChangePassNewPassMessage);
            label.setText(newPassword.getHint()+" cannot be empty");
            newPassword.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }
        showWaitDialog("Changing password...");
        CUPHelper.getPool().getUser(CUPHelper.getCurrUser()).changePasswordInBackground(cPass, nPass, callback);
    }

    private final GenericHandler callback = new GenericHandler() {
        @Override
        public void onSuccess() {
            closeWaitDialog();
            Activity act = ChangePasswordActivity.this;
            GeneralUtils.hideKeyboard(act);
            FlashbarFactory.generateTextOnlyFlashbar(act, "Success!", "Password changed",
                    Flashbar.Gravity.BOTTOM).show();
            clearInput();
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            newPassword.setBackground(getDrawable(R.drawable.text_border_error));
            currPassword.setBackground(getDrawable(R.drawable.text_border_error));
            showDialogMessage("Password change failed", CUPHelper.formatException(exception), false);
        }
    };

    private  void clearInput() {
        currPassword.setText("");
        newPassword.setText("");
    }

    private void showDialogMessage(String title, String body, final boolean exitActivity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    if (exitActivity) {
                        onBackPressed();
                    }
                } catch (Exception e) {
                    onBackPressed();
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new MaterialAlertDialogBuilder(this)
            .setTitle(message)
            .create();
        waitDialog.show();
    }

    private void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        }
        catch (Exception e) {
            // Wait dialog is already closed or does not exist
        }
    }
}
