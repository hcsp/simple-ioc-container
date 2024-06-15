package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
        try {
            prepareBeans();
            beans.forEach((beanName, beanInstance) -> {
                Class<?> cls = beanInstance.getClass();
                List<Field> autowiredFields = Stream.of(cls.getDeclaredFields())
                        .filter(field -> field.getAnnotation(Autowired.class) != null)
                        .collect(Collectors.toList());
                autowiredFields.forEach(field -> dependencyInject(field, beanInstance));
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }

    private void prepareBeans() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Properties properties = new Properties();
        properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
        for (Object beanName : properties.keySet()) {
            Object beanClassName = properties.get(beanName);
            Class<?> cls = Class.forName((String) beanClassName);
            beans.put((String) beanName, cls.getConstructor().newInstance());
        }
    }

    private void dependencyInject(Field field, Object target) {
        String fieldName = field.getName();
        field.setAccessible(true);
        try {
            field.set(target, beans.get(fieldName));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
//    private void
}
