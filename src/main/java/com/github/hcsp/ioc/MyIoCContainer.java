package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class MyIoCContainer {
    ConcurrentHashMap<String, Object> beanMap = new ConcurrentHashMap<>();

    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入
    public static void main(String[] args) {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    // 启动该容器
    public void start() {
        try {
            Properties properties = new Properties();
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
            properties.forEach(this::propertiesConvertIntoBeanMap);
            beanMap.values().forEach(this::DependencyInject);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void DependencyInject(Object beanObject) {
        Field[] declaredFields = beanObject.getClass().getDeclaredFields();
        Arrays.stream(declaredFields)
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .forEach(field -> fieldSetObj(beanObject, field));
    }

    private void fieldSetObj(Object beanObject, Field field) {
        field.setAccessible(true);
        try {
            field.set(beanObject, beanMap.get(field.getName()));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    private void propertiesConvertIntoBeanMap(Object beanName, Object className) {
        try {
            Class<?> klass = Class.forName((String) className);
            Object o = klass.getConstructor().newInstance();
            beanMap.put((String) beanName, o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beanMap.get(beanName);
    }
}
