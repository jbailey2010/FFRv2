package com.devingotaswitch.youruserpools;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;
import com.andrognito.flashbar.Flashbar;
import com.devingotaswitch.ffrv2.R;
import com.devingotaswitch.rankings.RankingsHome;
import com.devingotaswitch.utils.FlashbarFactory;
import com.devingotaswitch.utils.GeneralUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final String TAG="MainActivity";

    private AlertDialog userDialog;
    private AlertDialog waitDialog;

    // Screen fields
    private EditText inUsername;
    private EditText inPassword;
    private TextView titleView;
    private Toolbar toolbar;

    //Continuations
    private ForgotPasswordContinuation forgotPasswordContinuation;
    private NewPasswordContinuation newPasswordContinuation;

    // User Details
    private String username;
    private String password;

    // Mandatory overrides first
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set toolbar for this screen
        toolbar = findViewById(R.id.main_toolbar);
        toolbar.setTitle("");
        titleView = findViewById(R.id.main_toolbar_title);
        setSupportActionBar(toolbar);

        // Initialize application
        CUPHelper.init(getApplicationContext());
        setDisplayForLoading();
        initApp();
        findCurrent();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                // Register user
                if(resultCode == RESULT_OK) {
                    String name = data.getStringExtra("name");
                    if (!name.isEmpty()) {
                        inUsername.setText(name);
                        inPassword.setText("");
                        inPassword.requestFocus();
                    }
                    String userPasswd = data.getStringExtra("password");
                    if (!userPasswd.isEmpty()) {
                        inPassword.setText(userPasswd);
                    }
                    if (!name.isEmpty() && !userPasswd.isEmpty()) {
                        // We have the user details, so sign in!
                        username = name;
                        password = userPasswd;
                        CUPHelper.getPool().getUser(username).getSessionInBackground(new FFRAuthHandler(false));
                    }
                }
                break;
            case 2:
                // Confirm register user
                if(resultCode == RESULT_OK) {
                    String name = data.getStringExtra("name");
                    if (!name.isEmpty()) {
                        inUsername.setText(name);
                        inPassword.setText("");
                        inPassword.requestFocus();
                    }
                }
                break;
            case 3:
                // Forgot password
                if(resultCode == RESULT_OK) {
                    String newPass = data.getStringExtra("newPass");
                    String code = data.getStringExtra("code");
                    if (newPass != null && code != null) {
                        if (!newPass.isEmpty() && !code.isEmpty()) {
                            showWaitDialog("Setting new password...");
                            forgotPasswordContinuation.setPassword(newPass);
                            forgotPasswordContinuation.setVerificationCode(code);
                            forgotPasswordContinuation.continueTask();
                        }
                    }
                }
                break;
            case 4:
                // User
                if(resultCode == RESULT_OK) {
                    clearInput();
                    String name = data.getStringExtra("TODO");
                    if(name != null) {
                        if (!name.isEmpty()) {
                            onBackPressed();
                        }
                    }
                }
                break;
            case 6:
                //New password
                closeWaitDialog();
                boolean continueSignIn = false;
                if (resultCode == RESULT_OK) {
                   continueSignIn = data.getBooleanExtra("continueSignIn", false);
                }
                if (continueSignIn) {
                    continueWithFirstTimeSignIn();
                }
        }
    }

    // App methods
    // Register user - start process
    public void signUp(View view) {
        signUpNewUser();
    }

    // Login if a user is already present
    public void logIn(View view) {
        signInUser();
    }

    // Forgot password processing
    public void forgotPassword(View view) {
        forgotpasswordUser();
    }

    public void confirmSignUp(View view) {
        confirmUser(false);
    }

    private void signUpNewUser() {
        Intent registerActivity = new Intent(this, RegisterUser.class);
        startActivityForResult(registerActivity, 1);
    }

    private void signInUser() {
        username = inUsername.getText().toString();
        if(username == null || username.length() < 1) {
            TextView label = findViewById(R.id.textViewUserIdMessage);
            label.setText(inUsername.getHint()+" cannot be empty");
            inUsername.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }

        CUPHelper.setUser(username);

        password = inPassword.getText().toString();
        if(password == null || password.length() < 1) {
            TextView label = findViewById(R.id.textViewUserPasswordMessage);
            label.setText(inPassword.getHint()+" cannot be empty");
            inPassword.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }

        showWaitDialog("Signing in...");
        CUPHelper.getPool().getUser(username).getSessionInBackground(new FFRAuthHandler(false));
    }

    private void forgotpasswordUser() {
        username = inUsername.getText().toString();
        if(username == null) {
            TextView label = findViewById(R.id.textViewUserIdMessage);
            label.setText(inUsername.getHint()+" cannot be empty");
            inUsername.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }

        if(username.length() < 1) {
            TextView label = findViewById(R.id.textViewUserIdMessage);
            label.setText(inUsername.getHint()+" cannot be empty");
            inUsername.setBackground(getDrawable(R.drawable.text_border_error));
            return;
        }

        showWaitDialog("");
        CUPHelper.getPool().getUser(username).forgotPasswordInBackground(forgotPasswordHandler);
    }

    private void getForgotPasswordCode(ForgotPasswordContinuation forgotPasswordContinuation) {
        this.forgotPasswordContinuation = forgotPasswordContinuation;
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        intent.putExtra("destination",forgotPasswordContinuation.getParameters().getDestination());
        intent.putExtra("deliveryMed", forgotPasswordContinuation.getParameters().getDeliveryMedium());
        startActivityForResult(intent, 3);
    }

    private void continueWithFirstTimeSignIn() {
        newPasswordContinuation.setPassword(CUPHelper.getPasswordForFirstTimeLogin());
        Map <String, String> newAttributes = CUPHelper.getUserAttributesForFirstTimeLogin();
        if (newAttributes != null) {
            for(Map.Entry<String, String> attr: newAttributes.entrySet()) {
                Log.e(TAG, String.format("Adding attribute: %s, %s", attr.getKey(), attr.getValue()));
                newPasswordContinuation.setUserAttribute(attr.getKey(), attr.getValue());
            }
        }
        try {
            newPasswordContinuation.continueTask();
        } catch (Exception e) {
            closeWaitDialog();
            TextView label = findViewById(R.id.textViewUserIdMessage);
            label.setText("Sign-in failed");
            inPassword.setBackground(getDrawable(R.drawable.text_border_error));

            label = findViewById(R.id.textViewUserIdMessage);
            label.setText("Sign-in failed");
            inUsername.setBackground(getDrawable(R.drawable.text_border_error));

            showDialogMessage("Sign-in failed", CUPHelper.formatException(e));
        }
    }

    private void confirmUser(boolean doResend) {
        Intent confirmActivity = new Intent(this, SignUpConfirm.class);
        confirmActivity.putExtra("source","main");
        confirmActivity.putExtra("resend", doResend);
        confirmActivity.putExtra("username", username);
        startActivityForResult(confirmActivity, 2);

    }

    private void launchRankings() {
        Intent rankingsActivity = new Intent(this, RankingsHome.class);
        rankingsActivity.putExtra("name", username);
        startActivityForResult(rankingsActivity, 4);
    }

    private void findCurrent() {
        CognitoUser user = CUPHelper.getPool().getCurrentUser();
        username = user.getUserId();
        if(username != null) {
            CUPHelper.setUser(username);
            inUsername.setText(user.getUserId());
            user.getSessionInBackground(new FFRAuthHandler(true));
        } else {
            setDisplayForSignIn();
        }
    }

    private void getUserAuthentication(AuthenticationContinuation continuation, String username) {
        if(username != null) {
            this.username = username;
            CUPHelper.setUser(username);
        }
        if(this.password == null) {
            inUsername.setText(username);
            password = inPassword.getText().toString();
            if(password == null) {
                TextView label = findViewById(R.id.textViewUserPasswordMessage);
                label.setText(inPassword.getHint()+" enter password");
                inPassword.setBackground(getDrawable(R.drawable.text_border_error));
                return;
            }

            if(password.length() < 1) {
                TextView label = findViewById(R.id.textViewUserPasswordMessage);
                label.setText(inPassword.getHint()+" enter password");
                inPassword.setBackground(getDrawable(R.drawable.text_border_error));
                return;
            }
        }
        AuthenticationDetails authenticationDetails = new AuthenticationDetails(this.username, password, null);
        continuation.setAuthenticationDetails(authenticationDetails);
        continuation.continueTask();
    }

    // initialize app
    private void initApp() {
        inUsername = findViewById(R.id.editTextUserId);
        inUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() == 0) {
                    TextView label = findViewById(R.id.textViewUserIdLabel);
                    label.setText(R.string.Username);
                    inUsername.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = findViewById(R.id.textViewUserIdMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) {
                    TextView label = findViewById(R.id.textViewUserIdLabel);
                    label.setText("");
                }
            }
        });

        inPassword = findViewById(R.id.editTextUserPassword);
        inPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() == 0) {
                    TextView label = findViewById(R.id.textViewUserPasswordLabel);
                    label.setText(R.string.Password);
                    inPassword.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = findViewById(R.id.textViewUserPasswordMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) {
                    TextView label = findViewById(R.id.textViewUserPasswordLabel);
                    label.setText("");
                }
            }
        });
    }


    // Callbacks
    private final ForgotPasswordHandler forgotPasswordHandler = new ForgotPasswordHandler() {
        @Override
        public void onSuccess() {
            closeWaitDialog();
            showDialogMessage("Password successfully changed!","");
            inPassword.setText("");
            inPassword.requestFocus();
        }

        @Override
        public void getResetCode(ForgotPasswordContinuation forgotPasswordContinuation) {
            closeWaitDialog();
            getForgotPasswordCode(forgotPasswordContinuation);
        }

        @Override
        public void onFailure(Exception e) {
            closeWaitDialog();
            showDialogMessage("Forgot password failed", CUPHelper.formatException(e));
        }
    };

    private class FFRAuthHandler implements AuthenticationHandler {
        private final boolean isRefresh;

        FFRAuthHandler(boolean refresh) {
            this.isRefresh = refresh;
        }

        @Override
        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
            Log.i(TAG, "Auth Success");
            closeWaitDialog();
            launchRankings();
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String username) {
            closeWaitDialog();
            Locale.setDefault(Locale.US);
            getUserAuthentication(authenticationContinuation, username);
            setDisplayForSignIn();
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
            // Doesn't happen
            closeWaitDialog();
        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
            Log.d(TAG, "Authentication challenge thrown, should never happen.");
            setDisplayForSignIn();
        }

        @Override
        public void onFailure(Exception e) {
            closeWaitDialog();

            TextView label = findViewById(R.id.textViewUserIdMessage);
            label.setText("Sign-in failed");
            inPassword.setBackground(getDrawable(R.drawable.text_border_error));

            label = findViewById(R.id.textViewUserIdMessage);
            label.setText("Sign-in failed");
            inUsername.setBackground(getDrawable(R.drawable.text_border_error));

            if (!GeneralUtils.confirmInternet(getApplication())) {
                notifyUserOnInternet(isRefresh);
            } else {
                showDialogMessage("Sign-in failed", CUPHelper.formatException(e));
                setDisplayForSignIn();
            }
        }
    }

    private void notifyUserOnInternet(boolean isRefresh) {
        Flashbar.OnActionTapListener snackBarListener = flashbar -> findCurrent();
        if (isRefresh) {
            FlashbarFactory.generateInfiniteFlashbarWithAction(this, "No can do", "No internet connection", Flashbar.Gravity.BOTTOM,
                    snackBarListener, "Re-connect")
                    .show();
        } else {

            FlashbarFactory.generateTextOnlyFlashbar(this, "No can do", "No internet connection", Flashbar.Gravity.TOP)
                    .show();
        }
    }

    private void setDisplayForSignIn() {
        RelativeLayout buffer = findViewById(R.id.rankings_splash_buffer);
        buffer.setVisibility(View.GONE);
        RelativeLayout fields = findViewById(R.id.rankings_splash_bottom);
        fields.setVisibility(View.VISIBLE);
        titleView.setText("Welcome");
        toolbar.setVisibility(View.VISIBLE);
    }

    private void setDisplayForLoading() {
        RelativeLayout buffer = findViewById(R.id.rankings_splash_buffer);
        buffer.setVisibility(View.VISIBLE);
        RelativeLayout fields = findViewById(R.id.rankings_splash_bottom);
        fields.setVisibility(View.GONE);
        titleView.setText("");
        toolbar.setVisibility(View.INVISIBLE);
    }

    private void clearInput() {
        if(inUsername == null) {
            inUsername = findViewById(R.id.editTextUserId);
        }

        if(inPassword == null) {
            inPassword = findViewById(R.id.editTextUserPassword);
        }

        inUsername.setText("");
        inUsername.requestFocus();
        inUsername.setBackground(getDrawable(R.drawable.text_border_selector));
        inPassword.setText("");
        inPassword.setBackground(getDrawable(R.drawable.text_border_selector));
    }

    private void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new MaterialAlertDialogBuilder(this)
            .setTitle(message)
            .create();
        waitDialog.show();
    }

    private void showDialogMessage(String title, final String body) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", (dialog, which) -> {
            try {
                userDialog.dismiss();
                if (body.toLowerCase().contains("confirmed")) {
                    confirmUser(true);
                }
            } catch (Exception ignored) {
            }
        });
        userDialog = builder.create();
        userDialog.show();
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
