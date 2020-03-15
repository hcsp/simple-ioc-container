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
    Properties properties = new Properties();
    Map beanMap = new HashMap<String, Object>();

    public static void main(String[] args) throws IOException {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    // 启动该容器
    public void start() {
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        properties.forEach((beanKlassName, beanKlass) -> {
            try {
                Class klass = Class.forName((String) beanKlass);
                beanMap.put(beanKlassName,
                        klass.getConstructor().newInstance());
            } catch (Exception e) {
                throw new RuntimeException();
            }
        });
        dependencyInjection();
    }

    private void dependencyInjection() {
        beanMap.forEach((beanName, beanInstance) -> {
            List<Field> dependencyBeanList;
            Field[] fields = beanInstance.getClass().getDeclaredFields();
            dependencyBeanList = Stream.of(fields).
                    filter((field) -> field.getAnnotation(Autowired.class) != null).
                    collect(Collectors.toList());
            dependencyBeanList.forEach(field -> {
                try {
                    field.setAccessible(true);
                    field.set(beanInstance, beanMap.get(field.getName()));
                } catch (Exception e) {
                    throw new RuntimeException();
                }
            });
        });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        Object bean = beanMap.get(beanName);
        return bean;
    }
}
