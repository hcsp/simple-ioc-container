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
    private Map<String, Object> beans;
    public static final String BEAN_CONFIG_PATH = "/beans.properties";

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
            beans = new HashMap<>();
            initializeBeans(loadBeansConfig());
            injectBeansDependencies();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Properties loadBeansConfig() throws IOException {
        Properties properties = new Properties();
        properties.load(MyIoCContainer.class.getResourceAsStream(BEAN_CONFIG_PATH));
        return properties;
    }

    // 初始化 Bean
    public void initializeBeans(Properties properties) {
        properties.forEach((beanName, beanPath) -> {
            try {
                Object bean = Class.forName((String) beanPath).getConstructor().newInstance();
                beans.put((String) beanName, bean);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // 注入 bean 依赖
    public void injectBeansDependencies() {
        beans.forEach((beanName, beanInstance) -> {
            List<Field> fieldWithAutoWiredList = Stream.of(beanInstance.getClass().getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(Autowired.class)).collect(Collectors.toList());
            fieldWithAutoWiredList.forEach(field -> {
                final String injectBeanName = field.getName();
                try {
                    field.setAccessible(true);
                    field.set(beanInstance, beans.get(injectBeanName));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
