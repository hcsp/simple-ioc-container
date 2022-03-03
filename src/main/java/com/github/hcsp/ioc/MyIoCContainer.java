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
    private final Map<String, Object>beans =new HashMap<>();

    public static void main(String[] args) {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
//        OrderService orderService = (OrderService) container.getBean("orderService");
//        orderService.createOrder();
    }

    // 启动该容器
    public void start() {
        Properties properties =new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        properties.forEach((className,pagName) -> {
            try {
                Class<?> aClass = Class.forName((String) pagName);
                Object instance = aClass.getConstructor().newInstance();
                beans.put((String) className,instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        beans.forEach((className,instance) -> { dependencyInject(instance);});


    }

    private void dependencyInject(Object instance) {
        List<Field> fieldList = Stream.of(instance.getClass().getDeclaredFields())
                .filter(i -> i.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());
        fieldList.forEach(field->{
            try {
                String fieldName=field.getName();
                Object dependencyInstance = beans.get(fieldName);
                field.setAccessible(true);
                field.set(instance,dependencyInstance);
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
