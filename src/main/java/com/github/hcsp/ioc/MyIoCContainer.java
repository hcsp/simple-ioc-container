package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {
    private final Properties properties = new Properties();
    private final Map<String, Object> beanMap = new HashMap<>();

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
            String beanFileName = "beans.properties";
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(beanFileName);
            properties.load(resourceAsStream);
            properties.forEach((beanName, beanClass) -> {
                try {
                    Object beanInstance = Class.forName((String) beanClass).getConstructor().newInstance();
                    beanMap.put((String) beanClass, beanInstance);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });

            beanMap.forEach((beanClass, beanInstance) -> dependencyInject(beanInstance));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dependencyInject(Object beanInstance) {
        Stream.of(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .forEach(field -> {
                    try {
                        String fieldType = field.getGenericType().getTypeName();
                        field.setAccessible(true);
                        field.set(beanInstance, beanMap.get(fieldType));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        List<String> beanKeyList = beanMap.keySet().stream()
                .filter(beanClass -> beanClass.endsWith(beanName.substring(0, 1).toUpperCase(Locale.ROOT).concat(beanName.substring(1))))
                .collect(Collectors.toList());
        if (beanKeyList.size() > 0) {
            return beanMap.get(beanKeyList.get(0));
        }
        return null;
    }
}
