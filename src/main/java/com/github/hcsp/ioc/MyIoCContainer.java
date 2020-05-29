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

    // 定义一个容器! 存放 bean 的名字到 bean 实例对象的映射
    private Map<String, Object> beans = new HashMap<>();

    /**
     * 依赖注入
     *
     * @param beanInstance bean 的 实例
     * @param beans        存放所有 bean 的容器
     */
    private static void dependencyInject(Object beanInstance, Map<String, Object> beans) {
        // 拿到带有 @AutoWired 注解的 fields
        List<Field> fieldsToBeAutoWired = Stream.of(beanInstance.getClass().getDeclaredFields())
                .filter(field -> field.getAnnotation(Autowired.class) != null)
                .collect(Collectors.toList());
        // 为当前 bean 对象的需要依赖的字段注入依赖（设置字段值）
        fieldsToBeAutoWired.forEach(field -> {
            String fieldName = field.getName(); // 加了 @AutoWired 的字段名即是所要依赖的 bean 的名字
            Object dependencyBeanInstance = beans.get(fieldName); // 所依赖的 bean 实例
            try {
                field.setAccessible(true); // 设置为 true 用来压制针对被反射对象的访问检查
                field.set(beanInstance, dependencyBeanInstance); // 从而可以在这里设置当前 bean 的私有字段
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 启动该容器
     */
    public void start() {
        // bean 的初始化

        Properties properties = new Properties();

        // 从 InputStream 中读取属性列表（键值对）
        try {
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(properties);

        properties.forEach((beanName, beanClassName) -> {
            try {
                // 通过反射拿到 bean 的实例并放入容器中
                Class<?> klass = Class.forName((String) beanClassName);
                Object beanInstance = klass.getConstructor().newInstance();
                beans.put((String) beanName, beanInstance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // 使用反射，处理依赖关系，注入依赖
        beans.forEach((beanName, beanInstance) -> dependencyInject(beanInstance, beans));
    }

    /**
     * 从容器中获取一个bean
     *
     * @param beanName bean 的名字
     * @return 返回 bean 的实例
     */
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }

    public static void main(String[] args) {

        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }
}
