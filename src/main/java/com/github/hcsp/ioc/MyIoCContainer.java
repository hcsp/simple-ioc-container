package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MyIoCContainer {

    Map<String, Object> context = new HashMap<>();

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
        //获取配置文件
        InputStream inputStream = MyIoCContainer.class.getClassLoader().getResourceAsStream("beans.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //根据配置注入容器
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            context.put(entry.getKey().toString(), getObject((String) entry.getValue()));
        }

        //注入依赖
        for (Map.Entry<String, Object> bean : context.entrySet()) {
            Field[] declaredFields = bean.getValue().getClass().getDeclaredFields();
            Arrays.stream(declaredFields).forEach(field -> {
                field.setAccessible(true);
                try {
                    if (field.getAnnotation(Autowired.class) != null) {
                        field.set(bean.getValue(), context.get(field.getName()));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return context.get(beanName);
    }

    public Object getObject(String clazzPath) {
        Class clazz = null;
        try {
            clazz = Class.forName(clazzPath);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
