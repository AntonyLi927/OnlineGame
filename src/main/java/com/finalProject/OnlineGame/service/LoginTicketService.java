package com.finalProject.OnlineGame.service;

import com.finalProject.OnlineGame.dao.LoginTicketDao;
import com.finalProject.OnlineGame.model.LoginTicket;
import com.finalProject.OnlineGame.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;

@Service
public class LoginTicketService {

    @Autowired
    private LoginTicketDao loginTicketDao;

    public LoginTicket createLoginTicket(Integer userId) {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(userId);
        loginTicket.setTicket(CommonUtils.generateUUID());
        loginTicket.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60).getTime());
        loginTicket.setStatus(1);
        //loginTicketDao.addLoginTicket(loginTicket);
        return loginTicket;
    }


}
