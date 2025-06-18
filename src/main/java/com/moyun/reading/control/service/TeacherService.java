package com.moyun.reading.control.service;

import com.moyun.reading.entity.book.Book;
import com.moyun.reading.entity.recommendation.BookRecommendation;
import com.moyun.reading.entity.circle.Circle;
import com.moyun.reading.entity.user.Student;
import com.moyun.reading.entity.user.Teacher;
import com.moyun.reading.entity.user.TeacherStudentApplication;
import com.moyun.reading.repository.BookRecommendationRepository;
import com.moyun.reading.repository.BookRepository;
import com.moyun.reading.repository.TeacherStudentApplicationRepository;
import com.moyun.reading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 导师服务类
 * 提供导师相关的业务逻辑处理
 * 
 * @author Moyun Team
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TeacherService {

    private static final Logger logger = LoggerFactory.getLogger(TeacherService.class);

    private final BookRecommendationRepository bookRecommendationRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CircleService circleService;
    private final TeacherStudentApplicationRepository applicationRepository;
    private final StudentService studentService;

    // ==================== 申请管理功能 ====================

    /**
     * 获取导师收到的申请列表
     */
    @Transactional(readOnly = true)
    public Page<TeacherStudentApplication> getTeacherApplications(Teacher teacher, Pageable pageable) {
        return applicationRepository.findByTeacherAndDeletedFalseOrderByAppliedAtDesc(teacher, pageable);
    }

    /**
     * 获取导师收到的待处理申请
     */
    @Transactional(readOnly = true)
    public List<TeacherStudentApplication> getPendingApplications(Teacher teacher) {
        return applicationRepository.findByTeacherAndStatusAndDeletedFalseOrderByAppliedAtDesc(
                teacher, TeacherStudentApplication.ApplicationStatus.PENDING);
    }

    /**
     * 同意学生申请
     */
    public void approveApplication(Teacher teacher, Long applicationId, String replyMessage) {
        TeacherStudentApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在"));

        if (!application.getTeacher().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("无权限操作此申请");
        }

        if (!application.isPending()) {
            throw new IllegalArgumentException("只能处理待审核的申请");
        }

        // 同意申请
        application.approve(replyMessage);
        applicationRepository.save(application);

        // 建立师生关系
        Student student = application.getStudent();
        studentService.selectTeacher(student, teacher.getId());

        logger.info("导师 {} 同意了学生 {} 的申请", teacher.getDisplayName(), student.getDisplayName());
    }

    /**
     * 拒绝学生申请
     */
    public void rejectApplication(Teacher teacher, Long applicationId, String replyMessage) {
        TeacherStudentApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在"));

        if (!application.getTeacher().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("无权限操作此申请");
        }

        if (!application.isPending()) {
            throw new IllegalArgumentException("只能处理待审核的申请");
        }

        // 拒绝申请
        application.reject(replyMessage);
        applicationRepository.save(application);

        logger.info("导师 {} 拒绝了学生 {} 的申请", teacher.getDisplayName(),
                application.getStudent().getDisplayName());
    }

    /**
     * 统计导师收到的待处理申请数量
     */
    @Transactional(readOnly = true)
    public long countPendingApplications(Teacher teacher) {
        return applicationRepository.countByTeacherAndStatusAndDeletedFalse(
                teacher, TeacherStudentApplication.ApplicationStatus.PENDING);
    }

    // ==================== 图书推荐功能 ====================

    /**
     * 推荐图书给学生
     */
    public BookRecommendation recommendBookToStudent(Teacher teacher, Long studentId, Long bookId,
            String reason, Integer recommendationLevel,
            BookRecommendation.RecommendationType recommendationType) {
        Student student = (Student) userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("图书不存在"));

        // 检查是否为导师的学生
        if (!isTeacherStudent(teacher, student)) {
            throw new IllegalArgumentException("只能向自己的学生推荐图书");
        }

        BookRecommendation recommendation = new BookRecommendation();
        recommendation.setTeacher(teacher);
        recommendation.setStudent(student);
        recommendation.setBook(book);
        recommendation.setReason(reason);
        recommendation.setRecommendationLevel(recommendationLevel);
        recommendation.setRecommendationType(recommendationType);
        recommendation.setStatus(BookRecommendation.RecommendationStatus.PENDING);

        BookRecommendation savedRecommendation = bookRecommendationRepository.save(recommendation);
        logger.info("导师 {} 向学生 {} 推荐了图书: {}", teacher.getDisplayName(), student.getDisplayName(),
                book.getTitle());

        return savedRecommendation;
    }

    /**
     * 群体推荐图书
     */
    public void recommendBookToGroup(Teacher teacher, Long bookId, String reason,
            Integer recommendationLevel, BookRecommendation.RecommendationType recommendationType) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("图书不存在"));

        List<Student> students = teacher.getStudents();
        if (students == null || students.isEmpty()) {
            throw new IllegalArgumentException("没有学生可以推荐图书");
        }

        for (Student student : students) {
            BookRecommendation recommendation = new BookRecommendation();
            recommendation.setTeacher(teacher);
            recommendation.setStudent(student);
            recommendation.setBook(book);
            recommendation.setReason(reason);
            recommendation.setRecommendationLevel(recommendationLevel);
            recommendation.setRecommendationType(recommendationType);
            recommendation.setStatus(BookRecommendation.RecommendationStatus.PENDING);
            bookRecommendationRepository.save(recommendation);
        }

        logger.info("导师 {} 向 {} 名学生群体推荐了图书: {}", teacher.getDisplayName(), students.size(), book.getTitle());
    }

    /**
     * 推荐图书到圈子
     */
    public BookRecommendation recommendBookToCircle(Teacher teacher, Long bookId, Long circleId, String reason,
            Integer recommendationLevel, BookRecommendation.RecommendationType recommendationType) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("图书不存在"));

        // 通过CircleService获取完整的Circle对象
        Circle circle = circleService.findById(circleId);
        if (circle == null) {
            throw new IllegalArgumentException("圈子不存在");
        }

        BookRecommendation recommendation = new BookRecommendation();
        recommendation.setTeacher(teacher);
        recommendation.setCircle(circle);
        recommendation.setBook(book);
        recommendation.setReason(reason);
        recommendation.setRecommendationLevel(recommendationLevel);
        recommendation.setRecommendationType(recommendationType);
        recommendation.setStatus(BookRecommendation.RecommendationStatus.PENDING);

        BookRecommendation savedRecommendation = bookRecommendationRepository.save(recommendation);
        logger.info("导师 {} 向圈子 {} 推荐了图书: {}", teacher.getDisplayName(), circle.getName(), book.getTitle());

        return savedRecommendation;
    }

    /**
     * 获取导师的推荐记录
     */
    @Transactional(readOnly = true)
    public Page<BookRecommendation> getTeacherRecommendations(Teacher teacher, Pageable pageable) {
        return bookRecommendationRepository.findByTeacherAndDeletedFalseOrderByCreatedAtDesc(teacher, pageable);
    }

    /**
     * 搜索导师的推荐记录
     */
    @Transactional(readOnly = true)
    public Page<BookRecommendation> searchTeacherRecommendations(Teacher teacher, String keyword, Pageable pageable) {
        return bookRecommendationRepository.searchByTeacherAndKeyword(teacher, keyword, pageable);
    }

    /**
     * 获取导师最近的推荐记录
     */
    @Transactional(readOnly = true)
    public List<BookRecommendation> getRecentRecommendations(Teacher teacher, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return bookRecommendationRepository.findRecentRecommendationsByTeacher(teacher, pageable);
    }

    /**
     * 获取给特定学生的推荐记录
     */
    @Transactional(readOnly = true)
    public Page<BookRecommendation> getRecommendationsForStudent(Teacher teacher, Student student, Pageable pageable) {
        return bookRecommendationRepository.findByTeacherAndStudentAndDeletedFalseOrderByCreatedAtDesc(
                teacher, student, pageable);
    }

    // ==================== 学生管理功能 ====================

    /**
     * 获取导师的学生列表
     */
    @Transactional(readOnly = true)
    public List<Student> getTeacherStudents(Teacher teacher) {
        return teacher.getStudents();
    }

    /**
     * 检查学生是否为导师的学生
     */
    @Transactional(readOnly = true)
    public boolean isTeacherStudent(Teacher teacher, Student student) {
        return teacher.getStudents().contains(student);
    }

    /**
     * 获取导师统计信息
     */
    @Transactional(readOnly = true)
    public TeacherStats getTeacherStats(Teacher teacher) {
        TeacherStats stats = new TeacherStats();
        stats.setStudentCount(teacher.getStudents().size());
        stats.setRecommendationCount(bookRecommendationRepository.countByTeacherAndDeletedFalse(teacher));
        stats.setPendingApplicationCount(countPendingApplications(teacher));
        stats.setPendingDiscussionCount(0); // 暂时设为0，后续通过DiscussionService获取

        // 获取导师创建的圈子数量
        List<Circle> teacherCircles = circleService.findUserCreatedCircles(teacher.getId());
        stats.setCircleCount(teacherCircles != null ? teacherCircles.size() : 0);

        return stats;
    }

    /**
     * 导师统计信息内部类
     */
    public static class TeacherStats {
        private int studentCount;
        private long recommendationCount;
        private long pendingApplicationCount;
        private long pendingDiscussionCount;
        private int circleCount;

        public int getStudentCount() {
            return studentCount;
        }

        public void setStudentCount(int studentCount) {
            this.studentCount = studentCount;
        }

        public long getRecommendationCount() {
            return recommendationCount;
        }

        public void setRecommendationCount(long recommendationCount) {
            this.recommendationCount = recommendationCount;
        }

        public long getPendingApplicationCount() {
            return pendingApplicationCount;
        }

        public void setPendingApplicationCount(long pendingApplicationCount) {
            this.pendingApplicationCount = pendingApplicationCount;
        }

        public long getPendingDiscussionCount() {
            return pendingDiscussionCount;
        }

        public void setPendingDiscussionCount(long pendingDiscussionCount) {
            this.pendingDiscussionCount = pendingDiscussionCount;
        }

        public int getCircleCount() {
            return circleCount;
        }

        public void setCircleCount(int circleCount) {
            this.circleCount = circleCount;
        }
    }
}