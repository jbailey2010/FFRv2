package com.devingotaswitch.ffrv2.cognito;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.regions.Regions;

public class CognitoUserPoolsManager {
    private static final String USER_POOL_ID = "us-west-2_zxCdKKDP8";
    private static final String CLIENT_ID = "6bqbn1vscae9o8b05nu28ou24i";
    private static final String CLIENT_SECRET = null;
    private static final String CUP_TAG = "CUPManager";

    private static final String EMAIL_ATTRIBUTE = "email";
    private static final String PHONE_ATTRIBUTE = "phone_number";

    private static final String REGION = Regions.US_WEST_2.getName();

    private static CognitoUserPool userPool;

    public static CognitoUserPool getUserPool(Context context) {
        if (userPool == null) {
            userPool = new CognitoUserPool(context, USER_POOL_ID, CLIENT_ID, CLIENT_SECRET, new ClientConfiguration());
        }
        return userPool;
    }

    public void signUpUser(final Context context, String phoneNumber, String email, String userName, String password) {
        CognitoUserAttributes userAttributes = new CognitoUserAttributes();
        userAttributes.addAttribute(PHONE_ATTRIBUTE, phoneNumber);
        userAttributes.addAttribute(EMAIL_ATTRIBUTE, email);
        SignUpHandler signupCallback = new SignUpHandler() {
            @Override
            public void onSuccess(CognitoUser cognitoUser, boolean userConfirmed, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                if(!userConfirmed) {
                    // This user must be confirmed and a confirmation code was sent to the user
                    // cognitoUserCodeDeliveryDetails will indicate where the confirmation code was sent
                    // Get the confirmation code from user
                    // TODO: Finish this logic
                }
                else {

                    // TODO: this should send itself somewhere
                    Toast.makeText(context, "Successfully signed up", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception exception) {
                Log.e(CUP_TAG, "Failed to sign up", exception);
                // TODO: Make this a pop up
                Toast.makeText(context, exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        userPool.signUpInBackground(userName, password, userAttributes, null, signupCallback);
    }

    public String getLoginKey() {
        return new StringBuilder("cognito-idp.")
                .append(REGION)
                .append(".amazonaws.com/")
                .append(USER_POOL_ID)
                .toString();
    }
}