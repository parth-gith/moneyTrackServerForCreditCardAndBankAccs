package com.parth.money.moneyServer.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parth.money.moneyServer.Entity.BankTxnDetailsMain;
import com.parth.money.moneyServer.Entity.CreditCardTopTxnDetailsMain;
import com.parth.money.moneyServer.Entity.CreditCardTxnDetailsMain;
import com.parth.money.moneyServer.Entity.SummaryModel;
import com.parth.money.moneyServer.Repository.BankTxnDetailsMainRepository;
import com.parth.money.moneyServer.Repository.CreditCardTopTxnDetailsMainRepository;
import com.parth.money.moneyServer.Repository.CreditCardTxnDetailsMainRepository;
import com.parth.money.moneyServer.Repository.MoneyServerPropertiesDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
public class PreloaderRedisCache {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CreditCardTxnDetailsMainRepository creditCardTxnDetailsMainRepository;

    @Autowired
    MoneyServerPropertiesDataRepository moneyServerPropertiesDataRepository;

    @Autowired
    CreditCardTopTxnDetailsMainRepository creditCardTopTxnDetailsMainRepository;

    @Autowired
    SummaryUtilityMultiThreaded summaryUtilityMultiThreaded;

    @Autowired
    BankTxnDetailsMainRepository bankTxnDetailsMainRepository;

    List<String> months = Arrays.asList(
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
    );

