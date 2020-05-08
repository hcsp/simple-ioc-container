package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {
    public static final String PROPERTIES_FILE_PATH = "/beans.properties";
    private static Map<String, Object> beans = new ConcurrentHashMap<>();

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
        loadAllBeansFromProperties().forEach((k, v) -> putAllInstanceIntoMap((String) k, (String) v));
        beans.forEach((beanName, beanInstance) -> dependencyInjection(beanInstance));
    }

    private void dependencyInjection(Object beanInstance) {
        List<Field> fieldsToBeAutowired = Stream.of(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());
        fieldsToBeAutowired.forEach(field -> {
            String fieldName = field.getName();
            Object dependencyBeanInstance = beans.get(fieldName);
            field.setAccessible(true);
            try {
                field.set(beanInstance, dependencyBeanInstance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    private Properties loadAllBeansFromProperties() {
        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream(PROPERTIES_FILE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public void putAllInstanceIntoMap(String className, String classPath) {
        try {
            Class<?> aClass = Class.forName(classPath);
            beans.put(className, aClass.getConstructor().newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
