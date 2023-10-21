package com.github.lemerch.columbus.fieldmapper;

import com.github.lemerch.columbus.FieldMapper;
import com.github.lemerch.columbus.fieldmapper.beans.Bart;
import com.github.lemerch.columbus.fieldmapper.beans.Homer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FieldMapperMergeTests {
    private FieldMapper fieldMapper = new FieldMapper();

    @Test
    public void commonMergeTest() {
        Homer homer = new Homer(25, "Beeeer", "Crusty", "spider piIIG!");

        Bart bart = fieldMapper.withSchema("duff", "shirts").map(homer, Bart.class);

        assertEquals(bart.getId(), homer.getId());
        assertEquals(bart.getBurger(), homer.getBurger());
        assertEquals(bart.getSpiderpig(), homer.getSpiderpig());

        assertEquals(bart.getShirts(), homer.getDuff());
    }
}
