package com.parth.money.moneyserverapp.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.parth.money.moneyserverapp.Model.CustomSpinnerItem;

import java.util.List;

public class CustomSpinnerItemAdapter extends ArrayAdapter<CustomSpinnerItem> {
    private List<CustomSpinnerItem> itemList;
    public CustomSpinnerItemAdapter(@NonNull Context context, int resource, @NonNull List<CustomSpinnerItem> objects) {
        super(context, resource, objects);
        this.itemList = objects;
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public boolean isEnabled(int position){
        return itemList.get(position).isEnabled();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);

        if (!itemList.get(position).isEnabled()) {
            view.setTextColor(Color.GRAY);
            view.setAlpha(0.5f);
        } else {
            view.setTextColor(Color.BLACK);
            view.setAlpha(1.0f);
        }

        return view;
    }

}
