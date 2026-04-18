package com.parth.money.moneyServer.Entity;

public class SaveStateResponse {
    private boolean exist;
    private SaveState data;

    public SaveStateResponse(boolean exist, SaveState data) {
        this.exist = exist;
        this.data = data;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public SaveState getData() {
        return data;
    }

    public void setData(SaveState data) {
        this.data = data;
    }
}
