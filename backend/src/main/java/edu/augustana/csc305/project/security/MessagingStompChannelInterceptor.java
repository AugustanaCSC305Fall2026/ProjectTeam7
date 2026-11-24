package edu.augustana.csc305.project.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.bson.types.ObjectId;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

/** Authenticates STOMP connections from the same bearer JWT used by the REST API. */
public class MessagingStompChannelInterceptor implements ChannelInterceptor {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String PRIVATE_MESSAGE_DESTINATION = "/user/queue/messages";

    private final JwtService jwtService;

    public MessagingStompChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (accessor.getCommand() == StompCommand.CONNECT) {
            authenticate(accessor);
            return message;
        }

        if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            if (accessor.getUser() == null
                    || !PRIVATE_MESSAGE_DESTINATION.equals(accessor.getDestination())) {
                throw new MessagingException("Unauthorized messaging subscription");
            }
            return message;
        }

        if (accessor.getCommand() == StompCommand.SEND) {
            // Messages are created through the existing REST service so membership checks,
            // persistence, and event publication have one authoritative path.
            throw new MessagingException("Client message publishing is not allowed");
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new MessagingException("A bearer token is required");
        }

        try {
            Claims claims = jwtService.parseToken(authorization.substring(BEARER_PREFIX.length()));
            String userId = claims.get("id", String.class);
            if (userId == null || !ObjectId.isValid(userId)) {
                throw new MessagingException("Invalid authenticated user");
            }

            List<GrantedAuthority> authorities = new ArrayList<>();
            Object rolesClaim = claims.get("roles");
            if (rolesClaim instanceof List<?> roles) {
                for (Object role : roles) {
                    if (role instanceof String roleName && !roleName.isBlank()) {
                        String authority = roleName.startsWith("ROLE_")
                                ? roleName : "ROLE_" + roleName;
                        authorities.add(new SimpleGrantedAuthority(authority));
                    }
                }
            }

            // Use the trusted JWT user ID as Principal.getName(), which is the key used
            // by convertAndSendToUser. Never accept a user ID from the browser payload.
            accessor.setUser(new UsernamePasswordAuthenticationToken(
                    userId, null, authorities));
        } catch (JwtException | IllegalArgumentException exception) {
            throw new MessagingException("Invalid or expired bearer token", exception);
        }
    }
}
