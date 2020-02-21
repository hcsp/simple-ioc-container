package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
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

    Map<String, Object> beans = new HashMap<>();

    // 启动该容器
    public void start() {
        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //往beans加入实例对象
        //beanName用的是 属性名而不是类型名称，看properties就知道了
        properties.forEach((beanName, beanClass) -> {
            try {
                Class<?> klass = Class.forName((String) beanClass);
                Object beanInstance = klass.getDeclaredConstructor().newInstance();
                beans.put((String) beanName, beanInstance);
            } catch (Exception e) {
                throw new RuntimeException();
            }
        });
        beans.forEach(this::dependencyInject);

    }

    private void dependencyInject(String beanName, Object beanInstance) {
        List<Field> dependencyInject = Stream.of(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());

        dependencyInject.forEach(field -> {
            try {
                String fieldName = field.getName();
                Object dependencyBeanInstance = beans.get(fieldName);
                field.setAccessible(true);
                field.set(beanInstance, dependencyBeanInstance);
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
