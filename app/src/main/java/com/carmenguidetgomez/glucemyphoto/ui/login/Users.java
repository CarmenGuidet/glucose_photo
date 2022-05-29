package com.carmenguidetgomez.glucemyphoto.ui.login;

public class Users {
    public String firstName;
    public String lastName;
    public String email;
    public String birthDate;
    public String gender;

    public Users() {
    }

    public Users(String firstName, String lastName, String email, String birthDate, String gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.gender = gender;
    }
}
