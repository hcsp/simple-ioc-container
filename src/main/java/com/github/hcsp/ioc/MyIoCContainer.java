package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
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
    public static void main(String[] args) {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    private Map<String, Object> beanMap = new HashMap<>();

    // 启动该容器
    public void start() {

        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("beans.properties");
        Properties properties = new Properties();
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        properties.forEach((beanName, beanClassName) -> {
            try {
                Class<?> clazz = Class.forName((String) beanClassName);
                Object beanInstance = clazz.getConstructor().newInstance();
                beanMap.put((String) beanName, beanInstance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        beanMap.forEach((this::dependencyInject));
    }

    private void dependencyInject(String beanName, Object beanInstance) {
        List<Field> fieldToBeAutowired = Stream.of(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Autowired.class))
                .collect(Collectors.toList());
        fieldToBeAutowired.forEach(field -> {
            String fieldName = field.getName();
            field.setAccessible(true);
            Object dependencyInstance = beanMap.get(fieldName);
            try {
                field.set(beanInstance, dependencyInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beanMap.get(beanName);
    }
}
