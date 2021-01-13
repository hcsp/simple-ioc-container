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
    private final Map<String, Object> beans = new HashMap<>();
    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入
    public static void main(String[] args) {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = null;
        orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();

    }

    // 启动该容器
    public void start() {
        //读取配置
        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
            //遍历序列化配置中的类
            properties.forEach((beanKey, beanClass) -> {
                beans.put((String) beanKey, serialize((String) beanClass));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.dependencyInjection();
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }

    public Object serialize(String beanName) {
        Object beanObject = null;
        try {
            Class kclass = Class.forName(beanName);
            beanObject = kclass.getConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return beanObject;
    }

    private void dependencyInjection() {
        //装配依赖
        beans.forEach((beanInitKey, beanInitObject) -> {
            //扫描托管的类 getDeclaredFields方法 获取所有修饰符的成员属性 并且获取从中带有Autowired注解的属性
            List<Field> fields = Stream.of(beanInitObject.getClass().getDeclaredFields())
                    .filter(field -> field.getAnnotation(Autowired.class) != null)
                    .collect(Collectors.toList());
            //要注入的类
            fields.forEach(field -> {
                //获取需要注入的依赖名
                String fieldsName = field.getName();
                //获取要注入的依赖
                Object depend = beans.get(fieldsName);
                //塞回实例化好的依赖 注入
                try {
                    field.setAccessible(true);
                    field.set(beanInitObject, depend);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        });

    }


}
