package com.devingotaswitch.ffrv2.cognito;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.regions.Regions;
import com.amazonaws.util.StringUtils;

public class CognitoUserPoolsManager {
    private static final String USER_POOL_ID = "us-west-2_zxCdKKDP8";
    private static final String CLIENT_ID = "6bqbn1vscae9o8b05nu28ou24i";
    private static final String CLIENT_SECRET = null;
    private static final String CUP_TAG = "CUPManager";

    private static final String EMAIL_ATTRIBUTE = "email";
    private static final String PHONE_ATTRIBUTE = "phone_number";
    private static final String NAME_ATTRIBUTE = "name";

    private static final String REGION = Regions.US_WEST_2.getName();

    private static CognitoUserPool userPool;

    public static CognitoUserPool getUserPool(Context context) {
        if (userPool == null) {
            userPool = new CognitoUserPool(context, USER_POOL_ID, CLIENT_ID, CLIENT_SECRET, new ClientConfiguration());
        }
        return userPool;
    }

    public void resumeSession(Context context) {
        CognitoUserPool userPool = getUserPool(context);
        CognitoUser user = userPool.getCurrentUser();
        if (!StringUtils.isBlank(user.getUserId())) {
            // No user has signed in, no session to resume.
            return;
        }

        AuthenticationHandler handler = new AuthenticationHandler(){
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                // TODO: Send to rankings/home
            }

            @Override
            public void getAuthenticationDetails(final AuthenticationContinuation continuation, final String userID) {
                // TODO: Go to sign in
            }

            @Override
            public void getMFACode(final MultiFactorAuthenticationContinuation continuation) {
                // TODO: Go to sign in
            }

            @Override
            public void authenticationChallenge(final ChallengeContinuation continuation) {
                // TODO: Go to sign in
            }

            @Override
            public void onFailure(final Exception exception) {
                // TODO: Go to sign in
                // TODO: Maybe log failure?
            }
        };
        user.getSession(handler);
    }
    public void signUpUser(final Context context, String phoneNumber, String email, String password, String name) {
        CognitoUserAttributes userAttributes = new CognitoUserAttributes();
        userAttributes.addAttribute(PHONE_ATTRIBUTE, phoneNumber);
        userAttributes.addAttribute(EMAIL_ATTRIBUTE, email);
        userAttributes.addAttribute(NAME_ATTRIBUTE, name);
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
        userPool.signUpInBackground(email, password, userAttributes, null, signupCallback);
    }

    public String getLoginKey() {
        return new StringBuilder("cognito-idp.")
                .append(REGION)
                .append(".amazonaws.com/")
                .append(USER_POOL_ID)
                .toString();
    }
}