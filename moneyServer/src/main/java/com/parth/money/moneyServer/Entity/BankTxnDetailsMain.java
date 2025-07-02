package com.parth.money.moneyServer.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name="BANK_TXNS_DETAILS_MAIN")
public class BankTxnDetailsMain implements Serializable {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name="bank_txn_id")
    private String banktxnId;

    @Column(name="bank_txn_billing_month")
    private String banktxnBillingMonth;

    @Column(name="bank_txn_billing_year")
    private String banktxnBillingYear;

    @Column(name="bank_txn_billing_date")
    private String banktxnBillingDate;

    @Column(name="bank_acc_name")
    private String bankAccName;

    @Column(name="bank_txn_details")
    private String banktxnDetails;

    @Column(name="bank_txn_amount")
    private BigDecimal banktxnAmount;

    @Column(name="bank_od_txn_amount")
    private BigDecimal bankODtxnAmount;

    @Column(name="bank_txn_seq_number_order")
    private BigDecimal bankTxnSeqNumOrder;

    //TODO : add total txn value

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

    public int getBankTxnBillingYearINTEGER(){
        return Integer.parseInt(banktxnBillingYear);
    }

    public BigDecimal getBankODtxnAmount() {
        return bankODtxnAmount;
    }

    public void setBankODtxnAmount(BigDecimal bankODtxnAmount) {
        this.bankODtxnAmount = bankODtxnAmount;
    }

    public int getBankTxnBillingMonthINTEGER(){
        List<String> months = Arrays.asList(
                "January", "February", "March", "April",
                "May", "June", "July", "August",
                "September", "October", "November", "December"
        );
        return 1 + months.indexOf(banktxnBillingMonth);
    }

    public int getBanktxnBillingDateINTEGER(){
        return Integer.parseInt(banktxnBillingDate.trim());
    }

    @Override
    public String toString() {
        return "BankTxnDetailsMain{" +
                "banktxnId='" + banktxnId + '\'' +
                ", banktxnBillingMonth='" + banktxnBillingMonth + '\'' +
                ", banktxnBillingYear='" + banktxnBillingYear + '\'' +
                ", banktxnBillingDate='" + banktxnBillingDate + '\'' +
                ", bankAccName='" + bankAccName + '\'' +
                ", banktxnDetails='" + banktxnDetails + '\'' +
                ", banktxnAmount=" + banktxnAmount +
                ", bankODtxnAmount=" + bankODtxnAmount +
                ", bankTxnSeqNumOrder=" + bankTxnSeqNumOrder +
                '}';
    }
}
