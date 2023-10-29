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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * <h3>This class is a great tool for creating universal dao classes in your project</h3>
 * <p>Jdbc Mapper does not have its own constructor, however it has three subclasses {@link forModel}, {@link forDTO}, {@link forContractDTO}</p>
 */
public class JdbcMapper {

    private JdbcMapper() {}

    /**
     * <h3>This class is designed to generate and store RowMapper</h3>
     * <p>{@link RowMapper} generation occurs when an object is created, and you can take it via the {@link forModel#get()} method</p>
     * @param <MODEL>
     */
    public static class forModel<MODEL> {

        private RowMapper<MODEL> rowMapper;

        /**
         * This map used in {@link forContractDTO}
         */
        private final Map<String, String> map;
        public forModel(Class<MODEL> clazz, String... model$db) {
            if (model$db.length % 2 != 0) {
                throw new ColumbusException("The number of model$db (key-value) must be even");
            } else if (model$db.length == 0) {
                throw new ColumbusException("Size of Map must be greater than 0");
            }

            Map<String, String> map = new HashMap<>();

            for (int i = 0; i < model$db.length; i+=2) {
                map.put(model$db[i], model$db[i + 1]);

                try {
                    clazz.getDeclaredField(model$db[i]);
                } catch (NoSuchFieldException e) {
                    throw new ColumbusException(e);
                }
            }
            this.map = map;
            generateRowMapper(clazz, map);
        }

        public RowMapper<MODEL> get() { return this.rowMapper; }
        private void generateRowMapper(Class<MODEL> clazz, Map<String, String> map) {
            this.rowMapper = (rs, rowNum) -> {

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
            };
        }
    }

    /**
     * <h3>This class will help you not to create the same type of mappers for your dto</h3>
     *
     * <p>To be more precise, this class allows you to generate {@link forDTO} quite efficiently based on {@link forModel} and export it to this object</p>
     * <p>Relative efficiency is achieved by a private constructor in the {@link forDTO} class, which does not check the fields once again, because they have already been checked in the {@link forContractDTO} constructor</p>
     */
    public static class forContractDTO {
        /**
         * insert into `table` (columns) values(...)
         */
        public final String columns;
        /**
         * insert into `table`(...) values(values)
         */
        public final String values;

        /**
         * values of your object `dto`
         */
        public final Map<String, Object> params;

        public<MODEL, DTO> forContractDTO(JdbcMapper.forModel<MODEL> modelMapper, DTO dto) {

            // 1. generate dtoMap
            Map<String, String> newMap = new HashMap<>();

            for (String key : modelMapper.map.keySet()) {
                try {
                    dto.getClass().getDeclaredField(key);
                    newMap.put(key, modelMapper.map.get(key));
                } catch (NoSuchFieldException e) {
                    // continue because the model can store fields that will not be in the dto
                }
            }
            // 2. create dtoMapper
            forDTO<DTO> dtoMapper = (forDTO<DTO>) new forDTO<>(dto.getClass(), newMap);

            // 3. export into this object
            this.columns = dtoMapper.columns;
            this.values = dtoMapper.values;
            this.params = dtoMapper.getParams(dto);
        }
    }

    /**
     * <p>The jdbctemplate class.forDTO is perhaps one of the main classes in this project. At least because of him, I started writing this library :)</p>
     * <p>The basis of this class is the constructor - {@link forDTO#forDTO(Class, String...)} and the {@link forDTO#getParams(Object)} method</p>
     * @param <DTO>
     */
    public static class forDTO<DTO> {
        /**
         * insert into `table` (columns) values(...)
         */
        public final String columns;
        /**
         * insert into `table`(...) values(values)
         */
        public final String values;
        public forDTO(Class<DTO> clazz, String... dto$db) {
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
        private forDTO(Class<DTO> clazz, Map<String, String> dto$db) {

            StringBuilder columnBuilder = new StringBuilder();
            StringBuilder valueBuilder = new StringBuilder();

            int iter = 0;
            for (String key : dto$db.keySet()) {
                try {
                    clazz.getDeclaredField(key);
                } catch (NoSuchFieldException e) {
                    throw new ColumbusException(e);
                }
                if (iter == dto$db.size()-1) {
                    valueBuilder.append(":").append(key);
                    columnBuilder.append(dto$db.get(key));
                }else {
                    valueBuilder.append(":").append(key).append(", ");
                    columnBuilder.append(dto$db.get(key)).append(", ");
                }
                iter++;
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
        public Map<String, Object> getParams(DTO dto) {
            Map<String, Object> params = new TreeMap<>();
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
