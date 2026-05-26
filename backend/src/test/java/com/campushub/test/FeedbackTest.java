package com.campushub.test;

import com.campushub.dto.AdminFeedbackUpdateRequest;
import com.campushub.dto.FeedbackCreateRequest;
import com.campushub.entity.Feedback;
import com.campushub.service.FeedbackService;
import com.campushub.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FeedbackTest extends IntegrationTestSupport {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private MessageService messageService;

    @Test
    public void testAdminCanMoveFeedbackToInProgressAndNotifyUser() {
        Feedback feedback = createFeedback();

        AdminFeedbackUpdateRequest request = new AdminFeedbackUpdateRequest();
        request.setStatus("in_progress");

        Feedback updated = feedbackService.updateFeedback(4L, feedback.getId(), request);

        assertNotNull(updated.getHandledAt());
        assertEquals("in_progress", updated.getStatus());
        assertTrue(messageService.getMessagesByReceiverId(1L).stream()
            .anyMatch(message -> message.getContent().contains("[Feedback Status]") && message.getContent().contains("in progress")));
    }

    @Test
    public void testResolvingFeedbackRequiresReply() {
        Feedback feedback = createFeedback();

        AdminFeedbackUpdateRequest request = new AdminFeedbackUpdateRequest();
        request.setStatus("resolved");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> feedbackService.updateFeedback(4L, feedback.getId(), request));
        assertEquals("Resolved feedback requires a reply", exception.getMessage());
    }

    @Test
    public void testAdminCanResolveFeedbackWithReply() {
        Feedback feedback = createFeedback();

        AdminFeedbackUpdateRequest request = new AdminFeedbackUpdateRequest();
        request.setStatus("resolved");
        request.setAdminReply("Issue confirmed and fixed. Please try again.");

        Feedback updated = feedbackService.updateFeedback(4L, feedback.getId(), request);

        assertEquals("resolved", updated.getStatus());
        assertEquals("Issue confirmed and fixed. Please try again.", updated.getAdminReply());
        assertTrue(messageService.getMessagesByReceiverId(1L).stream()
            .anyMatch(message -> message.getContent().contains("[Feedback Reply]") && message.getContent().contains("Issue confirmed and fixed")));
    }

    @Test
    public void testFeedbackPriorityDefaultsByType() {
        FeedbackCreateRequest request = new FeedbackCreateRequest();
        request.setType("SUGGESTION");
        request.setTitle("Add saved search");
        request.setContent("It would be helpful to save common filters.");

        Feedback suggestion = feedbackService.createFeedback(1L, request);

        assertEquals("NORMAL", suggestion.getPriority());
    }

    @Test
    public void testAdminCanAdjustFeedbackPriority() {
        Feedback feedback = createFeedback();

        AdminFeedbackUpdateRequest request = new AdminFeedbackUpdateRequest();
        request.setStatus("in_progress");
        request.setPriority("URGENT");
        request.setAdminReply("Escalated for immediate review.");

        Feedback updated = feedbackService.updateFeedback(4L, feedback.getId(), request);

        assertEquals("URGENT", updated.getPriority());
        assertEquals("Escalated for immediate review.", updated.getAdminReply());
    }

    private Feedback createFeedback() {
        FeedbackCreateRequest request = new FeedbackCreateRequest();
        request.setType("BUG");
        request.setTitle("Message page issue");
        request.setContent("The conversation list sometimes renders blank.");
        return feedbackService.createFeedback(1L, request);
    }
}
