package com.devingotaswitch.utils;

import android.graphics.Color;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

public class GraphUtils {

    public static LineDataSet getLineDataSet(List<Entry> entries, String label, String color) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(Color.parseColor(color));
        dataSet.setDrawIcons(false);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        return dataSet;
    }

    public static void conditionallyAddData(LineData lineData, List<Entry> entries, String label, String color) {
        if (entries.size() > 0) {
            lineData.addDataSet(GraphUtils.getLineDataSet(entries, label, color));
        }
    }
}
