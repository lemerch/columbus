package com.github.lemerch.columbus.fieldmapper;

import com.github.lemerch.columbus.ColumbusException;
import com.github.lemerch.columbus.FieldMapper;
import com.github.lemerch.columbus.fieldmapper.beans.schemafirst.One;
import com.github.lemerch.columbus.fieldmapper.beans.schemafirst.Two;
import com.github.lemerch.columbus.fieldmapper.beans.schemasecond.Andy;
import com.github.lemerch.columbus.fieldmapper.beans.schemasecond.Santa;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class FieldMapperSchemaTests {

    private FieldMapper fieldMapper = new FieldMapper();
    private final Map<String, String> oneTwoSchema =
            Map.of("first", "sfirst",
                    "second", "ssecond");

    private final Map<String, String> reverseSchema =
            Map.of("first", "ssecond",
                    "second", "sfirst");

    private final Map<String, String> nullSchema =
            Map.of("first", "ssecond");

    private final Map<String, String> incorrectSchema =
            Map.of("first", "second");

    private final Map<String, String> otherIncorrectSchema =
            Map.of("first", "ssecond", "hello", "world");

    @Test
    public void simpleSchemaTest() {
        One one = new One(1L,"hello", "world");

        Two two = fieldMapper.withSchema(oneTwoSchema).map(one, Two.class);

        assertEquals(two.getSfirst(), one.getFirst());
        assertEquals(two.getSsecond(), one.getSecond());
    }

    @Test
    public void reverseSchemaTest() {
        One one = new One(1L,"hello", "world");

        Two two = fieldMapper.withSchema(reverseSchema).map(one, Two.class);

        assertEquals(two.getSfirst(), one.getSecond());
        assertEquals(two.getSsecond(), one.getFirst());
    }

    @Test
    public void withNullSchemaTest() {
        One one = new One(1L,"hello", "world");

        Two two = fieldMapper.withSchema(nullSchema).map(one, Two.class);

        assertEquals(two.getSsecond(), one.getFirst());
        assertNull(two.getSfirst());
    }

    @Test
    public void withNoNullPolicyTest() {
        One one = new One(1L,"hello", "world");

        assertThrows(ColumbusException.class, () -> {
            fieldMapper.withSchema(nullSchema).withNotNullPolicy().map(one, Two.class);
        });
    }

    @Test
    public void incorrectSchemaTest() {
        One one = new One(1L,"hello", "world");

        assertThrows(ColumbusException.class, () -> {
            fieldMapper.withSchema(incorrectSchema).map(one, Two.class);
        });
    }

    @Test
    public void otherIncorrectSchemaTest() {
        One one = new One(1L,"hello", "world");

        assertThrows(ColumbusException.class, () -> {
            fieldMapper.withSchema(otherIncorrectSchema).map(one, Two.class);
        });
    }

    @Test
    public void typeCastExceptionTest() {
        Santa santa = new Santa("IT xD");

        assertThrows(ColumbusException.class, () -> {
            fieldMapper.withSchema("bookGift", "playstationGift").map(santa, Andy.class);
        });
    }
}
