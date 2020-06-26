package com.github.hcsp.ioc;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;

public class MyIoCContainer {
    /*
     * 实现一个简单的IoC容器，使得： // 1. 从beans.properties里加载bean定义 // 2.
     * 自动扫描bean中的@Autowired注解并完成依赖注入
     */
    public static void main(String[] args) {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    private Map<String, Object> beans;
    /*
     * 启动该容器
     */
    public void start() {
        Properties properties = new Properties();
        FileInputStream propertiesInputStream;
        try {
            propertiesInputStream = new FileInputStream("./src/main/resources/beans.properties");
            properties.load(propertiesInputStream);
            properties.forEach((k,v)->beans.add(k,Class.forName(p).getDeclaredConstructor().newInstance()));
            propertiesInputStream.close();
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * 从容器中获取一个bean
     */
    public Object getBean(String beanName) {
        Properties beans = new Properties();
        Object result = null;
        FileInputStream properties;
        try {
            properties = new FileInputStream("./src/main/resources/beans.properties");
            beans.load(properties);
            String clazzString = beans.getProperty(beanName);
            result = Class.forName(clazzString).getDeclaredConstructor().newInstance();
            properties.close();
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }
}
