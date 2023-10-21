package com.github.lemerch.columbus.jdbcmapper;

import com.github.lemerch.columbus.JdbcMapper;
import com.github.lemerch.columbus.jdbcmapper.beans.DTO;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DtoTests {

    private static final JdbcMapper.forDTO<DTO> dtoMapper =
            new JdbcMapper.forDTO<>(DTO.class, "name","sname",
                                                        "message", "smessage");

    @Test
    public void simpleDtoTest() {
        DTO dto = new DTO();
        dto.setName("a");
        dto.setMessage("b");

        Map<String, Object> map = dtoMapper.getParams(dto);

        assertEquals("sname, smessage", dtoMapper.columns);
        assertEquals(":name, :message", dtoMapper.values);

        assertEquals("a", map.get("name"));
        assertEquals("b", map.get("message"));
    }
}
