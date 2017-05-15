package com.devingotaswitch.ffrv2.cognito;


import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.regions.Regions;

public class CognitoClientManager {

    private static final String IDENTITY_POOL_ID = "us-west-2:2dbf85d8-fe1f-4e97-9c93-a4731883aed5";
    private static final Regions REGION = Regions.US_WEST_2;

    private static CognitoCredentialsProvider credentialsProvider;

    public CognitoCredentialsProvider getCredentialsProvider(Context context) {
        if (credentialsProvider == null) {
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    context,
                    IDENTITY_POOL_ID,
                    REGION
            );
        }
        return credentialsProvider;
    }
}
