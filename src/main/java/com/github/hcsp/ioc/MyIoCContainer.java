package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class MyIoCContainer {
    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入
    public static void main(String[] args) throws IOException {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
        System.out.println();
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

        this.init(properties);
    }

    private void init(Properties properties) {
        properties.forEach((beanName, beanClass) -> {
            try {
                Class<?> clazz = Class.forName((String) beanClass);
                Object beanInstance = clazz.getConstructor().newInstance();
                beans.put((String) beanName, beanInstance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });

        beans.forEach((beanName, beanInstance) -> {
            injectDependency(beanName, beanInstance, beans);
        });
    }

    private void injectDependency(String beanName, Object beanInstance, Map<String, Object> beans) {
        List<Field> fieldsToBeAutowired = Arrays.stream(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());

        fieldsToBeAutowired.forEach(field -> {
            String fieldName = field.getName();
            Object dependencyBeanInstance = this.getBean(fieldName);
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
        return beans.get(beanName);
    }
}
