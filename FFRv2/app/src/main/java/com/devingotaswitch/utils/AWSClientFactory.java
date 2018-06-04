package com.devingotaswitch.utils;

import android.content.Context;

import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.sigv4.BasicCognitoUserPoolsAuthProvider;
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Regions;

public class AWSClientFactory {

    /*
     * Generic stuff
     */
    private static final Regions REGION = Regions.US_WEST_2;

    /*
     * AppSync stuff
     */
    private static volatile AWSAppSyncClient APPSYNC_CLIENT;
    // AppSync constants
    private static final String API_ID = "3gdsyyofs5euzmqyq3sq6yu7p4";
    private static final String API_URL = "https://ra7nkjn6mbhath7o3z2c42fbvu.appsync-api.us-west-2.amazonaws.com/graphql";

    public static AWSAppSyncClient getAppSyncInstance(final Context context) {
        if (APPSYNC_CLIENT == null) {
            APPSYNC_CLIENT = AWSAppSyncClient.builder()
                    .context(context)
                    .cognitoUserPoolsAuthProvider(getUserPools(context)) // For use with User Pools authorization
                    .region(REGION)
                    .serverUrl(API_URL)
                    .build();
        }
        return APPSYNC_CLIENT;
    }

    private static CognitoUserPoolsAuthProvider getUserPools(final Context context) {
        return new BasicCognitoUserPoolsAuthProvider(new CognitoUserPool(
                context,
                USER_POOL_ID,
                CLIENT_ID,
                CLIENT_SECRET,
                REGION));
    }

    /*
     * Cognito stuff
     */
    private static CognitoUserPool USER_POOLS_CLIENT;
    // Cogneato constants
    private static final String USER_POOL_ID = "us-west-2_LMdno4yy1";
    private static final String CLIENT_ID = "1d6n52mmd2m28t270fnu7ntbca";
    private static final String CLIENT_SECRET = "nh4tkf4kbppd2rckd4g5j0qhn1q55l2h4lcq8kqjahg1nlfe4r9";

    public static CognitoUserPool getUserPoolsInstance(final Context context) {
        if (USER_POOLS_CLIENT == null) {
            USER_POOLS_CLIENT = new CognitoUserPool(context, USER_POOL_ID, CLIENT_ID, CLIENT_SECRET, REGION);
        }
        return USER_POOLS_CLIENT;
    }
}
