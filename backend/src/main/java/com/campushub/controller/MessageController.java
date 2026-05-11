package com.campushub.controller;

import com.campushub.dto.MessageRequest;
import com.campushub.entity.Message;
import com.campushub.service.MessageService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public List<Message> getMessages(Authentication authentication) {
        return messageService.getMessagesByUserId(getCurrentUserId(authentication));
    }

    @PostMapping
    public Message sendMessage(@RequestBody MessageRequest request, Authentication authentication) {
        return messageService.sendMessage(request, getCurrentUserId(authentication));
    }

    @PutMapping("/{id}/read")
    public Message markAsRead(@PathVariable Long id, Authentication authentication) {
        return messageService.markAsRead(id, getCurrentUserId(authentication));
    }

    @GetMapping("/unread/count")
    public Map<String, Integer> getUnreadCount(Authentication authentication) {
        int count = messageService.getUnreadCount(getCurrentUserId(authentication));
        Map<String, Integer> result = new java.util.HashMap<>();
        result.put("count", count);
        return result;
    }

    private Long getCurrentUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }

}
