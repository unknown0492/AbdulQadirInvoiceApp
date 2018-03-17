package com.silentcoders.abdulqadir.classes;

/**
 * Created by Sohail on 06-01-2018.
 */

public class Carton {

    private String id;
    private String ai_id;
    private String carton_sr_no;
    private String carton_name;
    private String carton_price;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAIID() {
        return ai_id;
    }

    public void setAIID(String ai_id) {
        this.ai_id = ai_id;
    }

    public String getCartonName() {
        return carton_name;
    }

    public void setCartonName(String carton_name) {
        this.carton_name = carton_name;
    }

    public String getCartonPrice() {
        return carton_price;
    }

    public void setCartonPrice(String carton_price) {
        this.carton_price = carton_price;
    }

    public String getCartonSrNo() {
        return carton_sr_no;
    }

    public void setCartonSrNo(String carton_sr_no) {
        this.carton_sr_no = carton_sr_no;
    }

}
