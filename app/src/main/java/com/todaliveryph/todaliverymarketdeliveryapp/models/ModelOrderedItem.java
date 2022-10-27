package com.todaliveryph.todaliverymarketdeliveryapp.models;

public class ModelOrderedItem {

    private String pID, name, cost, price, quantity;

    public ModelOrderedItem() {

    }

    public ModelOrderedItem(String pID, String name, String cost, String price, String quantity) {
        this.pID = pID;
        this.name = name;
        this.cost = cost;
        this.price = price;
        this.quantity = quantity;
    }

    public String getpID() {
        return pID;
    }

    public void pID(String pID) {
        this.pID = pID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
