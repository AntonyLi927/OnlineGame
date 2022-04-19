package com.finalProject.OnlineGame.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalProject.OnlineGame.constants.MsgConstants;
import com.finalProject.OnlineGame.model.JSONData;
import com.finalProject.OnlineGame.model.User;
import com.finalProject.OnlineGame.service.WordService;
import com.finalProject.OnlineGame.util.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.annotation.Resource;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/pictionaryServer")
@Component
@Slf4j
public class PictionaryServer {

    private Gson gson = new Gson();

    private final Integer maxUserNum = 8;

    private Session session;

    private Integer roomId;

    private User user;

    private static CopyOnWriteArraySet<PictionaryServer> webSocketSet = new CopyOnWriteArraySet<>();

    private static Map<Integer, List<User>> pictionaryRoomContainer = new HashMap<>();

    private static Map<Integer, List<Session>> sessionContainer = new HashMap<>();

    private static Map<Integer, List<Object[]>> rooms = new HashMap<>(); // the first index of the object array saves User,
    // the second index saves Session, the third place records the total points of certain user.


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
        ObjectMapper objectMapper = new ObjectMapper();
        JSONData data = objectMapper.readValue(message, new TypeReference<JSONData>() {});

        JsonObject jsonObject = (JsonObject) new JsonParser().parse(message);

        if (data.getMsgType().equals(MsgConstants.SYNC_STROKE) || data.getMsgType().equals(MsgConstants.CHAT_MSG)) {
            for (Session item : sessionContainer.get(gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), Integer.class))) {
                //同步异步说明参考：http://blog.csdn.net/who_is_xiaoming/article/details/53287691
                //System.out.println("send message to others ----------------");
                if (session.isOpen()) {
                    item.getBasicRemote().sendText(message);
                }

                //item.session.getAsyncRemote().sendText(message);//异步发送消息.
            }
        }
//        System.out.println(jsonObject.get("msgType").getAsString());

        if (jsonObject.get("msgType").getAsString().equals(MsgConstants.JOIN_WITH_CODE)) {
            User user = gson.fromJson(((JsonObject) jsonObject.get("data")).get("user"), User.class);

            JSONData jsonData = new JSONData();
            Integer roomCode = null;
            try {
                 roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), Integer.class);
            } catch (Exception e) {
                jsonData.setMsg("err");
                jsonData.setMsgType("joinResult");
                jsonData.setData(roomCode);
                synchronized (session) {
                    session.getBasicRemote().sendText(gson.toJson(jsonData));
                }
                return;
            }

            if (roomCode == null) return;

            List<Object[]> playerList = rooms.get(roomCode);
            if (pictionaryRoomContainer.get(roomCode) != null && sessionContainer.get(roomCode) != null && rooms.get(roomCode) != null) {
                pictionaryRoomContainer.get(roomCode).add(user);
                sessionContainer.get(roomCode).add(session);
                rooms.get(roomCode).add(new Object[]{user, session, 0.0});
                jsonData.setMsg("ok");
                jsonData.setMsgType("joinResult");
                jsonData.setData(roomCode);
                synchronized (session) {
                    session.getBasicRemote().sendText(gson.toJson(jsonData));
                }

                jsonData.setMsg("");
                jsonData.setMsgType("playerList");
                jsonData.setData(pictionaryRoomContainer.get(roomCode));

                for (Object[] obj : playerList) {
                    Session temp = (Session) obj[1];
                    temp.getBasicRemote().sendText(gson.toJson(jsonData));
                }

            } else {
                jsonData.setMsg("err");
                jsonData.setMsgType("joinResult");
                jsonData.setData(roomCode);
                synchronized (session) {
                    session.getBasicRemote().sendText(gson.toJson(jsonData));
                }
                return;
            }
            System.out.println(pictionaryRoomContainer);
            System.out.println(sessionContainer);
        }


