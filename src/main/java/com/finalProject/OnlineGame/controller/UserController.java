package com.finalProject.OnlineGame.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalProject.OnlineGame.dao.UserDao;
import com.finalProject.OnlineGame.model.LoginTicket;
import com.finalProject.OnlineGame.model.User;
import com.finalProject.OnlineGame.service.LoginTicketService;
import com.finalProject.OnlineGame.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginTicketService loginTicketService;

    @Autowired
    private UserDao userDao;

    @ResponseBody
    @RequestMapping("/welcome")
    public String welcomeUser(@CookieValue(value = "loginTicket", required = false) String ticket, HttpServletResponse response) throws JsonProcessingException {
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        ObjectMapper objectMapper = new ObjectMapper();
        if (ticket == null || ticket.equals("")) {
            User user = userService.signUpUserByDefault();
            LoginTicket loginTicket = loginTicketService.createLoginTicket(user.getUserId());
            Cookie cookie = new Cookie("loginTicket", loginTicket.getTicket());
            cookie.setMaxAge(60);
            response.addCookie(cookie);
            return objectMapper.writeValueAsString(user);
        } else {
            return "haha";
        }
    }
}
