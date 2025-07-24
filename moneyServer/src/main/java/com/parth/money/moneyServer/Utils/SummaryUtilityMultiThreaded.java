package com.parth.money.moneyServer.Utils;

import com.parth.money.moneyServer.Entity.CreditCardTxnDetailsMain;
import com.parth.money.moneyServer.Entity.Pair;
import com.parth.money.moneyServer.Entity.SummaryModel;
import com.parth.money.moneyServer.Repository.CreditCardTxnDetailsMainRepository;
import com.parth.money.moneyServer.Repository.MoneyServerPropertiesDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class SummaryUtilityMultiThreaded {
    @Autowired
    CreditCardTxnDetailsMainRepository creditCardTxnDetailsMainRepository;

    @Autowired
    MoneyServerPropertiesDataRepository moneyServerPropertiesDataRepository;

    List<String> months = Arrays.asList(
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
    );

    public List<String> getMonths() {
        return months;
    }

    public List<SummaryModel> getSummarylist() throws Exception{
        int startMonthinDB = months.indexOf("September");
        String startYearinDB = "2023";

        String stopperEMIMonthinDB = moneyServerPropertiesDataRepository.findById("emilastUsedmonth").getValue();
        String stopperEMIYearinDB = moneyServerPropertiesDataRepository.findById("emilastUsedyear").getValue();

        Map<String,List<String>> yearsToMonthsMap = new LinkedHashMap<>();
        while(true){
            if(stopperEMIMonthinDB.equalsIgnoreCase(months.get(startMonthinDB%12))
                    && stopperEMIYearinDB.equalsIgnoreCase(startYearinDB)){
                yearsToMonthsMap.computeIfAbsent(startYearinDB,x->new ArrayList<>())
                                .add(months.get(startMonthinDB%12));
                break;
            }
            yearsToMonthsMap.computeIfAbsent(startYearinDB,x->new ArrayList<>())
                    .add(months.get(startMonthinDB%12));
            if(startMonthinDB%12==11){
                startYearinDB = String.valueOf(Integer.parseInt(startYearinDB)+1);
            }
            startMonthinDB+=1;
        }

        // Map < year , Map < month , SummaryModel > >
        Map<String,Map<String,SummaryModel>> yearsToMonthsAndSummaryModelMap = new TreeMap<>(Comparator.reverseOrder());

        for(Map.Entry<String,List<String>> item : yearsToMonthsMap.entrySet()){
            String currYear = item.getKey();
            List<String> currYearMonths = item.getValue().stream()
                    .sorted(Comparator.comparingInt(months::indexOf).reversed())
                    .collect(Collectors.toList());


            ExecutorService executor = Executors.newFixedThreadPool(Math.min(currYearMonths.size(),Runtime.getRuntime().availableProcessors()));

            Map<String,SummaryModel> monthToSummaryModelMap = new LinkedHashMap<>();

            try{
                List<Future<Pair<String,SummaryModel>>> futuresAsyncList =
                            currYearMonths.stream()
                                    .map(month -> executor.submit(() -> new Pair<>(month,getSummaryFromTxns(month,currYear))))
                                    .collect(Collectors.toList());

                for(Future<Pair<String,SummaryModel>> futureItem : futuresAsyncList){
                    Pair<String,SummaryModel> monthSummarypair = futureItem.get();
                    monthToSummaryModelMap.put(monthSummarypair.getFirst(),monthSummarypair.getSecond());
                }

                yearsToMonthsAndSummaryModelMap.put(currYear,monthToSummaryModelMap);

            }catch (Exception e){
                throw e;
            }finally {
                executor.shutdown();
            }
        }


        List<SummaryModel> returnListData = yearsToMonthsAndSummaryModelMap.entrySet().stream()
                                                .flatMap(outerMap -> outerMap.getValue().entrySet().stream()
                                                        .map(innerMap -> {
                                                                return innerMap.getValue();
                                                            }
                                                        )
                                                ).collect(Collectors.toList());

        SummaryModel completeModel = getSummaryFromTxns("X","X");
        returnListData.add(0,completeModel);

        return returnListData;
    }

    public SummaryModel getSummaryFromTxns(String month, String year){
        SummaryModel returnModel = new SummaryModel();

        ConcurrentHashMap<String, AtomicReference<BigDecimal>> totalsTxnHM = new ConcurrentHashMap<>();
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

        txnList.parallelStream().forEach(txn -> {
            String cardType = resolveCardKey(txn.getTxnCCused());
            BigDecimal txnAmount = txn.getTxnAmount();
            totalsTxnHM.computeIfAbsent(cardType,x->new AtomicReference<>(BigDecimal.ZERO))
                    .getAndUpdate(existingAmt -> existingAmt.add(txnAmount));
        });


        returnModel.setStandard_Chartered_Ultimate_Total(getAmount(totalsTxnHM,"STANDCHARTULTIMATE"));
        returnModel.setHDFC_Regalia_Gold_Total(getAmount(totalsTxnHM,"REGALIAGOLD"));
        returnModel.setAmazonPay_icici_Total(getAmount(totalsTxnHM,"AMZNPAYICICI"));
        returnModel.setAmazon_PayLater_Total(getAmount(totalsTxnHM,"AMZNPL"));
        returnModel.setFlipkart_PayLater_Total(getAmount(totalsTxnHM,"FLPKRTPL"));
        returnModel.setHSBC_Visa_Platinum(getAmount(totalsTxnHM,"HSBCVISAPLAT"));
        returnModel.setOneCard_Metal(getAmount(totalsTxnHM,"ONECARDMETAL"));
        returnModel.setSwiggy_HDFC(getAmount(totalsTxnHM,"SWIGGYHDFC"));
        returnModel.setIrctc_SBI(getAmount(totalsTxnHM,"IRCTCSBI"));
        returnModel.setYesBank_ElitePlus(getAmount(totalsTxnHM,"YESELITEPLUS"));
        returnModel.setRBL_WorldSafari(getAmount(totalsTxnHM,"RBLWORLDSAFARI"));
        returnModel.setMarriott_HDFC(getAmount(totalsTxnHM,"MARRIOTTBONVHDFC"));
        returnModel.setRupay_HDFC(getAmount(totalsTxnHM,"RUPAYHDFC"));
        returnModel.setYesBank_Reserv(getAmount(totalsTxnHM,"YESRESERV"));
        returnModel.setMmt_ICICI(getAmount(totalsTxnHM,"MMTICICI"));

        BigDecimal totalAmount = totalsTxnHM.values().stream()
                               .map(AtomicReference::get)
                               .reduce(BigDecimal.ZERO,BigDecimal::add);

        returnModel.setAmount_Total(totalAmount);

        return returnModel;
    }

    private BigDecimal getAmount(Map<String, AtomicReference<BigDecimal>> totalsTxnHM, String cardType) {
        return totalsTxnHM.getOrDefault(cardType, new AtomicReference<>(BigDecimal.ZERO)).get();
    }

    private String resolveCardKey(String name) {
        if (name.startsWith("Standard Chartered Ultimate")) return "STANDCHARTULTIMATE";
        if (name.startsWith("HDFC Regalia Gold")) return "REGALIAGOLD";
        if (name.startsWith("AmazonPay ICICI")) return "AMZNPAYICICI";
        if (name.startsWith("AmazonPayLater")) return "AMZNPL";
        if (name.startsWith("FlipkartPayLater")) return "FLPKRTPL";
        if (name.startsWith("HSBC PLATINUM")) return "HSBCVISAPLAT";
        if (name.startsWith("ONECard METAL")) return "ONECARDMETAL";
        if (name.startsWith("Swiggy HDFC")) return "SWIGGYHDFC";
        if (name.startsWith("IRCTC SBI")) return "IRCTCSBI";
        if (name.startsWith("YesBank Elite+")) return "YESELITEPLUS";
        if (name.startsWith("RBL WorldSafari")) return "RBLWORLDSAFARI";
        if (name.startsWith("Marriott Bonvoy HDFC")) return "MARRIOTTBONVHDFC";
        if (name.startsWith("Rupay HDFC")) return "RUPAYHDFC";
        if (name.startsWith("YesBank Reserv")) return "YESRESERV";
        if (name.startsWith("MakeMyTrip ICICI")) return "MMTICICI";
        return "UNKNOWN";
    }
}
