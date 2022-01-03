package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class MyIoCContainer {
    private final Properties properties = new Properties();
    private final Map<String, Object> produceInstanceMap = new HashMap<>();

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
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
            properties.forEach((beanName, beanClassName) -> {
                try {
                    Class<?> clazz = Class.forName((String) beanClassName);
                    Object beanInstance = clazz.getConstructor().newInstance();
                    produceInstanceMap.put((String) beanName, beanInstance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        // map 中 { beanname , instance}
        // 对于每个实例  筛选出带有注解的fields
        for (Map.Entry<String, Object> entry : produceInstanceMap.entrySet()) {
            selectEveryFields(entry.getValue(), produceInstanceMap);
        }
        //produceInstanceMap.forEach(beanName,beanInstance->selectEveryFields(beanInstance,beanName,produceInstanceMap));
        return produceInstanceMap.get(beanName);
    }

    private static void selectEveryFields(Object beanInstance, Map<String, Object> produceInstanceMap) {
        List<Field> fielsToBeAutowired = Arrays.stream(beanInstance.getClass()
                        .getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());

        fielsToBeAutowired.forEach(field -> {
            try {
                //找出每个带有Autowired注解的filed名字
                String fieldName = field.getName();
                //找个对应key的value实例
                Object dependencyObject = produceInstanceMap.get(fieldName);
                field.setAccessible(true);
                //改变beanInstance实例上field的值
                field.set(beanInstance, dependencyObject);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }
}
