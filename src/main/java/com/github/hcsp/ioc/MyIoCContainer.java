package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.Stream;

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

    HashMap<String, Object> beansMap = new HashMap();

    // 启动该容器
    public void start() {
        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        properties.forEach((beanName, beanClass) -> {
            try {
                Class klass = Class.forName((String) beanClass);
                Object beanInstance = klass.getConstructor().newInstance();
                beansMap.put((String) beanName, beanInstance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        beansMap.forEach((name, instance) -> {
            dependencyInject(instance, beansMap);
        });
    }

    private static void dependencyInject(Object beanInstance, HashMap beansMap) {
        Stream.of(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .forEach(field -> {
                    String fieldName = field.getName();
                    Object dependencyBeanInstance = beansMap.get(fieldName);
                    field.setAccessible(true);
                    try {
                        field.set(beanInstance, dependencyBeanInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beansMap.get(beanName);
    }
}
