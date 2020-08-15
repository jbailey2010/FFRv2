package com.devingotaswitch.utils

import android.content.Context
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.sigv4.BasicCognitoUserPoolsAuthProvider
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.regions.Regions

object AWSClientFactory {
    /*
     * Generic stuff
     */
    private val REGION = Regions.US_WEST_2

    /*
     * AppSync stuff
     */
    @Volatile
    private var APPSYNC_CLIENT: AWSAppSyncClient? = null

    // AppSync constants
    private const val API_ID = "3gdsyyofs5euzmqyq3sq6yu7p4"
    private const val API_URL = "https://ra7nkjn6mbhath7o3z2c42fbvu.appsync-api.us-west-2.amazonaws.com/graphql"
    @JvmStatic
    fun getAppSyncInstance(context: Context): AWSAppSyncClient? {
        if (APPSYNC_CLIENT == null) {
            APPSYNC_CLIENT = AWSAppSyncClient.builder()
                    .context(context)
                    .cognitoUserPoolsAuthProvider(getUserPools(context)) // For use with User Pools authorization
                    .region(REGION)
                    .serverUrl(API_URL)
                    .build()
        }
        return APPSYNC_CLIENT
    }

    private fun getUserPools(context: Context): CognitoUserPoolsAuthProvider {
        return BasicCognitoUserPoolsAuthProvider(CognitoUserPool(
                context,
                USER_POOL_ID,
                CLIENT_ID,
                CLIENT_SECRET,
                REGION))
    }

    /*
     * Cognito stuff
     */
    private var USER_POOLS_CLIENT: CognitoUserPool? = null

    // Cogneato constants
    private const val USER_POOL_ID = "us-west-2_WaDeKBgWJ"
    private const val CLIENT_ID = "1k3mgj979jdu5aburhj6pcotdg"
    private const val CLIENT_SECRET = "1g0bf9bthvmdu5llhafgg4pe23devkorm22sig28idn6hc431he6"
    @JvmStatic
    fun getUserPoolsInstance(context: Context): CognitoUserPool? {
        if (USER_POOLS_CLIENT == null) {
            USER_POOLS_CLIENT = CognitoUserPool(context, USER_POOL_ID, CLIENT_ID, CLIENT_SECRET, REGION)
            USER_POOLS_CLIENT!!.setAdvancedSecurityDataCollectionFlag(false)
        }
        return USER_POOLS_CLIENT
    }
}