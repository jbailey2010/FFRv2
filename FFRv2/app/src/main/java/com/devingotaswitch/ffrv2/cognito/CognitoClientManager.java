package com.devingotaswitch.ffrv2.cognito;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.regions.Regions;

public class CognitoClientManager {

    private static final String IDENTITY_POOL_ID = "us-west-2:2dbf85d8-fe1f-4e97-9c93-a4731883aed5";
    private static final Regions REGION = Regions.US_WEST_2;
    private static final String COGNITO_TAG = "CognitoClientManager";

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

    public void refreshCredentialsAsync(Context context) {
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
