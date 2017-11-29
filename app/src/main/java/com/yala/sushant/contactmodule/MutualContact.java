package com.yala.sushant.contactmodule;

/**
 * Created by sushant on 11/28/17.
 */

public class MutualContact {

    private String name, phone, userId;

    public MutualContact() {
    }

    public MutualContact(String name, String phone, String userId) {
        this.name = name;
        this.phone = phone;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
