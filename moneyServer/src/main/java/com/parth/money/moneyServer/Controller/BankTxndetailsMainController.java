package com.parth.money.moneyServer.Controller;

import com.parth.money.moneyServer.Entity.*;
import com.parth.money.moneyServer.Repository.BankTxnDetailsMainRepository;
import com.parth.money.moneyServer.Repository.CreditCardTopTxnDetailsMainRepository;
import com.parth.money.moneyServer.Repository.MoneyServerPropertiesDataRepository;
import com.parth.money.moneyServer.Utils.BankSummaryUtility;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/moneyServer/BankTxnDetails")
public class BankTxndetailsMainController {

    @Autowired
    BankTxnDetailsMainRepository bankTxnDetailsMainRepository;

    @Autowired
    BankSummaryUtility bankSummaryUtility;

    @Autowired
    MoneyServerPropertiesDataRepository moneyServerPropertiesDataRepository;

    @Autowired
    CreditCardTopTxnDetailsMainRepository creditCardTopTxnDetailsMainRepository;

    @Autowired
    EntityManager entityManager;

    List<String> months = Arrays.asList(
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
    );


    //GET txns
    @GetMapping("/txn/{id}")
    public BankTxnDetailsMain getTxnbyId(@PathVariable String id){
        return bankTxnDetailsMainRepository.findByBanktxnId(id);
    }
    @GetMapping("/txn/allTxns")
    public List<BankTxnDetailsMain> getAll(){
        List<BankTxnDetailsMain> returnList =  bankTxnDetailsMainRepository.findAll();
        Collections.sort(returnList, Comparator.comparing(BankTxnDetailsMain::getBankTxnBillingYearINTEGER,Comparator.reverseOrder())
                .thenComparing(BankTxnDetailsMain::getBankTxnBillingMonthINTEGER,Comparator.reverseOrder()).thenComparing(BankTxnDetailsMain::getBanktxnBillingDateINTEGER,Comparator.reverseOrder()).thenComparing(BankTxnDetailsMain::getBankTxnSeqNumOrder,Comparator.reverseOrder()));
        return returnList;
    }

    @GetMapping("/txn")
    public List<BankTxnDetailsMain> getTxnbyMonthAndYear(@RequestParam String month, @RequestParam String year){
        List<BankTxnDetailsMain> returnList = bankTxnDetailsMainRepository.findByBanktxnBillingMonthAndBanktxnBillingYear(month,year);
        Collections.sort(returnList, Comparator.comparing(BankTxnDetailsMain::getBankTxnBillingYearINTEGER,Comparator.reverseOrder())
                .thenComparing(BankTxnDetailsMain::getBankTxnBillingMonthINTEGER,Comparator.reverseOrder()).thenComparing(BankTxnDetailsMain::getBanktxnBillingDateINTEGER,Comparator.reverseOrder()).thenComparing(BankTxnDetailsMain::getBankTxnSeqNumOrder,Comparator.reverseOrder()));
        return returnList;
    }

    @GetMapping("/txn/Summary")
    public BankSummaryModel getSummaryByMonthAndYear(@RequestParam(value="month",required=false,defaultValue="X")String month, @RequestParam(value="year",required=false,defaultValue="X")String year){
        BankSummaryModel summaryModel = bankSummaryUtility.getSummaryFromTxns(month,year);
        return summaryModel;
    }

    @GetMapping("/txn/Summary/allTxns")
    public List<BankSummaryModel> getSummaryAlltxns(){
        return bankSummaryUtility.getSummarylist();
    }

    @GetMapping("/txn/avlBal")
    public BigDecimal getAvlBalFromAccName(@RequestParam String bankAccName){
        String avlBal = moneyServerPropertiesDataRepository.findById(bankAccName).getValue();
        return new BigDecimal(avlBal);
    }