    @Async
    public void preloadRedisCache() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        preloadRedisCache_CCinternal();
        preloadRedisCache_Bankinternal();
        System.out.println("Redis preload cache complete !  -  STARTUP");
    }

    @Async
    public void preloadRedisCache_CreditCardsTXN(){
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        preloadRedisCache_CCinternal();
        System.out.println("Redis reload cache complete !  -  CCUPDATE");
    }

    @Async
    public void preloadRedisCache_BankTXN(){
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        preloadRedisCache_Bankinternal();
        System.out.println("Redis reload cache complete !  -  BANKUPDATE");
    }



    // private fns
    private void preloadRedisCache_CCinternal(){
        preloadRedisCache_getAll();
        preloadRedisCache_getTxnbyMonthAndYear();
        preloadRedisCache_getSummaryAlltxns();
        preloadRedisCache_getAllTopTxns();
        System.out.println("Redis cache process complete : preloadRedisCache_CCinternal");
    }
    private void preloadRedisCache_Bankinternal(){
        preloadRedisCache_getAll_Bank();
        preloadRedisCache_getTxnbyMonthAndYear_Bank();
        preloadRedisCache_getAllTopTxns();
        preloadRedisCache_getAvlBalFromAccName_Bank();
        System.out.println("Redis cache process complete : preloadRedisCache_Bankinternal");
    }

    // CC preload
    private void preloadRedisCache_getAll(){
        try {
            List<CreditCardTxnDetailsMain> returnList =  creditCardTxnDetailsMainRepository.findAll();
            Collections.sort(returnList, Comparator.comparing(CreditCardTxnDetailsMain::getTxnBillingYearINTEGER,Comparator.reverseOrder())
                    .thenComparing(CreditCardTxnDetailsMain::getTxnBillingMonthINTEGER,Comparator.reverseOrder()));
            String json = objectMapper.writeValueAsString(returnList);
            redisTemplate.opsForValue().set("allccTXN", json);
            System.out.println("preloaded redis cache for key : allccTXN");

        } catch (Exception e) {
            System.out.println("Exception during preloadRedisCache_getAll() ... E = "+e);
        }
    }

    private void preloadRedisCache_getTxnbyMonthAndYear(){
        int startMonthinDB = months.indexOf("September");
        String startYearinDB = "2023";
        String stopperEMIMonthinDB = moneyServerPropertiesDataRepository.findById("emilastUsedmonth").getValue();
        String stopperEMIYearinDB = moneyServerPropertiesDataRepository.findById("emilastUsedyear").getValue();
        try {
            while(true){
                if(stopperEMIMonthinDB.equalsIgnoreCase(months.get(startMonthinDB%12))
                        && stopperEMIYearinDB.equalsIgnoreCase(startYearinDB)){
                    break;
                }

                List<CreditCardTxnDetailsMain> returnList = creditCardTxnDetailsMainRepository.findByTxnBillingMonthAndTxnBillingYear(months.get(startMonthinDB%12),startYearinDB);
                Collections.sort(returnList, Comparator.comparing(CreditCardTxnDetailsMain::getTxnBillingYearINTEGER,Comparator.reverseOrder())
                        .thenComparing(CreditCardTxnDetailsMain::getTxnBillingMonthINTEGER,Comparator.reverseOrder()));
                String json = objectMapper.writeValueAsString(returnList);
                String dataKey = months.get(startMonthinDB%12) + "-" + startYearinDB + "-" + "ccTXN";
                redisTemplate.opsForValue().set(dataKey, json);
                System.out.println("preloaded redis cache for key : "+dataKey);

                if(startMonthinDB%12==11){
                    startYearinDB = String.valueOf(Integer.parseInt(startYearinDB)+1);
                }
                startMonthinDB+=1;
            }

        } catch (Exception e) {
            System.out.println("Exception during preloadRedisCache_getTxnbyMonthAndYear() ... E = "+e);
        }
    }

    private void preloadRedisCache_getSummaryAlltxns(){
        try{
            List<SummaryModel> returnData = summaryUtilityMultiThreaded.getSummarylist();
            String json = objectMapper.writeValueAsString(returnData);
            redisTemplate.opsForValue().set("allccTXNsummary", json);
            System.out.println("preloaded redis cache for key : allccTXNsummary");
        }catch (Exception e){
            System.out.println("Exception during preloadRedisCache_getSummaryAlltxns() ... E = "+e);
        }
    }

    private void preloadRedisCache_getAllTopTxns(){
        try{
            List<CreditCardTopTxnDetailsMain> returnList = creditCardTopTxnDetailsMainRepository.findAll();
            Collections.sort(returnList,Comparator.comparing(CreditCardTopTxnDetailsMain::getTxnAmount).reversed());
            String json = objectMapper.writeValueAsString(returnList);
            redisTemplate.opsForValue().set("allccTXNtop", json);
            System.out.println("preloaded redis cache for key : allccTXNtop");
        }catch(Exception e){
            System.out.println("Exception during preloadRedisCache_getSummaryAlltxns() ... E = "+e);
        }
    }

    // Bank preload
    private void preloadRedisCache_getAll_Bank(){
        List<BankTxnDetailsMain> returnList;
        try{
            returnList =  bankTxnDetailsMainRepository.findAll();
            Collections.sort(returnList, Comparator.comparing(BankTxnDetailsMain::getBankTxnBillingYearINTEGER,Comparator.reverseOrder())
                    .thenComparing(BankTxnDetailsMain::getBankTxnBillingMonthINTEGER,Comparator.reverseOrder()).thenComparing(BankTxnDetailsMain::getBanktxnBillingDateINTEGER,Comparator.reverseOrder()).thenComparing(BankTxnDetailsMain::getBankTxnSeqNumOrder,Comparator.reverseOrder()));
            String json = objectMapper.writeValueAsString(returnList);
            redisTemplate.opsForValue().set("allBankTXN", json);
            System.out.println("preloaded redis cache for key : allBankTXN");
        }catch(Exception e){
            System.out.println("Exception during preloadRedisCache_getAll_Bank() ... E = "+e);
        }
    }

    private void preloadRedisCache_getTxnbyMonthAndYear_Bank(){
        int startMonthinDB = months.indexOf("January");
        String startYearinDB = "2023";
        try{
            String stopperBankMonthinDB = months.get(LocalDate.now().getMonthValue());
            String stopperBankYearinDB = String.valueOf(LocalDate.now().getYear());

            while(true){
                if(stopperBankMonthinDB.equalsIgnoreCase(months.get(startMonthinDB%12))
                        && stopperBankYearinDB.equalsIgnoreCase(startYearinDB)){
                    break;
                }

                List<BankTxnDetailsMain> returnList = bankTxnDetailsMainRepository.findByBanktxnBillingMonthAndBanktxnBillingYear(months.get(startMonthinDB%12),startYearinDB);
                Collections.sort(returnList, Comparator.comparing(BankTxnDetailsMain::getBankTxnBillingYearINTEGER,Comparator.reverseOrder())
                        .thenComparing(BankTxnDetailsMain::getBankTxnBillingMonthINTEGER,Comparator.reverseOrder()).thenComparing(BankTxnDetailsMain::getBanktxnBillingDateINTEGER,Comparator.reverseOrder()).thenComparing(BankTxnDetailsMain::getBankTxnSeqNumOrder,Comparator.reverseOrder()));
                String json = objectMapper.writeValueAsString(returnList);
                String dataKey = months.get(startMonthinDB%12) + "-" + startYearinDB + "-" + "bankTXN";
                redisTemplate.opsForValue().set(dataKey, json);
                System.out.println("preloaded redis cache for key : "+dataKey);

                if(startMonthinDB%12==11){
                    startYearinDB = String.valueOf(Integer.parseInt(startYearinDB)+1);
                }
                startMonthinDB+=1;
            }
        }catch(Exception e){
            System.out.println("Exception during preloadRedisCache_getTxnbyMonthAndYear() ... E = "+e);
        }
    }

    private void preloadRedisCache_getAvlBalFromAccName_Bank(){
        try{
            String avlBalHDFC = moneyServerPropertiesDataRepository.findById("avlBal-HDFC").getValue();
            redisTemplate.opsForValue().set("avlBal-HDFC", avlBalHDFC);
            String avlBalBOM = moneyServerPropertiesDataRepository.findById("avlBal-BOM").getValue();
            redisTemplate.opsForValue().set("avlBal-BOM", avlBalBOM);
            String avlBalHDFCOD = moneyServerPropertiesDataRepository.findById("avlBal-HDFC-OD").getValue();
            redisTemplate.opsForValue().set("avlBal-HDFC-OD", avlBalHDFCOD);
            System.out.println("preloaded redis cache for avlBals..");
        }catch (Exception e){
            System.out.println("Exception during preloadRedisCache_getAvlBalFromAccName_Bank() ... E = "+e);
        }
    }
}
