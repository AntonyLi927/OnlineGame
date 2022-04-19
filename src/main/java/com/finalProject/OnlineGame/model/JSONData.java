package com.finalProject.OnlineGame.model;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class JSONData {
    private Integer status;
    private String msg;
    private String msgType;
    private Object data;
}
