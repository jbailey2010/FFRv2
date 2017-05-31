package com.devingotaswitch.youruserpools;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.regions.Regions;
import com.amazonaws.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class CIBHelper {
    private static final String TAG = "CIBHelper";

    private static CognitoCachingCredentialsProvider credentialsProvider;

    private static final String IDENTITY_POOL_ID = "us-west-2:2dbf85d8-fe1f-4e97-9c93-a4731883aed5";
    private static final Regions COGNITO_REGION = Regions.US_WEST_2;

    public static void init(Context context) {
        if (credentialsProvider != null) {
            return;
        }
        credentialsProvider = new CognitoCachingCredentialsProvider(context, IDENTITY_POOL_ID, COGNITO_REGION);
    }

    public static void refreshCredentials(Context context) {
        if (credentialsProvider == null) {
            Log.d(TAG, "Cannot refresh credentials, CIBHelper not initialized.");
            return;
        }
        CognitoUserPool pool = CUPHelper.getPool();
        if (pool == null) {
            Log.d(TAG, "Cannot refresh credentials, CUPHelper not initialized.");
            return;
        }
        CognitoUser user = pool.getCurrentUser();
        if (user == null || StringUtils.isBlank(user.getUserId())) {
            Log.d(TAG, "Cannot refresh credentials, no user logged in.");
            return;
        }
        if (!shouldRefreshCIB()) {
            Log.d(TAG, "Credentials are valid and cached.");
            return;
        }

        if (CUPHelper.shouldRefreshCUP()) {
            Log.d(TAG, "CUP needs a new session before getting creds.");
            CUPHelper.refreshSessionForUser(user, new UserRefreshHandler(context));
        } else {
            Log.d(TAG, "CUP did not need a new session before getting creds.");
            refreshCredentialsInternal(CUPHelper.getCurrSession().getIdToken().getJWTToken());
        }
    }

    private static void refreshCredentialsInternal(String idToken) {
        Log.d(TAG, "Getting new CIB credentials.");
        Map<String, String> logins = new HashMap<>();
        logins.put(CUPHelper.getIdentityPoolLoginKey(), idToken);
        credentialsProvider.setLogins(logins);
        new CredentialsRefresh().execute(credentialsProvider);
    }

    private static boolean shouldRefreshCIB() {
        if (credentialsProvider == null) {
            return false;
        } else if (credentialsProvider.getSessionCredentitalsExpiration() == null) {
            return true;
        }
        long currentTime = System.currentTimeMillis()
                - SDKGlobalConfiguration.getGlobalTimeOffset() * 1000;
        long timeRemaining = credentialsProvider.getSessionCredentitalsExpiration().getTime()
                - currentTime;
        return timeRemaining < (credentialsProvider.getRefreshThreshold() * 1000);
    }
    private static class CredentialsRefresh extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            CognitoCachingCredentialsProvider provider = (CognitoCachingCredentialsProvider) params[0];
            try {
                provider.getCredentials();
            } catch (Exception e) {
                Log.e(TAG, "Failed to refresh credentials", e);
            }
            return null;
        }
    }

    private static class UserRefreshHandler implements AuthenticationHandler {
        private Context cont;

        UserRefreshHandler(Context context) {
            cont = context;
        }

        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice device) {
            CUPHelper.setCurrSession(userSession);
            refreshCredentialsInternal(userSession.getIdToken().getJWTToken());
        }

        @Override
        public void getAuthenticationDetails(final AuthenticationContinuation continuation, final String userID) {
            Intent intent = new Intent(cont, MainActivity.class);
            cont.startActivity(intent);
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
            Log.e(TAG, "MFA challenge thrown when refreshing. This shouldn't happen.");
            Intent intent = new Intent(cont, MainActivity.class);
            cont.startActivity(intent);
        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
            Log.e(TAG, "Authentication challenge thrown when refreshing. This shouldn't happen.");
            Intent intent = new Intent(cont, MainActivity.class);
            cont.startActivity(intent);
        }

        @Override
        public void onFailure(final Exception exception) {
            Log.e(TAG, exception.getMessage());
            Intent intent = new Intent(cont, MainActivity.class);
            cont.startActivity(intent);
        }
    }

    public static void signOut() {
        credentialsProvider.clear();
    }
}
