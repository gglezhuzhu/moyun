package com.moyun.reading.entity.discussion;

import com.moyun.reading.entity.base.BaseEntity;
import com.moyun.reading.entity.base.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 讨论实体类
 * 
 * @author Moyun Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "discussions")
public class Discussion extends BaseEntity {

    @NotBlank(message = "讨论标题不能为空")
    @Size(max = 200, message = "讨论标题长度不能超过200个字符")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotBlank(message = "讨论内容不能为空")
    @Size(max = 3000, message = "讨论内容长度不能超过3000个字符")
    @Column(name = "content", nullable = false, length = 3000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DiscussionType type = DiscussionType.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DiscussionStatus status = DiscussionStatus.OPEN;

    @OneToMany(mappedBy = "discussion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiscussionReply> replies;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @Column(name = "is_answered", nullable = false)
    private Boolean isAnswered = false;

    /**
     * 讨论类型枚举
     */
    public enum DiscussionType {
        GENERAL("一般讨论"),
        QUESTION("问题咨询"),
        BOOK_DISCUSSION("图书讨论"),
        READING_EXPERIENCE("阅读心得");

        private final String displayName;

        DiscussionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 讨论状态枚举
     */
    public enum DiscussionStatus {
        OPEN("开放"),
        CLOSED("关闭"),
        LOCKED("锁定");

        private final String displayName;

        DiscussionStatus(String displayName) {
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
     * 获取回复数量
     */
    public int getReplyCount() {
        return replies != null ? replies.size() : 0;
    }

    /**
     * 是否已解决
     */
    public boolean isResolved() {
        return isAnswered != null && isAnswered;
    }

    /**
     * 标记为已解决
     */
    public void markAsAnswered() {
        this.isAnswered = true;
    }

    /**
     * 是否为问题类型
     */
    public boolean isQuestion() {
        return DiscussionType.QUESTION.equals(type);
    }
}