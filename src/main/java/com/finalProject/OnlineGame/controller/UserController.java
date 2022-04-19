package com.finalProject.OnlineGame.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalProject.OnlineGame.dao.UserDao;
import com.finalProject.OnlineGame.model.LoginTicket;
import com.finalProject.OnlineGame.model.User;
import com.finalProject.OnlineGame.service.LoginTicketService;
import com.finalProject.OnlineGame.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginTicketService loginTicketService;

    @Autowired
    private UserDao userDao;

    ObjectMapper objectMapper = new ObjectMapper();

    @ResponseBody
    @RequestMapping("/welcome")
    public String welcomeUser(@CookieValue(value = "loginTicket", required = false) String ticket, HttpServletResponse response) throws JsonProcessingException {
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        if (ticket == null || ticket.equals("")) {
            User user = userService.signUpUserByDefault();
            LoginTicket loginTicket = loginTicketService.createLoginTicket(user.getUserId());
            Cookie cookie = new Cookie("loginTicket", loginTicket.getTicket());
            cookie.setMaxAge(60 * 60 * 5);
            response.addCookie(cookie);
            return objectMapper.writeValueAsString(user);
        } else {
            LoginTicket loginTicket = loginTicketService.selectFromTicket(ticket);
            User user = userService.getUserById(loginTicket.getUserId());
            return objectMapper.writeValueAsString(user);
        }
    }



    @RequestMapping(path = "/user", method = RequestMethod.PUT)
    @ResponseBody
    @CrossOrigin(origins = {"http://localhost:8080"}, allowCredentials = "true")
    public String updateUsername(HttpServletRequest req, HttpServletResponse res, @RequestBody Map<String, String> requestBody) throws JsonProcessingException {
        res.setHeader("Access-Control-Allow-Methods", "OPTIONS,PUT,GET,POST");
        Integer userId =  Integer.parseInt(requestBody.get("userId"));
        String username = requestBody.get("username");
        return objectMapper.writeValueAsString(userService.updateUsername(userId, username));
    }
}
