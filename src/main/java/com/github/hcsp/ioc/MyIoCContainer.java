package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

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
    private final Map<String, Object> beans = new HashMap<>();

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
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                Object beanName = entry.getKey();
                Object beanClassName = entry.getValue();
                Object instance = null;
                instance = Class.forName((String) beanClassName).getConstructor().newInstance();
                beans.put((String) beanName, instance);
            }

            beans.forEach((beanName, beanInstance) -> dependencyInjection(beanName, beanInstance, beans));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void dependencyInjection(String beanName, Object beanInstance, Map<String, Object> beans) {
        List<Field> fieldsToBeAutowired = Stream.of(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Autowired.class))
                .collect(Collectors.toList());
        fieldsToBeAutowired.forEach(field -> {
            Object dependencyBeanInstance = getBean(field.getName());
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
