package com.parth.money.moneyserverapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.parth.money.moneyserverapp.Adapter.CustomAdapter;
import com.parth.money.moneyserverapp.Adapter.CustomAdapterForSummary;
import com.parth.money.moneyserverapp.Model.SummaryModel;
import com.parth.money.moneyserverapp.Model.moneyServerCCResponseEntity;
import com.parth.money.moneyserverapp.NetworkUtils.RetrofitUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private static CustomAdapter adapter;
    private static CustomAdapterForSummary summaryAdapter;
    ListView listView;
    ListView listView2;
    Button refreshButton;
    Switch summarySwitch;
    Button addNormaltxnbtn;
    Button topTxnViewButton;
    Button bankViewButton;

    static String monthForQueryMainList = "COMPLETE";
    static String yearForQueryMainList = "COMPLETE";

    List<String> months = Arrays.asList(
            "COMPLETE", "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView=(ListView)findViewById(R.id.list);
        MainListPopulator(listView);

        refreshButton = findViewById(R.id.refreshResults);
        summarySwitch = findViewById(R.id.SummarySwitchButton);
        listView2 = findViewById(R.id.list2);
        addNormaltxnbtn = findViewById(R.id.addNormaltxn);
        topTxnViewButton = findViewById(R.id.ToptxnsButton);
        bankViewButton = findViewById(R.id.BankTxnButton);


        //MONTH - monthSpinnerMainAct
        Spinner monthSpinner = findViewById(R.id.monthSpinnerMainAct);
        ArrayAdapter<String> monthsadapter = new ArrayAdapter<>(this,  R.layout.my_spinner, months);
        monthsadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthsadapter);
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                monthForQueryMainList = monthSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                monthForQueryMainList = "COMPLETE";
            }
        });

        //YEAR - yearSpinnerMainAct
        Spinner yearSpinner = findViewById(R.id.yearSpinnerMainAct);
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        years.add("COMPLETE");
        for (int i = currentYear - 10; i <= currentYear + 10; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearsadapter = new ArrayAdapter<>(this, R.layout.my_spinner, years);
        yearsadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearsadapter);
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                yearForQueryMainList = yearSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                yearForQueryMainList = "COMPLETE";
            }
        });


        // Refill Button
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainListPopulator(listView);
            }
        });

        // Summary Switch
        summarySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked==true){
                    listView2.setVisibility(View.VISIBLE);
                    SummaryListPopulator(listView2);

                }else{
                    listView2.setVisibility(View.GONE);
                }
            }
        });

        // Add Txn CreditCard
        addNormaltxnbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddNormalTxnActivity.class);
                startActivity(intent);
            }
        });

        // Top Txn View
        topTxnViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,TopTxnsActivity.class);
                startActivity(intent);
            }
        });

        // Bank Txns View
        bankViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,BankActivity.class);
                startActivity(intent);
            }
        });

    }


    public void SummaryListPopulator(ListView listView2){
        Call<List<SummaryModel>> responseFromRetrofit = RetrofitUtils.getInstance().getApiService().getAllDataSummary("true");

        responseFromRetrofit.enqueue(new Callback<List<SummaryModel>>() {
            @Override
            public void onResponse(Call<List<SummaryModel>> call, Response<List<SummaryModel>> response) {
                List<SummaryModel> responseList = response.body();

                summaryAdapter = new CustomAdapterForSummary((ArrayList<SummaryModel>) responseList,getApplicationContext());
                listView2.setAdapter(summaryAdapter);
            }

            @Override
            public void onFailure(Call<List<SummaryModel>> call, Throwable t) {

            }
        });
    }


    public void MainListPopulator(ListView listView){
        Call<List<moneyServerCCResponseEntity>> responseFromRetrofit = null;
        if("COMPLETE".equalsIgnoreCase(monthForQueryMainList) || "COMPLETE".equalsIgnoreCase(yearForQueryMainList)){
            responseFromRetrofit = RetrofitUtils.getInstance().getApiService().getAllData();
        }else{
            responseFromRetrofit = RetrofitUtils.getInstance().getApiService().getDataFromMonthAndYear(monthForQueryMainList,yearForQueryMainList);
        }

        if(responseFromRetrofit==null){
            return;
        }

        responseFromRetrofit.enqueue(new Callback<List<moneyServerCCResponseEntity>>() {
            @Override
            public void onResponse(Call<List<moneyServerCCResponseEntity>> call, Response<List<moneyServerCCResponseEntity>> response) {

                List<moneyServerCCResponseEntity> responseList = response.body();

                adapter = new CustomAdapter((ArrayList<moneyServerCCResponseEntity>) responseList,getApplicationContext());
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        moneyServerCCResponseEntity itemOnPosition = responseList.get(position);
                                String dialogData = itemOnPosition.getTxnBillingMonth()+"/"+itemOnPosition.getTxnBillingYear()
                                        +" --> "+itemOnPosition.getTxnCCused()+" --> "+itemOnPosition.getTxnDetails()+" --> Amt: "
                                        +itemOnPosition.getTxnAmount()+" --> isEmi: "+itemOnPosition.getTxnIsEmi();
                                showDialog(dialogData,itemOnPosition);
                    }
                });
            }

            @Override
            public void onFailure(Call<List<moneyServerCCResponseEntity>> call, Throwable t) {

            }
        });

    }

    private void showDialog(String dialogData,moneyServerCCResponseEntity itemOnPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(dialogData)
                .setCancelable(true)
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(!itemOnPosition.getTxnIsEmi()){
                            Call<String> response = RetrofitUtils.getInstance().getApiService().deleteNormaltxn(itemOnPosition.getTxnId());
                            response.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    if(response.code()==200){
                                        if(response.body()!=null){
                                            if("DELETE_ERROR".equalsIgnoreCase(response.body().toString())){
                                                showDialogAfterdelete("FAIL: DB lastUsed year/month mismatch - delete txn unsuccessful");
                                                dialog.dismiss();
                                            }else if ("DELETE_SUCCESS".equalsIgnoreCase(response.body().toString())){
                                                showDialogAfterdelete("SUCCESS: 200 - OK - delete txn successful");
                                                dialog.dismiss();
                                            }
                                        }else{
                                            showDialogAfterdelete("FAIL: 500 - null in response - delete txn unsuccessful");
                                            dialog.dismiss();
                                        }
                                    } else if (response.code()==500) {
                                        showDialogAfterdelete("FAIL: 500 - Internal server error - delete txn unsuccessful");
                                        dialog.dismiss();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {

                                }
                            });
                        }else if (itemOnPosition.getTxnIsEmi()){
                            Call<String> response = RetrofitUtils.getInstance().getApiService().deleteEmitxn(itemOnPosition.getTxnId());
                            response.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    if(response.code()==200){
                                        if(response.body()!=null){
                                            if("DELETE_ERROR".equalsIgnoreCase(response.body().toString())){
                                                showDialogAfterdelete("FAIL: DB lastUsed year/month mismatch - delete txn unsuccessful");
                                                dialog.dismiss();

                                            }else if ("DELETE_SUCCESS".equalsIgnoreCase(response.body().toString())){
                                                showDialogAfterdelete("SUCCESS: 200 - OK - delete txn successful");
                                                dialog.dismiss();
                                            }
                                        }else{
                                            showDialogAfterdelete("FAIL: 500 - null in response - delete txn unsuccessful");
                                            dialog.dismiss();
                                        }
                                    } else if (response.code()==500) {
                                        showDialogAfterdelete("FAIL: 500 - Internal server error - delete txn unsuccessful");
                                        dialog.dismiss();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {

                                }
                            });
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showDialogAfterdelete(String dialogData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(dialogData)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}