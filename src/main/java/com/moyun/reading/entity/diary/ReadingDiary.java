package com.moyun.reading.entity.diary;

import com.moyun.reading.entity.base.BaseEntity;
import com.moyun.reading.entity.base.User;
import com.moyun.reading.entity.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 读书日志实体类
 * 
 * @author Moyun Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "reading_diaries")
public class ReadingDiary extends BaseEntity {

    @NotBlank(message = "日志标题不能为空")
    @Size(max = 200, message = "日志标题长度不能超过200个字符")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotBlank(message = "日志内容不能为空")
    @Size(max = 5000, message = "日志内容长度不能超过5000个字符")
    @Column(name = "content", nullable = false, length = 5000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood", nullable = false)
    private ReadingMood mood = ReadingMood.NEUTRAL;

    @Column(name = "reading_progress")
    private Integer readingProgress;

    @Column(name = "reading_time_minutes")
    private Integer readingTimeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy", nullable = false)
    private DiaryPrivacy privacy = DiaryPrivacy.PUBLIC;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiaryComment> comments;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    /**
     * 阅读心情枚举
     */
    public enum ReadingMood {
        EXCITED("兴奋"),
        HAPPY("愉快"),
        PEACEFUL("平静"),
        NEUTRAL("一般"),
        THOUGHTFUL("深思"),
        CONFUSED("困惑"),
        DISAPPOINTED("失望");

        private final String displayName;

        ReadingMood(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getEmoji() {
            return switch (this) {
                case EXCITED -> "🤩";
                case HAPPY -> "😊";
                case PEACEFUL -> "😌";
                case NEUTRAL -> "😐";
                case THOUGHTFUL -> "🤔";
                case CONFUSED -> "😕";
                case DISAPPOINTED -> "😞";
            };
        }
    }

    /**
     * 日志隐私设置枚举
     */
    public enum DiaryPrivacy {
        PUBLIC("公开"),
        FRIENDS_ONLY("仅好友可见"),
        PRIVATE("私密");

        private final String displayName;

        DiaryPrivacy(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 增加浏览次数
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 增加点赞数
     */
    public void incrementLikeCount() {
        this.likeCount++;
    }

    /**
     * 减少点赞数
     */
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    /**
     * 获取评论数量
     */
    public int getCommentCount() {
        return comments != null ? comments.size() : 0;
    }

    /**
     * 是否公开
     */
    public boolean isPublic() {
        return DiaryPrivacy.PUBLIC.equals(privacy);
    }

    /**
     * 是否私密
     */
    public boolean isPrivate() {
        return DiaryPrivacy.PRIVATE.equals(privacy);
    }

    /**
     * 是否仅好友可见
     */
    public boolean isFriendsOnly() {
        return DiaryPrivacy.FRIENDS_ONLY.equals(privacy);
    }

    /**
     * 获取阅读进度描述
     */
    public String getProgressDescription() {
        if (readingProgress == null) {
            return "未记录";
        }
        if (readingProgress >= 100) {
            return "已完成";
        }
        return readingProgress + "%";
    }

    /**
     * 获取阅读时长描述
     */
    public String getReadingTimeDescription() {
        if (readingTimeMinutes == null || readingTimeMinutes == 0) {
            return "未记录";
        }
        if (readingTimeMinutes < 60) {
            return readingTimeMinutes + "分钟";
        }
        int hours = readingTimeMinutes / 60;
        int minutes = readingTimeMinutes % 60;
        if (minutes == 0) {
            return hours + "小时";
        }
        return hours + "小时" + minutes + "分钟";
    }
}