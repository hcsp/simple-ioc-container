package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {
    Map<String, Object> beans;
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
        beans = getBeansFromPropertiesFile();
        injectDependency(beans);
    }

    private Map<String, Object> getBeansFromPropertiesFile() {
        Properties properties = loadPropertiesFromFile();
        return getBeansInstanceMap(properties);
    }

    private Properties loadPropertiesFromFile() {
        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream("/beans.properties"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    private Map<String, Object> getBeansInstanceMap(Properties properties) {
        Map<String, Object> beans = new ConcurrentHashMap<>();
        properties.forEach((beanName, beanClass) -> {
            try{
                Class<?> clazz = Class.forName((String) beanClass);
                Object beanInstance = clazz.getConstructor().newInstance();
                beans.put((String) beanName, beanInstance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return beans;
    }

    private static void injectDependency(Map<String, Object> beans) {
        beans.forEach((beanName, beanInstance) -> {
            List<Field> fieldsToBeInjected = getFieldsToBeInjected(beanInstance);
            injectDependency(beans, beanInstance, fieldsToBeInjected);
        });
    }

    private static List<Field> getFieldsToBeInjected(Object beanInstance) {
        Field[] fields = beanInstance.getClass().getDeclaredFields();
        return Stream.of(fields).filter(field -> field.getAnnotation(Autowired.class) != null).collect(Collectors.toList());
    }

    private static void injectDependency(Map<String, Object> beans, Object beanInstance, List<Field> fieldsToBeInjected) {
        fieldsToBeInjected.forEach(field -> {
            Object dependencyInstance = beans.get(field.getName());
            field.setAccessible(true);
            try {
                field.set(beanInstance, dependencyInstance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
