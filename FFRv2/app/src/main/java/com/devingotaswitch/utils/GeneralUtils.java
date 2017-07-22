package com.devingotaswitch.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GeneralUtils {

    private static final Long SECONDS_CONVERSION_THRESHOLD = 1000L;

    public static boolean confirmInternet(Context cont) {
        ConnectivityManager connectivityManager = (ConnectivityManager) cont
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static List<String> sortData(
            List<String> data) {
        Collections.sort(data, new Comparator<String>() {
            public int compare(String a, String b) {
                int judgment = a.compareTo(b);
                if (judgment < 0) {
                    return -1;
                }
                if (judgment > 0) {
                    return 1;
                }
                return 0;
            }
        });
        return data;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static String nameifyWords(String line) {
        return Character.toUpperCase(line.charAt(0))
                + line.substring(1).toLowerCase();
    }

    public static long getLatency(long start) {
        return (System.currentTimeMillis() - start) / SECONDS_CONVERSION_THRESHOLD;
    }
}
