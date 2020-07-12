package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MyIoCContainer {
    private HashMap<String, Object> beans;

    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入
    public static void main(String[] args) {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        System.out.println(container);
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    // 启动该容器
    public void start() {
        beans = new HashMap<>();
        Properties properties = new Properties();
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String beanName = (String) entry.getKey();
                String beanClass = (String) entry.getValue();
                Class<?> klass = Class.forName(beanClass);

                beans.put(beanName, klass.getConstructor().newInstance());
            }

            beans.forEach(this::autoWiredFieldBean);
        } catch (IOException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private void autoWiredFieldBean(String beanName, Object bean) {
        Field[] fields = bean.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.getAnnotation(Autowired.class) != null) {
                Object autoWiredBean = beans.get(field.getName());
                field.setAccessible(true);
                try {
                    field.set(bean, autoWiredBean);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
