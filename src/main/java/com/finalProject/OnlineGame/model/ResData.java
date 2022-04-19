package com.finalProject.OnlineGame.model;

import lombok.Data;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@Data
public class ResData {
    private Boolean success;
    private String message;
    private Map<String, Object> data;
}