    // POST txns
    @Transactional
    @PostMapping("/txn")
    public BankTxnDetailsMain addNewTxn(@RequestBody BankTxnDetailsMain entity){

        String currDBlastusedYear = moneyServerPropertiesDataRepository.findById("banklastUsedyear").getValue();
        String currDBlastusedMonth = moneyServerPropertiesDataRepository.findById("banklastUsedmonth").getValue();

        if(entity.getBankTxnBillingYearINTEGER()==Integer.parseInt(currDBlastusedYear)){
            if((entity.getBankTxnBillingMonthINTEGER()-1)<months.indexOf(currDBlastusedMonth)){
                return null;
            }
        } else if (entity.getBankTxnBillingYearINTEGER()<Integer.parseInt(currDBlastusedYear)) {
            return null;
        }
        Query query = entityManager.createNamedQuery("MoneyServerPropertiesData.updateLastUsedMonthYear");
        if(!currDBlastusedMonth.equalsIgnoreCase(entity.getBanktxnBillingMonth())){
            query.setParameter("newData",entity.getBanktxnBillingMonth());
            query.setParameter("id","banklastUsedmonth");
            query.executeUpdate();
        }
        if(!currDBlastusedYear.equalsIgnoreCase(entity.getBanktxnBillingYear())){
            query.setParameter("newData",entity.getBanktxnBillingYear());
            query.setParameter("id","banklastUsedyear");
            query.executeUpdate();
        }
        BankTxnDetailsMain returnEntity = null;

        String bankAccName = "avlBal-HDFC";
        if(entity.getBankAccName().startsWith("HDFC Bank")){
            bankAccName = "avlBal-HDFC";
        }
        if(entity.getBankAccName().startsWith("Bank Of Maharashtra")){
            bankAccName = "avlBal-BOM";
        }
        String avlBaltemp = moneyServerPropertiesDataRepository.findById(bankAccName).getValue();
        BigDecimal floatAvlBal = new BigDecimal(avlBaltemp);
        BigDecimal txnAmtcurrent = entity.getBanktxnAmount();
        if(txnAmtcurrent.signum()==-1){
            // expense
            BigDecimal floatAvlBalAfterCal = floatAvlBal.add(txnAmtcurrent);
            if(bankAccName.equals("avlBal-HDFC")){
                if(floatAvlBalAfterCal.compareTo(BigDecimal.ZERO) == -1 && floatAvlBal.compareTo(BigDecimal.ZERO) == 1){
                    // new acc bal going -ve & existing acc bal is +ve  so, OD will be trigerred
                    // extra -ve bal will be added as OD bal & update acc bal as 0
//                    System.out.println("// new acc bal going -ve & existing acc bal is +ve  so, OD will be trigerred\n" +
//                            "                    // extra -ve bal will be added as OD bal & update acc bal as 0");
                    String avlODBaltemp = moneyServerPropertiesDataRepository.findById(bankAccName+"-OD").getValue();
                    BigDecimal floatAvlODBal = new BigDecimal(avlODBaltemp);
                    floatAvlODBal = floatAvlODBal.add(floatAvlBalAfterCal);

                    BigDecimal totalTxnAmt = entity.getBanktxnAmount();
                    BigDecimal txnMainPart = floatAvlBal.negate();
                    BigDecimal txnODPart = floatAvlBalAfterCal;

                    entity.setBanktxnAmount(txnMainPart);
                    entity.setBankODtxnAmount(txnODPart);
                    returnEntity =  bankTxnDetailsMainRepository.save(entity);
                    System.out.println("entity to save is : "+entity.toString());

                    CreditCardTopTxnDetailsMain topTxnEntity = new CreditCardTopTxnDetailsMain();
                    topTxnEntity.setTxnId(returnEntity.getBanktxnId());
                    topTxnEntity.setTxnDetails(returnEntity.getBanktxnDetails());
                    topTxnEntity.setTxnAmount(totalTxnAmt.negate());
                    topTxnEntity.setCcorbank("B");
                    creditCardTopTxnDetailsMainRepository.save(topTxnEntity);

                    System.out.println("floatAvlBal is "+floatAvlBal+" and floatAvlODBal is "+floatAvlODBal);
                    Query queryAvlBalupdate = entityManager.createNamedQuery("MoneyServerPropertiesData.updateAvlBalBybankAccName");
                    queryAvlBalupdate.setParameter("id",bankAccName+"-OD");
                    queryAvlBalupdate.setParameter("newData",floatAvlODBal.toString());
                    queryAvlBalupdate.executeUpdate();

                    floatAvlBal = BigDecimal.ZERO;
                    queryAvlBalupdate.setParameter("id",bankAccName);
                    queryAvlBalupdate.setParameter("newData",floatAvlBal.toString());
                    queryAvlBalupdate.executeUpdate();

                }else if(floatAvlBalAfterCal.compareTo(BigDecimal.ZERO) == -1 && floatAvlBal.compareTo(BigDecimal.ZERO) == 0){
                    // new acc bal going -ve & existing acc bal is 0  so, OD will be trigerred
                    // txn amount bal will be added as OD bal & acc bal should be already 0
//                    System.out.println("// new acc bal going -ve & existing acc bal is 0  so, OD will be trigerred\n" +
//                            "                    // txn amount bal will be added as OD bal & acc bal should be already 0");
                    String avlODBaltemp = moneyServerPropertiesDataRepository.findById(bankAccName+"-OD").getValue();
                    BigDecimal floatAvlODBal = new BigDecimal(avlODBaltemp);
                    floatAvlODBal = floatAvlODBal.add(txnAmtcurrent);

                    BigDecimal totalTxnAmt = entity.getBanktxnAmount();
                    BigDecimal txnMainPart = BigDecimal.ZERO;
                    BigDecimal txnODPart = txnAmtcurrent;

                    entity.setBanktxnAmount(txnMainPart);
                    entity.setBankODtxnAmount(txnODPart);
                    returnEntity =  bankTxnDetailsMainRepository.save(entity);
                    System.out.println("entity to save is : "+entity.toString());

                    CreditCardTopTxnDetailsMain topTxnEntity = new CreditCardTopTxnDetailsMain();
                    topTxnEntity.setTxnId(returnEntity.getBanktxnId());
                    topTxnEntity.setTxnDetails(returnEntity.getBanktxnDetails());
                    topTxnEntity.setTxnAmount(totalTxnAmt.negate());
                    topTxnEntity.setCcorbank("B");
                    creditCardTopTxnDetailsMainRepository.save(topTxnEntity);

                    System.out.println("floatAvlBal is "+floatAvlBal+" and floatAvlODBal is "+floatAvlODBal);
                    Query queryAvlBalupdate = entityManager.createNamedQuery("MoneyServerPropertiesData.updateAvlBalBybankAccName");
                    queryAvlBalupdate.setParameter("id",bankAccName+"-OD");
                    queryAvlBalupdate.setParameter("newData",floatAvlODBal.toString());
                    queryAvlBalupdate.executeUpdate();

                }else if(floatAvlBalAfterCal.compareTo(BigDecimal.ZERO) >= 0){
                    // normal case - new acc bal +ve and NO OD
//                    System.out.println("// normal case - new acc bal +ve and NO OD");
                    BigDecimal totalTxnAmt = entity.getBanktxnAmount();
                    BigDecimal txnODPart = BigDecimal.ZERO;

                    entity.setBanktxnAmount(totalTxnAmt);
                    entity.setBankODtxnAmount(txnODPart);
                    returnEntity =  bankTxnDetailsMainRepository.save(entity);
                    System.out.println("entity to save is : "+entity.toString());

                    CreditCardTopTxnDetailsMain topTxnEntity = new CreditCardTopTxnDetailsMain();
                    topTxnEntity.setTxnId(returnEntity.getBanktxnId());
                    topTxnEntity.setTxnDetails(returnEntity.getBanktxnDetails());
                    topTxnEntity.setTxnAmount(totalTxnAmt.negate());
                    topTxnEntity.setCcorbank("B");
                    creditCardTopTxnDetailsMainRepository.save(topTxnEntity);

                    System.out.println("floatAvlBal is "+floatAvlBal);
                    Query queryAvlBalupdate = entityManager.createNamedQuery("MoneyServerPropertiesData.updateAvlBalBybankAccName");
                    floatAvlBal = floatAvlBalAfterCal;
                    queryAvlBalupdate.setParameter("id",bankAccName);
                    queryAvlBalupdate.setParameter("newData",floatAvlBal.toString());
                    queryAvlBalupdate.executeUpdate();
                }
            }else if(bankAccName.equals("avlBal-BOM")){
                BigDecimal totalTxnAmt = entity.getBanktxnAmount();
                BigDecimal txnODPart = BigDecimal.ZERO;

                entity.setBanktxnAmount(totalTxnAmt);
                entity.setBankODtxnAmount(txnODPart);
                returnEntity =  bankTxnDetailsMainRepository.save(entity);
                System.out.println("entity to save is : "+entity.toString());

                CreditCardTopTxnDetailsMain topTxnEntity = new CreditCardTopTxnDetailsMain();
                topTxnEntity.setTxnId(returnEntity.getBanktxnId());
                topTxnEntity.setTxnDetails(returnEntity.getBanktxnDetails());
                topTxnEntity.setTxnAmount(totalTxnAmt.negate());
                topTxnEntity.setCcorbank("B");
                creditCardTopTxnDetailsMainRepository.save(topTxnEntity);

                System.out.println("floatAvlBal is "+floatAvlBal);
                Query queryAvlBalupdate = entityManager.createNamedQuery("MoneyServerPropertiesData.updateAvlBalBybankAccName");
                floatAvlBal = floatAvlBalAfterCal;
                queryAvlBalupdate.setParameter("id",bankAccName);
                queryAvlBalupdate.setParameter("newData",floatAvlBal.toString());
                queryAvlBalupdate.executeUpdate();
            }
        }else if(txnAmtcurrent.signum()==1){
            // income txn
            if(bankAccName.equals("avlBal-HDFC")){
                String avlODBaltemp = moneyServerPropertiesDataRepository.findById(bankAccName+"-OD").getValue();
                BigDecimal floatAvlODBal = new BigDecimal(avlODBaltemp);
                if(floatAvlODBal.compareTo(BigDecimal.ZERO) == -1){
                    // incoming txn amt is used for clearing OD
//                    System.out.println("// incoming txn amt is used for clearing OD");
                    BigDecimal floatTxnAmtAfterODSwap = floatAvlODBal.add(txnAmtcurrent);
                    if(floatTxnAmtAfterODSwap.compareTo(BigDecimal.ZERO) == 0){
                        // txn amt fully used in OD swap, outstanding amt due in OD is now 0
                        System.out.println("// txn amt fully used in OD swap, outstanding amt due in OD is now 0");
                        floatAvlODBal = BigDecimal.ZERO;
                        BigDecimal totalTxnAmt = txnAmtcurrent;
                        BigDecimal txnMainPart = BigDecimal.ZERO;
                        BigDecimal txnODPart = txnAmtcurrent;

                        entity.setBanktxnAmount(txnMainPart);
                        entity.setBankODtxnAmount(txnODPart);
                        returnEntity =  bankTxnDetailsMainRepository.save(entity);
                        System.out.println("entity to save is : "+entity.toString());

                        CreditCardTopTxnDetailsMain topTxnEntity = new CreditCardTopTxnDetailsMain();
                        topTxnEntity.setTxnId(returnEntity.getBanktxnId());
                        topTxnEntity.setTxnDetails(returnEntity.getBanktxnDetails());
                        topTxnEntity.setTxnAmount(totalTxnAmt.negate());
                        topTxnEntity.setCcorbank("B");
                        creditCardTopTxnDetailsMainRepository.save(topTxnEntity);

                        System.out.println("floatAvlBal is "+floatAvlBal+" and floatAvlODBal is "+floatAvlODBal);
                        Query queryAvlBalupdate = entityManager.createNamedQuery("MoneyServerPropertiesData.updateAvlBalBybankAccName");
                        queryAvlBalupdate.setParameter("id",bankAccName+"-OD");
                        queryAvlBalupdate.setParameter("newData",floatAvlODBal.toString());
                        queryAvlBalupdate.executeUpdate();

                    }else if(floatTxnAmtAfterODSwap.compareTo(BigDecimal.ZERO) == 1){
                        // txn amt partially used in OD swap making it 0 , remaining amt added to acc bal
//                        System.out.println("// txn amt partially used in OD swap making it 0 , remaining amt added to acc bal");
                        BigDecimal totalTxnAmt = txnAmtcurrent;
                        BigDecimal txnMainPart = floatTxnAmtAfterODSwap;
                        BigDecimal txnODPart = floatAvlODBal;
                        floatAvlODBal = BigDecimal.ZERO;
                        floatAvlBal = floatAvlBal.add(txnMainPart);

                        entity.setBanktxnAmount(txnMainPart);
                        entity.setBankODtxnAmount(txnODPart.multiply(new BigDecimal(-1)));
                        returnEntity =  bankTxnDetailsMainRepository.save(entity);
                        System.out.println("entity to save is : "+entity.toString());

                        CreditCardTopTxnDetailsMain topTxnEntity = new CreditCardTopTxnDetailsMain();
                        topTxnEntity.setTxnId(returnEntity.getBanktxnId());
                        topTxnEntity.setTxnDetails(returnEntity.getBanktxnDetails());
                        topTxnEntity.setTxnAmount(totalTxnAmt.negate());
                        topTxnEntity.setCcorbank("B");
                        creditCardTopTxnDetailsMainRepository.save(topTxnEntity);

                        System.out.println("floatAvlBal is "+floatAvlBal+" and floatAvlODBal is "+floatAvlODBal);
                        Query queryAvlBalupdate = entityManager.createNamedQuery("MoneyServerPropertiesData.updateAvlBalBybankAccName");
                        queryAvlBalupdate.setParameter("id",bankAccName+"-OD");
                        queryAvlBalupdate.setParameter("newData",floatAvlODBal.toString());
                        queryAvlBalupdate.executeUpdate();

                        queryAvlBalupdate.setParameter("id",bankAccName);
                        queryAvlBalupdate.setParameter("newData",floatAvlBal.toString());
                        queryAvlBalupdate.executeUpdate();

                    }else if(floatTxnAmtAfterODSwap.compareTo(BigDecimal.ZERO) == -1){
                        // txn amt fully used in OD swap, still some outstanding amt due in OD (-ve bal)
//                        System.out.println("// txn amt fully used in OD swap, still some outstanding amt due in OD (-ve bal)");
                        BigDecimal totalTxnAmt = txnAmtcurrent;
                        BigDecimal txnMainPart = BigDecimal.ZERO;
                        BigDecimal txnODPart = txnAmtcurrent;
                        floatAvlODBal = floatTxnAmtAfterODSwap;

                        entity.setBanktxnAmount(txnMainPart);
                        entity.setBankODtxnAmount(txnODPart);
                        returnEntity =  bankTxnDetailsMainRepository.save(entity);
                        System.out.println("entity to save is : "+entity.toString());

                        CreditCardTopTxnDetailsMain topTxnEntity = new CreditCardTopTxnDetailsMain();
                        topTxnEntity.setTxnId(returnEntity.getBanktxnId());
                        topTxnEntity.setTxnDetails(returnEntity.getBanktxnDetails());
                        topTxnEntity.setTxnAmount(totalTxnAmt.negate());
                        topTxnEntity.setCcorbank("B");
                        creditCardTopTxnDetailsMainRepository.save(topTxnEntity);

                        System.out.println("floatAvlBal is "+floatAvlBal+" and floatAvlODBal is "+floatAvlODBal);
                        Query queryAvlBalupdate = entityManager.createNamedQuery("MoneyServerPropertiesData.updateAvlBalBybankAccName");
                        queryAvlBalupdate.setParameter("id",bankAccName+"-OD");
                        queryAvlBalupdate.setParameter("newData",floatAvlODBal.toString());
                        queryAvlBalupdate.executeUpdate();

                    }
                }else{
                    // normal case - OD dues 0 , txn amt added to acc bal
//                    System.out.println("// normal case - OD dues 0 , txn amt added to acc bal");
                    BigDecimal totalTxnAmt = txnAmtcurrent;
                    BigDecimal txnODPart = BigDecimal.ZERO;

                    entity.setBanktxnAmount(totalTxnAmt);
                    entity.setBankODtxnAmount(txnODPart);
                    returnEntity =  bankTxnDetailsMainRepository.save(entity);
                    System.out.println("entity to save is : "+entity.toString());

                    CreditCardTopTxnDetailsMain topTxnEntity = new CreditCardTopTxnDetailsMain();
                    topTxnEntity.setTxnId(returnEntity.getBanktxnId());
                    topTxnEntity.setTxnDetails(returnEntity.getBanktxnDetails());
                    topTxnEntity.setTxnAmount(totalTxnAmt.negate());
                    topTxnEntity.setCcorbank("B");
                    creditCardTopTxnDetailsMainRepository.save(topTxnEntity);

                    System.out.println("floatAvlBal is "+floatAvlBal);
                    Query queryAvlBalupdate = entityManager.createNamedQuery("MoneyServerPropertiesData.updateAvlBalBybankAccName");
                    floatAvlBal = floatAvlBal.add(totalTxnAmt);
                    queryAvlBalupdate.setParameter("id",bankAccName);
                    queryAvlBalupdate.setParameter("newData",floatAvlBal.toString());
                    queryAvlBalupdate.executeUpdate();

                }
            }else if(bankAccName.equals("avlBal-BOM")){
                BigDecimal totalTxnAmt = txnAmtcurrent;
                BigDecimal txnODPart = BigDecimal.ZERO;

                entity.setBanktxnAmount(totalTxnAmt);
                entity.setBankODtxnAmount(txnODPart);
                returnEntity =  bankTxnDetailsMainRepository.save(entity);
                System.out.println("entity to save is : "+entity.toString());

                CreditCardTopTxnDetailsMain topTxnEntity = new CreditCardTopTxnDetailsMain();
                topTxnEntity.setTxnId(returnEntity.getBanktxnId());
                topTxnEntity.setTxnDetails(returnEntity.getBanktxnDetails());
                topTxnEntity.setTxnAmount(totalTxnAmt.negate());
                topTxnEntity.setCcorbank("B");
                creditCardTopTxnDetailsMainRepository.save(topTxnEntity);

                System.out.println("floatAvlBal is "+floatAvlBal);
                Query queryAvlBalupdate = entityManager.createNamedQuery("MoneyServerPropertiesData.updateAvlBalBybankAccName");
                floatAvlBal = floatAvlBal.add(totalTxnAmt);
                queryAvlBalupdate.setParameter("id",bankAccName);
                queryAvlBalupdate.setParameter("newData",floatAvlBal.toString());
                queryAvlBalupdate.executeUpdate();
            }
        }
        Query queryAvlBalupdate = entityManager.createNamedQuery("MoneyServerPropertiesData.updateAvlBalBybankAccName");
        queryAvlBalupdate.setParameter("id",bankAccName);
        queryAvlBalupdate.setParameter("newData",floatAvlBal.toString());
        queryAvlBalupdate.executeUpdate();

        return returnEntity;
    }


