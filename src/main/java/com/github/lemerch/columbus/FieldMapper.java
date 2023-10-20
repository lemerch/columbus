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
        checkFrom(from);
        checkTo(to);

        TO result;
        try {
            result = to.getConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Default constructor not found in class `" + to + "`");
        }

        schemaInject(from, to, result);

        defaultInject(from, to, result);

        if (this.notNullPolicy) checkNotNull(result);

        this.notNullPolicy = false;
        this.schema = new HashMap<>();

        return result;
    }

    private<FROM, TO> void schemaInject(FROM from, Class<TO> to, TO result) {
        for (String key : this.schema.keySet()) {
            Field field;
            try {
                field = to.getDeclaredField(this.schema.get(key));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            Class<?> type = field.getType();
            String name = field.getName();
            Method toSetter;
            Method fromGetter;

            try {
                toSetter = to.getMethod(Utils.getSetterByFieldName(name), type);
                fromGetter = from.getClass().getMethod(Utils.getGetterByFieldName(key));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            try {
                toSetter.invoke(result, fromGetter.invoke(from));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private<FROM, TO> void defaultInject(FROM from, Class<TO> to, TO result) {
        for (Field fromField : from.getClass().getDeclaredFields()) {
            String fromName = fromField.getName();
            if (this.schema.containsKey(fromName)) continue;

            Class<?> fromType = fromField.getType();
            Method fromGetter;
            try {
                fromGetter = from.getClass().getMethod(Utils.getGetterByFieldName(fromName));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }


            for (Field toField : to.getDeclaredFields()) {
                String toName = toField.getName();
                if (this.schema.containsValue(toName)) continue;

                Class<?> toType = toField.getType();
                Method toSetter;
                if (fromName.equals(toName) && fromType.equals(toType)) {
                    try {
                        toSetter = to.getMethod(Utils.getSetterByFieldName(fromName), toType);
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        toSetter.invoke(result, fromGetter.invoke(from));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
    private<FROM> void checkFrom(FROM from) {
        if (this.schema.isEmpty()) return;

        Class<?> clazz = from.getClass();
        for (String key : schema.keySet()) {
            try {
                clazz.getDeclaredField(key);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException ("Filed with name `" + key + "` not found in `" + from + "`");
            }
        }
    }
    private<TO> void checkTo(Class<TO> to) {
        if (this.schema.isEmpty()) return;

        for (String value : schema.values()) {
            try {
                to.getDeclaredField(value);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException ("Filed with name `" + value + "` not found in `" + to + "`");
            }
        }
    }

    private<TO> void checkNotNull(TO result) {
        Class<?> clazz = result.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            Method getter;
            try {
                getter = clazz.getMethod(Utils.getGetterByFieldName(field.getName()));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            try {
                if (getter.invoke(result) == null) {
                    throw new RuntimeException("Field `" + field.getName() + "` is null, please check `from` object or your schema");
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

        }
    }
}