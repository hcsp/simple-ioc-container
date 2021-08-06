package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public class MyIoCContainer {
    private final Properties properties = new Properties();
    private final Map<String, Object> dependencyMap = new HashMap<>();

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
            String beanFileName = "beans.properties";
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(beanFileName);
            properties.load(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        try {
            if (dependencyMap.get(beanName) != null) {
                return dependencyMap.get(beanName);
            }
            Class<?> aClass = Class.forName(properties.getProperty(beanName));
            Object beanObject = aClass.getConstructor().newInstance();
            wireDependencies(aClass, beanObject);
            dependencyMap.put(beanName, beanObject);
            return beanObject;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void wireDependencies(Class<?> aClass, Object beanObject) {
        Stream.of(aClass.getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .forEach(field -> {
                    try {
                        String typeName = field.getGenericType().getTypeName();
                        Object instance = dependencyMap.get(typeName);
                        if (instance == null) {
                            Class<?> fieldClass = Class.forName(typeName);
                            instance = fieldClass.getConstructor().newInstance();
                            wireDependencies(fieldClass, instance);
                        }
                        dependencyMap.put(typeName, instance);
                        field.setAccessible(true);
                        field.set(beanObject, instance);
                    } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
