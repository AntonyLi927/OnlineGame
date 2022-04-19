package com.finalProject.OnlineGame.model;

import lombok.Data;

@Data
public class BLocation {
    public static final int UNGUESSED = 0;
    public static final int HIT = 1;
    public static final int MISSED = 2;
    public static final int DEAD = 3; // 船沉了

    private Boolean hasShip;
    private String dir; //hor / por
    private Integer len;
    private Integer status;
    private int shipHeadRow;
    private int shipHeadCol;


    public BLocation() {
        this.status = UNGUESSED;
        this.hasShip = false;
        this.dir = null;
        this.len = 0;

    }

}
