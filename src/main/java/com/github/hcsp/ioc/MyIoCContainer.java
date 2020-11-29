package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class MyIoCContainer {
    private static Map<String, Object> beans = new ConcurrentHashMap<>();

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
        beansPropertiesToInstanceMap().forEach(this::dependencyInject);
    }

    public void dependencyInject(String beanName, Object instanceByClassName) {
        Stream.of(instanceByClassName.getClass().getDeclaredFields())
                .filter((field) -> field.getAnnotation(Autowired.class) != null)
                .forEach((field) -> wireField(instanceByClassName, field));
    }

    private void wireField(Object beanInstance, Field field) {
        try {
            String fieldName = field.getName();
            Object dependencyBeanInstance = beans.get(fieldName);
            field.setAccessible(true);
            field.set(beanInstance, dependencyBeanInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getInstanceByClassName(String className) {
        Object instance = null;
        try {
            Class<?> aClass = Class.forName(className);
            instance = aClass.getConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return instance;
    }

    public Map<String, Object> beansPropertiesToInstanceMap() {
        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
            properties.forEach((k, v) -> {
                beans.put((String) k, getInstanceByClassName((String) v));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return beans;
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
