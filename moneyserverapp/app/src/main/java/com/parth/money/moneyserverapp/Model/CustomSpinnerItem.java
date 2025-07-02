package com.parth.money.moneyserverapp.Model;

public class CustomSpinnerItem {
    public String itemName;
    public boolean isEnabled;

    public CustomSpinnerItem(String itemName, boolean isEnabled) {
        this.itemName = itemName;
        this.isEnabled = isEnabled;
    }

    @Override
    public String toString() {
        return getItemName();
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
