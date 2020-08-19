package com.devingotaswitch.youruserpools

import android.content.Context
import android.util.Log
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.devingotaswitch.utils.AWSClientFactory.getUserPoolsInstance
import java.util.*

object CUPHelper {
    private var signUpFieldsC2O: MutableMap<String, String>? = null
    var pool: CognitoUserPool? = null
        private set
        @JvmStatic get
    var currUser: String? = null
        private set
        @JvmStatic get
    var userAttributesForFirstTimeLogin: Map<String, String>? = null
        private set
        @JvmStatic get

    @JvmStatic
    fun init(context: Context?) {
        setData()
        if (pool != null) {
            return
        }
        if (pool == null) {
            // Create a user pool with default ClientConfiguration
            pool = getUserPoolsInstance(context!!)
        }
        userAttributesForFirstTimeLogin = HashMap()
    }

    @JvmStatic
    fun getSignUpFieldsC2O(): Map<String, String>? {
        return signUpFieldsC2O
    }

    @JvmStatic
    fun setUser(newUser: String?) {
        currUser = newUser
    }

    @JvmStatic
    fun formatException(exception: Exception): String {
        var formattedString = "Internal Error"
        Log.e("App Error", exception.toString())
        Log.getStackTraceString(exception)
        val temp = exception.message
        if (temp != null && temp.isNotEmpty()) {
            formattedString = temp.split("\\(".toRegex())[0]
            if (temp.isNotEmpty()) {
                return formattedString
            }
        }
        return formattedString
    }

    private fun setData() {
        signUpFieldsC2O = HashMap()
        signUpFieldsC2O!!["Name"] = "name"
        signUpFieldsC2O!!["Given name"] = "given_name"
        signUpFieldsC2O!!["Family name"] = "family_name"
        signUpFieldsC2O!!["Nick name"] = "nickname"
        signUpFieldsC2O!!["Phone number"] = "phone_number"
        signUpFieldsC2O!!["Phone number verified"] = "phone_number_verified"
        signUpFieldsC2O!!["Email verified"] = "email_verified"
        signUpFieldsC2O!!["Email"] = "email"
        signUpFieldsC2O!!["Middle name"] = "middle_name"
    }
}