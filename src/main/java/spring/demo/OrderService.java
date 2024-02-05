package spring.demo;

import org.springframework.beans.factory.annotation.Autowired;

public class OrderService {

    @Autowired
    private OrderDao orderDao;

    public void doSomething() {
        orderDao.select();
    }
}
