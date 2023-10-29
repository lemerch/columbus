package com.github.lemerch.columbus.jdbcmapper;

import com.github.lemerch.columbus.JdbcMapper;
import com.github.lemerch.columbus.jdbcmapper.beans.DTO;
import com.github.lemerch.columbus.jdbcmapper.beans.Model;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ContractDTOTests {

    private final static JdbcMapper.forModel<Model> modelMapper =
            new JdbcMapper.forModel<>(Model.class,
                    "id", "id",
                    "message", "smessage",
                    "name", "sname");

    @Test
    public void example() {
        DTO dto = new DTO();
        dto.setName("a");
        dto.setMessage("b");

        JdbcMapper.forContractDTO contract = new JdbcMapper.forContractDTO(modelMapper, dto);

        assertEquals("a", contract.params.get("name"));
        assertEquals("b", contract.params.get("message"));
    }
}
