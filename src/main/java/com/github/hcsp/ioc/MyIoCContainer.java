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
    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入

    Map<String, Object> beans = new HashMap<>();

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

        beans = new HashMap<>();

        properties.forEach((beanName, beanClass) -> {
            try {
                Class klass = Class.forName((String) beanClass);
                Object beanInstance = klass.getConstructor().newInstance();
                beans.put((String) beanName, beanInstance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // 从容器中获取一个bean
    public Object getBean(String spBeanName) {
        beans.forEach((beanName, beanInstance) -> {
            List<Field> fieldWithoutWired = Stream.of(beanInstance.getClass().getDeclaredFields())
                    .filter(field -> field.getAnnotation(Autowired.class) != null)
                    .collect(Collectors.toList());
            fieldWithoutWired.forEach(field -> {
                String filedName = field.getName();
                Object dependencyBeanInstance = beans.get(filedName);
                field.setAccessible(true);
                try {
                    field.set(beanInstance, dependencyBeanInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        return beans.get(spBeanName);
    }
}
