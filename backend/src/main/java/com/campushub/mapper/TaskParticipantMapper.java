package com.campushub.mapper;

import com.campushub.entity.TaskParticipant;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TaskParticipantMapper {
    TaskParticipant selectById(Long id);
    List<TaskParticipant> selectByTaskId(Long taskId);
    List<TaskParticipant> selectByParticipantId(Long participantId);
    List<TaskParticipant> selectByTaskIdAndRole(Long taskId, String role);
    TaskParticipant selectByTaskIdAndParticipantIdAndRole(Long taskId, Long participantId, String role);
    int insert(TaskParticipant taskParticipant);
    int update(TaskParticipant taskParticipant);
    int delete(Long id);
    int deleteByTaskId(Long taskId);
}
