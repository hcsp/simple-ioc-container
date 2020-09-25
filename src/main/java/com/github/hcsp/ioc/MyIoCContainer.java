package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MyIoCContainer {
    private Map<String, Object> beanPool;

    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入
    public static void main(String[] args) {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    // 启动该容器
    public void start() {
        Properties beanDef = new Properties();
        try {
            beanDef.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Init bean pool
        if (!beanDef.isEmpty()) {
            beanPool = new HashMap<>();

            // Add instances to bean pool
            beanDef.forEach((beanName, beanId) -> {
                try {
                    Object instance = Class.forName((String) beanId).getConstructor().newInstance();
                    beanPool.put((String) beanName, instance);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }

        // Dependency inject to each bean
        dependenciesInject(beanPool);
    }

    private void dependenciesInject(Map<String, Object> beanPool) {
        beanPool.forEach((beanName, bean) -> Arrays.stream(bean.getClass().getDeclaredFields())
                .filter(field -> field.getDeclaredAnnotation(Autowired.class) != null)
                .forEach(field -> dependencyInject(bean, field))
        );
    }

    private void dependencyInject(Object bean, Field field) {
        field.setAccessible(true);
        try {
            field.set(bean, beanPool.get(field.getName()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beanPool.get(beanName);
    }
}
