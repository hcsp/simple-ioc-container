package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {
    private ConcurrentMap<String, Object> container = new ConcurrentHashMap<>();

    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入
    public static void main(String[] args) {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    private void dependenceInject(Object bean) {
        List<Field> autoWiredFiled = Stream.of(bean.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());
        autoWiredFiled.forEach(field -> {
            try {
                field.setAccessible(true);
                field.set(bean, container.get(field.getName()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void readConfigAndInitialBeans(String configPath) throws IOException {
        Properties properties = new Properties();
        properties.load(MyIoCContainer.class.getResourceAsStream(configPath));
        properties.forEach((beanName, beanClass) -> {
            try {
                Class<?> clazz = Class.forName((String) beanClass);
                Object instance = clazz.getConstructor().newInstance();
                container.put((String) beanName, instance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // 启动该容器
    public void start() {
        try {
            readConfigAndInitialBeans("/beans.properties");
            container.values().forEach(this::dependenceInject);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return container.get(beanName);
    }
}
