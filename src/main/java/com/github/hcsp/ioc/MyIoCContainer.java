package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {
    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入
    private Map<String, Object> beans;

    public static void main(String[] args) {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    // 启动该容器
    public void start() {
        Properties properties = new Properties();
        beans = new HashMap<>();

        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
            properties.forEach(this::getBeanInstanceAndPutIntoContainer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        beans.forEach((beanName, beanInstance) -> dependencyInject(beanName, beanInstance, beans));
    }

    private void getBeanInstanceAndPutIntoContainer(Object beanName, Object beanClass) {
        try {
            Class<?> clazz = Class.forName((String) beanClass);
            Object beanInstance = clazz.getConstructor().newInstance();
            beans.put((String) beanName, beanInstance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void dependencyInject(String beanName, Object beanInstance, Map<String, Object> beans) {
        List<Field> fieldsToBeAutowired = Stream.of(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());

        fieldsToBeAutowired.forEach(field -> wireField(beanInstance, field));
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

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