//        if (data.getMsgType().equals(MsgConstants.CONNECT_WITH_ROOM_ID)) {
//            List<User> room = pictionaryRoomContainer.getOrDefault(1, null);
//            if (room == null) {
//                // todo send message
//            } else {
//                if (room.indexOf(this.user) == -1) room.add(this.user);
//            }
//        } else
        if (data.getMsgType().equals(MsgConstants.CREATE_NEW_ROOM)) {
            Integer roomId = CommonUtils.generateRoomId(); // roomId is the same to roomCode
            List<Session> pictionaryServerList = new ArrayList<>();
            List<User> userList = new ArrayList<>();
            List<Object[]> room = new ArrayList<>();
            User user = gson.fromJson(((JsonObject) jsonObject.get("data")).get("user"), User.class); // 解析出前端发回来的user

            userList.add(user); // add current user to list
            pictionaryRoomContainer.put(roomId, userList);

            pictionaryServerList.add(session);
            sessionContainer.put(roomId, pictionaryServerList);

            room.add(new Object[]{user, session, 0.0});
            rooms.put(roomId, room);

            log.info(String.valueOf(sessionContainer));
            log.info(String.valueOf(pictionaryRoomContainer));

            JSONData jsonData = new JSONData();
            jsonData.setMsgType("roomCode");
            jsonData.setData(roomId);
            jsonData.setStatus(200);
            jsonData.setMsg("ok");

            this.session.getAsyncRemote().sendText(gson.toJson(jsonData));

            jsonData.setMsg("");
            jsonData.setMsgType("playerList");
            jsonData.setData(pictionaryRoomContainer.get(roomId));
            this.session.getAsyncRemote().sendText(gson.toJson(jsonData));
        }

        if(data.getMsgType().equals(MsgConstants.REQUEST_WORDS)) {
            List<String> res = CommonUtils.readTxt("F:\\BUPT_Final_Project\\OnlineGame\\src\\main\\resources\\static\\words.txt");
            String[] wordList = new String[3];
            wordList[0] = res.get(new Random().nextInt(res.size()));
            wordList[1] = res.get(new Random().nextInt(res.size()));
            wordList[2] = res.get(new Random().nextInt(res.size()));

            JSONData jsonData = new JSONData();
            jsonData.setMsgType("resWords");
            jsonData.setData(wordList);
            this.session.getAsyncRemote().sendText(gson.toJson(jsonData));
        }

        if (data.getMsgType().equals(MsgConstants.GAME_START)) {
            Integer roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), Integer.class);
            List<Object[]> playerList = rooms.get(roomCode);
            for (Object[] obj : playerList) {
                JSONData jsonData = new JSONData();
                Session temp = (Session) obj[1];

                jsonData.setMsgType("gameStart");
                jsonData.setData("");
                synchronized (temp) {
                    temp.getBasicRemote().sendText(gson.toJson(jsonData));
                }
            }

        }

        if(data.getMsgType().equals(MsgConstants.GAME_PROCESS)) {

            //User user = gson.fromJson(((JsonObject) jsonObject.get("data")).get("user"), User.class);
            Integer roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), Integer.class);
            Integer gameRound = gson.fromJson(((JsonObject) jsonObject.get("data")).get("round"), Integer.class);
            System.out.println(roomCode);
            log.info(roomCode + "game round " + gameRound);
            List<Object[]> playerList = rooms.get(roomCode);
            if (gameRound == (playerList.size() + 1)) {
                for (Object[] obj : playerList) {
                    System.out.println(obj);
                    JSONData jsonData = new JSONData();
                    Session temp = (Session) obj[1];

                    jsonData.setMsgType("gameOver");
                    jsonData.setData("");
                    synchronized (temp) {
                        temp.getBasicRemote().sendText(gson.toJson(jsonData));
                    }
                }
                log.info("game over");
                return;
            }

            User drawer = (User) playerList.get(gameRound - 1)[0];
            for (Object[] obj : playerList) {
                System.out.println(obj);
                JSONData jsonData = new JSONData();
                Session temp = (Session) obj[1];

                jsonData.setMsgType("round");
                jsonData.setData(gameRound);
                temp.getBasicRemote().sendText(gson.toJson(jsonData));

                jsonData.setMsgType(MsgConstants.GAME_PROCESS);
                jsonData.setData(drawer);
                synchronized (temp) {
                    temp.getBasicRemote().sendText(gson.toJson(jsonData));
                }
            }


        }

        if (data.getMsgType().equals(MsgConstants.TIME_IS_UP)) {
            Integer roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), Integer.class);
            List<Object[]> playerList = rooms.get(roomCode);
            for (Object[] obj : playerList) {
                //System.out.println(obj);
                JSONData jsonData = new JSONData();
                jsonData.setMsgType(MsgConstants.TIME_IS_UP);
                jsonData.setData("");
                Session temp = (Session) obj[1];
                synchronized (temp) {
                    temp.getBasicRemote().sendText(gson.toJson(jsonData));
                }
            }
        }

        if (data.getMsgType().equals(MsgConstants.WORD_CHOSEN)) {
            Integer roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), Integer.class);
            String word = gson.fromJson(((JsonObject) jsonObject.get("data")).get("word"), String.class);
            System.out.println("word chosen");
            List<Object[]> playerList = rooms.get(roomCode);
            for (Object[] obj : playerList) {
                //System.out.println(obj);
                JSONData jsonData = new JSONData();
                jsonData.setMsgType(MsgConstants.WORD_CHOSEN);
                jsonData.setData(word);
                Session temp = (Session) obj[1];
                synchronized (temp) {
                    temp.getBasicRemote().sendText(gson.toJson(jsonData));
                }
            }
        }

        if (data.getMsgType().equals(MsgConstants.RIGHT_MESSAGE)){
            Integer roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), Integer.class);
            User user = gson.fromJson(((JsonObject) jsonObject.get("data")).get("user"), User.class);
            List<Object[]> playerList = rooms.get(roomCode);
            for (Object[] obj : playerList) {
                //System.out.println(obj);
                JSONData jsonData = new JSONData();
                jsonData.setMsgType(MsgConstants.RIGHT_MESSAGE);
                jsonData.setData(user);
                Session temp = (Session) obj[1];
                synchronized (temp) {
                    temp.getBasicRemote().sendText(gson.toJson(jsonData));
                }
            }
        }

        if (data.getMsgType().equals(MsgConstants.ADD_POINTS)) {
            Integer roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), Integer.class);
            User user = gson.fromJson(((JsonObject) jsonObject.get("data")).get("user"), User.class);
            Double points = gson.fromJson(((JsonObject) jsonObject.get("data")).get("points"), Double.class);

            List<Object[]> playerList = rooms.get(roomCode);
            for (Object[] obj : playerList) {
                User tempUser = (User) obj[0];
                Map<String, Object> test = new HashMap<>();
                test.put("user", user);
                test.put("points", points);

                //System.out.println(obj);
                JSONData jsonData = new JSONData();
                jsonData.setMsgType(MsgConstants.ADD_POINTS);
                jsonData.setData(test);
                Session temp = (Session) obj[1];
                synchronized (temp) {
                    System.out.println(roomCode + tempUser.getUsername() + "add points" + gson.toJson(jsonData));
                    temp.getBasicRemote().sendText(gson.toJson(jsonData));
                }

                if (tempUser.getUserId() == user.getUserId()) {
                    Double total = (Double) obj[2];
                    total += points;
                    obj[2] = total;
                }
                log.info(String.valueOf(Arrays.asList(obj)));
            }
            log.info(String.valueOf(points));
        }

        if (data.getMsgType().equals(MsgConstants.GET_READY)) {
            Integer roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), Integer.class);
            User user = gson.fromJson(((JsonObject) jsonObject.get("data")).get("user"), User.class);
            List<Object[]> playerList = rooms.get(roomCode);
            for (Object[] obj : playerList) {
                //System.out.println(obj);
                JSONData jsonData = new JSONData();
                jsonData.setMsgType(MsgConstants.GET_READY);
                jsonData.setData(user);
                Session temp = (Session) obj[1];
                synchronized (temp) {
                    temp.getBasicRemote().sendText(gson.toJson(jsonData));
                }
            }
        }

        if (data.getMsgType().equals("totalPointsUserList")) {
            Integer roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), Integer.class);
            List<Object[]> playerList = rooms.get(roomCode);
            List<Map<String, Object>> totalPointsUserList = new ArrayList<>();
            for (Object[] obj : playerList) {
                Map<String, Object> userPoints = new HashMap<>();
                userPoints.put("user", obj[0]);
                userPoints.put("points", obj[2]);
                totalPointsUserList.add(userPoints);
            }
            for (Object[] obj : playerList) {
                //System.out.println(obj);
                JSONData jsonData = new JSONData();
                jsonData.setMsgType("totalPointsUserList");
                jsonData.setData(totalPointsUserList);
                Session temp = (Session) obj[1];
                synchronized (temp) {
                    temp.getBasicRemote().sendText(gson.toJson(jsonData));
                }
            }
        }

        if (data.getMsgType().equals(MsgConstants.PLAY_AGAIN)) {
            Integer roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), Integer.class);
            List<Object[]> playerList = rooms.get(roomCode);
            User user = gson.fromJson(((JsonObject) jsonObject.get("data")).get("user"), User.class);
            for (Object[] obj : playerList) {
                //System.out.println(obj);
                JSONData jsonData = new JSONData();
                jsonData.setMsgType(MsgConstants.PLAY_AGAIN);
                jsonData.setData(user);
                Session temp = (Session) obj[1];
                if (temp.isOpen()) {
                    synchronized (temp) {
                        temp.getBasicRemote().sendText(gson.toJson(jsonData));
                    }
                }
            }
        }

        if (data.getMsgType().equals("startDrawing")) {
            Integer roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), Integer.class);
            List<Object[]> playerList = rooms.get(roomCode);
            for (Object[] obj : playerList) {
                //System.out.println(obj);
                JSONData jsonData = new JSONData();
                jsonData.setMsgType("startDrawing");
                jsonData.setData("");
                Session temp = (Session) obj[1];
                synchronized (temp) {
                    temp.getBasicRemote().sendText(gson.toJson(jsonData));
                }
            }
        }

        if (data.getMsgType().equals(MsgConstants.LEAVE_GAME)) {

            Integer roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), Integer.class);
            List<Object[]> playerList = rooms.get(roomCode);
            User user = gson.fromJson(((JsonObject) jsonObject.get("data")).get("user"), User.class);
            pictionaryRoomContainer.get(roomCode).remove(user);
            Iterator<Object[]> iterator = playerList.iterator();
            while (iterator.hasNext()) {
                Object[] obj = iterator.next();
                User tempUser = (User) obj[0];
                Map<String, Object> map = new HashMap<>();
                if (tempUser.getUserId() == user.getUserId()) {
                    iterator.remove();
                }
                map.put("user", user);
                map.put("playerList", pictionaryRoomContainer.get(roomCode));
                JSONData jsonData = new JSONData();
                jsonData.setMsgType(MsgConstants.LEAVE_GAME);
                jsonData.setData(map);
                Session temp = (Session) obj[1];
                synchronized (temp) {
                    temp.getBasicRemote().sendText(gson.toJson(jsonData));
                }
            }

            if (playerList.size() == 0) {
                pictionaryRoomContainer.remove(roomCode);
                rooms.remove(roomCode);
                sessionContainer.remove(roomCode);
            }
        }




    }

    @OnClose
    public void onClose() {
        webSocketSet.remove(this);

//        for (Map.Entry<Integer, List<User>> entry : pictionaryRoomContainer.entrySet()) {
//            if (entry.getKey().equals(this.roomId)) {
//                entry.getValue().remove(this.user);
//            }
//        }
//
//        if (pictionaryRoomContainer.get(this.roomId).size() == 0) {
//            pictionaryRoomContainer.remove(this.roomId);
//        }

        System.out.println("close");
    }


}
