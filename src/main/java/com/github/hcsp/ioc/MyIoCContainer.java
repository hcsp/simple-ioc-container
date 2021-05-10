package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {

    private Map<String, Object> container = new ConcurrentHashMap<>();

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
        // read properties
        Map<String, String> clazzMap = readProperties();
        // foreach new Instance and put into container
        newInstances(clazzMap);
        // load @Autowired fields
        loadFields();
    }

    private void loadFields() {
        container.forEach((k, v) -> {
            Field[] declaredFields = v.getClass().getDeclaredFields();
            Stream.of(declaredFields).forEach(field -> {
                if (Arrays.stream(field.getDeclaredAnnotations()).anyMatch(a -> a.annotationType().equals(Autowired.class))) {
                    String fieldName = field.getName();
                    try {
                        field.setAccessible(true);
                        field.set(v, container.get(fieldName));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    private void newInstances(Map<String, String> clazzMap) {
        clazzMap.forEach((k, v) -> {
            container.put(k, getInstance(v));
        });
    }

    private Object getInstance(String className) {
        try {
            return Class.forName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            return null;
        }
    }

    private Map<String, String> readProperties() {
        List<String> strings = null;
        try {
            strings = Files.readAllLines(new File("src/main/resources/beans.properties").toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strings.stream().map(s -> s.split("="))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return container.get(beanName);
    }
}
