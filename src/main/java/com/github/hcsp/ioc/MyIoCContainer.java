package com.github.hcsp.ioc;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;

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

    public Map<String, Object> beans = new HashMap<>();

    /*
     * 启动该容器
     */
    public void start() {
        Properties properties = new Properties();
        FileInputStream propertiesInputStream = null;
        try {
            propertiesInputStream = new FileInputStream("./src/main/resources/beans.properties");
            properties.load(propertiesInputStream);
            properties.forEach((k, v) -> {
                try {
                    beans.put((String) k, Class.forName((String) v).getConstructor().newInstance());
                } catch (IllegalArgumentException | SecurityException | ClassNotFoundException | InstantiationException
                        | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        beans.forEach((beanName, bean) -> iocInject(beanName, bean, beans));
    }

    /**
     * @param beanName
     * @param bean
     * @param beans
     */
    private void iocInject(String beanName, Object bean, Map<String, Object> beans) {
        List<Field> fields = Stream.of(bean.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null).collect(Collectors.toList());

        fields.forEach(field -> {
            String filedName = field.getName();
            Object beanInstance = beans.get(filedName);
            field.setAccessible(true);
            try {
                field.set(bean, beanInstance);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }

    /*
     * 从容器中获取一个bean
     */
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
