package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

public class MyIoCContainer {
    private HashMap<String, Object> beanPool = new HashMap<>();

    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入
    public static void main(String[] args) {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService" );
        orderService.createOrder();
    }

    // 启动该容器
    public void start() {
        final Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/beans.properties" ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        setOriginBeanToBeanPool(properties);
        beanPool.forEach(this::DependcyInject);

    }

    private void DependcyInject(String s, Object o) {
        final Class<?> aClass = o.getClass();
        final Field[] declaredFields = aClass.getDeclaredFields();
        Arrays.stream(declaredFields).filter(field -> field.getAnnotation(Autowired.class) != null)
                .forEach(field -> setWiredFiled(o, field));
    }

    private void setWiredFiled(Object target, Field field) {
        final String fieldName = field.getName();
        field.setAccessible(true);
        try {
            field.set(target, beanPool.get(fieldName));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setOriginBeanToBeanPool(Properties properties) {
        properties.forEach(this::setBean);
    }

    private void setBean(Object key, Object value) {
        try {
            final Class<?> aClass = Class.forName((String) value);
            beanPool.put((String) key, aClass.getConstructor().newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beanPool.get(beanName);
    }
}
