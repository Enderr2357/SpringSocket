package com.example.demo.Controller;

import com.example.demo.domain.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * WebSocket 测试控制器
 *
 * @author haoxr
 * @since 2.3.0
 */
@RestController
@RequestMapping("/websocket")
@RequiredArgsConstructor
@Slf4j
public class WebsocketController {

    private final SimpMessagingTemplate messagingTemplate;


    /**
     * 广播发送消息
     *
     * @param message 消息内容
     */
    @MessageMapping("/sendToAll")
    @SendTo("/topic/notice")
    public String sendToAll(String message) {
        return "服务端通知: " + message;
    }

    /**
     * 点对点发送消息
     * <p>
     * 模拟 张三 给 李四 发送消息场景
     *
     * @param principal 当前用户
     * @param username  接收消息的用户
     * @param message   消息内容
     */
    @MessageMapping("/sendToUser/{deviceId}")
//    @SendTo("/gogo/greeting")
//    @SendTo("/queue/greeting")
    public String sendToUser(Principal principal,@DestinationVariable String deviceId, String message,@Headers Map<String, Object> headers) {
//       LinkedMultiValueMap<String, String> nativeHeaders = (LinkedMultiValueMap<String, String>) headers.get("nativeHeaders");
//        String sender=nativeHeaders.getFirst("name");
        String  sender = principal.getName(); // 发送人
        log.info("发送人为:{}",sender);
        String receiver = deviceId; // 接收人

        log.info("接收人:{},接收数据为:{}", receiver,message);
        // 发送消息给指定用户 /user/{username}/queue/greeting
        messagingTemplate.convertAndSendToUser(receiver, "/queue/greeting", new ChatMessage(sender, message));
        return message;
    }
}