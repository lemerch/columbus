package com.github.lemerch.columbus.fieldmapper.beans.linearfirst;

import java.math.BigDecimal;

public class FirstDTO {
    private int id;
    private BigDecimal astra;
    private String name;

    public FirstDTO() {}

    public FirstDTO(int id, BigDecimal astra, String name) {
        this.id = id;
        this.astra = astra;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigDecimal getAstra() {
        return astra;
    }

    public void setAstra(BigDecimal astra) {
        this.astra = astra;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
