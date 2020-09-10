package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
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

    private HashMap<String, Object> beans = new HashMap<>();

    // 启动该容器
    public void start() {
        try {
            Properties properties = new Properties();
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
            //通过反射将properties中的对象装入容器
            properties.forEach((beanName, beanClass) -> {
                try {
                    Class klass = Class.forName((String) beanClass);
                    beans.put((String) beanName, klass.getConstructor().newInstance());
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            });
            beans.forEach(this::dependencyInject);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void dependencyInject(String beanName, Object beanInstance) {
        //获取当前对象中标有Autowired注解的字段
        List<Field> autowiredFields =
                Stream.of(beanInstance.getClass().getDeclaredFields())
                        .filter(field -> field.getAnnotation(Autowired.class) != null)
                        .collect(Collectors.toList());
        //将当前对象标有Autowired注解的字段从容器中获取对象并赋值，
        autowiredFields.forEach(field -> {
            try {
                String fieldName = field.getName();
                //从容器中获取该注解字段的对象
                Object autowiredBean = beans.get(fieldName);
                field.setAccessible(true);
                //设置依赖对象
                field.set(beanInstance, autowiredBean);
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
