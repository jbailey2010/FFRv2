package com.devingotaswitch.youruserpools;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Switch;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSettings;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.devingotaswitch.ffrv2.R;

import java.util.Map;

class MFASettingsHelper {
    private Switch smsSwitch;

    private CognitoUserSettings newSettings;

    private ProgressDialog waitDialog;
    private AlertDialog userDialog;

    private boolean settingsChanged;

    private final Activity activity;

    public MFASettingsHelper(Activity activity) {
        this.activity = activity;
        init();
    }

    private void init() {
        newSettings = new CognitoUserSettings();
        settingsChanged = false;
        smsSwitch = activity.findViewById(R.id.switchSettingsPhone);

        if(smsSwitch != null) {
            smsSwitch.setClickable(true);
            smsSwitch.setChecked(false);
        }

        Map<String, String> settings = CUPHelper.getUserDetails().getSettings().getSettings();

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
        CUPHelper.getPool().getUser(CUPHelper.getCurrUser()).setUserSettingsInBackground(newSettings, updateSettingHandler);
    }

    private void getDetails() {
        settingsChanged = true;
        CUPHelper.getPool().getUser(CUPHelper.getCurrUser()).getDetailsInBackground(detailsHandler);
    }

    private final GenericHandler updateSettingHandler = new GenericHandler() {
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
            showDialogMessage("Could not change MFA settings", CUPHelper.formatException(exception), false);
        }
    };

    private final GetDetailsHandler detailsHandler = new GetDetailsHandler() {
        @Override
        public void onSuccess(CognitoUserDetails cognitoUserDetails) {
            closeWaitDialog();
            // Store details in the AppHandler
            CUPHelper.setUserDetails(cognitoUserDetails);
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
}
