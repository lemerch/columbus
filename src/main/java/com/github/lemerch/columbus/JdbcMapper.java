package com.github.lemerch.columbus;

import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class JdbcMapper {

    private JdbcMapper() {}
    public static class forModel<T> {
        // model - db
        /**
         * This map must be as < model field name, db field name >
         */
        private final TreeMap<String, String> map = new TreeMap<>();
        private final Class<T> clazz;
        private final RowMapper<T> rowMapper;
        public forModel(Class<T> clazz, String... model$db) {
            if (model$db.length % 2 != 0) {
                throw new ColumbusException("The number of model$db values must be even");
            } else if (model$db.length == 0) {
                throw new ColumbusException("Size of map must be more then 0");
            }
            for (int i = 0; i < model$db.length; i+=2) {
                map.put(model$db[i], model$db[i + 1]);

                try {
                    clazz.getDeclaredField(model$db[i]);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
            this.clazz = clazz;
            this.rowMapper = generateRowMapper();
        }

        public RowMapper<T> getRowMapper() { return this.rowMapper; }
        private RowMapper<T> generateRowMapper() {
            return new RowMapper<T>() {
                @Override
                public T mapRow(ResultSet rs, int rowNum) throws SQLException {

                    // init - must be default constructor
                    T obj;

                    try {
                        obj = clazz.getConstructor().newInstance();
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException("Constructor not found" + e);
                    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    // iterate fields, get its setters,
                    // and set value from rs.getObject(map.get(field name))
                    for (Field field : clazz.getDeclaredFields()) {
                        Class<?> type;
                        String name;
                        Method setter;
                        try {
                            type = field.getType();
                            name = field.getName();
                            setter = clazz.getMethod(Utils.getSetterByFieldName(name), type);

                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            setter.invoke(obj, rs.getObject(map.get(name)));
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    return obj;
                }
            };
        }
    }


    public static class forDTO<T> {

        // db, ...
        private String columns;
        // :dto, ...
        private String values;
        public forDTO(Class<T> clazz, String... dto$db) {
            if (dto$db.length % 2 != 0) {
                throw new ColumbusException("The number of model$db values must be even");
            } else if (dto$db.length == 0) {
                throw new ColumbusException("Size of map must be more then 0");
            }

            StringBuilder columnBuilder = new StringBuilder();
            StringBuilder valueBuilder = new StringBuilder();

            for (int i = 0; i < dto$db.length; i++) {
                 // keys - dto(this.values)
                 if (i % 2 == 0) {
                     try {
                         clazz.getDeclaredField(dto$db[i]);
                     } catch (NoSuchFieldException e) {
                         throw new RuntimeException(e);
                     }
                     if (i == dto$db.length-2) columnBuilder.append(dto$db[i]);
                     else  columnBuilder.append(dto$db[i]).append(", ");
                 // values - db(this.columns)
                 }else {
                     if (i == dto$db.length-1) valueBuilder.append(":").append(dto$db[i]);
                     else valueBuilder.append(":").append(dto$db[i]).append(", ");
                 }

            }
            this.columns = columnBuilder.toString();
            this.values = valueBuilder.toString();
        }

        public String getColumns() {
            return this.columns;
        }
        public String getValues() {
            return this.values;
        }
        public<T> Map<String, Object> getParams(T dto) {
            Map<String, Object> params = new HashMap<>();
            Class<?> clazz = dto.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                String name;
                Method getter;
                try {
                    name = field.getName();
                    getter = clazz.getMethod(Utils.getGetterByFieldName(name));

                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }

                try {
                    params.put(name, getter.invoke(dto));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            return params;
        }

    }

}
