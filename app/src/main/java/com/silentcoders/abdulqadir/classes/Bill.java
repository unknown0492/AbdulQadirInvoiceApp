package com.silentcoders.abdulqadir.classes;

/**
 * Created by Sohail on 30-01-2018.
 */

public class Bill {

    String id;
    String timestamp;
    String customerName;
    String place;
    String remarks;
    String salesman;
    String invoiceBy;
    String packedBy;
    String cartonsMeta;
    String itemMeta;
    String billStatus;
    String billExtras;
    String userID;

    public String getTotalCartons() {
        return totalCartons;
    }

    public void setTotalCartons(String totalCartons) {
        this.totalCartons = totalCartons;
    }

    String totalCartons;
    String date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getSalesman() {
        return salesman;
    }

    public void setSalesman(String salesman) {
        this.salesman = salesman;
    }

    public String getInvoiceBy() {
        return invoiceBy;
    }

    public void setInvoiceBy(String invoiceBy) {
        this.invoiceBy = invoiceBy;
    }

    public String getPackedBy() {
        return packedBy;
    }

    public void setPackedBy(String packedBy) {
        this.packedBy = packedBy;
    }

    public String getCartonsMeta() {
        return cartonsMeta;
    }

    public void setCartonsMeta(String cartonsMeta) {
        this.cartonsMeta = cartonsMeta;
    }

    public String getItemMeta() {
        return itemMeta;
    }

    public void setItemMeta(String itemMeta) {
        this.itemMeta = itemMeta;
    }

    public String getBillStatus() {
        return billStatus;
    }

    public void setBillStatus(String billStatus) {
        this.billStatus = billStatus;
    }

    public String getBillExtras() {
        return billExtras;
    }

    public void setBillExtras(String billExtras) {
        this.billExtras = billExtras;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
