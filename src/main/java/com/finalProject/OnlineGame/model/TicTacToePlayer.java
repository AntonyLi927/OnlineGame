package com.finalProject.OnlineGame.model;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class TicTacToePlayer {
    private Integer userId;
    private String username;
    private String email;
    private String avatarUrl;
    private String type; // cross/circle
    private Integer points;
}
