package com.devingotaswitch.ffrv2.cognito;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.regions.Regions;

public class CognitoFederatedIdentitiesManager {

    private static final String IDENTITY_POOL_ID = "us-west-2:2dbf85d8-fe1f-4e97-9c93-a4731883aed5";
    private static final String COGNITO_TAG = "CognitoFederatedIdentitiesManager";

    private static final Regions REGION = Regions.US_WEST_2;

    private static CognitoCredentialsProvider credentialsProvider;

    public static CognitoCredentialsProvider getCredentialsProvider(Context context) {
        if (credentialsProvider == null) {
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    context,
                    IDENTITY_POOL_ID,
                    REGION
            );
        }
        return credentialsProvider;
    }

    public void refreshCredentialsAsync(Context context) {
        //TODO: This should take login tokens
        new CredentialsRefresh().execute(getCredentialsProvider(context));
    }

    private class CredentialsRefresh extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            CognitoCredentialsProvider provider = (CognitoCredentialsProvider) params[0];
            try {
                provider.refresh();
            } catch (Exception e) {
                Log.e(COGNITO_TAG, "Failed to refresh credentials", e);
            }
            return null;
        }
    }
}
