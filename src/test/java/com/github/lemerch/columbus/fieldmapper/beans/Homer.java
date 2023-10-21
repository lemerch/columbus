package com.github.lemerch.columbus.fieldmapper.beans;

public class Homer {
    private Integer id;
    private String duff;
    private String burger;
    private String spiderpig;

    public Homer() {}
    public Homer(Integer id, String duff, String burger, String spiderpig) {
        this.id = id;
        this.duff = duff;
        this.burger = burger;
        this.spiderpig = spiderpig;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDuff() {
        return duff;
    }

    public void setDuff(String duff) {
        this.duff = duff;
    }

    public String getBurger() {
        return burger;
    }

    public void setBurger(String burger) {
        this.burger = burger;
    }

    public String getSpiderpig() {
        return spiderpig;
    }

    public void setSpiderpig(String spiderpig) {
        this.spiderpig = spiderpig;
    }
}
