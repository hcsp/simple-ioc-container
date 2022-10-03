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
    public static void main(String[] args) throws IOException {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    HashMap<String, Object> beans = new HashMap<>();

    // 启动该容器
    public void start() {
        try {
            //读取配置文件
            Properties properties = new Properties();
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));

            //创建所有bean对应的实例
            properties.forEach((beanName, beanClass) -> {
                try {
                    Class<?> klass = Class.forName((String) beanClass);
                    Object beanInstance = klass.getConstructor().newInstance();
                    beans.put((String) beanName, beanInstance);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            //寻找每个bean实例中带有注解的field
            beans.forEach((beanName, beanInstance) -> {
                List<Field> fieldsToBeAutowired = Stream.of(beanInstance.getClass().getDeclaredFields())
                        .filter(field -> field.getAnnotation(Autowired.class) != null)
                        .collect(Collectors.toList());

                //将带有注解的field添加到beanInstance中
                fieldsToBeAutowired.forEach(field -> {
                    try {
                        String fieldName = field.getName();
                        Object dependencyBeanInstance = beans.get(fieldName);
                        field.setAccessible(true);
                        field.set(beanInstance, dependencyBeanInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
