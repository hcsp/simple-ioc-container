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
    private Map<String, Object> beans = new HashMap<>();

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
            properties.load(getClass().getResourceAsStream("/beans.properties"));
            properties.forEach(this::transformBeanFromProperty);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        Object instance = beans.get(beanName);
        if (instance != null) {
            Stream.of(instance.getClass().getDeclaredFields())
                    .filter(this::hasAnnotation)
                    .forEach(field -> injectForField(field, instance));
            return instance;
        }
        return null;
    }

    private void transformBeanFromProperty(Object beanName, Object className) {
        try {
            Class<?> klass = Class.forName((String) className);
            Object instance = klass.getConstructor().newInstance();
            beans.put((String) beanName, instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean hasAnnotation(Field field) {
        return field.getAnnotation(Autowired.class) != null;
    }

    private void injectForField(Field field, Object bean) {
        try {
            String fieldName = field.getName();
            Object injectInstance = beans.get(fieldName);
            traversalInjectForBean(injectInstance);
            field.setAccessible(true);
            field.set(bean, injectInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void traversalInjectForBean(Object bean) {
        try {
            List<Field> fields = Stream.of(bean.getClass().getDeclaredFields())
                    .filter(this::hasAnnotation).collect(Collectors.toList());
            if (!fields.isEmpty()) {
                fields.forEach(field -> injectForField(field, bean));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
