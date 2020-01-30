package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {
    private static Map<String, Object> beanMapCache = new HashMap<>();

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
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        if (beanNotExistingInCache(beanName)) {
            putBeanIntoCacheAndDealWithDependencies();
        }
        return beanMapCache.get(beanName);
    }

    private void putBeanIntoCacheAndDealWithDependencies() {
        loadProperties();
        beanMapCache.forEach((name, object) -> injectionDependency(name, object, beanMapCache));
    }

    private void loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        properties.forEach((key, value) -> {
            try {
                Class<?> aClass = Class.forName((String) value);
                Object object = aClass.getConstructor().newInstance();
                beanMapCache.put((String) key, object);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean beanNotExistingInCache(String beanName) {
        return beanMapCache.get(beanName) == null;
    }

    private void injectionDependency(String name, Object object, Map<String, Object> beanMap) {
        List<Field> beanTobeWired = Stream.of(object.getClass().getDeclaredFields()).filter(field -> field.getAnnotation(Autowired.class) != null).collect(Collectors.toList());
        beanTobeWired.forEach(field -> {
            String fieldName = field.getName();
            Object beanInstance = beanMap.get(fieldName);
            field.setAccessible(true);
            try {
                field.set(object, beanInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
