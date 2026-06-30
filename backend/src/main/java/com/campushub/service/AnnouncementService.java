package com.campushub.service;

import com.campushub.dto.AdminAnnouncementRequest;
import com.campushub.entity.Announcement;
import com.campushub.mapper.AnnouncementMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final UserService userService;

    public AnnouncementService(AnnouncementMapper announcementMapper, UserService userService) {
        this.announcementMapper = announcementMapper;
        this.userService = userService;
    }

    public List<Announcement> getAnnouncements() {
        return announcementMapper.selectAll();
    }

    public Announcement createAnnouncement(Long adminId, AdminAnnouncementRequest request) {
        requireAdmin(adminId);
        String title = trimToNull(request.getTitle());
        String content = trimToNull(request.getContent());
        if (title == null) {
            throw new RuntimeException("公告标题不能为空");
        }
        if (content == null) {
            throw new RuntimeException("公告内容不能为空");
        }

        Announcement announcement = new Announcement();
        announcement.setAuthorId(adminId);
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setPinned(request.getPinned() == null ? Boolean.TRUE : request.getPinned());
        announcement.setCreatedAt(LocalDateTime.now());
        announcement.setUpdatedAt(LocalDateTime.now());
        announcementMapper.insert(announcement);
        return announcementMapper.selectById(announcement.getId());
    }

    private void requireAdmin(Long userId) {
        if (!userService.isAdmin(userId)) {
            throw new RuntimeException("无管理员权限");
        }
    }

    public void deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
    }

    /** 每天凌晨 3 点执行：删除超过 7 天的公告 */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldAnnouncements() {
        int deleted = announcementMapper.deleteOlderThanDays(7);
        if (deleted > 0) {
            System.out.println("[清理] 删除了 " + deleted + " 条超过 7 天的公告");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
