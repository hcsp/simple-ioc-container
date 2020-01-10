package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {
    private HashMap map = new HashMap();
    Properties properties;

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
            properties = PropertiesLoaderUtils.loadProperties(new InputStreamResource(getClass().getClassLoader().getResourceAsStream("beans.properties")));
            properties.entrySet().forEach(entry -> {
                if (!map.containsKey(entry.getKey())) {
                    try {
                        map.put(entry.getKey(), injectionAccountValue((String) entry.getValue()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private Object injectionAccountValue(String className) throws IllegalAccessException, InstantiationException {
        Class kCla = null;
        try {
            kCla = Class.forName(className);
            List<Field> fields = getFields(kCla);
            injectionFields(fields);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return kCla.newInstance();
    }

    private List<Field> getFields(Class kCla) {
        return Stream.of(kCla.getDeclaredFields()).filter(field -> field.getAnnotation(Autowired.class) != null).collect(Collectors.toList());
    }

    private void injectionFields(List<Field> fields) {
        fields.stream().forEach(field -> {
            String fileName = field.getName();
            String fieldClassName = properties.getProperty(fileName);
            try {
                Class<?> aClass = Class.forName(fieldClassName);
                injectionFields(getFields(aClass));
                field.setAccessible(true);
                field.set(fileName,aClass.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return map.get(beanName);
    }
}
