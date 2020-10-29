package com.example.demowebsocket_base.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一对一
 */
@ServerEndpoint("/websocket/{name}")
@RestController
@Slf4j
public class WebSocketServer {

    private Session session;

    private String name;

    //存储客户端的连接对象,每个客户端连接都会产生一个连接对象
    private static ConcurrentHashMap<String,WebSocketServer> serverConcurrentHashMap = new ConcurrentHashMap<>();


    @OnOpen
    public void onOpen(@PathParam("name")String name,Session session){

        this.session = session;
        this.name = name;

        serverConcurrentHashMap.put(name,this);
        log.info(name +" 连接服务器成功!");
        log.info(" [websocket消息] 有新的连接，总数:{} " ,serverConcurrentHashMap.size());

    }

    @OnClose
    public void onClose(){
        serverConcurrentHashMap.remove(name);
        log.info("[websocket消息] 连接断开，name={}",name);
    }

    @OnError
    public void onError(Throwable throwable){
        log.info(throwable.getMessage());
        log.info("[websocket消息] 错误出现异常 name={}",name);
    }

    @OnMessage
    public void onMessage(String message){
        log.info("收到  " + name + " :" + message);
        log.info(" [websocket消息] 有新的连接，总数:{} " ,serverConcurrentHashMap.size());

        Set<Map.Entry<String,WebSocketServer>> entries = serverConcurrentHashMap.entrySet();
        for (Map.Entry<String,WebSocketServer> entry : entries) {
            if (entry.getKey().equals(name)){//将消息发送到其它非自身客服端
                entry.getValue().send(message);
//                entry.getValue().session.getBasicRemote().sendText(message);
            }
        }
    }

    public void send(String message){
        if (session.isOpen()){
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
