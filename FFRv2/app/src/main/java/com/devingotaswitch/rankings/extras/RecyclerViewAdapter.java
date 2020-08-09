package com.devingotaswitch.rankings.extras;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<Map<String, String>> mData;
    private LayoutInflater mInflater;
    private OnItemClickListener mClickListener;
    private OnItemLongClickListener mLongClickListener;
    private View.OnTouchListener mTouchListener;
    private int mLayoutView;
    private String[] mMapKeys;
    private int[] mViewIds;

    // data is passed into the constructor
    public RecyclerViewAdapter(Context context, List<Map<String, String>> data, int layoutView, String[] mapKeys,
                               int[] viewIds) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mLayoutView = layoutView;
        this.mMapKeys = mapKeys;
        this.mViewIds = viewIds;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(mLayoutView, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Map<String, String> datum = mData.get(position);
        for (int i = 0; i < mViewIds.length; i++) {
            String mapKey = mMapKeys[i];
            int viewId = mViewIds[i];
            if (holder.viewMap.get(viewId) instanceof TextView) {
                ((TextView)holder.viewMap.get(viewId)).setText(datum.get(mapKey));
            } else if (holder.viewMap.get(viewId) instanceof ImageView && !StringUtils.isBlank(datum.get(mapKey))) {
                ((ImageView)holder.viewMap.get(viewId)).setImageResource(Integer.parseInt(datum.get(mapKey)));
            } else if (holder.viewMap.get(viewId) instanceof ImageView) {
                ((ImageView)holder.viewMap.get(viewId)).setImageResource(android.R.color.transparent);
            }
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, View.OnClickListener, View.OnLongClickListener {

        Map<Integer, View> viewMap = new HashMap<>();

        ViewHolder(View itemView) {
            super(itemView);
            for (int id : mViewIds) {
                viewMap.put(id, itemView.findViewById(id));
            }
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnTouchListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (mLongClickListener != null) {
                return mLongClickListener.onItemLongClick(view, getAdapterPosition());
            } else {
                return true;
            }
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (mTouchListener != null) {
                return mTouchListener.onTouch(view, motionEvent);
            } else {
                return false;
            }
        }
    }

    // convenience method for getting data at click position
    Map<String, String> getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        this.mTouchListener = onTouchListener;

    }

    // parent activity will implement this method to respond to click events
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }
}