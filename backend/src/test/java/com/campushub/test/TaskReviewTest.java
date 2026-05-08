package com.campushub.test;

import com.campushub.dto.TaskCreateRequest;
import com.campushub.dto.TaskReviewCreateRequest;
import com.campushub.entity.Task;
import com.campushub.entity.TaskReview;
import com.campushub.mapper.TaskReviewMapper;
import com.campushub.mapper.UserMapper;
import com.campushub.service.TaskReviewService;
import com.campushub.service.TaskService;
import com.campushub.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskReviewTest extends IntegrationTestSupport {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskReviewService taskReviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TaskReviewMapper taskReviewMapper;

    @Test
    public void testCreateDualReviewsAndUpdateScore() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("评价测试任务");
        request.setDescription("用于验证互评与信用分");
        request.setBadgePrimary("普通");
        request.setBadgeSecondary("校园配送");
        request.setLocationText("南楼");
        request.setTimeText("今晚");
        request.setRewardTitle("奖励");
        request.setRewardText("10 积分");
        request.setImpactTitle("任务类型");
        request.setImpactText("errand");

        Task task = taskService.createTask(request, 1L);
        taskService.acceptTask(task.getId(), 2L);
        taskService.completeTask(task.getId(), 1L);
        taskService.completeTask(task.getId(), 2L);

        int requesterPointsBeforeReview = userMapper.selectById(1L).getPoints();
        int helperPointsBeforeReview = userMapper.selectById(2L).getPoints();

        TaskReviewCreateRequest requesterReview = new TaskReviewCreateRequest();
        requesterReview.setRating(5);
        requesterReview.setContent("响应很快，沟通顺畅。");
        TaskReview reviewToHelper = taskReviewService.createTaskReview(task.getId(), requesterReview, 1L);

        TaskReviewCreateRequest helperReview = new TaskReviewCreateRequest();
        helperReview.setRating(4);
        helperReview.setContent("需求描述清楚，配合度高。");
        TaskReview reviewToRequester = taskReviewService.createTaskReview(task.getId(), helperReview, 2L);

        assertNotNull(reviewToHelper.getId());
        assertNotNull(reviewToRequester.getId());
        assertEquals(
            taskReviewMapper.selectAverageRatingByRevieweeId(2L).setScale(2, RoundingMode.HALF_UP),
            userMapper.selectById(2L).getScore()
        );
        assertEquals(
            taskReviewMapper.selectAverageRatingByRevieweeId(1L).setScale(2, RoundingMode.HALF_UP),
            userMapper.selectById(1L).getScore()
        );
        assertEquals(requesterPointsBeforeReview + 8, userMapper.selectById(1L).getPoints());
        assertEquals(helperPointsBeforeReview + 11, userMapper.selectById(2L).getPoints());

        List<TaskReview> reviews = taskReviewService.getTaskReviews(task.getId());
        assertEquals(2, reviews.size());
    }

    @Test
    public void testCannotReviewSameTaskTwice() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("重复评价测试");
        request.setDescription("同一用户不能重复评价");
        request.setBadgePrimary("普通");
        request.setBadgeSecondary("校园配送");
        request.setLocationText("教学楼");
        request.setTimeText("明天");
        request.setRewardTitle("奖励");
        request.setRewardText("10 积分");
        request.setImpactTitle("任务类型");
        request.setImpactText("errand");

        Task task = taskService.createTask(request, 1L);
        taskService.acceptTask(task.getId(), 2L);
        taskService.completeTask(task.getId(), 1L);
        taskService.completeTask(task.getId(), 2L);

        TaskReviewCreateRequest reviewRequest = new TaskReviewCreateRequest();
        reviewRequest.setRating(5);
        taskReviewService.createTaskReview(task.getId(), reviewRequest, 1L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskReviewService.createTaskReview(task.getId(), reviewRequest, 1L));
        assertEquals("你已经评价过这项任务", exception.getMessage());
    }

    @Test
    public void testLowStarReviewCanDeductRevieweePoints() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("低分评价测试");
        request.setDescription("验证低分评价积分结算");
        request.setBadgePrimary("普通");
        request.setBadgeSecondary("校园配送");
        request.setLocationText("图书馆");
        request.setTimeText("今晚");
        request.setRewardTitle("奖励");
        request.setRewardText("10 积分");
        request.setImpactTitle("任务类型");
        request.setImpactText("errand");

        Task task = taskService.createTask(request, 1L);
        taskService.acceptTask(task.getId(), 2L);
        taskService.completeTask(task.getId(), 1L);
        taskService.completeTask(task.getId(), 2L);

        int helperPointsBeforeReview = userMapper.selectById(2L).getPoints();

        TaskReviewCreateRequest requesterReview = new TaskReviewCreateRequest();
        requesterReview.setRating(1);
        requesterReview.setContent("履约明显不符合预期。");
        taskReviewService.createTaskReview(task.getId(), requesterReview, 1L);

        assertEquals(helperPointsBeforeReview - 3, userMapper.selectById(2L).getPoints());
    }
}
