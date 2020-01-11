package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {
    private final Map<String, Object> beanFactory = new HashMap<>();

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
        Properties properties = new Properties();
        loadBeanProperty(properties);

        beanFactory.forEach((beanName, beanInstance) -> {
            Field[] declaredFields = beanInstance.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                List<Field> fieldsTobeAutowired = getFieldsFilterByAnnotation(field, Autowired.class);

                fieldsTobeAutowired.forEach(fieldTobeAutowired ->
                        dependencyInjection(beanInstance, fieldTobeAutowired)
                );
            }
        });
    }

    private void dependencyInjection(Object beanInstance, Field fieldTobeAutowired) {
        Object dependencyObject = beanFactory.get(fieldTobeAutowired.getName());
        try {
            fieldTobeAutowired.setAccessible(true);
            fieldTobeAutowired.set(beanInstance, dependencyObject);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("属性访问权限异常");
        }
    }

    private <T> List<Field> getFieldsFilterByAnnotation(Field field, Class<? extends Annotation> clazz) {
        return Stream.of(field)
                .filter(field1 -> field1.getAnnotation(clazz) != null)
                .collect(Collectors.toList());
    }

    private void loadBeanProperty(Properties properties) {
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        properties.forEach((beanName, beanClass) -> {
            try {
                Class<?> clazz = Class.forName((String) beanClass);
                beanFactory.put((String) beanName, clazz.getConstructor().newInstance());
            } catch (Exception e) {
                throw new RuntimeException("从配置文件加载类时出现错误");
            }
        });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beanFactory.get(beanName);
    }
}
