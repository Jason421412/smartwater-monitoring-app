package com.smartwater.monitoring.network.dto;

public class LoginReq {
    public String email;
    public String password;

    public LoginReq(String email, String password) {
        this.email = email;
        this.password = password;
    }
}