    //DELETE txns
    @Transactional
    @DeleteMapping("/txn/txnDeleteNormal/{id}")
    public String txnDeleteNormal(@PathVariable String id){
        BankTxnDetailsMain responseFromID = bankTxnDetailsMainRepository.findByBanktxnId(id);
        String currDBlastusedYear = moneyServerPropertiesDataRepository.findById("banklastUsedyear").getValue();
        String currDBlastusedMonth = moneyServerPropertiesDataRepository.findById("banklastUsedmonth").getValue();
        if(responseFromID.getBankTxnBillingYearINTEGER()!=Integer.parseInt(currDBlastusedYear)){
            return "DELETE_ERROR";
        }else{
            if((responseFromID.getBankTxnBillingMonthINTEGER()-1)<months.indexOf(currDBlastusedMonth)){
                return "DELETE_ERROR";
            }
        }
        bankTxnDetailsMainRepository.deleteByBankTxnid(id);
        creditCardTopTxnDetailsMainRepository.deleteTopTxnByCCid(id);

        String bankAccName = "avlBal-HDFC";
        if(responseFromID.getBankAccName().startsWith("HDFC Bank")){
            bankAccName = "avlBal-HDFC";
        }
        if(responseFromID.getBankAccName().startsWith("Bank Of Maharashtra")){
            bankAccName = "avlBal-BOM";
        }
        String avlBaltemp = moneyServerPropertiesDataRepository.findById(bankAccName).getValue();
        String avlODBaltemp = moneyServerPropertiesDataRepository.findById(bankAccName+"-OD").getValue();
        BigDecimal floatAvlBaltemp = new BigDecimal(avlBaltemp);
        BigDecimal floatODAvlBaltemp = new BigDecimal(avlODBaltemp);
        BigDecimal txnAmtcurrent = responseFromID.getBanktxnAmount();
        BigDecimal txnODAmtcurrent = responseFromID.getBankODtxnAmount();

        floatAvlBaltemp = floatAvlBaltemp.subtract(txnAmtcurrent);
        floatODAvlBaltemp = floatODAvlBaltemp.subtract(txnODAmtcurrent);

        Query queryAvlBalupdate = entityManager.createNamedQuery("MoneyServerPropertiesData.updateAvlBalBybankAccName");
        queryAvlBalupdate.setParameter("id",bankAccName);
        queryAvlBalupdate.setParameter("newData",floatAvlBaltemp.toString());
        queryAvlBalupdate.executeUpdate();

        queryAvlBalupdate.setParameter("id",bankAccName+"-OD");
        queryAvlBalupdate.setParameter("newData",floatODAvlBaltemp.toString());
        queryAvlBalupdate.executeUpdate();

        return "DELETE_SUCCESS";
    }

}
