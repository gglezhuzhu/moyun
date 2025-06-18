package com.moyun.reading.entity.recommendation;

import com.moyun.reading.entity.base.BaseEntity;
import com.moyun.reading.entity.book.Book;
import com.moyun.reading.entity.circle.Circle;
import com.moyun.reading.entity.user.Student;
import com.moyun.reading.entity.user.Teacher;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 图书推荐实体类
 * 
 * @author Moyun Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "book_recommendations")
public class BookRecommendation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "circle_id")
    private Circle circle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Size(max = 1000, message = "推荐理由长度不能超过1000个字符")
    @Column(name = "reason", length = 1000)
    private String reason;

    @Min(value = 1, message = "推荐等级最小为1")
    @Max(value = 5, message = "推荐等级最大为5")
    @Column(name = "recommendation_level", nullable = false)
    private Integer recommendationLevel = 3;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_type", nullable = false)
    private RecommendationType recommendationType = RecommendationType.PERSONAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RecommendationStatus status = RecommendationStatus.PENDING;

    @Size(max = 500, message = "学生反馈长度不能超过500个字符")
    @Column(name = "student_feedback", length = 500)
    private String studentFeedback;

    @Column(name = "student_rating")
    private Integer studentRating;

    @Column(name = "viewed_at")
    private java.time.LocalDateTime viewedAt;

    @Column(name = "completed_at")
    private java.time.LocalDateTime completedAt;

    /**
     * 推荐类型枚举
     */
    public enum RecommendationType {
        PERSONAL("个人推荐"),
        GROUP("群体推荐"),
        COURSE("课程推荐"),
        RESEARCH("研究推荐");

        private final String displayName;

        RecommendationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 推荐状态枚举
     */
    public enum RecommendationStatus {
        PENDING("待查看"),
        VIEWED("已查看"),
        ACCEPTED("已接受"),
        READING("阅读中"),
        COMPLETED("已完成"),
        DECLINED("已拒绝");

        private final String displayName;

        RecommendationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 标记为已查看
     */
    public void markAsViewed() {
        if (this.status == RecommendationStatus.PENDING) {
            this.status = RecommendationStatus.VIEWED;
            this.viewedAt = java.time.LocalDateTime.now();
        }
    }

    /**
     * 接受推荐
     */
    public void accept() {
        this.status = RecommendationStatus.ACCEPTED;
    }

    /**
     * 拒绝推荐
     */
    public void decline() {
        this.status = RecommendationStatus.DECLINED;
    }

    /**
     * 开始阅读
     */
    public void startReading() {
        this.status = RecommendationStatus.READING;
    }

    /**
     * 完成阅读
     */
    public void complete(String feedback, Integer rating) {
        this.status = RecommendationStatus.COMPLETED;
        this.studentFeedback = feedback;
        this.studentRating = rating;
        this.completedAt = java.time.LocalDateTime.now();
    }

    /**
     * 获取推荐等级星级显示
     */
    public String getStarDisplay() {
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= recommendationLevel) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }

    /**
     * 获取状态样式类
     */
    public String getStatusClass() {
        return switch (status) {
            case PENDING -> "badge bg-warning";
            case VIEWED -> "badge bg-info";
            case ACCEPTED -> "badge bg-primary";
            case READING -> "badge bg-success";
            case COMPLETED -> "badge bg-dark";
            case DECLINED -> "badge bg-secondary";
        };
    }

    /**
     * 是否为个人推荐
     */
    public boolean isPersonalRecommendation() {
        return student != null;
    }

    /**
     * 是否为圈子推荐
     */
    public boolean isCircleRecommendation() {
        return circle != null;
    }
}