package com.carmenguidetgomez.glucemyphoto.ui.login;

public class Users {
    public String firstName;
    public String lastName;
    public String email;
    public String birthDate;
    public String gender;
    public String diabetes;

    public Users() {
    }

    public Users(String firstName, String lastName, String email, String birthDate, String gender, String diabetes) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.gender = gender;
        this.diabetes = diabetes;
    }
}
