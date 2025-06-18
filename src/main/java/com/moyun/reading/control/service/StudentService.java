package com.moyun.reading.control.service;

import com.moyun.reading.entity.recommendation.BookRecommendation;
import com.moyun.reading.entity.user.Student;
import com.moyun.reading.entity.user.Teacher;
import com.moyun.reading.entity.user.TeacherStudentApplication;
import com.moyun.reading.repository.BookRecommendationRepository;
import com.moyun.reading.repository.UserRepository;
import com.moyun.reading.repository.TeacherStudentApplicationRepository;
import com.moyun.reading.entity.user.TeacherStudentApplication;
import com.moyun.reading.repository.TeacherStudentApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 学生服务类 - 控制类
 * 
 * @author Moyun Team
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    private final UserRepository userRepository;
    private final BookRecommendationRepository bookRecommendationRepository;
    private final TeacherStudentApplicationRepository applicationRepository;

    /**
     * 获取所有可选择的导师
     */
    @Transactional(readOnly = true)
    public Page<Teacher> getAvailableTeachers(Pageable pageable) {
        return userRepository.findAllTeachers(pageable);
    }

    /**
     * 搜索导师
     */
    @Transactional(readOnly = true)
    public Page<Teacher> searchTeachers(String keyword, Pageable pageable) {
        return userRepository.searchTeachers(keyword, pageable);
    }

    /**
     * 学生申请导师
     */
    public void applyToTeacher(Student student, Long teacherId, String applicationMessage) {
        // 检查学生是否已有导师
        if (student.hasTeacher()) {
            throw new IllegalArgumentException("您已经有导师了，无法申请新导师");
        }

        Teacher teacher = (Teacher) userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("导师不存在"));

        // 检查是否已有待处理的申请
        boolean hasExistingApplication = applicationRepository.existsByStudentAndTeacherAndStatusAndDeletedFalse(
                student, teacher, TeacherStudentApplication.ApplicationStatus.PENDING);

        if (hasExistingApplication) {
            throw new IllegalArgumentException("您已向该导师发出申请，请等待处理");
        }

        // 创建申请
        TeacherStudentApplication application = new TeacherStudentApplication();
        application.setStudent(student);
        application.setTeacher(teacher);
        application.setApplicationMessage(applicationMessage);
        application.setStatus(TeacherStudentApplication.ApplicationStatus.PENDING);

        applicationRepository.save(application);

        logger.info("学生 {} 向导师 {} 发出申请", student.getDisplayName(), teacher.getDisplayName());
    }

    /**
     * 取消申请导师
     */
    public void cancelApplication(Student student, Long applicationId) {
        TeacherStudentApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在"));

        if (!application.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("无权限操作此申请");
        }

        if (!application.isPending()) {
            throw new IllegalArgumentException("只能取消待处理的申请");
        }

        application.cancel();
        applicationRepository.save(application);

        logger.info("学生 {} 取消了向导师 {} 的申请",
                student.getDisplayName(), application.getTeacher().getDisplayName());
    }

    /**
     * 获取学生的申请列表
     */
    @Transactional(readOnly = true)
    public Page<TeacherStudentApplication> getStudentApplications(Student student, Pageable pageable) {
        return applicationRepository.findByStudentAndDeletedFalseOrderByAppliedAtDesc(student, pageable);
    }

    /**
     * 获取学生的待处理申请
     */
    @Transactional(readOnly = true)
    public List<TeacherStudentApplication> getStudentPendingApplications(Student student) {
        return applicationRepository.findByStudentAndStatusAndDeletedFalseOrderByAppliedAtDesc(
                student, TeacherStudentApplication.ApplicationStatus.PENDING);
    }

    /**
     * 检查学生是否可以申请导师
     */
    @Transactional(readOnly = true)
    public boolean canApplyToTeacher(Student student, Teacher teacher) {
        if (student.hasTeacher()) {
            return false;
        }

        return !applicationRepository.existsByStudentAndTeacherAndStatusAndDeletedFalse(
                student, teacher, TeacherStudentApplication.ApplicationStatus.PENDING);
    }

    /**
     * 获取学生对特定导师的申请状态
     */
    @Transactional(readOnly = true)
    public Optional<TeacherStudentApplication> getApplicationToTeacher(Student student, Teacher teacher) {
        return applicationRepository.findByStudentAndTeacherAndDeletedFalse(student, teacher);
    }

    /**
     * 学生选择导师（内部方法，用于申请被同意后）
     */
    public void selectTeacher(Student student, Long teacherId) {
        Teacher teacher = (Teacher) userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("导师不存在"));

        student.setTeacher(teacher);
        userRepository.save(student);

        logger.info("学生 {} 选择了导师 {}", student.getDisplayName(), teacher.getDisplayName());
    }

    /**
     * 学生更换导师
     */
    public void changeTeacher(Student student, Long newTeacherId) {
        Teacher newTeacher = (Teacher) userRepository.findById(newTeacherId)
                .orElseThrow(() -> new IllegalArgumentException("导师不存在"));

        // 保留changeTeacher方法中的检查，因为这是学生主动更换导师的操作
        if (!newTeacher.canAcceptNewStudent()) {
            throw new IllegalArgumentException("该导师暂时不接收新学生");
        }

        Teacher oldTeacher = student.getTeacher();
        student.setTeacher(newTeacher);
        userRepository.save(student);

        logger.info("学生 {} 从导师 {} 更换到导师 {}",
                student.getDisplayName(),
                oldTeacher != null ? oldTeacher.getDisplayName() : "无",
                newTeacher.getDisplayName());
    }

    /**
     * 取消选择导师
     */
    public void unselectTeacher(Student student) {
        if (!student.hasTeacher()) {
            throw new IllegalArgumentException("您还没有选择导师");
        }

        Teacher oldTeacher = student.getTeacher();
        student.setTeacher(null);
        userRepository.save(student);

        logger.info("学生 {} 取消了导师 {}", student.getDisplayName(), oldTeacher.getDisplayName());
    }

    /**
     * 获取学生接收的推荐
     */
    @Transactional(readOnly = true)
    public Page<BookRecommendation> getStudentRecommendations(Student student, Pageable pageable) {
        return bookRecommendationRepository.findByStudentAndDeletedFalseOrderByCreatedAtDesc(student, pageable);
    }

    /**
     * 获取学生待处理的推荐
     */
    @Transactional(readOnly = true)
    public List<BookRecommendation> getPendingRecommendations(Student student) {
        return bookRecommendationRepository.findByStudentAndStatusAndDeletedFalseOrderByCreatedAtDesc(
                student, BookRecommendation.RecommendationStatus.PENDING);
    }

    /**
     * 统计学生未查看的推荐数量
     */
    @Transactional(readOnly = true)
    public long countUnviewedRecommendations(Student student) {
        return bookRecommendationRepository.countByStudentAndStatusAndDeletedFalse(
                student, BookRecommendation.RecommendationStatus.PENDING);
    }

    /**
     * 标记推荐为已查看
     */
    public void markRecommendationAsViewed(Long recommendationId, Student student) {
        BookRecommendation recommendation = bookRecommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new IllegalArgumentException("推荐不存在"));

        if (!recommendation.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("无权限操作此推荐");
        }

        if (recommendation.getStatus() == BookRecommendation.RecommendationStatus.PENDING) {
            recommendation.setStatus(BookRecommendation.RecommendationStatus.VIEWED);
            bookRecommendationRepository.save(recommendation);
        }
    }

    /**
     * 获取学生统计信息
     */
    @Transactional(readOnly = true)
    public StudentStats getStudentStats(Student student) {
        StudentStats stats = new StudentStats();
        stats.setRecommendationCount(bookRecommendationRepository.countByStudentAndDeletedFalse(student));
        stats.setUnviewedRecommendationCount(countUnviewedRecommendations(student));
        stats.setHasTeacher(student.hasTeacher());
        stats.setPendingApplicationCount(applicationRepository.countByStudentAndStatusAndDeletedFalse(
                student, TeacherStudentApplication.ApplicationStatus.PENDING));
        return stats;
    }

    /**
     * 学生统计信息内部类
     */
    public static class StudentStats {
        private long recommendationCount;
        private long unviewedRecommendationCount;
        private boolean hasTeacher;
        private long pendingApplicationCount;

        public long getRecommendationCount() {
            return recommendationCount;
        }

        public void setRecommendationCount(long recommendationCount) {
            this.recommendationCount = recommendationCount;
        }

        public long getUnviewedRecommendationCount() {
            return unviewedRecommendationCount;
        }

        public void setUnviewedRecommendationCount(long unviewedRecommendationCount) {
            this.unviewedRecommendationCount = unviewedRecommendationCount;
        }

        public boolean isHasTeacher() {
            return hasTeacher;
        }

        public void setHasTeacher(boolean hasTeacher) {
            this.hasTeacher = hasTeacher;
        }

        public long getPendingApplicationCount() {
            return pendingApplicationCount;
        }

        public void setPendingApplicationCount(long pendingApplicationCount) {
            this.pendingApplicationCount = pendingApplicationCount;
        }
    }
}