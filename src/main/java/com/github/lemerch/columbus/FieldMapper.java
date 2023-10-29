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


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * <h3>Field Mapper is needed for mapping your objects or any other JavaBean type classes</h3>
 * <p></p>
 * <p>By default, the {@link FieldMapper#map(Object, Class)} method copies values based on the field names, if the field names are identical, but the type is different, then FieldMapper will throw an {@link ColumbusException}</p>
 * <p>For additional control, you can use the {@link FieldMapper#withSchema(String...)} methods through which you can bind the `from` fields to the `to` fields. However, their type must match</p>
 * <p>In addition, if you need a reliable check that your `to` object will not contain null values, then you can use the {@link FieldMapper#withNotNullPolicy()} method</p>
 * <p></p>
 * <h3>Warrning</h3>
 * <p>All these additional settings are valid until you run map</p>
 * <p>Therefore, I do not advise making it static or using a single FieldMapper in multiple threads. Create it inside each thread. This class weighs little and cleans itself, so it won't bother you</p>
 */
public class FieldMapper {

    private Map<String, String> schema = new HashMap<>();
    private boolean notNullPolicy = false;

    public FieldMapper() {}

    /**
     * This method controls that the fields of your object are `to` so that they are not null
     *
     * @return current FieldMapper
     */
    public FieldMapper withNotNullPolicy() {
        this.notNullPolicy = true;
        return this;
    }

    /**
     * <p>
     * This method allows you to set the scheme of linking the fields of the `from` class to the `to` class. If their type is not identical,
     * the {@link FieldMapper#map(Object, Class)} method throws an {@link ColumbusException}
     * </p>
     *
     * @param map Map< FROM, TO > fields
     * @return current FIeldMapper
     */
    public FieldMapper withSchema(Map<String, String> map) {
        schema.putAll(map);
        return this;
    }

    /**
     * <p>
     * This method allows you to set the scheme of linking the fields of the `from` class to the `to` class. If their type is not identical,
     * the {@link FieldMapper#map(Object, Class)} method throws an {@link ColumbusException}
     * </p>
     *
     * @param from$to Map< FROM, TO > fields
     * @return current FIeldMapper
     */
    public FieldMapper withSchema(String... from$to) {
        if (from$to.length % 2 != 0) {
            throw new ColumbusException("The number of from$to values must be even");

        } else if (from$to.length == 0) {
            throw new ColumbusException("Size of map must be more then 0");
        }
        for (int i = 0; i < from$to.length; i+=2) {
            this.schema.put(from$to[i], from$to[i+1]);
        }
        return this;
    }

    /**
     *
     * <p>This method is your `to` object and fills it with fields from the `from` object</p>
     *
     *
     * @param from object the object to be mapped to `to` class
     * @param to the class that will be created and populated based on the `from` object
     * @return `to` object
     * @param <FROM>
     * @param <TO>
     */
    public<FROM, TO> TO map(FROM from, Class<TO> to) {

        Field[] fromFields = from.getClass().getDeclaredFields();
        Field[] toFields = to.getDeclaredFields();

        // check if schema key / values are exist in `from` / `to` classes
        schemaCheck(fromFields, toFields);

        // create new instance of `to` class
        TO result;
        try {
            result = to.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new ColumbusException("Default constructor not found in class `" + to + "`");
        }

        // inject `from` values into `to` (result) fields
        handle(from, to, result, fromFields, toFields);

        // clear additional data
        this.notNullPolicy = false;
        this.schema = new HashMap<>();

        return result;
    }

    private<FROM, TO> void handle(FROM from, Class<TO> to, TO result, Field[] fromFields, Field[] toFields) {

        // for not null policy
        int toFieldsLen = toFields.length;
        int toFieldsLenCounter = 0;

        for (Field fromField : fromFields) {

            // schema data
            boolean schemaWas = false;
            String schemaVal = "";


            if (this.schema.containsKey(fromField.getName())) {
                schemaWas = true;
                schemaVal = this.schema.get(fromField.getName());
            }

            Method fromGetter;
            try {
                fromGetter = from.getClass().getMethod(Utils.getGetterByFieldName(fromField.getName()));
            } catch (NoSuchMethodException e) {
                throw new ColumbusException(e);
            }

            for (Field toField : toFields) {

                // schema fields found
                if (schemaWas && toField.getName().equals(schemaVal)) {
                    if (fromField.getType().equals(toField.getType())) {
                        toFieldsLenCounter++;
                        inject(from, to, result, toField, fromGetter);
                    } else {
                        throw new ColumbusException("Schema cast type exception from `" +
                                fromField.getName() + "` type of `" + fromField.getType() + "` to `" +
                                toField.getName() + "` type of `" + toField.getType() + "`");
                    }
                    break;

                // `from` field found, but `to`... doesn't
                }else if (schemaWas) continue;

                // if `from` field doesn't contains in schema
                // but `to` field already set from schema
                if (this.schema.containsValue(toField.getName())) continue;

                // if `from` field doesn't contains in schema
                if (fromField.getName().equals(toField.getName())) {
                    if (fromField.getType().equals(toField.getType())) {
                        toFieldsLenCounter++;
                        inject(from, to, result, toField, fromGetter);
                    } else {
                        throw new ColumbusException("Field cast type exception from `" +
                                fromField.getName() + "` type of `" + fromField.getType() + "` to `" +
                                toField.getName() + "` type of `" + toField.getType() + "`");

                    }
                }

            } // for ( toField )

        } // for ( fromField )

        if (this.notNullPolicy && toFieldsLen != toFieldsLenCounter)
            throw new ColumbusException("Not Null Policy Exception, some fields in `to object` are null, please check fields names, types or schema");
    }

    // boilerplate
    private<FROM, TO> void inject(
            FROM from, Class<TO> to, TO result, Field toField, Method fromGetter
    ) {
        Method toSetter;
        try {
            toSetter = to.getMethod(Utils.getSetterByFieldName(toField.getName()), toField.getType());
        } catch (NoSuchMethodException e) {
            throw new ColumbusException(e);
        }

        try {
            toSetter.invoke(result, fromGetter.invoke(from));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ColumbusException(e);
        }

    }
    private void schemaCheck(Field[] fromFields, Field[] toFields) {
        if (this.schema.isEmpty()) return;

        for (String key : schema.keySet()) {
            boolean fromCorrect = false;
            boolean toCorrect = false;
            for (Field fromField : fromFields) {
                if (key.equals(fromField.getName())) {
                    fromCorrect = true;
                    break;
                }
            }
            for (Field toField : toFields) {
                if (schema.get(key).equals(toField.getName())) toCorrect = true;
            }
            if (!fromCorrect) {
                throw new ColumbusException ("Filed with name `" + key + "` not found in `from` object");
            } else if (!toCorrect) {
                throw new ColumbusException ("Filed with name `" + schema.get(key) + "` not found in `to` class");
            }
        }
    }

}