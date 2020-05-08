package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class MyIoCContainer {

    private Map<String, Object> beanMap = new ConcurrentHashMap<>();

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
        InputStream beansProperties = MyIoCContainer.class.getResourceAsStream("/beans.properties");
        Properties properties = new Properties();
        try {
            properties.load(beansProperties);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orderDaoName = properties.getProperty("orderDao");
        String userDaoName = properties.getProperty("userDao");
        String userServiceName = properties.getProperty("userService");
        String orderServiceName = properties.getProperty("orderService");

        //反射调用构造方法
        try {
            Class<?> orderDaoClass = Class.forName(orderDaoName);
            OrderDao orderDao = (OrderDao) orderDaoClass.getDeclaredConstructor().newInstance();
            beanMap.put("orderDao", orderDao);

            Class<?> userDaoClass = Class.forName(userDaoName);
            UserDao userDao = (UserDao) userDaoClass.getDeclaredConstructor().newInstance();
            beanMap.put("userDao", userDao);

            Class<?> userServiceClass = Class.forName(userServiceName);
            UserService userService = (UserService) userServiceClass.getDeclaredConstructor().newInstance();
            beanMap.put("userService", userService);

            Class<?> orderServiceClass = Class.forName(orderServiceName);
            OrderService orderService = (OrderService) orderServiceClass.getDeclaredConstructor().newInstance();
            beanMap.put("orderService", orderService);

            //Field注入
            autowiredField(orderDaoClass);
            autowiredField(userDaoClass);
            autowiredField(userServiceClass);
            autowiredField(orderServiceClass);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void autowiredField(Class<?> klz) throws IllegalAccessException {
        Field[] orderFields = klz.getDeclaredFields();
        String beanSourceName = klz.getSimpleName();
        String indexChar = beanSourceName.substring(0, 1).toLowerCase();
        String beanName = indexChar + beanSourceName.substring(1, beanSourceName.length());
        for (Field field : orderFields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                //使得private field可以被注入value
                field.setAccessible(true);
                field.set(getBean(beanName), getBean(field.getName()));
            }
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beanMap.get(beanName);
    }
}
