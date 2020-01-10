package com.github.hcsp.ioc;


import java.lang.reflect.Field;
import java.util.stream.Stream;

public class OrderService {
    public Integer a =1;


    public void createOrder() {

    }

    public static void main(String[] args) throws IllegalAccessException, InstantiationException, NoSuchFieldException {
        Class aClass = OrderService.class;
        Object o = aClass.newInstance();
        Field a = aClass.getDeclaredField("a");
        a.set(o,5);
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field field : declaredFields) {
            System.out.println(field.get(o));
            field.setInt(o,2);
            System.out.println(field.get(o));
        }
        Stream.of(declaredFields).forEach(field -> {
            try {


            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
}
