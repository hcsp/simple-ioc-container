package spring.demo;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringMain {
    public static void main(String[] args) {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("classpath:spring/config.xml");

        OrderService orderService = (OrderService) beanFactory.getBean("orderService");
        OrderDao orderDao = (OrderDao) beanFactory.getBean("orderDao");

        System.out.println(orderService);
        orderService.doSomething();
        System.out.println(orderDao);
    }
}
