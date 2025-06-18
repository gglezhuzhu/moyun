package com.moyun.reading.repository;

import com.moyun.reading.entity.recommendation.BookRecommendation;
import com.moyun.reading.entity.circle.Circle;
import com.moyun.reading.entity.user.Student;
import com.moyun.reading.entity.user.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 图书推荐Repository接口
 * 
 * @author Moyun Team
 */
@Repository
public interface BookRecommendationRepository extends JpaRepository<BookRecommendation, Long> {

        /**
         * 查找导师的所有推荐
         */
        Page<BookRecommendation> findByTeacherAndDeletedFalseOrderByCreatedAtDesc(Teacher teacher, Pageable pageable);

        /**
         * 查找学生接收的推荐
         */
        Page<BookRecommendation> findByStudentAndDeletedFalseOrderByCreatedAtDesc(Student student, Pageable pageable);

        /**
         * 查找导师给特定学生的推荐
         */
        Page<BookRecommendation> findByTeacherAndStudentAndDeletedFalseOrderByCreatedAtDesc(
                        Teacher teacher, Student student, Pageable pageable);

        /**
         * 按推荐类型查找
         */
        Page<BookRecommendation> findByTeacherAndRecommendationTypeAndDeletedFalseOrderByCreatedAtDesc(
                        Teacher teacher, BookRecommendation.RecommendationType type, Pageable pageable);

        /**
         * 按状态查找
         */
        Page<BookRecommendation> findByTeacherAndStatusAndDeletedFalseOrderByCreatedAtDesc(
                        Teacher teacher, BookRecommendation.RecommendationStatus status, Pageable pageable);

        /**
         * 查找学生待处理的推荐
         */
        List<BookRecommendation> findByStudentAndStatusAndDeletedFalseOrderByCreatedAtDesc(
                        Student student, BookRecommendation.RecommendationStatus status);

        /**
         * 统计学生未查看的推荐数量
         */
        long countByStudentAndStatusAndDeletedFalse(Student student, BookRecommendation.RecommendationStatus status);

        /**
         * 查找群体推荐（不指定特定学生）
         */
        @Query("SELECT br FROM BookRecommendation br WHERE br.teacher = :teacher AND br.student IS NULL " +
                        "AND br.deleted = false ORDER BY br.createdAt DESC")
        Page<BookRecommendation> findGroupRecommendationsByTeacher(@Param("teacher") Teacher teacher,
                        Pageable pageable);

        /**
         * 查找热门推荐（按推荐等级排序）
         */
        @Query("SELECT br FROM BookRecommendation br WHERE br.teacher = :teacher AND br.deleted = false " +
                        "ORDER BY br.recommendationLevel DESC, br.createdAt DESC")
        List<BookRecommendation> findTopRecommendationsByTeacher(@Param("teacher") Teacher teacher, Pageable pageable);

        /**
         * 搜索推荐（按图书标题或推荐理由）
         */
        @Query("SELECT br FROM BookRecommendation br JOIN br.book b WHERE br.teacher = :teacher AND br.deleted = false "
                        +
                        "AND (b.title LIKE %:keyword% OR br.reason LIKE %:keyword%) " +
                        "ORDER BY br.createdAt DESC")
        Page<BookRecommendation> searchByTeacherAndKeyword(@Param("teacher") Teacher teacher,
                        @Param("keyword") String keyword,
                        Pageable pageable);

        /**
         * 统计导师的推荐数量
         */
        long countByTeacherAndDeletedFalse(Teacher teacher);

        /**
         * 统计学生接收的推荐数量
         */
        long countByStudentAndDeletedFalse(Student student);

        /**
         * 查找导师最近的推荐
         */
        @Query("SELECT br FROM BookRecommendation br WHERE br.teacher = :teacher AND br.deleted = false " +
                        "ORDER BY br.createdAt DESC")
        List<BookRecommendation> findRecentRecommendationsByTeacher(@Param("teacher") Teacher teacher,
                        Pageable pageable);

        /**
         * 查找圈子中的推荐图书
         */
        Page<BookRecommendation> findByCircleAndDeletedFalseOrderByCreatedAtDesc(Circle circle, Pageable pageable);

        /**
         * 查找圈子中的推荐图书（按状态过滤）
         */
        Page<BookRecommendation> findByCircleAndStatusAndDeletedFalseOrderByCreatedAtDesc(
                        Circle circle, BookRecommendation.RecommendationStatus status, Pageable pageable);

        /**
         * 统计圈子中的推荐数量
         */
        long countByCircleAndDeletedFalse(Circle circle);

        /**
         * 查找圈子中最近的推荐
         */
        @Query("SELECT br FROM BookRecommendation br WHERE br.circle = :circle AND br.deleted = false " +
                        "ORDER BY br.createdAt DESC")
        List<BookRecommendation> findRecentRecommendationsByCircle(@Param("circle") Circle circle, Pageable pageable);
}