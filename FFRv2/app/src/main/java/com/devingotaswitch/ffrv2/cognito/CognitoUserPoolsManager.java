package com.devingotaswitch.ffrv2.cognito;

import android.content.Context;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Regions;

public class CognitoUserPoolsManager {
    private static final String USER_POOL_ID = "us-west-2_zxCdKKDP8";
    private static final String CLIENT_ID = "6bqbn1vscae9o8b05nu28ou24i";
    private static final String CLIENT_SECRET = null;
    private static final String REGION = Regions.US_WEST_2.getName();

    private static CognitoUserPool userPool;

    public static CognitoUserPool getUserPool(Context context) {
        if (userPool == null) {
            userPool = new CognitoUserPool(context, USER_POOL_ID, CLIENT_ID, CLIENT_SECRET, new ClientConfiguration());
        }
        return userPool;
    }

    public String getLoginKey() {
        return new StringBuilder("cognito-idp.")
                .append(REGION)
                .append(".amazonaws.com/")
                .append(USER_POOL_ID)
                .toString();
    }
}