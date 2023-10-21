package com.github.lemerch.columbus.jdbcmapper;

import com.github.lemerch.columbus.JdbcMapper;
import com.github.lemerch.columbus.jdbcmapper.beans.Model;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import static org.junit.Assert.assertNotNull;

public class ModelTests {

    private static RowMapper<Model> rowMapper =
            JdbcMapper.generateRowMapper(Model.class, "id", "id",
                                                        "name", "sname",
                                                        "message", "smessage");

    @Test
    public void simpleModelTest() {
        assertNotNull(rowMapper);
    }
}
