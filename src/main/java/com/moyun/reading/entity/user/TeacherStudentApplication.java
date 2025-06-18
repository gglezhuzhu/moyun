package com.moyun.reading.entity.user;

import com.moyun.reading.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 导师学生申请实体类
 * 管理学生申请导师的审批流程
 * 
 * @author Moyun Team
 */
@Entity
@Table(name = "teacher_student_applications")
@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherStudentApplication extends BaseEntity {

    /**
     * 申请学生
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * 被申请的导师
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    /**
     * 申请状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    /**
     * 申请消息
     */
    @Column(name = "application_message", columnDefinition = "TEXT")
    private String applicationMessage;

    /**
     * 导师回复消息
     */
    @Column(name = "teacher_reply", columnDefinition = "TEXT")
    private String teacherReply;

    /**
     * 申请时间
     */
    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    /**
     * 处理时间
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * 申请状态枚举
     */
    public enum ApplicationStatus {
        PENDING("待审核"),
        APPROVED("已同意"),
        REJECTED("已拒绝"),
        CANCELLED("已取消");

        private final String description;

        ApplicationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 是否为待处理状态
     */
    public boolean isPending() {
        return status == ApplicationStatus.PENDING;
    }

    /**
     * 是否已被同意
     */
    public boolean isApproved() {
        return status == ApplicationStatus.APPROVED;
    }

    /**
     * 是否已被拒绝
     */
    public boolean isRejected() {
        return status == ApplicationStatus.REJECTED;
    }

    /**
     * 是否已取消
     */
    public boolean isCancelled() {
        return status == ApplicationStatus.CANCELLED;
    }

    /**
     * 同意申请
     */
    public void approve(String replyMessage) {
        this.status = ApplicationStatus.APPROVED;
        this.teacherReply = replyMessage;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 拒绝申请
     */
    public void reject(String replyMessage) {
        this.status = ApplicationStatus.REJECTED;
        this.teacherReply = replyMessage;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 取消申请
     */
    public void cancel() {
        this.status = ApplicationStatus.CANCELLED;
        this.processedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (appliedAt == null) {
            appliedAt = LocalDateTime.now();
        }
    }
}