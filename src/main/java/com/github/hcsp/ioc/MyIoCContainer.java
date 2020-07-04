package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class MyIoCContainer {
    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入
    public static void main(String[] args) {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    private final Map<String, Object> beans = new HashMap<>();

    // 启动该容器
    public void start() {
        try {
            final Properties properties = new Properties();
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
            properties.forEach((beanName, beanClass) -> createBeanInstanceAndPutIntoMap(beans, (String) beanName, (String) beanClass));
            beans.forEach((beanName, beanInstance)->dependencyInject(beanName, beanInstance, beans));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dependencyInject(String beanName, Object beanInstance, Map<String, Object> beans) {
        final List<Field> fieldsToBeAutowired = Arrays.stream(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());
        fieldsToBeAutowired.forEach(field -> {
            try {
                final String name = field.getName();
                final Object dependencyBeanInstance = beans.get(name);
                field.setAccessible(true);
                field.set(beanInstance, dependencyBeanInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void createBeanInstanceAndPutIntoMap(Map<String, Object> beans, String beanName, String beanClass) {
        try {
            Class<?> klass = Class.forName(beanClass);
            Object beanInstance = klass.getConstructor().newInstance();
            beans.put(beanName, beanInstance);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
