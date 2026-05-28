package com.campushub.dto;

public class AdminVerificationReviewRequest {
    private String status;
    private String rejectReason;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}
