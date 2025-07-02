package com.parth.money.moneyServer.Utils;

import com.parth.money.moneyServer.Entity.CreditCardTxnDetailsMain;
import com.parth.money.moneyServer.Entity.MoneyServerPropertiesData;
import com.parth.money.moneyServer.Entity.SummaryModel;
import com.parth.money.moneyServer.Repository.CreditCardTxnDetailsMainRepository;
import com.parth.money.moneyServer.Repository.MoneyServerPropertiesDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SummaryUtility {

    @Autowired
    CreditCardTxnDetailsMainRepository creditCardTxnDetailsMainRepository;

    @Autowired
    MoneyServerPropertiesDataRepository moneyServerPropertiesDataRepository;

    List<String> months = Arrays.asList(
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
    );

    public List<SummaryModel> getSummarylist(){
        int startMonthinDB = months.indexOf("September");
        String startYearinDB = "2023";
        List<SummaryModel> returnListData = new ArrayList<>();

        String stopperEMIMonthinDB = moneyServerPropertiesDataRepository.findById("emilastUsedmonth").getValue();
        String stopperEMIYearinDB = moneyServerPropertiesDataRepository.findById("emilastUsedyear").getValue();
        while(true){
            if(stopperEMIMonthinDB.equalsIgnoreCase(months.get(startMonthinDB%12))
                            && stopperEMIYearinDB.equalsIgnoreCase(startYearinDB)){
                returnListData.add(getSummaryFromTxns(months.get(startMonthinDB%12),startYearinDB));
                break;
            }
            returnListData.add(getSummaryFromTxns(months.get(startMonthinDB%12),startYearinDB));
            if(startMonthinDB%12==11){
                startYearinDB = String.valueOf(Integer.parseInt(startYearinDB)+1);
            }
            startMonthinDB+=1;
        }
        returnListData.add(getSummaryFromTxns("X","X"));

        return returnListData;
    }

    public SummaryModel getSummaryFromTxns(String month,String year){
        SummaryModel returnModel = new SummaryModel();
        BigDecimal scu = new BigDecimal(0);
        BigDecimal hdfcrg = new BigDecimal(0);
        BigDecimal amznicici = new BigDecimal(0);
        BigDecimal hsbcvisaplat = new BigDecimal(0);
        BigDecimal onecardmetal = new BigDecimal(0);
        BigDecimal swiggyhdfc = new BigDecimal(0);
        BigDecimal irctcSbi = new BigDecimal(0);
        BigDecimal yesElitePlus = new BigDecimal(0);
        BigDecimal rblWorldSafari = new BigDecimal(0);
        BigDecimal marriotthdfc = new BigDecimal(0);
        BigDecimal rupayhdfc = new BigDecimal(0);
        BigDecimal yesReserv = new BigDecimal(0);
        BigDecimal mmtIcici = new BigDecimal(0);
        BigDecimal amznpl = new BigDecimal(0);
        BigDecimal flpkrtpl = new BigDecimal(0);
        BigDecimal totalAmt = new BigDecimal(0);

        List<CreditCardTxnDetailsMain> txnList;

        if("X".equalsIgnoreCase(month) && "X".equalsIgnoreCase(year)){
            txnList = creditCardTxnDetailsMainRepository.findAll();
            returnModel.setYear("COMPLETE");
            returnModel.setMonth("COMPLETE");
        }else{
            txnList = creditCardTxnDetailsMainRepository.findByTxnBillingMonthAndTxnBillingYear(month,year);
            returnModel.setYear(year);
            returnModel.setMonth(month);
        }
        for(CreditCardTxnDetailsMain txn:txnList){
            if(txn.getTxnCCused().startsWith("Standard Chartered Ultimate")){
                scu = scu.add(txn.getTxnAmount());
            }
            if(txn.getTxnCCused().startsWith("HDFC Regalia Gold")){
                hdfcrg = hdfcrg.add(txn.getTxnAmount());
            }
            if(txn.getTxnCCused().startsWith("AmazonPay ICICI")){
                amznicici = amznicici.add(txn.getTxnAmount());
            }
            if(txn.getTxnCCused().startsWith("AmazonPayLater")){
                amznpl = amznpl.add(txn.getTxnAmount());
            }
            if(txn.getTxnCCused().startsWith("FlipkartPayLater")){
                flpkrtpl = flpkrtpl.add(txn.getTxnAmount());
            }
            if(txn.getTxnCCused().startsWith("HSBC PLATINUM")){
                hsbcvisaplat = hsbcvisaplat.add(txn.getTxnAmount());
            }
            if(txn.getTxnCCused().startsWith("ONECard METAL")){
                onecardmetal = onecardmetal.add(txn.getTxnAmount());
            }
            if(txn.getTxnCCused().startsWith("Swiggy HDFC")){
                swiggyhdfc = swiggyhdfc.add(txn.getTxnAmount());
            }
            if(txn.getTxnCCused().startsWith("IRCTC SBI")){
                irctcSbi = irctcSbi.add(txn.getTxnAmount());
            }
            if(txn.getTxnCCused().startsWith("YesBank Elite+")){
                yesElitePlus = yesElitePlus.add(txn.getTxnAmount());
            }
            if(txn.getTxnCCused().startsWith("RBL WorldSafari")){
                rblWorldSafari = rblWorldSafari.add(txn.getTxnAmount());
            }
            if(txn.getTxnCCused().startsWith("Marriott HDFC")){
                marriotthdfc = marriotthdfc.add(txn.getTxnAmount());
            }
            if(txn.getTxnCCused().startsWith("Rupay HDFC")){
                rupayhdfc = rupayhdfc.add(txn.getTxnAmount());
            }
            if(txn.getTxnCCused().startsWith("YesBank Reserv")){
                yesReserv = yesReserv.add(txn.getTxnAmount());
            }
            if(txn.getTxnCCused().startsWith("MakeMyTrip ICICI")){
                mmtIcici = mmtIcici.add(txn.getTxnAmount());
            }
        }

        totalAmt = totalAmt.add(scu);
        totalAmt = totalAmt.add(hdfcrg);
        totalAmt = totalAmt.add(amznicici);
        totalAmt = totalAmt.add(amznpl);
        totalAmt = totalAmt.add(flpkrtpl);
        totalAmt = totalAmt.add(hsbcvisaplat);
        totalAmt = totalAmt.add(onecardmetal);
        totalAmt = totalAmt.add(swiggyhdfc);
        totalAmt = totalAmt.add(irctcSbi);
        totalAmt = totalAmt.add(yesElitePlus);
        totalAmt = totalAmt.add(rblWorldSafari);
        totalAmt = totalAmt.add(marriotthdfc);
        totalAmt = totalAmt.add(rupayhdfc);
        totalAmt = totalAmt.add(yesReserv);
        totalAmt = totalAmt.add(mmtIcici);

        returnModel.setStandard_Chartered_Ultimate_Total(scu);
        returnModel.setHDFC_Regalia_Gold_Total(hdfcrg);
        returnModel.setAmazonPay_icici_Total(amznicici);
        returnModel.setAmazon_PayLater_Total(amznpl);
        returnModel.setFlipkart_PayLater_Total(flpkrtpl);
        returnModel.setHSBC_Visa_Platinum(hsbcvisaplat);
        returnModel.setOneCard_Metal(onecardmetal);
        returnModel.setSwiggy_HDFC(swiggyhdfc);
        returnModel.setIrctc_SBI(irctcSbi);
        returnModel.setYesBank_ElitePlus(yesElitePlus);
        returnModel.setRBL_WorldSafari(rblWorldSafari);
        returnModel.setMarriott_HDFC(marriotthdfc);
        returnModel.setRupay_HDFC(rupayhdfc);
        returnModel.setYesBank_Reserv(yesReserv);
        returnModel.setMmt_ICICI(mmtIcici);
        returnModel.setAmount_Total(totalAmt);

        return returnModel;
    }
}
