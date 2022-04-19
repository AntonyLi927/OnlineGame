package com.finalProject.OnlineGame.websocket;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalProject.OnlineGame.model.*;
import com.finalProject.OnlineGame.util.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ServerEndpoint("/tictactoeServer")
@Component
@Slf4j
public class TicTacToeServer {

    private Gson gson = new Gson();

    private ObjectMapper objectMapper = new ObjectMapper();

    private Session session;

    public static Map<Integer, TicTacToePlayer> playerMap = new HashMap<>();

    public static Map<String, List<Object[]>> roomList = new HashMap<>(); // 0 - tictactoeplayer 1 - session

    public static Map<String, Board> boardList = new HashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("open tic tac toe");
    }

    @OnMessage
    public synchronized void onMessage(String message, Session session) throws Exception {
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(message);

        if (jsonObject.get("msgType").getAsString().equals("newGame")) {
            Integer roomCode = CommonUtils.generateRoomId();
            User user = gson.fromJson(((JsonObject) jsonObject.get("data")).get("user"), User.class);
            TicTacToePlayer ticTacToePlayer = new TicTacToePlayer();
            if (playerMap.get(user.getUserId()) == null) {
                ticTacToePlayer.setEmail(user.getEmail());
                ticTacToePlayer.setUserId(user.getUserId());
                ticTacToePlayer.setUsername(user.getUsername());
                ticTacToePlayer.setAvatarUrl(user.getAvatarUrl());
                ticTacToePlayer.setPoints(0);
                //ticTacToePlayer.setSession(session);
                playerMap.put(ticTacToePlayer.getUserId(), ticTacToePlayer);

                int rd = Math.random() > 0.5 ? 1 : 0;
                ticTacToePlayer.setType(rd == 1 ? "cross" : "circle");
                //List<TicTacToePlayer> playerList = new ArrayList<>();

            } else {
                ticTacToePlayer = playerMap.get(user.getUserId());
                ticTacToePlayer.setPoints(0);
            }

            Object[] player = new Object[2];
            player[0] = ticTacToePlayer;
            player[1] = session;

            List<Object[]> playerList = new ArrayList<>();
            playerList.add(player);

            roomList.put(String.valueOf(roomCode), playerList);
            boardList.put(String.valueOf(roomCode), new Board());

            JSONData jsonData = new JSONData();
            jsonData.setMsgType("newGame");
            Map<String, Object> resData = new HashMap<>();
            resData.put("roomCode", roomCode);
            resData.put("ticPlayer", ticTacToePlayer);
            jsonData.setData(resData);
            jsonData.setStatus(200);
            jsonData.setMsg("ok");
            session.getBasicRemote().sendText(objectMapper.writeValueAsString(jsonData));
        }

        if (jsonObject.get("msgType").getAsString().equals("joinExisting")) {
                Integer roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), Integer.class);
                User user = gson.fromJson(((JsonObject) jsonObject.get("data")).get("user"), User.class);
                TicTacToePlayer ticTacToePlayer  = null;
                System.out.println(roomList);
                if (roomList.get(String.valueOf(roomCode)) == null) {
                    System.out.println("not exist todo");
                    //todo
                    return;
                }

                ticTacToePlayer = playerMap.get(user.getUserId());
                if (ticTacToePlayer == null) {
                    ticTacToePlayer = new TicTacToePlayer();
                    ticTacToePlayer.setEmail(user.getEmail());
                    ticTacToePlayer.setUserId(user.getUserId());
                    ticTacToePlayer.setUsername(user.getUsername());
                    ticTacToePlayer.setAvatarUrl(user.getAvatarUrl());
                }
                ticTacToePlayer.setPoints(0);
                List<Object[]> room = roomList.get(String.valueOf(roomCode));
                TicTacToePlayer opponent = (TicTacToePlayer) room.get(0)[0];
                if (opponent.getType() == "cross") {
                    ticTacToePlayer.setType("circle");
                } else {
                    ticTacToePlayer.setType("cross");
                }

                //ticTacToePlayer.setSession(session);
                playerMap.put(ticTacToePlayer.getUserId(), ticTacToePlayer);

                Object[] player = new Object[2];
                player[0] = ticTacToePlayer;
                player[1] = session;
                List<Object[]> playerList = roomList.get(String.valueOf(roomCode));
                playerList.add(player);
                List<TicTacToePlayer> players = new ArrayList<>();
                for (Object[] p :roomList.get(String.valueOf(roomCode))) {
                    players.add((TicTacToePlayer) p[0]);
                }

                for (Object[] p :roomList.get(String.valueOf(roomCode))) {
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


        if (jsonObject.get("msgType").getAsString().equals("playerActions")) {
            String roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), String.class);
            Integer row = gson.fromJson(((JsonObject) jsonObject.get("data")).get("row"), Integer.class);
            Integer col = gson.fromJson(((JsonObject) jsonObject.get("data")).get("col"), Integer.class);
            String type = gson.fromJson(((JsonObject) jsonObject.get("data")).get("type"), String.class);
            Board board = boardList.get(roomCode);
            List<Object[]> playerList = roomList.get(roomCode);
            Map<String, Object> data = new HashMap<>();


            try {
                board.addMark(row, col, type);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(board.checkWin() == 1) {
                //game over
                data.put("gameOver", "cross");
                for (Object[] p : playerList) {
                    TicTacToePlayer temp = (TicTacToePlayer) p[0];
                    if (temp.getType().equals("cross")) {
                        temp.setPoints(temp.getPoints() + 1);
                        data.put("winner", temp);
                    }
                }
                board.resetBoard();
            } else if (board.checkWin() == 2) {
                data.put("gameOver", "circle");
                for (Object[] p : playerList) {
                    TicTacToePlayer temp = (TicTacToePlayer) p[0];
                    if (temp.getType().equals("circle")) {
                        temp.setPoints(temp.getPoints() + 1);
                        data.put("winner", temp);
                    }
                }
                board.resetBoard();
            } else {
                if (!board.checkAvailable()){
                    //drew
                    data.put("gameOver", "drew");
                    board.resetBoard();
                }
            }


            for (Object[] player : playerList) {
                data.put("row", row);
                data.put("col",col);
                data.put("type", type);
                JSONData jsonData = new JSONData();
                jsonData.setMsgType("playerActions");
                jsonData.setData(data);
                jsonData.setStatus(200);
                jsonData.setMsg("ok");
                Session temp = (Session) player[1];
                synchronized (temp) {
                    temp.getAsyncRemote().sendText(gson.toJson(jsonData));
                }
            }


        }


        if (jsonObject.get("msgType").getAsString().equals("getReady")) {
            String roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), String.class);
            List<Object[]> playerList = roomList.get(roomCode);
            TicTacToePlayer ticTacToePlayer = gson.fromJson(((JsonObject) jsonObject.get("data")).get("ticPlayer"), TicTacToePlayer.class);
            for (Object[] player : playerList) {
                JSONData jsonData = new JSONData();
                jsonData.setMsgType("getReady");
                jsonData.setData(ticTacToePlayer);
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
            String lastRoundStartType = gson.fromJson(((JsonObject) jsonObject.get("data")).get("lastRoundFirstType"), String.class);
            String type = "";
            List<Object[]> playerList = roomList.get(roomCode);
            if (lastRoundStartType.equals("")) {
                int rd = Math.random() > 0.5 ? 1 : 0;
                type = rd == 1 ? "cross" : "circle";
            } else if (lastRoundStartType.equals("cross")) {
                type = "circle";
            } else if (lastRoundStartType.equals("circle")) {
                type = "cross";
            } else {
                throw new Exception("illegal type");
            }
            for (Object[] player : playerList) {
                JSONData jsonData = new JSONData();
                jsonData.setMsgType("gameStart");
                jsonData.setData(type);
                jsonData.setStatus(200);
                jsonData.setMsg("ok");
                Session temp = (Session) player[1];
                synchronized (temp) {
                    temp.getBasicRemote().sendText(gson.toJson(jsonData));
                }
            }
        }

        if (jsonObject.get("msgType").getAsString().equals("playAgain")) {
            String roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), String.class);
            List<Object[]> playerList = roomList.get(roomCode);
            TicTacToePlayer ticTacToePlayer = gson.fromJson(((JsonObject) jsonObject.get("data")).get("ticPlayer"), TicTacToePlayer.class);
            for (Object[] player : playerList) {
                JSONData jsonData = new JSONData();
                jsonData.setMsgType("playAgain");
                jsonData.setData(ticTacToePlayer);
                jsonData.setStatus(200);
                jsonData.setMsg("ok");
                Session temp = (Session) player[1];
                synchronized (temp) {
                    temp.getBasicRemote().sendText(gson.toJson(jsonData));
                }
            }
        }


        if (jsonObject.get("msgType").getAsString().equals("chatMsg")) {
            String roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), String.class);
            List<Object[]> playerList = roomList.get(roomCode);
            TicTacToePlayer ticTacToePlayer = gson.fromJson(((JsonObject) jsonObject.get("data")).get("ticPlayer"), TicTacToePlayer.class);
            String chatMessage = gson.fromJson(((JsonObject) jsonObject.get("data")).get("chatMessage"), String.class);

            Map<String, Object> data = new HashMap<>();
            data.put("ticPlayer", ticTacToePlayer);
            data.put("chatMessage", chatMessage);

            for (Object[] player : playerList) {
                JSONData jsonData = new JSONData();
                jsonData.setMsgType("chatMsg");
                jsonData.setData(data);
                jsonData.setStatus(200);
                jsonData.setMsg("ok");
                Session temp = (Session) player[1];
                synchronized (temp) {
                    temp.getAsyncRemote().sendText(gson.toJson(jsonData));
                }
            }
        }

        if (jsonObject.get("msgType").getAsString().equals("leave")) {
            String roomCode = gson.fromJson(((JsonObject) jsonObject.get("data")).get("roomCode"), String.class);
            TicTacToePlayer ticPlayer = gson.fromJson(((JsonObject) jsonObject.get("data")).get("ticPlayer"), TicTacToePlayer.class);
            List<Object[]> room = roomList.get(roomCode);
            TicTacToePlayer temp = (TicTacToePlayer)room.get(0)[0];
            if (temp.getUserId() == ticPlayer.getUserId()) {
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

    }

}
