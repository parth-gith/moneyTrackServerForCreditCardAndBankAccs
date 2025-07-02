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

import com.parth.money.moneyserverapp.Model.SummaryModel;
import com.parth.money.moneyserverapp.R;

import java.math.BigDecimal;
import java.util.ArrayList;


public class CustomAdapterForSummary extends ArrayAdapter<SummaryModel> {

    private ArrayList<SummaryModel> dataSet;
    Context mContext;


    private static class ViewHolder {
        TextView t1;
        TextView t2;
        TextView t3;
        TextView t4;
        TextView t8;
        TextView t9;
        TextView t10;
        TextView t11;
        TextView t12;
        TextView t13;
        TextView t14;
        TextView t15;
        TextView t16;
        TextView t17;
        TextView t5;
        TextView t6;
        TextView t7;
    }

    public CustomAdapterForSummary(ArrayList<SummaryModel> data, Context context) {
        super(context, R.layout.card_view_summary, data);
        this.dataSet = data;
        this.mContext=context;

    }




    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SummaryModel summaryModel = getItem(position);
        ViewHolder viewHolder;

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.card_view_summary, parent, false);
            viewHolder.t1 = (TextView) convertView.findViewById(R.id.SummaryTextView1);
            viewHolder.t2 = (TextView) convertView.findViewById(R.id.SummaryTextView2);
            viewHolder.t3 = (TextView) convertView.findViewById(R.id.SummaryTextView3);
            viewHolder.t4 = (TextView) convertView.findViewById(R.id.SummaryTextView4);
            viewHolder.t8 = (TextView) convertView.findViewById(R.id.SummaryTextView8);
            viewHolder.t9 = (TextView) convertView.findViewById(R.id.SummaryTextView9);
            viewHolder.t10 = (TextView) convertView.findViewById(R.id.SummaryTextView10);
            viewHolder.t11 = (TextView) convertView.findViewById(R.id.SummaryTextView11);
            viewHolder.t12 = (TextView) convertView.findViewById(R.id.SummaryTextView12);
            viewHolder.t13 = (TextView) convertView.findViewById(R.id.SummaryTextView13);
            viewHolder.t14 = (TextView) convertView.findViewById(R.id.SummaryTextView14);
            viewHolder.t15 = (TextView) convertView.findViewById(R.id.SummaryTextView15);
            viewHolder.t16 = (TextView) convertView.findViewById(R.id.SummaryTextView16);
            viewHolder.t17 = (TextView) convertView.findViewById(R.id.SummaryTextView17);
            viewHolder.t5 = (TextView) convertView.findViewById(R.id.SummaryTextView5);
            viewHolder.t6 = (TextView) convertView.findViewById(R.id.SummaryTextView6);
            viewHolder.t7 = (TextView) convertView.findViewById(R.id.SummaryTextView7);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CustomAdapterForSummary.ViewHolder) convertView.getTag();
            result=convertView;
        }

        String DataMonthandyear = summaryModel.getMonth() +"-"+summaryModel.getYear();
        BigDecimal Standard_Chartered_Ultimate_Total = summaryModel.getStandard_Chartered_Ultimate_Total();
        BigDecimal HDFC_Regalia_Gold_Total = summaryModel.getHdfc_Regalia_Gold_Total();
        BigDecimal AmazonPay_icici_Total = summaryModel.getAmazonPay_icici_Total();
        BigDecimal HSBC_Visa_Platinum_Total = summaryModel.getHsbc_Visa_Platinum();
        BigDecimal OneCard_Metal_Total = summaryModel.getOneCard_Metal();
        BigDecimal Swiggy_HDFC_Total = summaryModel.getSwiggy_HDFC();
        BigDecimal IRCTC_SBI_Total = summaryModel.getIrctc_SBI();
        BigDecimal YesBank_ELITEPLUS_Total = summaryModel.getYesBank_ElitePlus();
        BigDecimal RBL_WorldSafari_Total = summaryModel.getRbl_WorldSafari();
        BigDecimal Marriott_HDFC_Total = summaryModel.getMarriott_HDFC();
        BigDecimal Rupay_HDFC_Total = summaryModel.getRupay_HDFC();
        BigDecimal YesBank_RESERV_Total = summaryModel.getYesBank_Reserv();
        BigDecimal MMT_ICICI_Total = summaryModel.getMmt_ICICI();
        BigDecimal Amazon_PayLater_Total = summaryModel.getAmazon_PayLater_Total();
        BigDecimal Flipkart_PayLater_Total = summaryModel.getFlipkart_PayLater_Total();
        BigDecimal Amount_Total = summaryModel.getAmount_Total();

        viewHolder.t1.setText(DataMonthandyear);
        viewHolder.t2.setText("Standard Chartered Ultimate MasterCard-WORLD : ₹"+ Standard_Chartered_Ultimate_Total.toString());
        viewHolder.t3.setText("HDFC Regalia Gold MasterCard-WORLD : ₹"+HDFC_Regalia_Gold_Total.toString());
        viewHolder.t4.setText("AmazonPay ICICI Visa : ₹"+AmazonPay_icici_Total.toString());
        viewHolder.t5.setText("AmazonPayLater OLD-CLOSED : ₹"+Amazon_PayLater_Total.toString());
        viewHolder.t6.setText("FlipkartPayLater OLD-CLOSED : ₹"+Flipkart_PayLater_Total.toString());
        viewHolder.t7.setText("Amount_Total : ₹"+Amount_Total.toString());
        viewHolder.t8.setText("HSBC PLATINUM Visa : ₹"+HSBC_Visa_Platinum_Total.toString());
        viewHolder.t9.setText("ONECard METAL Visa : ₹"+OneCard_Metal_Total.toString());
        viewHolder.t10.setText("Swiggy HDFC Visa : ₹"+Swiggy_HDFC_Total.toString());
        viewHolder.t11.setText("IRCTC SBI Visa : ₹"+IRCTC_SBI_Total.toString());
        viewHolder.t12.setText("YesBank Elite+ MasterCard-WORLD : ₹"+YesBank_ELITEPLUS_Total.toString());
        viewHolder.t13.setText("RBL WorldSafari MasterCard-WORLD : ₹"+RBL_WorldSafari_Total.toString());
        viewHolder.t14.setText("Marriott Bonvoy HDFC Diner's Club International : ₹"+Marriott_HDFC_Total.toString());
        viewHolder.t15.setText("Rupay HDFC : ₹"+Rupay_HDFC_Total.toString());
        viewHolder.t16.setText("YesBank Reserv MasterCard-WORLD : ₹"+YesBank_RESERV_Total.toString());
        viewHolder.t17.setText("MakeMyTrip ICICI MasterCard-WORLD & Rupay : ₹"+MMT_ICICI_Total.toString());
        viewHolder.t12.setTextColor(Color.parseColor("#E57373"));
        viewHolder.t5.setTextColor(Color.parseColor("#E57373"));
        viewHolder.t6.setTextColor(Color.parseColor("#E57373"));


        return convertView;
    }
}
