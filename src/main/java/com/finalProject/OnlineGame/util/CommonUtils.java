package com.finalProject.OnlineGame.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CommonUtils {


    /**
     * 生成随机字符串
     * @return
     */
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
