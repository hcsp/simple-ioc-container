package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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

    private final Map<String, Object> beanContainer = new HashMap<>();

    private static final Properties beanDefinition = new Properties();

    private static final Map<String, Class<?>> beanDefinitionMap = new HashMap<>();

    static {
        InputStream resourceAsStream = MyIoCContainer.class.getClassLoader().getResourceAsStream("beans.properties");
        try {
            beanDefinition.load(resourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        beanDefinition.forEach((k, v) -> {
            try {
                beanDefinitionMap.put((String) k, Class.forName((String) v));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e.getMessage());
            }
        });
    }

    // 启动该容器
    public void start() {
        createBeanInstances();
        processDependency();
    }

    private void createBeanInstances() {
        beanDefinitionMap.forEach((beanName, beanClass) -> {
            try {
                beanContainer.put(beanName, beanClass.newInstance());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        });
    }

    private void processDependency() {
        beanDefinitionMap.forEach((beanName, beanClass) -> {
            for (Field field : beanClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    Object bean = getBean(field.getType());
                    if (null == bean) {
                        throw new RuntimeException("none candidates for " + field.getType());
                    }
                    ensureAccessible(field);
                    try {
                        field.set(beanContainer.get(beanName), bean);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }
            }
        });
    }

    private void ensureAccessible(AccessibleObject accessibleObject) {
        accessibleObject.setAccessible(true);
    }

    private Object getBean(Class<?> type) {
        for (Object bean : beanContainer.values()) {
            if (type.isAssignableFrom(bean.getClass())) {
                return bean;
            }
        }
        return null;
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beanContainer.get(beanName);
    }
}
