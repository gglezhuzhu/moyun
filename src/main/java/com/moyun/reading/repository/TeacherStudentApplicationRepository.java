package com.moyun.reading.repository;

import com.moyun.reading.entity.user.TeacherStudentApplication;
import com.moyun.reading.entity.user.Student;
import com.moyun.reading.entity.user.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 导师学生申请Repository接口
 * 
 * @author Moyun Team
 */
@Repository
public interface TeacherStudentApplicationRepository extends JpaRepository<TeacherStudentApplication, Long> {

    /**
     * 查找导师收到的申请
     */
    Page<TeacherStudentApplication> findByTeacherAndDeletedFalseOrderByAppliedAtDesc(
            Teacher teacher, Pageable pageable);

    /**
     * 查找导师收到的待处理申请
     */
    List<TeacherStudentApplication> findByTeacherAndStatusAndDeletedFalseOrderByAppliedAtDesc(
            Teacher teacher, TeacherStudentApplication.ApplicationStatus status);

    /**
     * 查找学生发出的申请
     */
    Page<TeacherStudentApplication> findByStudentAndDeletedFalseOrderByAppliedAtDesc(
            Student student, Pageable pageable);

    /**
     * 查找学生发出的待处理申请
     */
    List<TeacherStudentApplication> findByStudentAndStatusAndDeletedFalseOrderByAppliedAtDesc(
            Student student, TeacherStudentApplication.ApplicationStatus status);

    /**
     * 查找学生对特定导师的申请
     */
    Optional<TeacherStudentApplication> findByStudentAndTeacherAndDeletedFalse(
            Student student, Teacher teacher);

    /**
     * 查找学生对特定导师的待处理申请
     */
    Optional<TeacherStudentApplication> findByStudentAndTeacherAndStatusAndDeletedFalse(
            Student student, Teacher teacher, TeacherStudentApplication.ApplicationStatus status);

    /**
     * 统计导师收到的待处理申请数量
     */
    long countByTeacherAndStatusAndDeletedFalse(
            Teacher teacher, TeacherStudentApplication.ApplicationStatus status);

    /**
     * 统计学生发出的待处理申请数量
     */
    long countByStudentAndStatusAndDeletedFalse(
            Student student, TeacherStudentApplication.ApplicationStatus status);

    /**
     * 检查学生是否已向导师发出申请
     */
    boolean existsByStudentAndTeacherAndStatusAndDeletedFalse(
            Student student, Teacher teacher, TeacherStudentApplication.ApplicationStatus status);

    /**
     * 查找所有待处理的申请
     */
    @Query("SELECT a FROM TeacherStudentApplication a WHERE a.status = 'PENDING' AND a.deleted = false ORDER BY a.appliedAt DESC")
    List<TeacherStudentApplication> findAllPendingApplications();

    /**
     * 根据状态查找申请
     */
    Page<TeacherStudentApplication> findByStatusAndDeletedFalseOrderByAppliedAtDesc(
            TeacherStudentApplication.ApplicationStatus status, Pageable pageable);
}