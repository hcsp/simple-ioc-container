package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {

  private Map<String, Object> beanMap = new HashMap<>();

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
    Properties properties = new Properties();
    try {
      properties.load(getClass().getResourceAsStream("/beans.properties"));
      for (Map.Entry entry : properties.entrySet()
      ) {
        String beanName = entry.getKey().toString();
        Class bean = Class.forName(entry.getValue().toString());
        beanMap.put(beanName, bean.newInstance());
      }
      beanMap.forEach((beanName, instance) -> dependencyInject(beanName, instance, beanMap));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void dependencyInject(String beanName, Object instance, Map<String, Object> beanMap) {
    List<Field> fieldToBeAutowired = Stream.of(instance.getClass().getDeclaredFields())
      .filter(field -> field.getAnnotation(Autowired.class) != null)
      .collect(Collectors.toList());
    fieldToBeAutowired.forEach(field -> {
      try {
        String fieldName = field.getName();
        Object dependencyBeanInstance = beanMap.get(fieldName);
        field.setAccessible(true);
        field.set(instance, dependencyBeanInstance);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    });
  }

  // 从容器中获取一个bean
  public Object getBean(String beanName) {
    return beanMap.get(beanName);
  }
}
