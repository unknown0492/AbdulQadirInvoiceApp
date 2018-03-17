package com.silentcoders.abdulqadir.classes;

/**
 * Created by Sohail on 08-01-2018.
 */

public class Item {

    private String id;
    private String itemName;
    private String itemCode;
    private String price;
    private String discount;
    private String finalPrice;
    private String quantityType;
    private String shopId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(String finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getQuantityType() {
        return quantityType;
    }

    public void setQuantityType(String quantityType) {
        this.quantityType = quantityType;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String[] getAllItemNames( Item items[] ){
        if( items.length == 0 ){
            String names[] = new String[ 1 ];
            names[ 0 ] = "";
            return names;
        }
        String names[] = new String[ items.length ];
        for( int i = 0 ; i < items.length ; i++ ){
            names[ i ] = items[ i ].getItemName();
        }
        return names;
    }

    public String[] getAllItemCodes( Item items[] ){
        String codes[] = new String[ items.length ];
        for( int i = 0 ; i < items.length ; i++ ){
            codes[ i ] = items[ i ].getItemCode();
        }
        return codes;
    }
}
