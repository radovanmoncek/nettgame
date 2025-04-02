package cz.radovanmoncek.ship.bay.utilities.reflection;

import java.lang.reflect.*;
import java.util.Arrays;

public final class ReflectionUtilities {

    private ReflectionUtilities() {}

    public static Field returnDeclaredFieldReflectively(Object object, String fieldName) throws NoSuchFieldException {

        Class<?> clazz = object.getClass();

        while(Arrays.stream(clazz.getDeclaredFields()).noneMatch(field -> field.getName().equals(fieldName)))
            clazz = clazz.getSuperclass();

        final var field = clazz.getDeclaredField(fieldName);

        field.setAccessible(true);

        return field;
    }

    public static Object returnFieldValueReflectively(Object object, Field field) throws IllegalAccessException {

        return field.get(object);
    }

    public static Object returnValueOnFieldReflectively(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {

        final var field = returnDeclaredFieldReflectively(object, fieldName);

        return returnFieldValueReflectively(object, field);
    }

    public static void setValueOnFieldReflectively(Object object, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {

        returnDeclaredFieldReflectively(object, fieldName)
                .set(object, value);
    }

    public static void invokeNonPublicMethod(Object object, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        final var method = object
                .getClass()
                .getDeclaredMethod(methodName, parameterTypes);

        method.setAccessible(true);
        method.invoke(object, parameters);
    }

    //Big thanks to: google.gson
    public static Class<?> returnTypeParameterAtIndex(ParameterizedType type, int i) {

        return (Class<?>) type.getActualTypeArguments()[i];
    }

    public static Class<?> findActualClass(Object object) {

        final var clazz = object.getClass();

        if (clazz.isAnonymousClass())
            return clazz.getSuperclass();

        return clazz;
    }
}
