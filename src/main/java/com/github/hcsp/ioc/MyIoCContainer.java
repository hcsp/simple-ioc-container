package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public class MyIoCContainer {
    private static Map<String, Object> beans = new HashMap<>();
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
        this.initByProperty();
        this.initByAnnotation();
    }

    private void initByAnnotation(){
        beans.forEach((String beanName, Object beanInstance )->{
            Stream.of(beanInstance.getClass().getDeclaredFields()).filter((Field field)->{
                return field.getAnnotation(Autowired.class) !=null;
            }).forEach(field -> {
                field.setAccessible(true); //有的字段可能是私有的，所以设置访问权限
                try {
                    field.set(beanInstance, beans.get(field.getName())); //针对包含特殊注解的字段，挂在对象
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void initByProperty(){
        //1、从配置文件中根据key获取全限定类名
        //2、根据限定类名创建实例，缓存在容器中
        //3、利用反射判断实例上是否存在autoWired注解，若存在则进行初始赋值，完成bean的初始化
        //4、根据key从容器中获取获取对应的实例

        try {
            Properties properties = new Properties();
            properties.load(MyIoCContainer.class.getResourceAsStream("/beans.properties"));
            properties.forEach((Object beanName, Object beanClassName) ->{
                String name = (String) beanName;
                String className = (String) beanClassName;

                try {
                    Class<?> klass = Class.forName(className);
                    beans.put(name, klass.getConstructor().newInstance());

                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}
