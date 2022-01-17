package com.finalProject.OnlineGame.model;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Date;

@Data
@Component
public class User {
    private Integer userId;
    private String username;
    private String password;
    private String salt;
    private String email;
    private Integer isSignUp;
    private String avatarUrl;
    private Long registrationTime;
}
