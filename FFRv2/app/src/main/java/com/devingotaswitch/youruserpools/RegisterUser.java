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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.services.cognitoidentityprovider.model.CodeDeliveryDetailsType;
import com.amazonaws.services.cognitoidentityprovider.model.SignUpResult;
import com.devingotaswitch.ffrv2.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class RegisterUser extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private EditText givenName;
    private EditText phone;
    private EditText email;

    private AlertDialog userDialog;
    private AlertDialog waitDialog;
    private String usernameInput;
    private String userPasswd;

    private static final String TAG = "RegisterUser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // get back to main screen
            String value = extras.getString("TODO");
            if (value.equals("exit")) {
                onBackPressed();
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar_Register);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        TextView main_title = findViewById(R.id.signUp_toolbar_title);
        main_title.setText("Sign up");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        init();
    }


    // This will create the list/form for registration
    private void init() {
        username = findViewById(R.id.editTextRegUserId);
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = findViewById(R.id.textViewRegUserIdLabel);
                    label.setText(username.getHint());
                    username.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = findViewById(R.id.textViewRegUserIdMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = findViewById(R.id.textViewRegUserIdLabel);
                    label.setText("");
                }
            }
        });
        //
        password = findViewById(R.id.editTextRegUserPassword);
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length() == 0) {
                    TextView label = findViewById(R.id.textViewRegUserPasswordLabel);
                    label.setText(password.getHint());
                    password.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = findViewById(R.id.textViewUserRegPasswordMessage);
                label.setText("");

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) {
                    TextView label = findViewById(R.id.textViewRegUserPasswordLabel);
                    label.setText("");
                }
            }
        });
        //
        givenName = findViewById(R.id.editTextRegGivenName);
        givenName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = findViewById(R.id.textViewRegGivenNameLabel);
                    label.setText(givenName.getHint());
                    givenName.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = findViewById(R.id.textViewRegGivenNameMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = findViewById(R.id.textViewRegGivenNameLabel);
                    label.setText("");
                }
            }
        });
        //
        phone = findViewById(R.id.editTextRegPhone);
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = findViewById(R.id.textViewRegPhoneLabel);
                    label.setText(phone.getHint());
                    phone.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = findViewById(R.id.textViewRegPhoneMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = findViewById(R.id.textViewRegPhoneLabel);
                    label.setText("");
                }
            }
        });
        //
        email = findViewById(R.id.editTextRegEmail);
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0) {
                    TextView label = findViewById(R.id.textViewRegEmailLabel);
                    label.setText(email.getHint());
                    email.setBackground(getDrawable(R.drawable.text_border_selector));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView label = findViewById(R.id.textViewRegEmailMessage);
                label.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    TextView label = findViewById(R.id.textViewRegEmailLabel);
                    label.setText("");
                }
            }
        });

        Button signUp = findViewById(R.id.signUp);
        signUp.setOnClickListener(v -> {
            // Read user data and register
            CognitoUserAttributes userAttributes = new CognitoUserAttributes();

            usernameInput = username.getText().toString();
            if (usernameInput == null || usernameInput.isEmpty()) {
                TextView view = findViewById(R.id.textViewRegUserIdMessage);
                view.setText(username.getHint() + " cannot be empty");
                username.setBackground(getDrawable(R.drawable.text_border_error));
                return;
            }

            String userpasswordInput = password.getText().toString();
            userPasswd = userpasswordInput;
            if (userpasswordInput == null || userpasswordInput.isEmpty()) {
                TextView view = findViewById(R.id.textViewUserRegPasswordMessage);
                view.setText(password.getHint() + " cannot be empty");
                password.setBackground(getDrawable(R.drawable.text_border_error));
                return;
            }

            String userInput = givenName.getText().toString();
            if (userInput != null && userInput.length() > 0) {
                userAttributes.addAttribute(CUPHelper.getSignUpFieldsC2O().get("Name"), userInput);
            } else {
                TextView view = findViewById(R.id.textViewRegGivenNameMessage);
                view.setText(givenName.getHint() + " cannot be empty");
                givenName.setBackground(getDrawable(R.drawable.text_border_error));
                return;
            }

            String phoneNumber = phone.getText().toString();
            if (phoneNumber != null && phoneNumber.length() > 0) {
                userAttributes.addAttribute(CUPHelper.getSignUpFieldsC2O().get("Phone number"), phoneNumber);
            } else {
                TextView view = findViewById(R.id.textViewRegPhoneMessage);
                view.setText(phone.getHint() + " cannot be empty");
                phone.setBackground(getDrawable(R.drawable.text_border_error));
                return;
            }

            String emailAddress = email.getText().toString();
            if (emailAddress != null && emailAddress.length() > 0) {
                userAttributes.addAttribute(CUPHelper.getSignUpFieldsC2O().get("Email"), emailAddress);
            } else {
                TextView view = findViewById(R.id.textViewRegEmailMessage);
                view.setText(email.getHint() + " cannot be empty");
                email.setBackground(getDrawable(R.drawable.text_border_error));
                return;
            }

            showWaitDialog();

            CUPHelper.getPool().signUpInBackground(usernameInput, userpasswordInput,
                    userAttributes, null, signUpHandler);

        });
    }

    private final SignUpHandler signUpHandler = new SignUpHandler() {
        @Override
        public void onSuccess(CognitoUser user, SignUpResult signUpResult) {
            // Check signUpConfirmationState to see if the user is already confirmed
            closeWaitDialog();
            if (signUpResult.isUserConfirmed()) {
                // User is already confirmed
                showDialogMessage("Sign up successful!",usernameInput+" has been confirmed", true);
            }
            else {
                // User is not confirmed
                confirmSignUp(signUpResult.getCodeDeliveryDetails());
            }
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            TextView label = findViewById(R.id.textViewRegUserIdMessage);
            label.setText("Sign up failed");
            username.setBackground(getDrawable(R.drawable.text_border_error));
            showDialogMessage("Sign up failed", CUPHelper.formatException(exception),false);
        }
    };

    private void confirmSignUp(CodeDeliveryDetailsType cognitoUserCodeDeliveryDetails) {
        Intent intent = new Intent(this, SignUpConfirm.class);
        intent.putExtra("source","signup");
        intent.putExtra("name", usernameInput);
        intent.putExtra("destination", cognitoUserCodeDeliveryDetails.getDestination());
        intent.putExtra("deliveryMed", cognitoUserCodeDeliveryDetails.getDeliveryMedium());
        intent.putExtra("attribute", cognitoUserCodeDeliveryDetails.getAttributeName());

        startActivityForResult(intent, 10);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if(resultCode == RESULT_OK){
                String name = null;
                if(data.hasExtra("name")) {
                    name = data.getStringExtra("name");
                }
                exit(name, userPasswd);
            }
        }
    }

    private void showDialogMessage(String title, String body, final boolean exit) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", (dialog, which) -> {
            try {
                userDialog.dismiss();
                if(exit) {
                    exit(usernameInput);
                }
            } catch (Exception e) {
                if(exit) {
                    exit(usernameInput);
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void showWaitDialog() {
        closeWaitDialog();
        waitDialog = new MaterialAlertDialogBuilder(this)
            .setTitle("Signing up...")
            .create();
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

    private void exit(String uname) {
        exit(uname, null);
    }

    private void exit(String uname, String password) {
        Intent intent = new Intent();
        if (uname == null) {
            uname = "";
        }
        if (password == null) {
            password = "";
        }
        intent.putExtra("name", uname);
        intent.putExtra("password", password);
        setResult(RESULT_OK, intent);
        finish();
    }
}
