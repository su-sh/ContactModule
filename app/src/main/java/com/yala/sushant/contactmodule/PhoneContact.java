package com.yala.sushant.contactmodule;

import java.util.ArrayList;

/**
 * Created by sushant on 11/27/17.
 */

public class PhoneContact {
    private String name,dob;
    private ArrayList<String> phone;

    public PhoneContact(){}


    public PhoneContact(String name, String dob, ArrayList<String> phone) {
        this.name = name;
        this.dob = dob;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public ArrayList<String> getPhone() {
        return phone;
    }

    public void setPhone(ArrayList<String> phone) {
        this.phone = phone;
    }
}
