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
    private Map<String, Object> beans = new HashMap<>();
    private static final String BEAN_PROPERTIES = "/beans.properties";

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
        try {
            loadBeanFromProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }

    private void loadBeanFromProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(MyIoCContainer.class.getResourceAsStream(BEAN_PROPERTIES));

        properties.forEach((beanName, beanClass) -> {
            try {
                Class klass = Class.forName((String) beanClass);
                Object beanInstance = klass.getConstructor().newInstance();
                beans.put((String) beanName, beanInstance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        beans.forEach((beanName, beanInstance) -> dependencyInject(beanName, beanInstance, beans));
    }

    private void dependencyInject(String beanName, Object beanInstance, Map<String, Object> beans) {
        List<Field> fieldsToBeAutowired = Stream.of(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());

        for (Field field : fieldsToBeAutowired) {
            String fieldName = field.getName();
            field.setAccessible(true);
            Object dependencyBeanInstance = beans.get(fieldName);
            try {
                field.set(beanInstance, dependencyBeanInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

    }

    //
    // // 构造器注入
    // public void constructorDependencyInject(String beanName, String beanClass) throws ClassNotFoundException {
    //     Class klass = Class.forName(beanClass);
    //     List<Constructor> constructorsToBeAutoWired = Stream.of(klass.getDeclaredConstructors())
    //             .filter(constructor -> constructor.getAnnotation(Autowired.class) != null)
    //             .collect(Collectors.toList());
    //
    //     for (Constructor constructor : constructorsToBeAutoWired) {
    //         Stream.of(constructor.getParameterTypes())
    //                 .filter();
    //     }
    //
    //     constructorsToBeAutoWired.stream()
    //             .flatMap(constructor -> Stream.of(constructor.getParameterTypes()))
    //             .map(klass -> beans.get(klass.get)
    //
    //
    // }
}
