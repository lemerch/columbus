package com.github.lemerch.columbus.fieldmapper.schema;

import com.github.lemerch.columbus.ColumbusException;
import com.github.lemerch.columbus.FieldMapper;
import com.github.lemerch.columbus.fieldmapper.schema.beans.linearfirst.FirstDTO;
import com.github.lemerch.columbus.fieldmapper.schema.beans.linearfirst.SecondDTO;
import com.github.lemerch.columbus.fieldmapper.schema.beans.linearsecond.EasterBunny;
import com.github.lemerch.columbus.fieldmapper.schema.beans.linearsecond.Scott;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class FieldMapperLinearTests {

    private FieldMapper fieldMapper = new FieldMapper();

    @Test
    public void simpleLinearMapTest() {
        FirstDTO firstDTO = new FirstDTO(1, new BigDecimal(23.2), "hey");
        SecondDTO secondDTO = fieldMapper.map(firstDTO, SecondDTO.class);

        assertEquals(secondDTO.getAstra(), firstDTO.getAstra());
        assertEquals(secondDTO.getName(), firstDTO.getName());
    }

    @Test
    public void incorrectLinearTypeCastTest() {
        EasterBunny easterBunny = new EasterBunny("egg");

        assertThrows(ColumbusException.class, () -> {
            Scott scott = fieldMapper.map(easterBunny, Scott.class);
        });
    }
}
