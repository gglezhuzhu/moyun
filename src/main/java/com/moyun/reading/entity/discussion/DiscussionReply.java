package com.moyun.reading.entity.discussion;

import com.moyun.reading.entity.base.BaseEntity;
import com.moyun.reading.entity.base.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 讨论回复实体类
 * 
 * @author Moyun Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "discussion_replies")
public class DiscussionReply extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discussion_id", nullable = false)
    private Discussion discussion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 2000, message = "回复内容长度不能超过2000个字符")
    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private DiscussionReply parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_user_id")
    private User replyToUser;

    @Column(name = "is_answer", nullable = false)
    private Boolean isAnswer = false;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    /**
     * 是否为顶级回复
     */
    public boolean isTopLevel() {
        return parent == null;
    }

    /**
     * 是否为嵌套回复
     */
    public boolean isNestedReply() {
        return parent != null;
    }

    /**
     * 标记为最佳答案
     */
    public void markAsAnswer() {
        this.isAnswer = true;
    }

    /**
     * 取消最佳答案标记
     */
    public void unmarkAsAnswer() {
        this.isAnswer = false;
    }

    /**
     * 增加点赞数
     */
    public void incrementLike() {
        this.likeCount++;
    }

    /**
     * 减少点赞数
     */
    public void decrementLike() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}