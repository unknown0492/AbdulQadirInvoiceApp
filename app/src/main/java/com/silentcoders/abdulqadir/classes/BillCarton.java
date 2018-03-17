package com.silentcoders.abdulqadir.classes;

/**
 * Created by Sohail on 11-01-2018.
 */

public class BillCarton {

    private String cartonName;
    private String cartonPrice;
    private String isDespatched;
    private String cartonTotal;
    private String cartonSerial;

    public String getCartonName() {
        return cartonName;
    }

    public void setCartonName(String cartonName) {
        this.cartonName = cartonName;
    }

    public String getCartonPrice() {
        return cartonPrice;
    }

    public void setCartonPrice(String cartonPrice) {
        this.cartonPrice = cartonPrice;
    }

    public String getCartonTotal() {
        return cartonTotal;
    }

    public void setCartonTotal(String cartonTotal) {
        this.cartonTotal = cartonTotal;
    }

    public String getCartonSrNo() {
        return cartonSerial;
    }

    public void setCartonSrNo(String cartonSerial) {
        this.cartonSerial = cartonSerial;
    }

    public String getIsDespatched() {
        return isDespatched;
    }

    public void setIsDespatched(String isDespatched) {
        this.isDespatched = isDespatched;
    }
}
