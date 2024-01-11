package com.example.demo.config;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.ObjectUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Configuration
@EnableWebSocketMessageBroker // 启用WebSocket消息代理功能和配置STOMP协议，实现实时双向通信和消息传递
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 注册一个端点，客户端通过这个端点进行连接
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")   // 注册了一个 /ws 的端点
                .setAllowedOriginPatterns("*") // 允许跨域的 WebSocket 连接
                .withSockJS();  // 启用 SockJS (浏览器不支持WebSocket，SockJS 将会提供兼容性支持)
    }

    /**
     * 配置消息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 客户端发送消息的请求前缀
        registry.setApplicationDestinationPrefixes("/app");

        // 客户端订阅消息的请求前缀，topic一般用于广播推送，queue用于点对点推送
        registry.enableSimpleBroker("/topic", "/queue","/gogo");

        // 服务端通知客户端的前缀，可以不设置，默认为user
//        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 配置客户端入站通道拦截器
     * <p>
     * 添加 ChannelInterceptor 拦截器，用于在消息发送前，从请求头中获取 token 并解析出用户信息(deviceId)，用于点对点发送消息给指定用户
     *
     * @param registration 通道注册器
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                // 如果是连接请求（CONNECT 命令），从请求头中取出 token 并设置到认证信息中
                log.info("拦截到请求,message为:{}",message);
                log.info("拦截到请求,channel为:{}",channel);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    //获取设备唯一id
                    String deviceId = accessor.getFirstNativeHeader("name");
                    log.info("是连接请求,设备id为:{}",deviceId);
                    Principal principal = new Principal() {
                        @Override
                        public String getName() {
                            return deviceId;
                        }
                    };
                    accessor.setUser(principal);
                    //若为空,抛出异常
                    if (ObjectUtil.isNull(deviceId)){
                        log.info("设备id为null,抛出异常");
                    }
                }
                //若不是连接请求，则放行
                return ChannelInterceptor.super.preSend(message, channel);
            }
        });
    }
}