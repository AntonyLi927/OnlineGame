package com.finalProject.OnlineGame.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalProject.OnlineGame.model.*;
import com.finalProject.OnlineGame.util.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ServerEndpoint("/battleshipServer")
@Component
@Slf4j
public class BattleshipServer {

    private Session session;

    private static Map<Integer, BPlayer> playerList = new HashMap<>();

    private static Map<String, List<Object[]>> roomList = new HashMap<>(); // 0 - BPlayer      1 - session

    private ObjectMapper objectMapper = new ObjectMapper();

    Gson gson = new Gson();



    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        this.session.getAsyncRemote().sendText("connect successfully");
    }


    @OnMessage
    public void onMessage(String message, Session session) throws Exception {
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(message);


        if (jsonObject.get("msgType").getAsString().equals("newGame")) {
            Integer roomCode = CommonUtils.generateRoomId();
            User user = gson.fromJson(((JsonObject) jsonObject.get("data")).get("user"), User.class);
            BPlayer bPlayer;
            if (playerList.get(user.getUserId()) == null) {
                bPlayer = new BPlayer();
                bPlayer.setUserId(user.getUserId());
                bPlayer.setEmail(user.getEmail());
                bPlayer.setPoints(0);
                bPlayer.setUsername(user.getUsername());
                bPlayer.setGrid(new BGrid());
                playerList.put(user.getUserId(), bPlayer);
            } else {
                bPlayer = playerList.get(user.getUserId());
            }

            List<Object[]> room = new ArrayList<>();
            Object[] objs = new Object[2];
            objs[0] = bPlayer;
            objs[1] = session;
            room.add(objs);
            roomList.put(String.valueOf(roomCode), room);

            JSONData jsonData = new JSONData();
            jsonData.setMsgType("newGame");
            Map<String, Object> resData = new HashMap<>();
            resData.put("roomCode", roomCode);
            resData.put("bPlayer", bPlayer);
            jsonData.setData(resData);
            jsonData.setStatus(200);
            jsonData.setMsg("ok");
            session.getBasicRemote().sendText(objectMapper.writeValueAsString(jsonData));
        }

        if (jsonObject.get("msgType").getAsString().equals("joinExisting")) {
            String roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), String.class);
            User user = gson.fromJson(((JsonObject) jsonObject.get("data")).get("user"), User.class);
            BPlayer bPlayer  = null;
            System.out.println(roomList);
            if (roomList.get(roomCode) == null) {
                System.out.println("not exist todo");
                //todo
                return;
            }

            bPlayer = playerList.get(user.getUserId());
            if (bPlayer == null) {
                bPlayer = new BPlayer();
                bPlayer.setUserId(user.getUserId());
                bPlayer.setEmail(user.getEmail());

                bPlayer.setUsername(user.getUsername());
                bPlayer.setGrid(new BGrid());
                playerList.put(user.getUserId(), bPlayer);
            }
            bPlayer.setPoints(0);

            List<Object[]> room = roomList.get(roomCode);

            Object[] player = new Object[2];
            player[0] = bPlayer;
            player[1] = session;


            room.add(player);


            List<BPlayer> players = new ArrayList<>();
            for (Object[] p :roomList.get(roomCode)) {
                players.add((BPlayer) p[0]);
            }

            for (Object[] p :roomList.get(roomCode)) {
                Map<String, Object> data = new HashMap<>();
                data.put("players", players);
                data.put("roomCode", roomCode);
                Session temp = (Session) p[1];
                JSONData jsonData = new JSONData();
                jsonData.setMsgType("joinExisting");
                jsonData.setData(data);
                jsonData.setStatus(200);
                jsonData.setMsg("ok");
                synchronized (temp) {
                    temp.getAsyncRemote().sendText(gson.toJson(jsonData));
                }
            }

            System.out.println("after join" + roomList);
        }




        if (jsonObject.get("msgType").getAsString().equals("getReady")) {
            String roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), String.class);
            List<Object[]> playerList = roomList.get(roomCode);
            BPlayer bPlayer = gson.fromJson(((JsonObject) jsonObject.get("data")).get("bPlayer"), BPlayer.class);
            for (Object[] player : playerList) {
                JSONData jsonData = new JSONData();
                jsonData.setMsgType("getReady");
                jsonData.setData(bPlayer);
                jsonData.setStatus(200);
                jsonData.setMsg("ok");
                Session temp = (Session) player[1];
                synchronized (temp) {
                    temp.getAsyncRemote().sendText(gson.toJson(jsonData));
                }
            }
        }

        if (jsonObject.get("msgType").getAsString().equals("initializeGrid")) {
            Type typeOfObjectList = new TypeToken<ArrayList<IniGrid>>(){}.getType();
            List<IniGrid> ships = gson.fromJson(((JsonObject) jsonObject.get("data")).get("ships"), typeOfObjectList);
            BPlayer bPlayer = gson.fromJson(((JsonObject) jsonObject.get("data")).get("bPlayer"), BPlayer.class);
            String roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), String.class);
            List<Object[]> playerList = roomList.get(roomCode);
            for (Object[] player : playerList) {
                BPlayer temp = (BPlayer) player[0];
                if (temp.getUserId() == bPlayer.getUserId()) {
                    bPlayer = temp;
                    break;
                }
            }
            BGrid bGrid = new BGrid();

            for (IniGrid ship : ships) {
                int headRow = ship.getRow();
                int headCol = ship.getCol();
                String dir = ship.getDir();
                int length = ship.getLen();
                if (dir.equals("hor")) {
                    for (int i = headCol; i < headCol + length; i++) {
                        bGrid.addShip(headRow, i, length, dir, headRow, headCol);
                    }
                } else {
                    for (int i = headRow; i < headRow + length; i++) {
                        bGrid.addShip(i, headCol, length, dir, headRow, headCol);
                    }
                }
            }
            bPlayer.setGrid(bGrid);
            //System.out.println(bGrid);


            for (Object[] player : playerList) {
                JSONData jsonData = new JSONData();
                jsonData.setMsgType("initializeGrid");
                jsonData.setData(bPlayer.getUserId());
                jsonData.setStatus(200);
                jsonData.setMsg("ok");
                Session temp = (Session) player[1];
                synchronized (temp) {
                    temp.getBasicRemote().sendText(gson.toJson(jsonData));
                }
            }
        }


        if (jsonObject.get("msgType").getAsString().equals("gameStart")) {
            String roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), String.class);
            List<Object[]> room = roomList.get(roomCode);
            int rd = Math.random() > 0.5 ? 1 : 0;
            BPlayer curTurn = (BPlayer) room.get(rd)[0];
            for (Object[] player : room) {
                JSONData jsonData = new JSONData();
                jsonData.setMsgType("gameStart");
                jsonData.setData(curTurn.getUserId());
                jsonData.setStatus(200);
                jsonData.setMsg("ok");
                Session temp = (Session) player[1];
                synchronized (temp) {
                    temp.getAsyncRemote().sendText(gson.toJson(jsonData));
                }
            }
        }

        if (jsonObject.get("msgType").getAsString().equals("playerAction")) {
            String roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), String.class);
            Integer row = gson.fromJson(((JsonObject) jsonObject.get("data")).get("row"), Integer.class) - 1;
            Integer col = gson.fromJson(((JsonObject) jsonObject.get("data")).get("col"), Integer.class) - 1;
            List<Object[]> room = roomList.get(roomCode);
            Integer targetPlayerId = gson.fromJson(((JsonObject) jsonObject.get("data")).get("targetPlayerId"), Integer.class);
            boolean shootRes = false;
            boolean checkDead = false;
            BGrid bGrid = null;
            for (Object[] player : room) {
                BPlayer temp = (BPlayer) player[0];
                if (temp.getUserId().equals(targetPlayerId)) {
                    bGrid = temp.getGrid();
                    shootRes = temp.getGrid().shoot(row, col);
                    checkDead = temp.getGrid().checkDead(row, col);
                }
            }

            JSONData jsonData = new JSONData();
            jsonData.setMsgType("playerAction");
            jsonData.setStatus(200);
            jsonData.setMsg("ok");
            Map<String, Object> data = new HashMap<>();
            data.put("targetPlayerId", targetPlayerId);
            data.put("row", row);
            data.put("col", col);
            if (shootRes) {
                data.put("res", checkDead ? "dead" : "hit");
                data.put("curTurn", targetPlayerId == ((BPlayer)room.get(0)[0]).getUserId() ? ((BPlayer)room.get(1)[0]).getUserId() : ((BPlayer)room.get(0)[0]).getUserId());
                if (checkDead) {
                    data.put("headRow", bGrid.getGrid()[row][col].getShipHeadRow());
                    data.put("headCol", bGrid.getGrid()[row][col].getShipHeadCol());
                    data.put("dir", bGrid.getGrid()[row][col].getDir());
                    data.put("len", bGrid.getGrid()[row][col].getLen());
                    data.put("curTurn", targetPlayerId);
                    if (bGrid.checkWin()) {
                        data.put("gameOver", targetPlayerId == ((BPlayer)room.get(0)[0]).getUserId() ? ((BPlayer)room.get(1)[0]).getUserId() : ((BPlayer)room.get(0)[0]).getUserId());
                    }
                }
            } else {
                data.put("res", "missed");
                data.put("curTurn", targetPlayerId);
            }
            jsonData.setData(data);

            for (Object[] player : room) {
                Session temp = (Session) player[1];
                synchronized (temp) {
                    temp.getBasicRemote().sendText(gson.toJson(jsonData));
                }
            }
        }

        if (jsonObject.get("msgType").getAsString().equals("playAgain")) {
            String roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), String.class);
            BPlayer bPlayer = gson.fromJson(((JsonObject) jsonObject.get("data")).get("bPlayer"), BPlayer.class);
            bPlayer.setGrid(new BGrid());
            List<Object[]> room = roomList.get(roomCode);
            JSONData jsonData = new JSONData();
            jsonData.setMsgType("playAgain");
            jsonData.setStatus(200);
            jsonData.setMsg("ok");
            jsonData.setData(bPlayer.getUserId());

            for (Object[] player : room) {
                Session temp = (Session) player[1];
                synchronized (temp) {
                    temp.getBasicRemote().sendText(gson.toJson(jsonData));
                }
            }

        }

        if (jsonObject.get("msgType").getAsString().equals("leave")) {
            String roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), String.class);
            BPlayer bPlayer = gson.fromJson(((JsonObject) jsonObject.get("data")).get("bPlayer"), BPlayer.class);
            List<Object[]> room = roomList.get(roomCode);
            BPlayer temp = (BPlayer)room.get(0)[0];
            if (temp.getUserId() == bPlayer.getUserId()) {
                room.remove(0);
            } else {
                room.remove(1);
            }

            JSONData jsonData = new JSONData();
            jsonData.setMsgType("leave");
            jsonData.setStatus(200);
            jsonData.setMsg("ok");
            jsonData.setData("");

            for (Object[] player : room) {
                Session tempSession = (Session) player[1];
                synchronized (temp) {
                    tempSession.getBasicRemote().sendText(gson.toJson(jsonData));
                }
            }

        }
    }
    @OnClose
    public void onClose() {
        System.out.println("leave....");
    }


}
