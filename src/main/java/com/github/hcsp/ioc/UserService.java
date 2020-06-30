package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

public class UserService {
    @Autowired private UserDao userDao;

    public User getCurrentLoginUser() {
        return userDao.getUserById(1);
    }
}
