package com.limelight.utils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VMAuthReq {

    @SerializedName("pin")
    @Expose
    private String pin;

    public VMAuthReq(String pinStr) {
        this.pin = pinStr;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
