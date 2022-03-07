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

    private final HashMap<String, Object> beans = new HashMap<>();


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
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        propertiesToMap(properties);
        beans.forEach((beanName, beanInstance) -> dependencyInjection(beanInstance));
    }

    private void propertiesToMap(Properties properties) {
        properties.forEach((beanName, beanClass) -> {
            Object beanInstance;
            try {
                Class<?> klass = Class.forName((String) beanClass);
                beanInstance = klass.getConstructor().newInstance();
                beans.put((String) beanName, beanInstance);

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    private void dependencyInjection(Object beanInstance) {
        List<Field> fieldsToBeAutowired = Stream.of(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());
        fieldsToBeAutowired.forEach(field -> {
            try {
                String fieldName = field.getName();
                Object dependencyBeanInstance = beans.get(fieldName);
                field.setAccessible(true);
                field.set(beanInstance, dependencyBeanInstance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });

    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
