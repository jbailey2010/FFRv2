package com.devingotaswitch.utils

import android.graphics.Color
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

object GraphUtils {
    @JvmStatic
    fun getLineDataSet(entries: List<Entry?>?, label: String, color: String): LineDataSet {
        val dataSet = LineDataSet(entries, label)
        dataSet.color = Color.parseColor(color)
        dataSet.setDrawIcons(false)
        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)
        return dataSet
    }

    @JvmStatic
    fun conditionallyAddData(lineData: LineData, entries: List<Entry?>, label: String, color: String) {
        if (entries.isNotEmpty()) {
            lineData.addDataSet(getLineDataSet(entries, label, color))
        }
    }
}