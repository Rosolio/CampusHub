package com.campushub.controller;

import com.campushub.dto.FeedbackCreateRequest;
import com.campushub.entity.Feedback;
import com.campushub.service.FeedbackService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public Feedback createFeedback(@RequestBody FeedbackCreateRequest request, Authentication authentication) {
        return feedbackService.createFeedback(getCurrentUserId(authentication), request);
    }

    @GetMapping("/my")
    public List<Feedback> getMyFeedback(Authentication authentication) {
        return feedbackService.getMyFeedback(getCurrentUserId(authentication));
    }

    @DeleteMapping("/{feedbackId}")
    public void withdrawFeedback(@PathVariable Long feedbackId, Authentication authentication) {
        feedbackService.withdrawFeedback(getCurrentUserId(authentication), feedbackId);
    }

    private Long getCurrentUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
