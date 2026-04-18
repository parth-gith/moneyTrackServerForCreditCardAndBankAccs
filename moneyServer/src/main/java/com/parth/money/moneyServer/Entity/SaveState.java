package com.parth.money.moneyServer.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("moneyserver_push_save_state")
public class SaveState {
    @Id
    private String saveStateId;
    private String saveStateType;
    private String status;
    private String creditCardBillMonth;
    private String creditCardBillYear;
    private String lastUpdated;
    private List<SaveStateRowsDetail> saveStateRows;

    public String getSaveStateId() {
        return saveStateId;
    }

    public void setSaveStateId(String saveStateId) {
        this.saveStateId = saveStateId;
    }

    public String getSaveStateType() {
        return saveStateType;
    }

    public void setSaveStateType(String saveStateType) {
        this.saveStateType = saveStateType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreditCardBillMonth() {
        return creditCardBillMonth;
    }

    public void setCreditCardBillMonth(String creditCardBillMonth) {
        this.creditCardBillMonth = creditCardBillMonth;
    }

    public String getCreditCardBillYear() {
        return creditCardBillYear;
    }

    public void setCreditCardBillYear(String creditCardBillYear) {
        this.creditCardBillYear = creditCardBillYear;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<SaveStateRowsDetail> getSaveStateRows() {
        return saveStateRows;
    }

    public void setSaveStateRows(List<SaveStateRowsDetail> saveStateRows) {
        this.saveStateRows = saveStateRows;
    }
}
