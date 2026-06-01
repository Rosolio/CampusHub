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

    /** Batch mark messages as read — single request for multiple messages */
    @PutMapping("/read")
    public Map<String, Object> markAsReadBatch(@RequestBody Map<String, List<Long>> body, Authentication authentication) {
        List<Long> ids = body.getOrDefault("ids", List.of());
        int count = messageService.markAsReadBatch(ids, getCurrentUserId(authentication));
        return Map.of("count", count);
    }

    @GetMapping("/unread/count")
    public Map<String, Integer> getUnreadCount(Authentication authentication) {
        int count = messageService.getUnreadCount(getCurrentUserId(authentication));
        return Map.of("count", count);
    }

    private Long getCurrentUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }

}
