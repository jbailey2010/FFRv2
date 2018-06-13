package com.devingotaswitch.youruserpools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.devingotaswitch.ffrv2.R;


class FirstTimeLoginAttributesDisplayAdapter extends BaseAdapter {
    private final int count;
    private static LayoutInflater layoutInflater;

    public FirstTimeLoginAttributesDisplayAdapter(Context context) {

        count = CUPHelper.getFirstTimeLogInItemsCount();

        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;

        if(convertView == null) {
            convertView = layoutInflater.inflate(R.layout.fields_generic, null);
            holder = new Holder();
            holder.label = convertView.findViewById(R.id.textViewUserDetailLabel);
            holder.data = convertView.findViewById(R.id.editTextUserDetailInput);
            holder.message = convertView.findViewById(R.id.textViewUserDetailMessage);

            convertView.setTag(holder);
        }
        else {
            holder = (Holder) convertView.getTag();
        }

        ItemToDisplay item = CUPHelper.getUserAttributeForFirstLogInCheck(position);
        holder.label.setText(item.getLabelText());
        holder.label.setTextColor(item.getLabelColor());
        holder.data.setHint(item.getLabelText());
        holder.data.setText(item.getDataText());
        holder.data.setTextColor(item.getDataColor());
        holder.message.setText(item.getMessageText());
        holder.message.setTextColor(item.getMessageColor());

        return convertView;
    }

    // Helper class to recycle View's
    static class Holder {
        TextView label;
        TextView data;
        TextView message;
    }
}