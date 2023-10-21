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

public class JdbcMapper {

    private JdbcMapper() {}
    public static class forModel<T> {
        /**
         * This map must be as < model field name, db field name >
         */
        public final RowMapper<T> rowMapper;
        public forModel(Class<T> clazz, String... model$db) {
            if (model$db.length % 2 != 0) {
                throw new ColumbusException("The number of model$db values must be even");
            } else if (model$db.length == 0) {
                throw new ColumbusException("Size of map must be more then 0");
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
            this.rowMapper = generateRowMapper(clazz, map);
        }

        private RowMapper<T> generateRowMapper(Class<T> clazz, TreeMap<String, String> map) {
            return new RowMapper<T>() {
                @Override
                public T mapRow(ResultSet rs, int rowNum) throws SQLException {

                    // init - must be default constructor
                    T obj;

                    try {
                        obj = clazz.getConstructor().newInstance();
                    } catch (NoSuchMethodException e) {
                        throw new ColumbusException("Constructor not found" + e);
                    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                        throw new ColumbusException(e);
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
                            throw new ColumbusException(e);
                        }
                        try {
                            setter.invoke(obj, rs.getObject(map.get(name)));
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new ColumbusException(e);
                        }
                    }

                    return obj;
                }
            };
        }
    }


    public static class forDTO<T> {
        // db, ...
        public final String columns;
        // :dto, ...
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

        public Map<String, Object> getParams(T dto) {
            Map<String, Object> params = new HashMap<>();
            Class<?> clazz = dto.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                String name = field.getName();
                Method getter;

                try {
                    getter = clazz.getMethod(Utils.getGetterByFieldName(name));
                } catch (NoSuchMethodException e) {
                    throw new ColumbusException(e);
                }

                try {
                    params.put(name, getter.invoke(dto));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new ColumbusException(e);
                }
            }
            return params;
        }

    }

}
