-- 创建导师学生申请表
CREATE TABLE IF NOT EXISTS teacher_student_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    application_message TEXT,
    teacher_reply TEXT,
    applied_at DATETIME NOT NULL,
    processed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_student_id (student_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_status (status),
    INDEX idx_applied_at (applied_at),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 添加注释
ALTER TABLE teacher_student_applications COMMENT = '导师学生申请表';
ALTER TABLE teacher_student_applications MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '主键ID';
ALTER TABLE teacher_student_applications MODIFY COLUMN student_id BIGINT NOT NULL COMMENT '申请学生ID';
ALTER TABLE teacher_student_applications MODIFY COLUMN teacher_id BIGINT NOT NULL COMMENT '被申请导师ID';
ALTER TABLE teacher_student_applications MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '申请状态：PENDING-待审核，APPROVED-已同意，REJECTED-已拒绝，CANCELLED-已取消';
ALTER TABLE teacher_student_applications MODIFY COLUMN application_message TEXT COMMENT '申请消息';
ALTER TABLE teacher_student_applications MODIFY COLUMN teacher_reply TEXT COMMENT '导师回复消息';
ALTER TABLE teacher_student_applications MODIFY COLUMN applied_at DATETIME NOT NULL COMMENT '申请时间';
ALTER TABLE teacher_student_applications MODIFY COLUMN processed_at DATETIME COMMENT '处理时间';
ALTER TABLE teacher_student_applications MODIFY COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE teacher_student_applications MODIFY COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
ALTER TABLE teacher_student_applications MODIFY COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除'; 