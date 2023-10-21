package com.github.lemerch.columbus;

import com.github.lemerch.columbus.beans.filed.One;
import com.github.lemerch.columbus.beans.filed.Two;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class FieldMapperTest {

    private FieldMapper fieldMapper = new FieldMapper();
    private final Map<String, String> oneTwoSchema =
            Map.of("first", "sfirst",
                    "second", "ssecond");

    private final Map<String, String> reverseSchema =
            Map.of("first", "ssecond",
                    "second", "sfirst");

    private final Map<String, String> nullSchema =
            Map.of("first", "ssecond");

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
            fieldMapper.withSchema(nullSchema).withNoNullPolicy().map(one, Two.class);
        });
    }
}
