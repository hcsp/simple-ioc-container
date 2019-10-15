package com.github.hcsp.ioc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MyIoCContainerTest {
    @Test
    public void beanTest() {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    @Test
    public void singletonBeanTest() {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        Assertions.assertSame(container.getBean("orderService"), container.getBean("orderService"));
        Assertions.assertSame(container.getBean("userService"), container.getBean("userService"));
        Assertions.assertSame(container.getBean("orderDao"), container.getBean("orderDao"));
        Assertions.assertSame(container.getBean("userDao"), container.getBean("userDao"));
    }
}
