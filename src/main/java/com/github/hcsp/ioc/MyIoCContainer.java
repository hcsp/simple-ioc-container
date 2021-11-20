package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {
    private static final HashMap<String, Object> beans = new HashMap<>();

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
        Properties properties = null;
        try {
            properties = loadConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loadBeans(properties);
        beans.forEach((beanName, beanInstance) -> dependencyInject(beanInstance, getAutowiredFields(beanInstance)));
    }

    private void dependencyInject(Object beanInstance, List<Field> autowiredFieldList) {
        autowiredFieldList.forEach(filed -> {
            Object dependencyInstance = beans.get(filed.getName());
            try {
                filed.setAccessible(true);
                filed.set(beanInstance, dependencyInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private List<Field> getAutowiredFields(Object beanInstance) {
        Field[] fields = beanInstance.getClass().getDeclaredFields();
        return Stream.of(fields)
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());
    }

    private void loadBeans(Properties properties) {
        properties.forEach((beanName, beanClass) -> {
            try {
                Class<?> clazz = Class.forName((String) beanClass);
                Object newInstance = clazz.getConstructor().newInstance();
                beans.put((String) beanName, newInstance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Properties loadConfig() throws IOException {
        Properties properties = new Properties();
        properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
        return properties;
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
