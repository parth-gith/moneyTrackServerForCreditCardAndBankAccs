package com.parth.money.moneyserverapp.ApiUtils;

import com.parth.money.moneyserverapp.Model.SummaryBankModel;
import com.parth.money.moneyserverapp.Model.SummaryModel;
import com.parth.money.moneyserverapp.Model.bankResponseEntity;
import com.parth.money.moneyserverapp.Model.moneyServerCCResponseEntity;
import com.parth.money.moneyserverapp.Model.moneyServerCCTopTxnResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiService {

    @GET("moneyServer/CreditCardTxnDetails/txn/allTxns")
    Call<List<moneyServerCCResponseEntity>> getAllData();

    @GET("moneyServer/CreditCardTxnDetails/txn")
    Call<List<moneyServerCCResponseEntity>> getDataFromMonthAndYear(@Query("month") String month,@Query("year") String year);

    @GET("moneyServer/CreditCardTxnDetails/txn/Summary/allTxns")
    Call<List<SummaryModel>> getAllDataSummary(@Query("multithreaded") String multithreaded);

    @GET("moneyServer/CreditCardTxnDetails/txn/Toptxns/allTxns")
    Call<List<moneyServerCCTopTxnResponseEntity>> getAllTopTxnData();

    @GET("moneyServer/BankTxnDetails/txn/allTxns")
    Call<List<bankResponseEntity>> getAllBankData();

    @GET("moneyServer/BankTxnDetails/txn")
    Call<List<bankResponseEntity>> getBankDataFromMonthAndYear(@Query("month") String month,@Query("year") String year);

    @GET("moneyServer/BankTxnDetails/txn/Summary/allTxns")
    Call<List<SummaryBankModel>> getAllBankDataSummary();

    @GET("moneyServer/BankTxnDetails/txn/avlBal")
    Call<BigDecimal> getAvlBalFrombankAccName(@Query("bankAccName") String bankAccName);

    @POST("moneyServer/CreditCardTxnDetails/txn")
    Call<moneyServerCCResponseEntity> postNormalTxn(@Body moneyServerCCResponseEntity entity);

    @POST("moneyServer/CreditCardTxnDetails/txn/addEMItxn")
    Call<List<moneyServerCCResponseEntity>> postEMITxn(@Query ("noOfEMIs") int noOfEMIs, @Body moneyServerCCResponseEntity entity);

    @POST("moneyServer/BankTxnDetails/txn")
    Call<bankResponseEntity> postBankNormalTxn(@Body bankResponseEntity entity);

    @DELETE("moneyServer/CreditCardTxnDetails/txn/txnDeleteNormal/{id}")
    Call<String> deleteNormaltxn(@Path("id") String id);

    @DELETE("moneyServer/CreditCardTxnDetails/txn/txnDeleteEmi/{id}")
    Call<String> deleteEmitxn(@Path("id") String id);

    @DELETE("moneyServer/BankTxnDetails/txn/txnDeleteNormal/{id}")
    Call<String> deleteBankNormaltxn(@Path("id") String id);

}