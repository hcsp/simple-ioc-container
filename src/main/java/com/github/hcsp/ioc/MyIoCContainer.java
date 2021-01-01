package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public class MyIoCContainer {
    private final Map<String, Object> beans = new HashMap<>();

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
            properties.load(this.getClass().getResourceAsStream("/beans.properties"));
            properties.forEach((beansName, bean) -> beans.put((String) beansName, newClassInstance((String) bean)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        dependencyInject();
    }

    private void dependencyInject() {
        beans.forEach((beansName, bean) -> {
            Field[] declaredFields = bean.getClass().getDeclaredFields();
            Stream.of(declaredFields)
                    .filter(filed -> filed.getAnnotation(Autowired.class) != null)
                    .forEach((filed) -> {
                        filed.setAccessible(true);
                        try {
                            filed.set(bean, beans.get(filed.getName()));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });
        });
    }

    private Object newClassInstance(String bean) {
        Object instance = null;
        try {
            Class<?> clazz = Class.forName(bean);
            instance = clazz.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        if (instance != null) {
            return instance;
        } else {
            throw new RuntimeException(bean + "没有正确引用");
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
