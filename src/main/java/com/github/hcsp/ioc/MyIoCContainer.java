package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public class MyIoCContainer {
    private final Map<String, Object> beans = new HashMap<>();

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
        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
            properties.forEach((beanName, beanClass) -> {
                try {
                    Class<?> klass = Class.forName((String) beanClass);
                    Object beanObejct = klass.getConstructor().newInstance();
                    beans.put((String) beanName, beanObejct);
                } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                         IllegalAccessException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            });
            beans.values().forEach(this::injectDependency);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }

    private void injectDependency(Object o) {
        Stream<Field> fieldsToBeInjected = Arrays.stream(o.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Autowired.class));
        fieldsToBeInjected.forEach(field -> {
            try {
                String fieldName = field.getName();
                Object objectToBeAddedDependency = beans.get(fieldName);
                field.setAccessible(true);
                field.set(o, objectToBeAddedDependency);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
