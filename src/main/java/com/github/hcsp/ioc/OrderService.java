package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

public class OrderService {
    @Autowired private OrderDao orderDaox;
    @Autowired private UserService userService;

    public void createOrder() {
        orderDaox.createOrder(userService.getCurrentLoginUser());
    }
}
