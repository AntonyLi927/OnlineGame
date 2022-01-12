package com.finalProject.OnlineGame.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@ServerEndpoint(value = "/websocketDemo")
@Component
public class MyWebSocket {

    private static CopyOnWriteArraySet<MyWebSocket> webSocketSet = new CopyOnWriteArraySet<>();

    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        System.out.println(webSocketSet);
        //System.out.println("open");
        log.info(String.valueOf(session));

        this.session = session;
        webSocketSet.add(this);
        this.session.getAsyncRemote().sendText("connect successfully, online people: " + webSocketSet.size());
    }


    @OnMessage
    public synchronized void onMessage(String message, Session session) throws IOException {
        System.out.println(message);
        System.out.println(session);
        for (MyWebSocket item : webSocketSet) {
            //同步异步说明参考：http://blog.csdn.net/who_is_xiaoming/article/details/53287691
            item.session.getBasicRemote().sendText(message);
            //item.session.getAsyncRemote().sendText(message);//异步发送消息.
        }
    }

    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        System.out.println("close");
    }



}
