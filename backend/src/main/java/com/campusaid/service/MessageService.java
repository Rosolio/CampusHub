package com.campusaid.service;

import com.campusaid.dto.MessageRequest;
import com.campusaid.entity.Message;
import com.campusaid.mapper.MessageMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private final MessageMapper messageMapper;

    public MessageService(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    public List<Message> getMessagesByReceiverId(Long receiverId) {
        return messageMapper.selectByReceiverId(receiverId);
    }

    public List<Message> getMessagesByUserId(Long userId) {
        return messageMapper.selectByUserId(userId);
    }

    public Message sendMessage(MessageRequest request, Long senderId) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(request.getReceiverId());
        message.setTaskId(request.getTaskId());
        message.setContent(request.getContent());
        message.setStatus("unread");
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);
        return message;
    }

    public Message sendSystemTaskMessage(Long senderId, Long receiverId, Long taskId, String content) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setTaskId(taskId);
        message.setContent(content);
        message.setStatus("unread");
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);
        return message;
    }

    public Message markAsRead(Long messageId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new RuntimeException("消息不存在");
        }
        message.setStatus("read");
        messageMapper.update(message);
        return message;
    }

    public int getUnreadCount(Long receiverId) {
        return messageMapper.countUnreadByReceiverId(receiverId);
    }

}
