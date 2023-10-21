/**
 * Copyright 2023 Dmitry Terakov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * \ \ .       .             \                  O’er the glad waters of the dark
 * . \ \       |\            / \_               blue sea,
 * \ . .       | \            \                 Our thoughts as boundless, and
 * . \         |  \            \                our souls as free,
 *  .          |   \           /                Far as the breeze can bear, the
 *             |    \          \                billows foam,
 *             |     \                          Survey our empire, and behold
 *             |      \                         our home.
 *             |       \       ____O                             «The Corsair». L. Byron
 *             |        \     .' ./
 *             |   _.,-~"\  .',/~'
 *             &lt;-~"   _.,-~" ~ |
 * ^"~-,._.,-~"^"~-,._\       /,._.,-~"^"~-,._.,-~"^"~-,._
 * ~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._
 * ^"~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._
 * ~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^"~-,._.,-~"^ COLUMBUS - the discoverer of convenience mapping
 */
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

/**
 * <h3>this class is a great tool for creating universal dao classes in your project</h3>
 * <p>JdbcTemplate does not have a constructor, but it does have a static {@link JdbcMapper#generateRowMapper(Class, String...)} method with which you can generate a RowMapper for your db model</p>
 * <p>In addition, it has a {@link JdbcMapper.forDTO} class that will help you a lot for abstract repositories.</p>
 */
public class JdbcMapper {

    private JdbcMapper() {}

    /**
     * This method will generate you a {@link RowMapper} for the above model
     *
     * @param clazz MyModel.class
     * @param model$db Map of < ModelField, DBField >
     * @return RowMapper< MyModel >
     * @param <MODEL>
     */
    public static<MODEL> RowMapper<MODEL> generateRowMapper(Class<MODEL> clazz, String... model$db) {
        if (model$db.length % 2 != 0) {
            throw new ColumbusException("The number of model$db (key-value) must be even");
        } else if (model$db.length == 0) {
            throw new ColumbusException("Size of Map must be greater than 0");
        }

        TreeMap<String, String> map = new TreeMap<>();

        for (int i = 0; i < model$db.length; i+=2) {
            map.put(model$db[i], model$db[i + 1]);

            try {
                clazz.getDeclaredField(model$db[i]);
            } catch (NoSuchFieldException e) {
                throw new ColumbusException(e);
            }
        }
        return generateRowMapper(clazz, map);
    }
    private static<MODEL> RowMapper<MODEL> generateRowMapper(Class<MODEL> clazz, TreeMap<String, String> map) {
        return new RowMapper<MODEL>() {
            @Override
            public MODEL mapRow(ResultSet rs, int rowNum) throws SQLException {

                // init - must be default constructor
                MODEL obj;

                try {
                    obj = clazz.getConstructor().newInstance();
                } catch (NoSuchMethodException e) {
                    throw new ColumbusException("Default constructor not found in class `" + clazz + "`");
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    throw new ColumbusException(e);
                }

                // iterate fields, get its setters,
                // and set value from rs.getObject(map.get(field name))
                for (Field field : clazz.getDeclaredFields()) {

                    Method setter;
                    try {
                        setter = clazz.getMethod(Utils.getSetterByFieldName(field.getName()), field.getType());

                    } catch (NoSuchMethodException e) {
                        throw new ColumbusException(e);
                    }
                    try {
                        setter.invoke(obj, rs.getObject(map.get(field.getName())));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new ColumbusException(e);
                    }
                }
                return obj;
            }
        };
    }

    /**
     * <p>The jdbctemplate class.forDTO is perhaps one of the main classes in this project. At least because of him, I started writing this library :)</p>
     * <p>The basis of this class is the constructor - {@link forDTO#forDTO(Class, String...)} and the {@link forDTO#getParams(Object)} method</p>
     * @param <T>
     */
    public static class forDTO<T> {
        /**
         * insert into `table` (columns) values(...)
         */
        public final String columns;
        /**
         * insert into `table`(...) values(values)
         */
        public final String values;
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
                         throw new ColumbusException(e);
                     }
                     if (i == dto$db.length-2) valueBuilder.append(":").append(dto$db[i]);
                     else valueBuilder.append(":").append(dto$db[i]).append(", ");
                 // values - db(this.columns)
                 }else {
                     if (i == dto$db.length-1) columnBuilder.append(dto$db[i]);
                     else columnBuilder.append(dto$db[i]).append(", ");
                 }

            }
            this.columns = columnBuilder.toString();
            this.values = valueBuilder.toString();
        }

        /**
         * <p>This method generates a Map <fieldName, FieldValue>, which you should use in the insert request NamedJdbcTemplate</p>
         *
         * @param dto
         * @return Map< fieldName, fieldValue >
         */
        public Map<String, Object> getParams(T dto) {
            Map<String, Object> params = new HashMap<>();
            Class<?> clazz = dto.getClass();
            for (Field field : clazz.getDeclaredFields()) {

                Method getter;

                try {
                    getter = clazz.getMethod(Utils.getGetterByFieldName(field.getName()));
                } catch (NoSuchMethodException e) {
                    throw new ColumbusException(e);
                }

                try {
                    params.put(field.getName(), getter.invoke(dto));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new ColumbusException(e);
                }
            }
            return params;
        }

    }

}
