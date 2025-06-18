package com.moyun.reading.entity.diary;

import com.moyun.reading.entity.base.BaseEntity;
import com.moyun.reading.entity.base.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 日志评论实体类
 * 
 * @author Moyun Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "diary_comments")
public class DiaryComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private ReadingDiary diary;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容长度不能超过1000个字符")
    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private DiaryComment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_user_id")
    private User replyToUser;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    /**
     * 是否为顶级评论
     */
    public boolean isTopLevel() {
        return parent == null;
    }

    /**
     * 是否为回复评论
     */
    public boolean isReply() {
        return parent != null;
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