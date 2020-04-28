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
    private HashMap<String, Object> beans;

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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        beans = newInstance(properties);
        beans.forEach((name, instance) -> {
            dependencyInstance(name, instance, beans);
        });
    }

    private HashMap<String, Object> newInstance(Properties properties) {
        HashMap<String, Object> beans = new HashMap<>();
        properties.forEach((beanName, beanInstance) -> {
            try {
                Class<?> klass = Class.forName((String) beanInstance);
                Object newBeanInstance = klass.getConstructor().newInstance();
                beans.put((String) beanName, newBeanInstance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return beans;
    }

    public static void dependencyInstance(String beanName, Object beanInstance, HashMap<String, Object> beans) {
        List<Field> fields = Stream.of(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());

        fields.forEach(field -> {
            String filedName = field.getName();
            Object filedInstance = beans.get(filedName);
            field.setAccessible(true);
            try {
                field.set(beanInstance, filedInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
