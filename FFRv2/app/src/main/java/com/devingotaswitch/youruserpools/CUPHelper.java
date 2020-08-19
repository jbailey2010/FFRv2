package com.devingotaswitch.youruserpools;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.devingotaswitch.utils.AWSClientFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CUPHelper {
    private static Map<String, String> signUpFieldsC2O;

    private static CognitoUserPool CUP_CLIENT;
    private static String user;

    private static Map<String, String> firstTimeLogInUpDatedAttributes;

    public static void init(Context context) {
        setData();

        if (CUP_CLIENT != null) {
            return;
        }

        if (CUP_CLIENT == null) {
            // Create a user pool with default ClientConfiguration
            CUP_CLIENT = AWSClientFactory.getUserPoolsInstance(context);
        }

        firstTimeLogInUpDatedAttributes= new HashMap<>();
    }

    public static CognitoUserPool getPool() {
        return CUP_CLIENT;
    }

    public static Map<String, String> getSignUpFieldsC2O() {
        return signUpFieldsC2O;
    }

    public static String getCurrUser() {
        return user;
    }

    public static void setUser(String newUser) {
        user = newUser;
    }

    public static String formatException(Exception exception) {
        String formattedString = "Internal Error";
        Log.e("App Error",exception.toString());
        Log.getStackTraceString(exception);

        String temp = exception.getMessage();

        if(temp != null && temp.length() > 0) {
            formattedString = temp.split("\\(")[0];
            if(temp != null && temp.length() > 0) {
                return formattedString;
            }
        }

        return  formattedString;
    }

    public static Map<String, String> getUserAttributesForFirstTimeLogin() {
        return firstTimeLogInUpDatedAttributes;
    }

    private static void setData() {
        signUpFieldsC2O = new HashMap<>();
        signUpFieldsC2O.put("Name", "name");
        signUpFieldsC2O.put("Given name", "given_name");
        signUpFieldsC2O.put("Family name", "family_name");
        signUpFieldsC2O.put("Nick name", "nickname");
        signUpFieldsC2O.put("Phone number", "phone_number");
        signUpFieldsC2O.put("Phone number verified", "phone_number_verified");
        signUpFieldsC2O.put("Email verified", "email_verified");
        signUpFieldsC2O.put("Email","email");
        signUpFieldsC2O.put("Middle name","middle_name");
    }

}

