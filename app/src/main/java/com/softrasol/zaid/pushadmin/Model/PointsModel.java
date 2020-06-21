package com.softrasol.zaid.pushadmin.Model;

public class PointsModel {
    private String title, sub_title;

    public PointsModel() {
    }

    public PointsModel(String title, String sub_title) {
        this.title = title;
        this.sub_title = sub_title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSub_title() {
        return sub_title;
    }

    public void setSub_title(String sub_title) {
        this.sub_title = sub_title;
    }
}



