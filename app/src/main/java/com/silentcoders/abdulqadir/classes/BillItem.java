package com.silentcoders.abdulqadir.classes;

/**
 * Created by Sohail on 17-01-2018.
 */

public class BillItem implements Cloneable{

    String billItemID, itemPrice, itemDiscount, itemFinalPrice, itemSrNo, itemQtyType, itemQuantity, itemPriceTotal, itemDiscountTotal, itemFinalPriceTotal;
    private String itemName;
    private String itemCode;
    private String cartonTempID;

    public String getBillItemID() {
        return billItemID;
    }

    public void setBillItemID(String billItemID) {
        this.billItemID = billItemID;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemDiscount() {
        return itemDiscount;
    }

    public void setItemDiscount(String itemDiscount) {
        this.itemDiscount = itemDiscount;
    }

    public String getItemFinalPrice() {
        return itemFinalPrice;
    }

    public void setItemFinalPrice(String itemFinalPrice) {
        this.itemFinalPrice = itemFinalPrice;
    }

    public String getItemSrNo() {
        return itemSrNo;
    }

    public void setItemSrNo(String itemSrNo) {
        this.itemSrNo = itemSrNo;
    }

    public String getItemQtyType() {
        return itemQtyType;
    }

    public void setItemQtyType(String itemQtyType) {
        this.itemQtyType = itemQtyType;
    }

    public String getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(String itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public String getItemPriceTotal() {
        return itemPriceTotal;
    }

    public void setItemPriceTotal(String itemPriceTotal) {
        this.itemPriceTotal = itemPriceTotal;
    }

    public String getItemDiscountTotal() {
        return itemDiscountTotal;
    }

    public void setItemDiscountTotal(String itemDiscountTotal) {
        this.itemDiscountTotal = itemDiscountTotal;
    }

    public String getItemFinalPriceTotal() {
        return itemFinalPriceTotal;
    }

    public void setItemFinalPriceTotal(String itemFinalPriceTotal) {
        this.itemFinalPriceTotal = itemFinalPriceTotal;
    }


    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getCartonTempID() {
        return cartonTempID;
    }

    public void setCartonTempID(String cartonTempID) {
        this.cartonTempID = cartonTempID;
    }

    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Object cloneMe() throws CloneNotSupportedException {
        return clone();
    }

}
