package com.github.hcsp.ioc;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;

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

    private final Map<String, Object> beans = new HashMap<>();

    // 启动该容器
    public void start() {
        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
            properties.forEach((beanName, beanClass) -> {
                try {
                    Class<?> klass = Class.forName((String) beanClass);
                    Object beanInstance = klass.getConstructor().newInstance();
                    beans.put((String) beanName, beanInstance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            beans.forEach((beanName, beanInstance) -> dependencyInject(beanInstance, beans));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dependencyInject(Object beanInstance, Map<String, Object> beans) {
        Field[] fields = beanInstance.getClass().getDeclaredFields();
        List<Field> fieldsToAutoWired = Stream.of(fields)
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());
        fieldsToAutoWired.forEach(field -> {
            String fieldName = field.getName();
            Object dependencyBeanInstance = beans.get(fieldName);
            try {
                field.setAccessible(true);
                field.set(beanInstance, dependencyBeanInstance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
