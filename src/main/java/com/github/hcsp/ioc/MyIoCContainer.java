package com.github.hcsp.ioc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;

public class MyIoCContainer {
    private Properties properties;
    private HashMap<String, Object> beans;
    public MyIoCContainer() {
        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("beans.properties"));
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
        Set<Map.Entry<Object, Object>> entries = properties.entrySet();
        for (Map.Entry<Object, Object> entity: entries) {
            try {
                Class<?> aClass = getClass().getClassLoader().loadClass((String) entity.getValue());
                Object instance = aClass.newInstance();
                beans.put((String) entity.getKey(), instance);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        startWired();
    }

    public void startWired() {
        beans.forEach((name, bean) -> {
            Stream.of(bean.getClass().getDeclaredFields())
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
