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
    public static void main(String[] args){
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    private Map<String, Object> beanInstanceMap;

    // 启动该容器
    public void start(){
        beanInstanceMap = new HashMap<>();
        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        properties.forEach((beanName, className) -> {
            try {
                Class<?> klass = Class.forName((String) className);
                Object beanInstance = klass.getConstructor().newInstance();
                beanInstanceMap.put((String) beanName, beanInstance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        beanInstanceMap.forEach((beanName, beanInstance) -> injectDependency(beanName, beanInstance, beanInstanceMap));
    }

    private void injectDependency(String beanName, Object beanInstance, Map<String, Object> beanInstanceMap){
        List<Field> autoWiredFields = Stream.of(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());

        autoWiredFields.forEach(field -> {
            String fieldName = field.getName();
            try {
                field.setAccessible(true);
                field.set(beanInstance, beanInstanceMap.get(fieldName));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beanInstanceMap.get(beanName);
    }
}
