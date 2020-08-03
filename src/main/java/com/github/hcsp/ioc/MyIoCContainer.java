package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

public class MyIoCContainer {
    private Properties properties;
    private HashMap<String, Object> beans;

    public MyIoCContainer() {
        properties = new Properties();
        try {
            properties.load(ClassLoader.getSystemResourceAsStream("beans.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
        beans = new HashMap<>();
        Set<Object> keys = properties.keySet();
        for (Object key : keys) {
            try {
                Class<?> aClass = getClass().getClassLoader().loadClass((String) properties.get(key));
                Object newInstance = aClass.newInstance();
                beans.put(key.toString(), newInstance);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        scanNotefromBeans();
    }

    private void scanNotefromBeans() {
        beans.forEach((name, bean) -> {
            Arrays.stream(bean.getClass().getDeclaredFields())
                    .filter(field -> field.getAnnotation(Autowired.class) != null)
                    .forEach(field -> {
                        if (beans.get(field.getName()) != null) {
                            field.setAccessible(true);
                            try {
                                field.set(bean, beans.get(field.getName()));
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
        });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
