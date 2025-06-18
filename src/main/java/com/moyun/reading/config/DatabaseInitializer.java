package com.moyun.reading.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化器
 * 用于创建师生问答相关的数据库表
 * 
 * @author Moyun Team
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            createQuestionsTable();
            createAnswersTable();
            initTestData();
            logger.info("师生问答数据库表初始化完成");
        } catch (Exception e) {
            logger.error("数据库表初始化失败", e);
        }
    }

    private void createQuestionsTable() {
        String sql = """
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
                    INDEX idx_deleted (deleted)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='师生问答表'
                """;

        try {
            jdbcTemplate.execute(sql);
            logger.info("questions表创建成功");
        } catch (Exception e) {
            logger.warn("questions表可能已存在: {}", e.getMessage());
        }
    }

    private void createAnswersTable() {
        String sql = """
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
                    INDEX idx_deleted (deleted)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问答回答表'
                """;

        try {
            jdbcTemplate.execute(sql);
            logger.info("answers表创建成功");
        } catch (Exception e) {
            logger.warn("answers表可能已存在: {}", e.getMessage());
        }
    }

    /**
     * 初始化测试数据
     */
    private void initTestData() {
        try {
            // 检查是否已有问题数据
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM questions", Integer.class);
            if (count != null && count > 0) {
                logger.info("问题数据已存在，跳过测试数据初始化");
                return;
            }

            // 查找第一个学生和老师
            Integer studentId = jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE user_type = 'STUDENT' LIMIT 1", Integer.class);
            Integer teacherId = jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE user_type = 'TEACHER' LIMIT 1", Integer.class);

            if (studentId != null && teacherId != null) {
                // 插入测试问题
                String insertQuestion = """
                        INSERT INTO questions (title, content, student_id, teacher_id, status, is_public, asked_at, created_at, updated_at, deleted)
                        VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW(), NOW(), FALSE)
                        """;

                jdbcTemplate.update(insertQuestion,
                        "如何提高阅读理解能力？",
                        "老师您好，我在阅读时经常理解不深入，请问有什么好的方法可以提高阅读理解能力吗？",
                        studentId, teacherId, "PENDING", true);

                jdbcTemplate.update(insertQuestion,
                        "推荐一些经典文学作品",
                        "希望老师能推荐一些适合大学生阅读的经典文学作品，谢谢！",
                        studentId, teacherId, "PENDING", true);

                logger.info("测试问题数据初始化成功");
            } else {
                logger.warn("未找到学生或老师用户，跳过测试数据初始化");
            }
        } catch (Exception e) {
            logger.warn("测试数据初始化失败: {}", e.getMessage());
        }
    }
}