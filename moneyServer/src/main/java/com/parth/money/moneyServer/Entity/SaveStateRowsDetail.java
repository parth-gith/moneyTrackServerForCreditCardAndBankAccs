package com.parth.money.moneyServer.Entity;

public class SaveStateRowsDetail {
    private String saveStateRowId;
    private int txnAmount;
    private String txnDetail;
    private String bankTxnDay;
    private String bankTxnMonth;
    private String bankTxnYear;
    private int bankTxnSeqNo;

    public String getSaveStateRowId() {
        return saveStateRowId;
    }

    public void setSaveStateRowId(String saveStateRowId) {
        this.saveStateRowId = saveStateRowId;
    }

    public int getTxnAmount() {
        return txnAmount;
    }

    public void setTxnAmount(int txnAmount) {
        this.txnAmount = txnAmount;
    }

    public String getTxnDetail() {
        return txnDetail;
    }

    public void setTxnDetail(String txnDetail) {
        this.txnDetail = txnDetail;
    }

    public String getBankTxnMonth() {
        return bankTxnMonth;
    }

    public void setBankTxnMonth(String bankTxnMonth) {
        this.bankTxnMonth = bankTxnMonth;
    }

    public String getBankTxnYear() {
        return bankTxnYear;
    }

    public void setBankTxnYear(String bankTxnYear) {
        this.bankTxnYear = bankTxnYear;
    }

    public int getBankTxnSeqNo() {
        return bankTxnSeqNo;
    }

    public void setBankTxnSeqNo(int bankTxnSeqNo) {
        this.bankTxnSeqNo = bankTxnSeqNo;
    }

    public String getBankTxnDay() { return bankTxnDay; }

    public void setBankTxnDay(String bankTxnDay) {this.bankTxnDay = bankTxnDay;}
}
