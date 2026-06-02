package com.campushub.dto;

import java.util.Objects;
import java.util.StringJoiner;

public class TaskRecommendationQuery {
    private String mode;
    private String category;
    private String location;
    private String availableAt;
    private Integer limit;
    private Integer page;
    private Integer size;
    private String taskMode;
    private String sortBy;
    private String status;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAvailableAt() {
        return availableAt;
    }

    public void setAvailableAt(String availableAt) {
        this.availableAt = availableAt;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getTaskMode() {
        return taskMode;
    }

    public void setTaskMode(String taskMode) {
        this.taskMode = taskMode;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getEffectivePage() {
        return page == null || page < 1 ? 1 : page;
    }

    public int getEffectiveSize() {
        if (size == null || size < 1) return 20;
        return Math.min(size, 100);
    }

    public int getEffectiveOffset() {
        return (getEffectivePage() - 1) * getEffectiveSize();
    }

    /** Build a deterministic cache key from all query parameters */
    public String toCacheKey() {
        StringJoiner sj = new StringJoiner(":");
        sj.add(nullToEmpty(mode));
        sj.add(nullToEmpty(category));
        sj.add(nullToEmpty(location));
        sj.add(nullToEmpty(availableAt));
        sj.add(String.valueOf(getEffectivePage()));
        sj.add(String.valueOf(getEffectiveSize()));
        sj.add(nullToEmpty(taskMode));
        sj.add(nullToEmpty(sortBy));
        sj.add(nullToEmpty(status));
        return sj.toString();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskRecommendationQuery that)) return false;
        return Objects.equals(mode, that.mode)
            && Objects.equals(category, that.category)
            && Objects.equals(location, that.location)
            && Objects.equals(availableAt, that.availableAt)
            && Objects.equals(page, that.page)
            && Objects.equals(size, that.size)
            && Objects.equals(taskMode, that.taskMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, category, location, availableAt, page, size, taskMode);
    }
}
