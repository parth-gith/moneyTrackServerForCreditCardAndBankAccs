package com.parth.money.moneyserverapp.Model;

import java.math.BigDecimal;

public class bankResponseEntity {

    private String banktxnId;
    private String banktxnBillingMonth;
    private String banktxnBillingYear;
    private String banktxnBillingDate;
    private String bankAccName;
    private String banktxnDetails;
    private BigDecimal banktxnAmount;
    private BigDecimal bankODtxnAmount;
    private BigDecimal bankTxnSeqNumOrder;


    public String getBanktxnId() {
        return banktxnId;
    }

    public void setBanktxnId(String banktxnId) {
        this.banktxnId = banktxnId;
    }

    public String getBanktxnBillingMonth() {
        return banktxnBillingMonth;
    }

    public void setBanktxnBillingMonth(String banktxnBillingMonth) {
        this.banktxnBillingMonth = banktxnBillingMonth;
    }

    public String getBanktxnBillingYear() {
        return banktxnBillingYear;
    }

    public void setBanktxnBillingYear(String banktxnBillingYear) {
        this.banktxnBillingYear = banktxnBillingYear;
    }

    public String getBankAccName() {
        return bankAccName;
    }

    public void setBankAccName(String bankAccName) {
        this.bankAccName = bankAccName;
    }

    public String getBanktxnDetails() {
        return banktxnDetails;
    }

    public void setBanktxnDetails(String banktxnDetails) {
        this.banktxnDetails = banktxnDetails;
    }

    public BigDecimal getBanktxnAmount() {
        return banktxnAmount;
    }

    public void setBanktxnAmount(BigDecimal banktxnAmount) {
        this.banktxnAmount = banktxnAmount;
    }

    public BigDecimal getBankTxnSeqNumOrder() {
        return bankTxnSeqNumOrder;
    }

    public void setBankTxnSeqNumOrder(BigDecimal bankTxnSeqNumOrder) {
        this.bankTxnSeqNumOrder = bankTxnSeqNumOrder;
    }

    public String getBanktxnBillingDate() {
        return banktxnBillingDate;
    }

    public void setBanktxnBillingDate(String banktxnBillingDate) {
        this.banktxnBillingDate = banktxnBillingDate;
    }

    public BigDecimal getBankODtxnAmount() {
        return bankODtxnAmount;
    }

    public void setBankODtxnAmount(BigDecimal bankODtxnAmount) {
        this.bankODtxnAmount = bankODtxnAmount;
    }
}
