-- 创建师生问答表
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '问题标题',
    content TEXT NOT NULL COMMENT '问题内容',
    student_id BIGINT NOT NULL COMMENT '提问学生ID',
    teacher_id BIGINT NOT NULL COMMENT '被提问老师ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '问题状态：PENDING-待回答，ANSWERED-已回答，CLOSED-已关闭',
    is_public BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否公开显示',
    asked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提问时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    
    INDEX idx_student_id (student_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_status (status),
    INDEX idx_is_public (is_public),
    INDEX idx_asked_at (asked_at),
    INDEX idx_deleted (deleted),
    
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='师生问答表';

-- 创建回答表
CREATE TABLE IF NOT EXISTS answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL COMMENT '回答内容',
    question_id BIGINT NOT NULL COMMENT '问题ID',
    teacher_id BIGINT NOT NULL COMMENT '回答老师ID',
    answered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '回答时间',
    is_accepted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否被采纳',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    
    INDEX idx_question_id (question_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_answered_at (answered_at),
    INDEX idx_is_accepted (is_accepted),
    INDEX idx_deleted (deleted),
    
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问答回答表'; 