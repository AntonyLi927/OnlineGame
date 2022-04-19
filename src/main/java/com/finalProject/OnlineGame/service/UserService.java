package com.finalProject.OnlineGame.service;

import com.finalProject.OnlineGame.dao.UserDao;
import com.finalProject.OnlineGame.model.LoginTicket;
import com.finalProject.OnlineGame.model.User;
import com.finalProject.OnlineGame.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public User signUpUserByDefault() {
        User user = new User();
//        user.setUserId(2); // to be deleted
        user.setUsername("User_" + CommonUtils.generateUUID().substring(0, 11));
        user.setEmail("123@12.com");
        user.setIsSignUp(0);
        user.setRegistrationTime(new Date().getTime());
        userDao.addUser(user);
        return user;
    }

    public User getUserById(Integer userId) {
        return userDao.getUserById(userId);
    }

    public User updateUsername(Integer userId, String username) {
        userDao.updateUsername(userId, username);
        return userDao.getUserById(userId);
    }

}
