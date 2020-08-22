package com.devingotaswitch.rankings.extras

import android.R
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import androidx.appcompat.widget.AppCompatSpinner
import java.util.*

class MultiSelectionSpinner : AppCompatSpinner, DialogInterface.OnMultiChoiceClickListener {
    private var _items: Array<String>? = null
    private var mSelection: BooleanArray? = null
    private var defaultDisplay: String? = null
    private val simpleAdapter: ArrayAdapter<String>

    constructor(context: Context?) : super(context!!) {
        simpleAdapter = ArrayAdapter(context,
                R.layout.simple_spinner_item)
        super.setAdapter(simpleAdapter)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        simpleAdapter = ArrayAdapter(context,
                R.layout.simple_spinner_item)
        super.setAdapter(simpleAdapter)
    }

    override fun onClick(dialog: DialogInterface, which: Int, isChecked: Boolean) {
        if (mSelection != null && which < mSelection!!.size) {
            mSelection!![which] = isChecked
            simpleAdapter.clear()
            if (buildSelectedItemString().isNotEmpty()) {
                simpleAdapter.add(buildSelectedItemString())
            } else {
                simpleAdapter.add(defaultDisplay)
            }
        } else {
            throw IllegalArgumentException(
                    "Argument 'which' is out of bounds.")
        }
    }

    override fun performClick(): Boolean {
        val builder = AlertDialog.Builder(context)
        builder.setMultiChoiceItems(_items, mSelection, this)
        builder.setPositiveButton("Ok") { _: DialogInterface?, _: Int -> }
        builder.show()
        return true
    }

    override fun setAdapter(adapter: SpinnerAdapter) {
        throw RuntimeException(
                "setAdapter is not supported by MultiSelectSpinner.")
    }

    fun setItems(items: List<String>, defaultMesssage: String?) {
        _items = items.toTypedArray()
        mSelection = BooleanArray(_items!!.size)
        simpleAdapter.clear()
        simpleAdapter.add(defaultMesssage)
        defaultDisplay = defaultMesssage
        Arrays.fill(mSelection, false)
    }

    fun setSelection(selection: List<String>) {
        var found = 0
        Arrays.fill(mSelection, false)
        for (sel in selection) {
            for (j in _items!!.indices) {
                if (_items!![j] == sel) {
                    found++
                    mSelection!![j] = true
                }
            }
        }
        simpleAdapter.clear()
        if (found == 0) {
            simpleAdapter.add(defaultDisplay)
        } else {
            simpleAdapter.add(buildSelectedItemString())
        }
    }

    override fun setSelection(index: Int) {
        Arrays.fill(mSelection, false)
        if (index >= 0 && index < mSelection!!.size) {
            mSelection!![index] = true
        } else {
            throw IllegalArgumentException("Index " + index
                    + " is out of bounds.")
        }
        simpleAdapter.clear()
        simpleAdapter.add(buildSelectedItemString())
    }

    val selectedStrings: Set<String>
        get() {
            val selection: MutableSet<String> = HashSet()
            for (i in _items!!.indices) {
                if (mSelection!![i]) {
                    selection.add(_items!![i])
                }
            }
            return selection
        }

    private fun buildSelectedItemString(): String {
        val sb = StringBuilder()
        var foundOne = false
        for (i in _items!!.indices) {
            if (mSelection!![i]) {
                if (foundOne) {
                    sb.append(", ")
                }
                foundOne = true
                sb.append(_items!![i])
            }
        }
        return sb.toString()
    }
}