package com.moyun.reading.repository;

import com.moyun.reading.entity.discussion.Discussion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 讨论仓库接口
 * 
 * @author Moyun Team
 */
@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Long> {

        /**
         * 查找指定状态的讨论
         */
        Page<Discussion> findByDeletedFalseAndStatus(Discussion.DiscussionStatus status, Pageable pageable);

        /**
         * 查找指定类型的讨论
         */
        Page<Discussion> findByDeletedFalseAndType(Discussion.DiscussionType type, Pageable pageable);

        /**
         * 查找指定类型和状态的讨论
         */
        Page<Discussion> findByDeletedFalseAndTypeAndStatus(
                        Discussion.DiscussionType type,
                        Discussion.DiscussionStatus status,
                        Pageable pageable);

        /**
         * 查找某个用户的讨论
         */
        Page<Discussion> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

        /**
         * 查找置顶讨论
         */
        Page<Discussion> findByDeletedFalseAndIsPinnedTrue(Pageable pageable);

        /**
         * 查找未解决的问题类型讨论
         */
        Page<Discussion> findByDeletedFalseAndTypeAndIsAnsweredAndStatus(
                        Discussion.DiscussionType type,
                        Boolean isAnswered,
                        Discussion.DiscussionStatus status,
                        Pageable pageable);

        /**
         * 搜索讨论（按标题和内容）
         */
        @Query("SELECT d FROM Discussion d WHERE d.deleted = false " +
                        "AND (LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<Discussion> searchDiscussions(@Param("keyword") String keyword, Pageable pageable);

        /**
         * 获取热门讨论（按回复数排序）
         */
        @Query("SELECT d FROM Discussion d LEFT JOIN d.replies r " +
                        "WHERE d.deleted = false AND d.status = :status " +
                        "GROUP BY d ORDER BY COUNT(r) DESC")
        Page<Discussion> findHotDiscussions(@Param("status") Discussion.DiscussionStatus status, Pageable pageable);

        /**
         * 查找导师学生的问题咨询
         */
        @Query("SELECT d FROM Discussion d JOIN Student s ON d.user.id = s.id " +
                        "WHERE d.deleted = false AND s.teacher.id = :teacherId " +
                        "AND d.type = :type AND d.status = :status " +
                        "ORDER BY d.createdAt DESC")
        Page<Discussion> findStudentQuestionsForTeacher(
                        @Param("teacherId") Long teacherId,
                        @Param("type") Discussion.DiscussionType type,
                        @Param("status") Discussion.DiscussionStatus status,
                        Pageable pageable);

        /**
         * 查找导师学生的未解决问题咨询
         */
        @Query("SELECT d FROM Discussion d JOIN Student s ON d.user.id = s.id " +
                        "WHERE d.deleted = false AND s.teacher.id = :teacherId " +
                        "AND d.type = :type AND d.isAnswered = :isAnswered AND d.status = :status " +
                        "ORDER BY d.createdAt DESC")
        Page<Discussion> findUnansweredStudentQuestionsForTeacher(
                        @Param("teacherId") Long teacherId,
                        @Param("type") Discussion.DiscussionType type,
                        @Param("isAnswered") Boolean isAnswered,
                        @Param("status") Discussion.DiscussionStatus status,
                        Pageable pageable);

        /**
         * 查找导师学生的已解决问题咨询
         */
        @Query("SELECT d FROM Discussion d JOIN Student s ON d.user.id = s.id " +
                        "WHERE d.deleted = false AND s.teacher.id = :teacherId " +
                        "AND d.type = :type AND d.isAnswered = :isAnswered AND d.status = :status " +
                        "ORDER BY d.createdAt DESC")
        Page<Discussion> findAnsweredStudentQuestionsForTeacher(
                        @Param("teacherId") Long teacherId,
                        @Param("type") Discussion.DiscussionType type,
                        @Param("isAnswered") Boolean isAnswered,
                        @Param("status") Discussion.DiscussionStatus status,
                        Pageable pageable);

        /**
         * 统计导师学生的未解决问题数量
         */
        @Query("SELECT COUNT(d) FROM Discussion d JOIN Student s ON d.user.id = s.id " +
                        "WHERE d.deleted = false AND s.teacher.id = :teacherId " +
                        "AND d.type = :type AND d.isAnswered = :isAnswered AND d.status = :status")
        long countUnansweredStudentQuestionsForTeacher(
                        @Param("teacherId") Long teacherId,
                        @Param("type") Discussion.DiscussionType type,
                        @Param("isAnswered") Boolean isAnswered,
                        @Param("status") Discussion.DiscussionStatus status);

        /**
         * 统计导师学生的已解决问题数量
         */
        @Query("SELECT COUNT(d) FROM Discussion d JOIN Student s ON d.user.id = s.id " +
                        "WHERE d.deleted = false AND s.teacher.id = :teacherId " +
                        "AND d.type = :type AND d.isAnswered = :isAnswered AND d.status = :status")
        long countAnsweredStudentQuestionsForTeacher(
                        @Param("teacherId") Long teacherId,
                        @Param("type") Discussion.DiscussionType type,
                        @Param("isAnswered") Boolean isAnswered,
                        @Param("status") Discussion.DiscussionStatus status);
}