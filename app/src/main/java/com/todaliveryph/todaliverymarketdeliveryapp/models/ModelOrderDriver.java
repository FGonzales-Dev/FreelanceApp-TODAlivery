package com.todaliveryph.todaliverymarketdeliveryapp.models;

public class ModelOrderDriver {

    String customerAddress,customerName,customerPhone,itemsAmount,orderId,shopId,status,orderTime,customerId;

    public ModelOrderDriver() {

    }

    public ModelOrderDriver(String customerAddress, String customerName, String customerPhone, String itemsAmount, String orderId,String orderTime, String shopId,String status,String customerId) {
        this.customerAddress = customerAddress;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.itemsAmount = itemsAmount;
        this.orderId = orderId;
        this.orderTime = orderTime;
        this.shopId = shopId;
        this.status = status;
        this.customerId = customerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getItemsAmount() {
        return itemsAmount;
    }

    public void setItemsAmount(String itemsAmount) {
        this.itemsAmount = itemsAmount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



}
