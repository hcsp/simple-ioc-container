package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
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

    Map<String, Object> beans;

    public MyIoCContainer() {
        beans = new HashMap<>();
    }

    public static void main(String[] args) {

        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();

    }

    //实现依赖注入
    private static void dependencyInject(String beanName, Object beanInstance, Map<String, Object> beans) {
        List<Field> fieldsToBeAutowired = Stream.of(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());

        fieldsToBeAutowired.forEach(field -> {

            try {
                String fieldName = field.getName();
                field.setAccessible(true);
                Object dependcyBeanInstance = beans.get(fieldName);
                //核心一步，为引用 赋予了 已经创造好的实例
                field.set(beanInstance, dependcyBeanInstance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

    }

    // 启动该容器
    public void start() {


        //目标：根据人们填写的properties和注解完成依赖注入

        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Map<String, Object> beans = new HashMap<>();

        properties.forEach((beanName, beanClass) -> {
            try {
                //根据配置文件中的value来获取到class类，并实例化顶替掉values
                Class<?> klass = Class.forName((String) beanClass);
                Object beanInstance = klass.getConstructor().newInstance();
                beans.put((String) beanName, beanInstance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        //至此，HashMap中存入了 参数名 --> 对应实例 这样的关系
        //以此来进行依赖注入
        beans.forEach((beanName, beanInstance) -> dependencyInject(beanName, beanInstance, beans));


    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
