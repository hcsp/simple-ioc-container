package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public class MyIoCContainer {

    Properties properties;

    Map<String, Object> beans = new HashMap<>();
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
            // 1 获取bean的定义
            getBeanDefinition();
            // 2 生成所有定义的bean的实例
            generateBeanInstance();
            // 3 扫描所有生成的bean的实例中 有哪些字段加了@AutoWired注解, 并实现自动注入
            scanFieldToBeAutoWried();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void scanFieldToBeAutoWried() {
        beans.forEach((beanName, beanInstance) -> {
            Stream.of(beanInstance.getClass().getDeclaredFields())
                    .filter(field -> field.getAnnotation(Autowired.class) != null)
                    .forEach(field -> {
                        field.setAccessible(true);
                        String fieldName = field.getName();
                        Object dependencyInstance = beans.get(fieldName);
                        try {
                            field.set(beanInstance, dependencyInstance);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });

        });
    }

    // 按定义 生成bean的实例
    private void generateBeanInstance() {
        properties.forEach((beanName, beanClass) -> {
            try {
                Class klass = Class.forName((String) beanClass);
                Object beanInstance = klass.getConstructor().newInstance();
                beans.put((String) beanName, beanInstance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // 获取bean的定义
    private void getBeanDefinition() throws IOException {
        properties = new Properties();
        // 1 加载bean定义文件
        properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
