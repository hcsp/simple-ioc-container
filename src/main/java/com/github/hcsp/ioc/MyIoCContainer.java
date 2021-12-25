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
    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入
    private HashMap<String, Object> beans = new HashMap<>();

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
            properties.forEach((beanName, beanClass) -> {
                try {
                    Class<?> klass =  Class.forName((String) beanClass);
                    Object beanClassInstance = klass.getConstructor().newInstance();
                    this.beans.put((String) beanName, beanClassInstance);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        beans.forEach((beanName, beanClassInstance) -> dependencyBeans(beanClassInstance, beans));
    }

    private void dependencyBeans(Object beanClassInstance, HashMap<String, Object> beans) {
        List<Field> fieldsNeedWired = Stream.of(beanClassInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());

        fieldsNeedWired.forEach(field -> {
            try {
                String fieldName = field.getName();
                field.setAccessible(true);
                Object dependencyObject = this.beans.get(fieldName);
                field.set(beanClassInstance, dependencyObject);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return this.beans.get(beanName);
    }
}