package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {
    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入
    private Map<String, Object> beans = new HashMap<>();

    public static void main(String[] args) {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    private void getInstance() {
        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));

            properties.forEach((beanName, beanClass) -> {
                try {
                    Class<?> klass = Class.forName((String) beanClass);

                    Object beanInstance = klass.getConstructor().newInstance();

                    beans.put((String) beanName, beanInstance);
                } catch (ClassNotFoundException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dependencyInjection() {
        beans.forEach((beanName, beanInstance) -> {
            List<Field> fields = Stream.of(beanInstance.getClass().getDeclaredFields())
                    .filter(field -> field.getAnnotation(Autowired.class) != null)
                    .collect(Collectors.toList());

            fields.forEach(field -> {
                String fieldName = field.getName();
                Object object = beans.get(fieldName);

                field.setAccessible(true);
                try {
                    field.set(beanInstance, object);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    // 启动该容器
    public void start() {
        getInstance();

        dependencyInjection();
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
