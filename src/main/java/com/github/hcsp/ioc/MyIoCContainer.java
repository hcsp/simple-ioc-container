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

    public static Map<String, Object> beans = new HashMap<>();

    public static void main(String[] args) throws IOException {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    // 启动该容器
    public void start() {

        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));    //加载Bean定义
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        properties.forEach((beanName, beanClass) -> {
            try {
                Class klass = Class.forName((String) beanClass);
                Object beanInstance = klass.getConstructor().newInstance(); //实例化
                beans.put((String) beanName, beanInstance);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

        });  //bean 的初始化，通过反射实例化Bean

        beans.forEach((beanName, beanInstance) -> dependencyInject(beanName, beanInstance, beans));
    }

    private static void dependencyInject(String beanName, Object beanInstance, Map<String, Object> beans) {
        List<Field> fieldsToBeAutowired = Stream.of(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());
        fieldsToBeAutowired.forEach(field -> {
            try {
                String fieldName = field.getName();
                Object dependencyBeanInstance = beans.get(fieldName);  //通过反射找到依赖
                field.setAccessible(true); //默认情况下反射是不能够直接修改private成员的
                field.set(beanInstance, dependencyBeanInstance); // 注入
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });

    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
