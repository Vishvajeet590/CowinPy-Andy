package com.vishvajeet590.python.utils;

public class dataModel {
    String name;
    String benificiary;
    String search;
    String number;
    String vacType;
    String pincodes;
    String state;
    String district;
    String availibility;
    String refresh;
    String startDay;
    String cost;
    String auto_book;

    public dataModel(String name, String benificiary, String search, String number, String vacType, String pincodes, String state, String district, String availibility, String refresh, String startDay, String cost, String auto_book) {
        this.name = name;
        this.benificiary = benificiary;
        this.search = search;
        this.number = number;
        this.vacType = vacType;
        this.pincodes = pincodes;
        this.state = state;
        this.district = district;
        this.availibility = availibility;
        this.refresh = refresh;
        this.startDay = startDay;
        this.cost = cost;
        this.auto_book = auto_book;
    }
}
