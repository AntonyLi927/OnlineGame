package com.finalProject.OnlineGame.model;

import lombok.Data;
import org.springframework.stereotype.Component;


@Data
@Component
public class LoginTicket {
    private Integer ticketId;
    private Integer userId;
    private String ticket;
    private Integer status; // 1 non-expired , 0 expired
    private Long expiration;
}
