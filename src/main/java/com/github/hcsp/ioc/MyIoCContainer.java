package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MyIoCContainer {
    private Map<String, Object> beanMap;

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
        beanMap = new HashMap<>();
        Properties properties = new Properties();
        try {
            // 读取properties文件，创建类并添加到beanMap
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String beanName = (String) entry.getKey();
                String beanClass = (String) entry.getValue();
                Class<?> klass = Class.forName(beanClass);
                beanMap.put(beanName, klass.getConstructor().newInstance());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 注入
        beanMap.forEach(this::autoWiredFieldBean);


    }

    private void autoWiredFieldBean(String beanName, Object bean) {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Autowired.class) != null) {
                Object autoWiredBean = beanMap.get(field.getName());
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
        return beanMap.get(beanName);
    }
}
