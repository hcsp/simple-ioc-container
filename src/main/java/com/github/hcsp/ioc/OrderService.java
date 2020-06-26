package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

public class OrderService {
    @Autowired private OrderDao orderDao;
    @Autowired private UserService userService;

    public void createOrder() {
        orderDao = new OrderDao();
        userService = new UserService();
        orderDao.createOrder(userService.getCurrentLoginUser());
    }
}
