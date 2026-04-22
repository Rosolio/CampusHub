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
    private final UserService userService;

    public MessageService(MessageMapper messageMapper, UserService userService) {
        this.messageMapper = messageMapper;
        this.userService = userService;
    }

    public List<Message> getMessagesByReceiverId(Long receiverId) {
        return messageMapper.selectByReceiverId(receiverId);
    }

    public List<Message> getMessagesByUserId(Long userId) {
        return messageMapper.selectByUserId(userId);
    }

    public Message sendMessage(MessageRequest request, Long senderId) {
        if (userService.isAdmin(senderId) || userService.isAdmin(request.getReceiverId())) {
            throw new RuntimeException("管理员账号不参与普通用户私信沟通，请改用社区反馈或后台处理");
        }
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
