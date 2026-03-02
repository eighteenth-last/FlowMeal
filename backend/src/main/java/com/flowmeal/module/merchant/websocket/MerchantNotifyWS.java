package com.flowmeal.module.merchant.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 服务端 - 商家实时新订单通知
 *
 * 前端连接地址: ws://localhost:8080/api/ws/merchant/{merchantId}
 * 服务端推送: MerchantNotifyWS.sendToMerchant(merchantId, message)
 */
@Slf4j
@Component
@ServerEndpoint("/ws/merchant/{merchantId}")
public class MerchantNotifyWS {

    /** 维护商家ID -> Session 的映射 */
    private static final ConcurrentHashMap<Long, Session> SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("merchantId") Long merchantId) {
        SESSIONS.put(merchantId, session);
        log.info("商家 WebSocket 连接建立: merchantId={}", merchantId);
    }

    @OnClose
    public void onClose(@PathParam("merchantId") Long merchantId) {
        SESSIONS.remove(merchantId);
        log.info("商家 WebSocket 连接断开: merchantId={}", merchantId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 错误: ", error);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("merchantId") Long merchantId) {
        // 心跳处理
        if ("ping".equals(message)) {
            sendToMerchant(merchantId, "pong");
        }
    }

    /**
     * 向指定商家推送消息（新订单通知等）
     */
    public static void sendToMerchant(Long merchantId, String message) {
        Session session = SESSIONS.get(merchantId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.debug("推送消息给商家 {}: {}", merchantId, message);
            } catch (IOException e) {
                log.error("WebSocket 消息发送失败: merchantId={}", merchantId, e);
            }
        }
    }

    /**
     * 广播消息（通知所有在线商家）
     */
    public static void broadcast(String message) {
        SESSIONS.forEach((merchantId, session) -> {
            if (session.isOpen()) {
                sendToMerchant(merchantId, message);
            }
        });
    }
}
