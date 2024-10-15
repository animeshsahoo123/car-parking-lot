package com.demo.carparkinglot.utils;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Objects;

public class ReflectionTestUtils {


    public static void setFieldValue(Object object, String fieldName, Object value) {
        Field field = Objects.requireNonNull(ReflectionUtils.findField(object.getClass(), fieldName));
        field.setAccessible(true);
        ReflectionUtils.setField(field, object, value);
    }

    public static Object getFieldValue(Object object, String fieldName) {
        Field field = Objects.requireNonNull(ReflectionUtils.findField(object.getClass(), fieldName));
        field.setAccessible(true);
        return ReflectionUtils.getField(field, object);
    }
}
