package com.parth.money.moneyserverapp.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.parth.money.moneyserverapp.Model.bankResponseEntity;
import com.parth.money.moneyserverapp.Model.moneyServerCCResponseEntity;
import com.parth.money.moneyserverapp.R;

import java.math.BigDecimal;
import java.util.ArrayList;

public class CustomAdapterForBank extends ArrayAdapter<bankResponseEntity> {

    private ArrayList<bankResponseEntity> dataSet;
    Context mContext;


    private static class ViewHolder {
        TextView t1;
        TextView t2;
        TextView t3;
        TextView t4;
        TextView t5;
    }

    public CustomAdapterForBank(ArrayList<bankResponseEntity> data, Context context) {
        super(context, R.layout.card_view_banktxn, data);
        this.dataSet = data;
        this.mContext=context;

    }




    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        bankResponseEntity bankResponseEntityobj = getItem(position);
        ViewHolder viewHolder;

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.card_view_banktxn, parent, false);
            viewHolder.t1 = (TextView) convertView.findViewById(R.id.BanktextView1);
            viewHolder.t2 = (TextView) convertView.findViewById(R.id.BanktextView2);
            viewHolder.t3 = (TextView) convertView.findViewById(R.id.BanktextView3);
            viewHolder.t4 = (TextView) convertView.findViewById(R.id.BanktextView4);
            viewHolder.t5 = (TextView) convertView.findViewById(R.id.BanktextView5);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        String DataMonthandyear = bankResponseEntityobj.getBanktxnBillingDate()+"-"+bankResponseEntityobj.getBanktxnBillingMonth() +"-"+bankResponseEntityobj.getBanktxnBillingYear();
        String DataBankAccused = bankResponseEntityobj.getBankAccName();
        String DataTxnDetails = bankResponseEntityobj.getBanktxnDetails();
        BigDecimal DataTxnAmt = bankResponseEntityobj.getBanktxnAmount();
        BigDecimal DataTxnODAmt = bankResponseEntityobj.getBankODtxnAmount();

        viewHolder.t1.setText(DataMonthandyear);
        viewHolder.t2.setText(DataBankAccused);
        viewHolder.t3.setText(DataTxnDetails);
        viewHolder.t4.setText("Savings   :  "+DataTxnAmt.toString());
        viewHolder.t5.setText("OverDraft :  "+DataTxnODAmt.toString());

        if(DataTxnAmt.signum()==-1){
            viewHolder.t4.setTextColor(Color.parseColor("#FF0000"));
        }else{
            viewHolder.t4.setTextColor(Color.parseColor("#36802d"));
        }

        if(DataTxnODAmt.signum()==-1){
            viewHolder.t5.setTextColor(Color.parseColor("#FF0000"));
        }else{
            viewHolder.t5.setTextColor(Color.parseColor("#36802d"));
        }

        return convertView;
    }



}