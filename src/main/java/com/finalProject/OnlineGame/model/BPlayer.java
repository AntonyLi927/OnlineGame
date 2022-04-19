package com.finalProject.OnlineGame.model;

import lombok.Data;

@Data
public class BPlayer {
    private BGrid grid;
    private Integer userId;
    private String username;
    private String email;
    private String avatarUrl;
    private Integer points;
}
