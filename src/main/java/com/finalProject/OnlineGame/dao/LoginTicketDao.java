package com.finalProject.OnlineGame.dao;

import com.finalProject.OnlineGame.model.LoginTicket;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface LoginTicketDao {
    public void addLoginTicket(LoginTicket loginTicket);
    public LoginTicket selectFromTicket(String ticket);

}
