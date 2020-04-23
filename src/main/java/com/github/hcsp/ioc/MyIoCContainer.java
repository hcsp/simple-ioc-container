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
    static Map<String, Object> map = new HashMap<String, Object>();

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
        Properties pro = new Properties();
        try {
            pro.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pro.forEach((beanName, beanPath) -> {
            try {
                Object bean = Class.forName((String) beanPath).getConstructor().newInstance();
                map.put((String) beanName, bean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        map.forEach((beanName, bean) -> dependencyInject((String) beanName, bean, map));
    }

    // 从容器中获取一个bean
    public Object getBean(String name) {
        return map.get(name);
    }

    public static void dependencyInject(String beanName, Object bean, Map<String, Object> map) {
        List<Field> declaredFields = Stream.of(bean.getClass().getDeclaredFields())
                .filter(filed -> filed.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());
        declaredFields.forEach(field -> {
            try {
                String filedName = field.getName();
                Object dependencyBean = map.get(filedName);
                field.setAccessible(true);
                field.set(bean, dependencyBean);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
