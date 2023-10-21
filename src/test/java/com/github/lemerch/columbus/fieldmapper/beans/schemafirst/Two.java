package com.github.lemerch.columbus.fieldmapper.beans.schemafirst;

public class Two {
    private String sfirst;
    private String ssecond;

    public Two() {}

    public Two(String sfirst, String ssecond) {
        this.sfirst = sfirst;
        this.ssecond = ssecond;
    }

    public String getSfirst() {
        return sfirst;
    }

    public void setSfirst(String sfirst) {
        this.sfirst = sfirst;
    }

    public String getSsecond() {
        return ssecond;
    }

    public void setSsecond(String ssecond) {
        this.ssecond = ssecond;
    }
}
