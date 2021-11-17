package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class MyIoCContainer {
    private Map<String, Object> beansCache = new HashMap<>();

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
        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
            properties.forEach((propertyName, propertyClassName) -> {
                try {
                    beansCache.put((String) propertyName, Class.forName((String) propertyClassName).getConstructor().newInstance());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        beansCache.forEach((beanName, beanInstance) -> dependencyInjection(beanName, beanInstance, beansCache));
    }

    private void dependencyInjection(String beanName, Object beanInstance, Map<String, Object> beansCache) {
        List<Field> fieldList = Arrays.stream(beanInstance.getClass().getDeclaredFields()).filter(field -> field.getAnnotation(Autowired.class) != null).collect(Collectors.toList());
        fieldList.forEach(field -> {
            String fieldName = field.getName();
            field.setAccessible(true);
            try {
                field.set(beanInstance, beansCache.get(fieldName));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }


    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beansCache.get(beanName);
    }
}
