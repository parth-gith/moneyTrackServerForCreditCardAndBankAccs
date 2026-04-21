package com.parth.money.moneyServer.Entity;

public class SaveStateSummaryResponse {
    private String saveStateId;
    private String lastUpdated;

    public SaveStateSummaryResponse(String saveStateId, String lastUpdated) {
        this.saveStateId = saveStateId;
        this.lastUpdated = lastUpdated;
    }

    public String getSaveStateId() {
        return saveStateId;
    }

    public void setSaveStateId(String saveStateId) {
        this.saveStateId = saveStateId;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
