package com.smartwater.monitoring.network.dto;

public class LoginResp {
    public String token; // common
    public String jwt;   // sometimes backend uses jwt

    public String getAnyToken() {
        if (token != null && !token.isEmpty()) return token;
        if (jwt != null && !jwt.isEmpty()) return jwt;
        return null;
    }
}
