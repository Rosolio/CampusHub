package com.campushub.mapper;

import com.campushub.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    Message selectById(Long id);
    List<Message> selectByUserId(Long userId);
    List<Message> selectBySenderId(Long senderId);
    List<Message> selectByReceiverId(Long receiverId);
    List<Message> selectByTaskId(Long taskId);
    List<Message> selectUnreadByReceiverId(Long receiverId);
    int insert(Message message);
    int update(Message message);
    int delete(Long id);
    int countUnreadByReceiverId(Long receiverId);
}
