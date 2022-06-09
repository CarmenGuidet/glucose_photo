package com.carmenguidetgomez.glucemyphoto;

public class Glucose {
    private double glucose;
    private double media;
    private String url_photo;
    private String date;

    public Glucose(){

    }

    public Glucose(double glucose, double media, String url_photo, String date){

        this.glucose = glucose;
        this.media = media;
        this.url_photo = url_photo;
        this.date = date;
    }



    public double getGlucose() {
        return glucose;
    }

    public double getMedia() {
        return media;
    }

    public String getUrl_photo() {
        return url_photo;
    }

    public String getDate() { return date; }
}