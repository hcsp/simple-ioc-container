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
            properties.load(Class.class.getResourceAsStream("/beans.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }


        properties.forEach((beanName, beanClass) -> {
            try {
                Object classInstance = Class.forName((String) beanClass).getConstructor().newInstance();
                beans.put((String) beanName, classInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        beans.forEach(this::dependencyInjection);
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }

    // 依赖注入
    public void dependencyInjection(String beanName, Object beanInstance) {
        List<Field> fieldList = getHasAutowiredAnnotationField(beanInstance);

        fieldList.forEach(field -> {
            try {
                String fieldName = field.getName();
                Object dependencyBeanInstance = getBean(fieldName);
                field.setAccessible(true);
                field.set(beanInstance, dependencyBeanInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // 从 bean 实例中过滤带有 Autowired 注解的 field
    List<Field> getHasAutowiredAnnotationField(Object beanInstance) {
        return Stream.of(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());
    }

    public Map<String, Object> beans = new HashMap<>();
}
