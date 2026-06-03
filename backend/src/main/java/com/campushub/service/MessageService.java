package com.campushub.service;

import com.campushub.dto.MessageRequest;
import com.campushub.entity.Message;
import com.campushub.mapper.MessageMapper;
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
        Message message = messageMapper.selectByIdLight(messageId);
        if (message == null) {
            throw new RuntimeException("消息不存在");
        }
        return markAsRead(messageId, message.getReceiverId());
    }

    public Message markAsRead(Long messageId, Long currentUserId) {
        // Use lightweight query — no JOINs needed for status update
        Message message = messageMapper.selectByIdLight(messageId);
        if (message == null) {
            throw new RuntimeException("消息不存在");
        }
        if (!currentUserId.equals(message.getReceiverId())) {
            throw new RuntimeException("无权修改该消息状态");
        }
        messageMapper.updateStatus(messageId, "read");
        message.setStatus("read");
        return message;
    }

    public int markAsReadBatch(List<Long> messageIds, Long currentUserId) {
        if (messageIds == null || messageIds.isEmpty()) return 0;
        return messageMapper.markAsReadBatch(messageIds, currentUserId);
    }

    public int getUnreadCount(Long receiverId) {
        return messageMapper.countUnreadByReceiverId(receiverId);
    }

}
