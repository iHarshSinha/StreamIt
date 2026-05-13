package com.streamit.groupchatapp.controller;

import com.streamit.groupchatapp.dto.WsMessageRequest;
import com.streamit.groupchatapp.security.principal.UserPrincipal;
import com.streamit.groupchatapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final MessageService messageService;

    @MessageMapping("/chat.send")
    public void send(WsMessageRequest request, Principal principal) {
        UserPrincipal userPrincipal =
                (UserPrincipal) ((Authentication) principal).getPrincipal();

        messageService.sendMessage(request, userPrincipal);
    }
}
