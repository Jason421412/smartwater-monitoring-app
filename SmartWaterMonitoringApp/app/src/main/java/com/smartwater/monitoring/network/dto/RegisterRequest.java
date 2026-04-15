package com.smartwater.monitoring.network.dto;

public class RegisterRequest {
    public String firstName;
    public String lastName;
    public String email;
    public String password;
    public String contact;

    public RegisterRequest(String firstName, String lastName, String email, String password, String contact) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.contact = contact;
    }
}
