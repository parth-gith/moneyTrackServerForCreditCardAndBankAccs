package com.parth.money.moneyserverapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.parth.money.moneyserverapp.Adapter.CustomAdapterForSummary;
import com.parth.money.moneyserverapp.Adapter.CustomSpinnerItemAdapter;
import com.parth.money.moneyserverapp.Model.CustomSpinnerItem;
import com.parth.money.moneyserverapp.Model.SummaryModel;
import com.parth.money.moneyserverapp.Model.moneyServerCCResponseEntity;
import com.parth.money.moneyserverapp.NetworkUtils.RetrofitUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNormalTxnActivity extends AppCompatActivity {

    List<String> months = Arrays.asList(
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
    );
    static boolean isCheckedEMIflag = false;
    static Integer noOfEmisINTEGER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_normal_txn);

        moneyServerCCResponseEntity entityToPost = new moneyServerCCResponseEntity();

        //MONTH
        Spinner monthSpinner = findViewById(R.id.monthSpinner);
        ArrayAdapter<String> monthsadapter = new ArrayAdapter<>(this,  R.layout.my_spinner, months);
        monthsadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthsadapter);
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                entityToPost.setTxnBillingMonth(monthSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //YEAR
        Spinner yearSpinner = findViewById(R.id.yearSpinner);
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 10; i <= currentYear + 10; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearsadapter = new ArrayAdapter<>(this, R.layout.my_spinner, years);
        yearsadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearsadapter);
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                entityToPost.setTxnBillingYear(yearSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //CCNAME
        Spinner CCspinner = findViewById(R.id.CCnameSpinner);
        List<CustomSpinnerItem> spinnerItems = new ArrayList<>();
        spinnerItems.add(new CustomSpinnerItem("AmazonPay ICICI Visa", true));
        spinnerItems.add(new CustomSpinnerItem("HDFC Regalia Gold MasterCard-WORLD", true));
        spinnerItems.add(new CustomSpinnerItem("Standard Chartered Ultimate MasterCard-WORLD", true));
        spinnerItems.add(new CustomSpinnerItem("HSBC PLATINUM Visa", true));
        spinnerItems.add(new CustomSpinnerItem("ONECard METAL Visa", true));
        spinnerItems.add(new CustomSpinnerItem("Swiggy HDFC Visa", true));
        spinnerItems.add(new CustomSpinnerItem("IRCTC SBI Visa", true));
        spinnerItems.add(new CustomSpinnerItem("YesBank Reserv MasterCard-WORLD", true));
        spinnerItems.add(new CustomSpinnerItem("RBL WorldSafari MasterCard-WORLD", true));
        spinnerItems.add(new CustomSpinnerItem("MakeMyTrip ICICI MasterCard-WORLD & Rupay", true));
        spinnerItems.add(new CustomSpinnerItem("Marriott Bonvoy HDFC Diner's Club International", true));
        spinnerItems.add(new CustomSpinnerItem("Rupay HDFC", true));
        spinnerItems.add(new CustomSpinnerItem("YesBank Elite+ MasterCard-WORLD", false));
        spinnerItems.add(new CustomSpinnerItem("AmazonPayLater OLD-CLOSED", false));
        spinnerItems.add(new CustomSpinnerItem("FlipkartPayLater OLD-CLOSED", false));

        CustomSpinnerItemAdapter ccNamesAdapter = new CustomSpinnerItemAdapter(this, android.R.layout.simple_spinner_item, spinnerItems);
        CCspinner.setAdapter(ccNamesAdapter);
        CCspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                entityToPost.setTxnCCused(CCspinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //TXNDETAILS
        EditText txnDetailsField = findViewById(R.id.txnDetailsField);
        entityToPost.setTxnDetails("X");
        txnDetailsField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                entityToPost.setTxnDetails(s.toString());
            }
        });

        //TXNAMOUNT
        EditText txnAmountsField = findViewById(R.id.txnAmountField);
        entityToPost.setTxnAmount(new BigDecimal(0));
        txnAmountsField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(isNumeric(s.toString())){
                    entityToPost.setTxnAmount(new BigDecimal(s.toString()));
                }else{
                    txnAmountsField.setError("Only Numeric Values Allowed");
                }

            }
        });

        //ISEMI
        CheckBox isEMIcheck = findViewById(R.id.isEMIcheckbox);
        entityToPost.setTxnIsEmi(false);
        EditText noOfemiEditText = findViewById(R.id.noOfemiS);
        isEMIcheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    isCheckedEMIflag = true;
                    noOfemiEditText.setVisibility(View.VISIBLE);
                }else{
                    isCheckedEMIflag = false;
                    noOfemiEditText.setVisibility(View.GONE);
                }
            }
        });
        noOfemiEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(isNumeric(s.toString())){
                    noOfEmisINTEGER = Integer.parseInt(s.toString());
                }else{
                    noOfemiEditText.setError("Only Numeric Values Allowed");
                }
            }
        });

        // POST txn
        Button postTxnButton = findViewById(R.id.POSTtxnbtn);
        postTxnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!entityToPost.getTxnAmount().toString().isEmpty() && !entityToPost.getTxnDetails().toString().isEmpty() ){
                    if(!isCheckedEMIflag){
                        CCtxnPOSTER(entityToPost);
                    }else{
                        CCEMItxnPOSTER(entityToPost,noOfEmisINTEGER);
                        isCheckedEMIflag = false; // BugFix : static isCheckedEMIflag stays true if previous added txn is emi. So marking it false after eni txn is added successfully
                    }
                }else{
                    txnDetailsField.setError("Valid txn details required");
                    txnAmountsField.setError("Valid txn amount required");
                }
            }
        });

        // EXIT txn
        Button cancelPostTxn = findViewById(R.id.CANCELtxnbtn);
        cancelPostTxn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddNormalTxnActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }


    public void CCEMItxnPOSTER(moneyServerCCResponseEntity entityToPost,Integer noOfEmisINTEGER){
        Call<List<moneyServerCCResponseEntity>> responseFromRetrofit = RetrofitUtils.getInstance().getApiService().postEMITxn(noOfEmisINTEGER,entityToPost);
        responseFromRetrofit.enqueue(new Callback<List<moneyServerCCResponseEntity>>() {
            @Override
            public void onResponse(Call<List<moneyServerCCResponseEntity>> call, Response<List<moneyServerCCResponseEntity>> response) {
                if(response.code()==200){
                    if(response.body()==null){
                        showDialog("FAIL: DB lastUsed year/month mismatch - txn unsuccessful");
                    }else{
                        showDialog("SUCCESS: 200 - OK - txn successful");
                    }
                } else if (response.code()==500) {
                    showDialog("FAIL: 500 - Internal server error - txn unsuccessful");
                }
            }

            @Override
            public void onFailure(Call<List<moneyServerCCResponseEntity>> call, Throwable t) {

            }
        });

    }

    public void CCtxnPOSTER(moneyServerCCResponseEntity entityToPost){
        Call<moneyServerCCResponseEntity> responseFromRetrofit = RetrofitUtils.getInstance().getApiService().postNormalTxn(entityToPost);
        responseFromRetrofit.enqueue(new Callback<moneyServerCCResponseEntity>() {
            @Override
            public void onResponse(Call<moneyServerCCResponseEntity> call, Response<moneyServerCCResponseEntity> response) {
                if(response.code()==200){
                    if(response.body()==null){
                        showDialog("FAIL: DB lastUsed year/month mismatch - txn unsuccessful");
                    }else{
                        showDialog("SUCCESS: 200 - OK - txn successful");
                    }
                } else if (response.code()==500) {
                    showDialog("FAIL: 500 - Internal server error - txn unsuccessful");
                }
            }

            @Override
            public void onFailure(Call<moneyServerCCResponseEntity> call, Throwable t) {

            }
        });
    }

    private void showDialog(String dialogData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddNormalTxnActivity.this);
        builder.setMessage(dialogData)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(!dialogData.startsWith("FAIL:")){
                            Intent intent = new Intent(AddNormalTxnActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}