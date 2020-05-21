package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {

    HashMap<String, Object> beans = new HashMap<>(100);

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
        InputStream resource = MyIoCContainer.class.getResourceAsStream("/beans.properties");
        Properties properties = new Properties();
        try {
            properties.load(resource);
            properties.forEach((beanName, beanClass) -> {
                try {
                    Class<?> clazz = Class.forName((String) beanClass);
                    Object beanInstance = clazz.getConstructor().newInstance();
                    beans.put((String) beanName, beanInstance);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            beans.forEach((beanName, beanInstance) -> {
                dependencyInject(beanName, beanInstance, beans);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dependencyInject(String beanName, Object beanInstance, HashMap<String, Object> beans) {
        List<Field> fieldsToInject = Stream.of(beanInstance.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(Autowired.class)).collect(Collectors.toList());
        fieldsToInject.forEach((field -> {
            String fieldName = field.getName();
            Object dependencyBeanInstance = getBean(fieldName);
            try {
                field.setAccessible(true);
                field.set(beanInstance, dependencyBeanInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
