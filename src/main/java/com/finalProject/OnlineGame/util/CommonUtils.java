package com.finalProject.OnlineGame.util;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

@Component
public class CommonUtils {


    /**
     * 生成随机字符串
     * @return
     */
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static Integer generateRoomId() {
        int res = (int) (Math.random() * (1000000 - 100000 + 1));
        return res;
    }

    public static List<String> readTxt(String path) {
        List<String> res = new ArrayList<>();
        try {
//            String path = "C:\\Test\\test.txt";
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)), "UTF-8"));
            String lineTxt = null;
            int count = 0;
            // 逐行读取
            while ((lineTxt = br.readLine()) != null) {
                // 输出内容到控制台
                res.add(lineTxt);
                count++;
            }
            br.close();

        } catch (Exception e) {
            System.out.println(e);
        }
        return res;
    }

}
