package com.moyun.reading.boundary.controller;

import com.moyun.reading.control.service.BookService;
import com.moyun.reading.control.service.CircleService;
import com.moyun.reading.control.service.DiscussionService;
import com.moyun.reading.control.service.TeacherService;
import com.moyun.reading.entity.book.Book;
import com.moyun.reading.entity.circle.Circle;
import com.moyun.reading.entity.discussion.Discussion;
import com.moyun.reading.entity.recommendation.BookRecommendation;
import com.moyun.reading.entity.user.Student;
import com.moyun.reading.entity.user.Teacher;
import com.moyun.reading.entity.user.TeacherStudentApplication;
import com.moyun.reading.utility.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 导师控制器 - 边界类
 * 处理导师特有功能的所有请求
 * 
 * @author Moyun Team
 */
@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final CircleService circleService;
    private final BookService bookService;
    private final DiscussionService discussionService;

    /**
     * 导师工作台首页
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Teacher teacher = getCurrentTeacher(authentication);
        if (teacher == null) {
            return "redirect:/user/login";
        }

        // 获取统计信息
        TeacherService.TeacherStats stats = teacherService.getTeacherStats(teacher);

        // 获取最近的推荐
        List<BookRecommendation> recentRecommendations = teacherService.getRecentRecommendations(teacher, 5);

        // 获取导师创建的圈子
        Pageable pageable = PageRequest.of(0, 5);
        Page<Circle> myCircles = circleService.findUserCreatedCircles(teacher.getId(), pageable);

        // 获取需要回答的问题（来自自己学生的问题咨询）
        Page<Discussion> pendingDiscussions = discussionService.findUnansweredStudentQuestionsForTeacher(teacher,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")));

        // 更新统计信息中的待回复讨论数量
        stats.setPendingDiscussionCount(pendingDiscussions.getTotalElements());

        // 获取待处理的学生申请
        List<TeacherStudentApplication> pendingApplications = teacherService.getPendingApplications(teacher);

        model.addAttribute("teacher", teacher);
        model.addAttribute("stats", stats);
        model.addAttribute("recentRecommendations", recentRecommendations);
        model.addAttribute("myCircles", myCircles);
        model.addAttribute("pendingDiscussions", pendingDiscussions);
        model.addAttribute("pendingApplications", pendingApplications);
        model.addAttribute("pageTitle", "导师工作台");

        return "teacher/dashboard";
    }

    // ==================== 学生问答功能 ====================

    /**
     * 学生问题管理页面
     */
    @GetMapping("/questions")
    public String questions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "unanswered") String filter,
            Authentication authentication,
            Model model) {

        Teacher teacher = getCurrentTeacher(authentication);
        if (teacher == null) {
            return "redirect:/user/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Discussion> questions;

        if ("answered".equals(filter)) {
            // 查看已解决的问题
            questions = discussionService.findAnsweredStudentQuestionsForTeacher(teacher, pageable);
        } else {
            // 查看未解决的问题
            questions = discussionService.findUnansweredStudentQuestionsForTeacher(teacher, pageable);
        }

        // 获取统计数据
        long unansweredCount = 0;
        long answeredCount = 0;

        try {
            unansweredCount = discussionService.countUnansweredStudentQuestionsForTeacher(teacher);
            answeredCount = discussionService.countAnsweredStudentQuestionsForTeacher(teacher);
        } catch (Exception e) {
            // 如果统计方法不存在，使用简单计算
            if ("answered".equals(filter)) {
                answeredCount = questions.getTotalElements();
            } else {
                unansweredCount = questions.getTotalElements();
            }
        }

        model.addAttribute("questions", questions);
        model.addAttribute("filter", filter);
        model.addAttribute("unansweredCount", unansweredCount);
        model.addAttribute("answeredCount", answeredCount);
        model.addAttribute("pageTitle", "学生问题管理");

        return "teacher/questions";
    }

    /**
     * 标记讨论为已回答
     */
    @PostMapping("/questions/{discussionId}/mark-answered")
    public String markDiscussionAsAnswered(
            @PathVariable Long discussionId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Teacher teacher = getCurrentTeacher(authentication);
            Discussion discussion = discussionService.findById(discussionId);
            if (discussion != null) {
                discussion.setIsAnswered(true);
                discussionService.updateDiscussion(discussion);
                redirectAttributes.addFlashAttribute("successMessage", "已标记为已回答！");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "讨论不存在！");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "操作失败：" + e.getMessage());
        }

        return "redirect:/teacher/questions";
    }

    /**
     * 取消已回答标记
     */
    @PostMapping("/questions/{discussionId}/unmark-answered")
    public String unmarkDiscussionAsAnswered(
            @PathVariable Long discussionId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Teacher teacher = getCurrentTeacher(authentication);
            Discussion discussion = discussionService.findById(discussionId);
            if (discussion != null) {
                discussion.setIsAnswered(false);
                discussionService.updateDiscussion(discussion);
                redirectAttributes.addFlashAttribute("successMessage", "已取消已回答标记！");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "讨论不存在！");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "操作失败：" + e.getMessage());
        }

        return "redirect:/teacher/questions";
    }

    /**
     * 标记回复为最佳答案
     */
    @PostMapping("/questions/reply/{replyId}/mark-answer")
    public String markReplyAsAnswer(
            @PathVariable Long replyId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Teacher teacher = getCurrentTeacher(authentication);
            discussionService.markReplyAsAnswer(replyId, teacher);
            redirectAttributes.addFlashAttribute("successMessage", "已标记为最佳答案！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "操作失败：" + e.getMessage());
        }

        return "redirect:/teacher/questions";
    }

    /**
     * 取消最佳答案标记
     */
    @PostMapping("/questions/reply/{replyId}/unmark-answer")
    public String unmarkReplyAsAnswer(
            @PathVariable Long replyId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Teacher teacher = getCurrentTeacher(authentication);
            discussionService.unmarkReplyAsAnswer(replyId, teacher);
            redirectAttributes.addFlashAttribute("successMessage", "已取消最佳答案标记！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "操作失败：" + e.getMessage());
        }

        return "redirect:/teacher/questions";
    }

    // ==================== 图书推荐功能 ====================

    /**
     * 图书推荐管理页面
     */
    @GetMapping("/recommendations")
    public String recommendations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Authentication authentication,
            Model model) {

        Teacher teacher = getCurrentTeacher(authentication);
        if (teacher == null) {
            return "redirect:/user/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BookRecommendation> recommendations;

        if (search != null && !search.trim().isEmpty()) {
            recommendations = teacherService.searchTeacherRecommendations(teacher, search, pageable);
            model.addAttribute("search", search);
        } else {
            recommendations = teacherService.getTeacherRecommendations(teacher, pageable);
        }

        model.addAttribute("recommendations", recommendations);
        model.addAttribute("pageTitle", "图书推荐管理");

        return "teacher/recommendations";
    }

    /**
     * 推荐图书页面
     */
    @GetMapping("/recommendations/create")
    public String createRecommendationForm(
            @RequestParam(required = false) Long bookId,
            Authentication authentication,
            Model model) {

        Teacher teacher = getCurrentTeacher(authentication);
        if (teacher == null) {
            return "redirect:/user/login";
        }

        if (bookId != null) {
            Book book = bookService.findById(bookId);
            model.addAttribute("selectedBook", book);
        }

        // 获取导师创建的圈子
        List<Circle> circles = circleService.findUserCreatedCircles(teacher.getId());
        model.addAttribute("circles", circles);
        model.addAttribute("pageTitle", "推荐图书到圈子");

        return "teacher/create-recommendation";
    }

    /**
     * 处理图书推荐到圈子
     */
    @PostMapping("/recommendations/create")
    public String createRecommendation(
            @RequestParam Long bookId,
            @RequestParam Long circleId,
            @RequestParam String reason,
            @RequestParam Integer recommendationLevel,
            @RequestParam BookRecommendation.RecommendationType recommendationType,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Teacher teacher = getCurrentTeacher(authentication);

            // 验证圈子是否属于该导师
            Circle circle = circleService.findById(circleId);
            if (circle == null || !circleService.canManageCircle(circle, teacher)) {
                redirectAttributes.addFlashAttribute("errorMessage", "无权限操作该圈子！");
                return "redirect:/teacher/recommendations/create";
            }

            // 推荐图书到圈子
            teacherService.recommendBookToCircle(teacher, bookId, circleId, reason, recommendationLevel,
                    recommendationType);
            redirectAttributes.addFlashAttribute("successMessage", "图书推荐到圈子成功！");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "推荐失败：" + e.getMessage());
        }

        return "redirect:/teacher/recommendations";
    }

    // ==================== 学生管理功能 ====================

    /**
     * 学生管理页面
     */
    @GetMapping("/students")
    public String students(Authentication authentication, Model model) {
        Teacher teacher = getCurrentTeacher(authentication);
        if (teacher == null) {
            return "redirect:/user/login";
        }

        List<Student> students = teacherService.getTeacherStudents(teacher);
        model.addAttribute("students", students);
        model.addAttribute("teacher", teacher);
        model.addAttribute("pageTitle", "学生管理");

        return "teacher/students";
    }

    /**
     * 学生详情页面
     */
    @GetMapping("/students/{studentId}")
    public String studentDetail(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        Teacher teacher = getCurrentTeacher(authentication);
        if (teacher == null) {
            return "redirect:/user/login";
        }

        Student student = (Student) teacherService.getTeacherStudents(teacher).stream()
                .filter(s -> s.getId().equals(studentId))
                .findFirst()
                .orElse(null);

        if (student == null) {
            return "redirect:/teacher/students?error=notfound";
        }

        // 获取给该学生的推荐记录
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BookRecommendation> recommendations = teacherService.getRecommendationsForStudent(teacher, student,
                pageable);

        model.addAttribute("student", student);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("pageTitle", "学生详情 - " + student.getDisplayName());

        return "teacher/student-detail";
    }

    // ==================== 圈子管理功能 ====================

    /**
     * 我的圈子管理页面
     */
    @GetMapping("/circles")
    public String myCircles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        Teacher teacher = getCurrentTeacher(authentication);
        if (teacher == null) {
            return "redirect:/user/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Circle> circles = circleService.findUserCreatedCircles(teacher.getId(), pageable);

        model.addAttribute("circles", circles);
        model.addAttribute("pageTitle", "我的圈子");

        return "teacher/circles";
    }

    /**
     * 圈子成员管理页面
     */
    @GetMapping("/circles/{circleId}/members")
    public String circleMembers(
            @PathVariable Long circleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication,
            Model model) {

        Teacher teacher = getCurrentTeacher(authentication);
        if (teacher == null) {
            return "redirect:/user/login";
        }

        Circle circle = circleService.findById(circleId);
        if (circle == null || !circleService.canManageCircle(circle, teacher)) {
            return "redirect:/teacher/circles?error=unauthorized";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<com.moyun.reading.entity.circle.CircleMember> members = circleService.findCircleMembers(circleId,
                pageable);

        model.addAttribute("circle", circle);
        model.addAttribute("members", members);
        model.addAttribute("pageTitle", "成员管理 - " + circle.getName());

        return "teacher/circle-members";
    }

    // ==================== 学生申请管理功能 ====================

    /**
     * 学生申请管理页面
     */
    @GetMapping("/applications")
    public String applications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        Teacher teacher = getCurrentTeacher(authentication);
        if (teacher == null) {
            return "redirect:/user/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<TeacherStudentApplication> applications = teacherService.getTeacherApplications(teacher, pageable);

        model.addAttribute("applications", applications);
        model.addAttribute("teacher", teacher);
        model.addAttribute("pageTitle", "学生申请管理");

        return "teacher/applications";
    }

    /**
     * 同意学生申请
     */
    @PostMapping("/applications/{applicationId}/approve")
    public String approveApplication(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String replyMessage,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Teacher teacher = getCurrentTeacher(authentication);
            teacherService.approveApplication(teacher, applicationId, replyMessage);
            redirectAttributes.addFlashAttribute("successMessage", "申请已同意！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "操作失败：" + e.getMessage());
        }

        return "redirect:/teacher/applications";
    }

    /**
     * 拒绝学生申请
     */
    @PostMapping("/applications/{applicationId}/reject")
    public String rejectApplication(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String replyMessage,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Teacher teacher = getCurrentTeacher(authentication);
            teacherService.rejectApplication(teacher, applicationId, replyMessage);
            redirectAttributes.addFlashAttribute("successMessage", "申请已拒绝！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "操作失败：" + e.getMessage());
        }

        return "redirect:/teacher/applications";
    }

    // ==================== 工具方法 ====================

    /**
     * 获取当前登录的导师
     */
    private Teacher getCurrentTeacher(Authentication authentication) {
        if (!SecurityUtil.isTeacher()) {
            return null;
        }
        return (Teacher) SecurityUtil.getCurrentUser(authentication);
    }
}