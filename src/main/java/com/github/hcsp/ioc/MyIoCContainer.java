package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {
    private HashMap<String, Object> beans;

    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入
    public static void main(String[] args) {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    /**
     * 从.properties文件中获取信息
     *
     * @return 一个properties类
     */
    public static Properties getAndLoadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
        } catch (IOException e) {
            throw new RuntimeException("properties路径有误", e);
        }
        return properties;
    }


    /**
     * 遍历.properties文件中的内容，生成value的实例，将<key,value的实例>逐个映射到HashMap中
     *
     * @param properties 加载后的properties实例
     * @return 映射后的HashMap容器
     */
    public static HashMap<String, Object> newInstance(Properties properties) {
        HashMap<String, Object> hashMap = new HashMap<>();
        properties.forEach((beanName, beanInstance) -> {
            try {
                Class<?> klass = Class.forName((String) beanInstance);
                Object newBeanInstance = klass.getConstructor().newInstance();
                hashMap.put((String) beanName, newBeanInstance);
            } catch (Exception e) {
                throw new RuntimeException();
            }
        });
        return hashMap;
    }

    /**
     * 为带有@Autowired标签的成员变量设置依赖关系
     *
     * @param beanName     null
     * @param beanInstance 被依赖的类的全限定类名
     * @param beans        类与类名之间的映射关系
     */
    @SuppressWarnings("unused")
    public static void dependencyInstance(String beanName, Object beanInstance, HashMap<String, Object> beans) {
        List<Field> fields = Stream.of(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());
        fields.forEach(field -> {
            String filedName = field.getName();
            Object filedInstance = beans.get(filedName);
            field.setAccessible(true);
            try {
                field.set(beanInstance, filedInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // 启动该容器
    public void start() {
        Properties properties = getAndLoadProperties();
        beans = newInstance(properties);
        beans.forEach((name, instance) -> {
            dependencyInstance(name, instance, beans);
        });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
