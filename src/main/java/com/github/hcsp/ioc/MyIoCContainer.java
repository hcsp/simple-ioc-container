package com.github.hcsp.ioc;

import java.util.Properties;
import java.util.Set;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.support.PropertiesLoaderUtils;


public class MyIoCContainer {

    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

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
        context.refresh();
        registerAllBeansFromProperties("beans.properties", context);
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    public static void registerAllBeansFromProperties(String fileName, AnnotationConfigApplicationContext context) {
        try {
            Properties properties = PropertiesLoaderUtils.loadAllProperties(fileName);
            Set<Object> objects = properties.keySet();
            for (Object key : objects) {
                String className = (String) properties.get(key);
                Class<?> clazz = Class.forName(className);
                BeanDefinition beanDefinition = new RootBeanDefinition(clazz);
                context.registerBeanDefinition(key.toString(), beanDefinition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
