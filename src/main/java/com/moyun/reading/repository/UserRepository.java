package com.moyun.reading.repository;

import com.moyun.reading.entity.base.User;
import com.moyun.reading.entity.book.Book;
import com.moyun.reading.entity.user.Student;
import com.moyun.reading.entity.user.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓库接口
 * 
 * @author Moyun Team
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

        /**
         * 通过用户名查找用户
         */
        Optional<User> findByUsername(String username);

        /**
         * 通过邮箱查找用户
         */
        Optional<User> findByEmail(String email);

        /**
         * 检查用户名是否存在
         */
        boolean existsByUsername(String username);

        /**
         * 检查邮箱是否存在
         */
        boolean existsByEmail(String email);

        /**
         * 搜索用户（按用户名或真实姓名）
         */
        Page<User> findByUsernameContainingOrRealNameContaining(
                        String username, String realName, Pageable pageable);

        /**
         * 查找启用状态的用户
         */
        Page<User> findByEnabledTrue(Pageable pageable);

        /**
         * 查找禁用状态的用户
         */
        Page<User> findByEnabledFalse(Pageable pageable);

        /**
         * 获取用户添加的图书
         */
        @Query("SELECT b FROM Book b WHERE b.addedByUser.id = :userId AND b.deleted = false")
        List<Book> findUserBooks(@Param("userId") Long userId);

        /**
         * 统计用户数量
         */
        @Query("SELECT COUNT(u) FROM User u WHERE u.deleted = false")
        long countActiveUsers();

        /**
         * 按角色查找用户
         */
        @Query("SELECT u FROM User u WHERE TYPE(u) = :userType AND u.deleted = false")
        Page<User> findByUserType(@Param("userType") Class<? extends User> userType, Pageable pageable);

        /**
         * 更新用户类型
         */
        @Modifying
        @Query(value = "UPDATE users SET user_type = :userType WHERE id = :userId", nativeQuery = true)
        void updateUserType(@Param("userId") Long userId, @Param("userType") String userType);

        /**
         * 清空学生特定字段
         */
        @Modifying
        @Query(value = "UPDATE users SET school = NULL, major = NULL, grade = NULL, student_id = NULL, teacher_id = NULL WHERE id = :userId", nativeQuery = true)
        void clearStudentFields(@Param("userId") Long userId);

        /**
         * 清空导师特定字段
         */
        @Modifying
        @Query(value = "UPDATE users SET title = NULL, organization = NULL, specialization = NULL, years_of_experience = NULL, teaching_experience = NULL, accepting_students = NULL, max_students = NULL WHERE id = :userId", nativeQuery = true)
        void clearTeacherFields(@Param("userId") Long userId);

        /**
         * 清空管理员特定字段
         */
        @Modifying
        @Query(value = "UPDATE users SET admin_level = NULL, permissions = NULL, can_delete_users = NULL, can_manage_system = NULL WHERE id = :userId", nativeQuery = true)
        void clearAdminFields(@Param("userId") Long userId);

        /**
         * 查找导师的学生列表
         */
        @Query("SELECT s FROM Student s WHERE s.teacher.id = :teacherId AND s.deleted = false")
        List<Student> findStudentsByTeacherId(@Param("teacherId") Long teacherId);

        /**
         * 查找所有导师
         */
        @Query("SELECT t FROM Teacher t WHERE TYPE(t) = Teacher AND t.deleted = false")
        Page<Teacher> findAllTeachers(Pageable pageable);

        /**
         * 查找可接收学生的导师
         */
        @Query("SELECT t FROM Teacher t WHERE TYPE(t) = Teacher AND t.deleted = false AND t.acceptingStudents = true")
        Page<Teacher> findAvailableTeachers(Pageable pageable);

        /**
         * 搜索导师（按姓名）
         */
        @Query("SELECT t FROM Teacher t WHERE TYPE(t) = Teacher AND t.deleted = false " +
                        "AND (LOWER(t.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "OR LOWER(t.realName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<Teacher> searchTeachers(@Param("keyword") String keyword, Pageable pageable);

        /**
         * 通过邮箱查找未删除的用户
         */
        Optional<User> findByEmailAndDeletedFalse(String email);

        /**
         * 通过重置token查找未删除的用户
         */
        Optional<User> findByResetTokenAndDeletedFalse(String resetToken);

        /**
         * 通过用户名查找未删除的用户
         */
        Optional<User> findByUsernameAndDeletedFalse(String username);
}