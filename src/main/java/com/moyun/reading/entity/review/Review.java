package com.moyun.reading.entity.review;

import com.moyun.reading.entity.base.BaseEntity;
import com.moyun.reading.entity.base.User;
import com.moyun.reading.entity.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 书评实体类
 * 
 * @author Moyun Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "书评标题不能为空")
    @Size(max = 200, message = "书评标题长度不能超过200个字符")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotBlank(message = "书评内容不能为空")
    @Size(max = 5000, message = "书评内容长度不能超过5000个字符")
    @Column(name = "content", nullable = false, length = 5000)
    private String content;

    @Min(value = 1, message = "评分不能低于1分")
    @Max(value = 5, message = "评分不能高于5分")
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewComment> comments;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewLike> likes;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status = ReviewStatus.PUBLISHED;

    /**
     * 书评状态枚举
     */
    public enum ReviewStatus {
        DRAFT("草稿"),
        PUBLISHED("已发布"),
        HIDDEN("已隐藏");

        private final String displayName;

        ReviewStatus(String displayName) {
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
     * 获取点赞数
     */
    public int getLikeCount() {
        if (likes == null) {
            return 0;
        }
        return (int) likes.stream()
                .filter(like -> !like.isDeleted())
                .count();
    }

    /**
     * 获取评论数
     */
    public int getCommentCount() {
        return comments != null ? comments.size() : 0;
    }

    /**
     * 是否已发布
     */
    public boolean isPublished() {
        return ReviewStatus.PUBLISHED.equals(status);
    }

    /**
     * 获取评分的星星显示
     */
    public String getStarRating() {
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= rating) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
}