package com.devingotaswitch.rankings.extras

import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.util.StringUtils
import java.util.*

class RecyclerViewAdapter(context: Context?, data: MutableList<MutableMap<String, String?>>, layoutView: Int, mapKeys: Array<String>,
                          viewIds: IntArray) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
    private val mData: List<Map<String, String?>> = data
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mClickListener: OnItemClickListener? = null
    private var mLongClickListener: OnItemLongClickListener? = null
    private var mTouchListener: View.OnTouchListener? = null
    private val mLayoutView: Int = layoutView
    private val mMapKeys: Array<String> = mapKeys
    private val mViewIds: IntArray = viewIds

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(mLayoutView, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = mData[position]
        for (i in mViewIds.indices) {
            val mapKey = mMapKeys[i]
            val viewId = mViewIds[i]
            if (holder.viewMap[viewId] is TextView) {
                (holder.viewMap[viewId] as TextView?)!!.text = datum[mapKey]
            } else if (holder.viewMap[viewId] is ImageView && !StringUtils.isBlank(datum[mapKey])) {
                (holder.viewMap[viewId] as ImageView?)!!.setImageResource((datum[mapKey] ?: error("")).toInt())
            } else if (holder.viewMap[viewId] is ImageView) {
                (holder.viewMap[viewId] as ImageView?)!!.setImageResource(R.color.transparent)
            }
        }
    }

    // total number of rows
    override fun getItemCount(): Int {
        return mData.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnTouchListener, View.OnClickListener, View.OnLongClickListener {
        val viewMap: MutableMap<Int, View> = HashMap()
        override fun onClick(view: View) {
            if (mClickListener != null) {
                mClickListener!!.onItemClick(view, adapterPosition)
            }
        }

        override fun onLongClick(view: View): Boolean {
            return if (mLongClickListener != null) {
                mLongClickListener!!.onItemLongClick(view, adapterPosition)
            } else {
                true
            }
        }

        override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
            return if (mTouchListener != null) {
                mTouchListener!!.onTouch(view, motionEvent)
            } else {
                false
            }
        }

        init {
            for (id in mViewIds) {
                viewMap[id] = itemView.findViewById(id)
            }
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            itemView.setOnTouchListener(this)
        }
    }

    // convenience method for getting data at click position
    fun getItem(id: Int): Map<String, String?> {
        return mData[id]
    }

    // allows clicks events to be caught
    fun setOnItemClickListener(itemClickListener: OnItemClickListener?) {
        mClickListener = itemClickListener
    }

    fun setOnItemLongClickListener(itemLongClickListener: OnItemLongClickListener?) {
        mLongClickListener = itemLongClickListener
    }

    fun setOnTouchListener(onTouchListener: View.OnTouchListener?) {
        mTouchListener = onTouchListener
    }

    // parent activity will implement this method to respond to click events
    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(view: View?, position: Int): Boolean
    }

}