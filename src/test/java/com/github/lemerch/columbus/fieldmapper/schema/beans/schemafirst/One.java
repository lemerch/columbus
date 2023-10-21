package com.github.lemerch.columbus.fieldmapper.schema.beans.schemafirst;

public class One {
    private Long id;
    private String first;
    private String second;

    public One() {}
    public One(Long id, String first, String second) {
        this.id = id;
        this.first = first;
        this.second = second;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }
}
