package com.devingotaswitch.youruserpools;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSettings;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.devingotaswitch.ffrv2.R;

import java.util.Map;

public class MFASettingsHelper {
    private Switch smsSwitch;
    private Switch emailSwitch;

    private Map<String, String> settings;
    private CognitoUserSettings newSettings;

    private ProgressDialog waitDialog;
    private AlertDialog userDialog;

    private boolean settingsChanged;

    private Activity activity;

    public MFASettingsHelper(Activity activity) {
        this.activity = activity;
        init();
    }

    private void init() {
        newSettings = new CognitoUserSettings();
        settingsChanged = false;
        smsSwitch = (Switch) activity.findViewById(R.id.switchSettingsPhone);

        if(smsSwitch != null) {
            smsSwitch.setClickable(true);
            smsSwitch.setChecked(false);
        }

        settings = AppHelper.getUserDetails().getSettings().getSettings();

        if(settings != null) {
            if(settings.containsKey("phone_number")) {
                smsSwitch.setClickable(true);
                if(settings.get("phone_number").contains("sms") || settings.get("phone_number").contains("SMS")) {
                    smsSwitch.setChecked(true);
                    smsSwitch.setText("Enabled");
                    smsSwitch.setTextColor(Color.parseColor("#37A51C"));
                }
                else {
                    smsSwitch.setChecked(false);
                    smsSwitch.setText("Disabled");
                    smsSwitch.setTextColor(Color.parseColor("#E94700"));
                }
            }
        }

        smsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                toggleSwitch();
                if (smsSwitch.isChecked()) {
                    updateSetting("phone_number", "SMS");
                } else {
                    updateSetting("phone_number", null);
                }
            }
        });
    }

    private void updateSetting(String attribute, String value) {
        showWaitDialog("Changing SMS MFA setting...");
        newSettings = new CognitoUserSettings();
        newSettings.setSettings(attribute, value);
        AppHelper.getPool().getUser(AppHelper.getCurrUser()).setUserSettingsInBackground(newSettings, updateSettingHandler);
    }

    private void getDetails() {
        settingsChanged = true;
        AppHelper.getPool().getUser(AppHelper.getCurrUser()).getDetailsInBackground(detailsHandler);
    }

    GenericHandler updateSettingHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            // Success
            getDetails();
        }

        @Override
        public void onFailure(Exception exception) {
            // Failed
            closeWaitDialog();
            smsSwitch.toggle();
            toggleSwitch();
            showDialogMessage("Could not change MFA settings", AppHelper.formatException(exception), false);
        }
    };

    GetDetailsHandler detailsHandler = new GetDetailsHandler() {
        @Override
        public void onSuccess(CognitoUserDetails cognitoUserDetails) {
            closeWaitDialog();
            // Store details in the AppHandler
            AppHelper.setUserDetails(cognitoUserDetails);
            showDialogMessage("MFA setting successfully changed","",false);
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            //
        }
    };

    private void toggleSwitch() {
        if(smsSwitch.isChecked()) {
            smsSwitch.setText("Enabled");
            smsSwitch.setTextColor(Color.parseColor("#37A51C"));
        } else {
            smsSwitch.setText("Disabled");
            smsSwitch.setTextColor(Color.parseColor("#E94700"));
        }
    }

    private void showDialogMessage(String title, String body, final boolean exitActivity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    if (exitActivity) {
                        activity.onBackPressed();
                    }
                } catch (Exception e) {
                    activity.onBackPressed();
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new ProgressDialog(activity);
        waitDialog.setTitle(message);
        waitDialog.show();
    }

    private void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        }
        catch (Exception e) {
            //
        }
    }

    private void exit() {
        Intent intent = new Intent();
        intent.putExtra("refresh",settingsChanged);
        activity.setResult(activity.RESULT_OK, intent);
        activity.finish();
    }
}
