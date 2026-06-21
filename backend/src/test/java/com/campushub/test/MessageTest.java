package com.campushub.test;

import com.campushub.dto.MessageRequest;
import com.campushub.entity.Message;
import com.campushub.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTest extends IntegrationTestSupport {

    @Autowired
    private MessageService messageService;

    @Test
    public void testSendMessage() {
        MessageRequest request = new MessageRequest();
        request.setReceiverId(2L);
        request.setTaskId(1L);
        request.setContent("测试消息内容");

        Message message = messageService.sendMessage(request, 1L);
        assertNotNull(message);
        assertEquals("测试消息内容", message.getContent());
        assertEquals(1L, message.getSenderId());
        assertEquals(2L, message.getReceiverId());
    }

    @Test
    public void testGetMessages() {
        MessageRequest request = new MessageRequest();
        request.setReceiverId(2L);
        request.setTaskId(1L);
        request.setContent("测试会话消息");
        messageService.sendMessage(request, 1L);

        List<Message> messages = messageService.getMessagesByUserId(1L);
        assertNotNull(messages);
        assertFalse(messages.isEmpty());
    }

    @Test
    public void testMarkAsRead() {
        // 先发送一条消息
        MessageRequest request = new MessageRequest();
        request.setReceiverId(2L);
        request.setTaskId(1L);
        request.setContent("测试消息内容");

        Message message = messageService.sendMessage(request, 1L);
        Long messageId = message.getId();

        // 测试标记为已读
        Message markedMessage = messageService.markAsRead(messageId);
        assertNotNull(markedMessage);
        assertEquals("read", markedMessage.getStatus());
    }

    @Test
    public void testGetUnreadCount() {
        int count = messageService.getUnreadCount(2L);
        assertTrue(count >= 0);
    }

    @Test
    public void testBatchMarkAsReadOnlyUpdatesReceiverMessages() {
        MessageRequest request = new MessageRequest();
        request.setReceiverId(2L);
        request.setTaskId(1L);
        request.setContent("批量已读权限测试");
        Message message = messageService.sendMessage(request, 1L);

        int before = messageService.getUnreadCount(2L);
        int wrongUserUpdated = messageService.markAsReadBatch(List.of(message.getId()), 1L);
        assertEquals(0, wrongUserUpdated);
        assertEquals(before, messageService.getUnreadCount(2L));

        int receiverUpdated = messageService.markAsReadBatch(List.of(message.getId()), 2L);
        assertEquals(1, receiverUpdated);
        assertEquals(before - 1, messageService.getUnreadCount(2L));
    }

}
