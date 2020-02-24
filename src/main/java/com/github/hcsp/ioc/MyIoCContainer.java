package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class MyIoCContainer {
    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入
    Map<String, Object> beanMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    // 启动该容器
    public void start() {
        fillBeanMap();
        injectDependencies();
    }

    private void injectDependencies() {
        beanMap.forEach((beanName, instance) ->
                Stream.of(instance.getClass().getDeclaredFields())
                        .filter(field -> field.getAnnotation(Autowired.class) != null)
                        .forEach(field -> {
                            setFieldValue(instance, field);
                        }));
    }

    private void setFieldValue(Object instance, Field field) {
        try {
            field.setAccessible(true);
            field.set(instance, beanMap.get(field.getName()));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillBeanMap() {
        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        properties.forEach((beanName, beanClassName) -> {
            try {
                Class<?> klass = Class.forName((String) beanClassName);
                Object instance = klass.getConstructor().newInstance();
                beanMap.put((String) beanName, instance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beanMap.get(beanName);
    }
}
