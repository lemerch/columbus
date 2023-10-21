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

public class FieldMapper {

    private Map<String, String> schema = new HashMap<>();
    private boolean notNullPolicy = false;

    public FieldMapper() {}

    public FieldMapper withNotNullPolicy() {
        this.notNullPolicy = true;
        return this;
    }

    public FieldMapper withSchema(Map<String, String> map) {
        schema.putAll(map);
        return this;
    }
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
    public<FROM, TO> TO map(FROM from, Class<TO> to) {

        Field[] fromFields = from.getClass().getDeclaredFields();
        Field[] toFields = to.getDeclaredFields();

        schemaCheck(fromFields, toFields);

        TO result;
        try {
            result = to.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new ColumbusException("Default constructor not found in class `" + to + "`");
        }


        handle(from, to, result, fromFields, toFields);

        this.notNullPolicy = false;
        this.schema = new HashMap<>();

        return result;
    }

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
    private<FROM, TO> void handle(FROM from, Class<TO> to, TO result, Field[] fromFields, Field[] toFields) {

        int toFieldsLen = toFields.length;
        int toFieldsLenCounter = 0;

        for (Field fromField : fromFields) {

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
                }else if (schemaWas) continue;


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

    private void schemaCheck(Field[] fromFields, Field[] toFields) {
        if (this.schema.isEmpty()) return;

        for (String key : schema.keySet()) {
            boolean fromCorrect = false;
            boolean toCorrect = false;
            for (Field fromField : fromFields) {
                if (key.equals(fromField.getName())) fromCorrect = true;
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