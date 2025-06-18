package com.moyun.reading.boundary.controller;

import com.moyun.reading.control.service.StudentService;
import com.moyun.reading.control.service.DiscussionService;
import com.moyun.reading.entity.user.Student;
import com.moyun.reading.entity.user.Teacher;
import com.moyun.reading.entity.user.TeacherStudentApplication;
import com.moyun.reading.entity.discussion.Discussion;
import com.moyun.reading.utility.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * 学生控制器
 * 处理学生相关的页面请求和操作
 */
@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final DiscussionService discussionService;

    /**
     * 学生工作台首页
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Student student = getCurrentStudent(authentication);
        if (student == null) {
            return "redirect:/user/login";
        }

        // 获取学生的问题
        Page<Discussion> myQuestions = discussionService.findUserDiscussions(student.getId(),
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")));

        // 获取学生的待处理申请
        var pendingApplications = studentService.getStudentPendingApplications(student);

        model.addAttribute("student", student);
        model.addAttribute("myQuestions", myQuestions);
        model.addAttribute("pendingApplications", pendingApplications);
        model.addAttribute("pageTitle", "学生工作台");

        return "student/dashboard";
    }

    /**
     * 选择导师页面
     */
    @GetMapping("/teachers")
    public String selectTeacher(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            Authentication authentication,
            Model model) {

        Student student = getCurrentStudent(authentication);
        if (student == null) {
            return "redirect:/user/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Teacher> teachers;

        if (keyword != null && !keyword.trim().isEmpty()) {
            teachers = studentService.searchTeachers(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            teachers = studentService.getAvailableTeachers(pageable);
        }

        // 为每个导师检查申请状态
        teachers.getContent().forEach(teacher -> {
            Optional<TeacherStudentApplication> application = studentService.getApplicationToTeacher(student, teacher);
            teacher.setApplicationStatus(application.orElse(null));
        });

        model.addAttribute("teachers", teachers);
        model.addAttribute("student", student);
        model.addAttribute("pageTitle", "选择导师");

        return "student/select-teacher";
    }

    /**
     * 处理申请导师
     */
    @PostMapping("/teachers/apply")
    public String applyToTeacher(
            @RequestParam Long teacherId,
            @RequestParam(required = false) String applicationMessage,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Student student = getCurrentStudent(authentication);
            studentService.applyToTeacher(student, teacherId, applicationMessage);
            redirectAttributes.addFlashAttribute("successMessage", "申请已发送，请等待导师审核！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "申请失败：" + e.getMessage());
        }

        return "redirect:/student/teachers";
    }

    /**
     * 取消申请
     */
    @PostMapping("/applications/{id}/cancel")
    public String cancelApplication(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Student student = getCurrentStudent(authentication);
            studentService.cancelApplication(student, id);
            redirectAttributes.addFlashAttribute("successMessage", "申请已取消！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "取消失败：" + e.getMessage());
        }

        return "redirect:/student/applications";
    }

    /**
     * 我的申请页面
     */
    @GetMapping("/applications")
    public String myApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        Student student = getCurrentStudent(authentication);
        if (student == null) {
            return "redirect:/user/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<TeacherStudentApplication> applications = studentService.getStudentApplications(student, pageable);

        model.addAttribute("applications", applications);
        model.addAttribute("student", student);
        model.addAttribute("pageTitle", "我的申请");

        return "student/applications";
    }

    /**
     * 处理更换导师
     */
    @PostMapping("/teachers/change")
    public String changeTeacher(
            @RequestParam Long teacherId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Student student = getCurrentStudent(authentication);
            studentService.changeTeacher(student, teacherId);
            redirectAttributes.addFlashAttribute("successMessage", "导师更换成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "更换失败：" + e.getMessage());
        }

        return "redirect:/student/teachers";
    }

    /**
     * 处理取消选择导师
     */
    @PostMapping("/teachers/unselect")
    public String unselectTeacher(
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Student student = getCurrentStudent(authentication);
            studentService.unselectTeacher(student);
            redirectAttributes.addFlashAttribute("successMessage", "已取消选择导师！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "取消失败：" + e.getMessage());
        }

        return "redirect:/student/teachers";
    }

    /**
     * 我的问题页面
     */
    @GetMapping("/questions")
    public String myQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        Student student = getCurrentStudent(authentication);
        if (student == null) {
            return "redirect:/user/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Discussion> questions = discussionService.findUserDiscussions(student.getId(), pageable);

        model.addAttribute("questions", questions);
        model.addAttribute("student", student);
        model.addAttribute("pageTitle", "我的问题");

        return "student/questions";
    }

    /**
     * 获取当前登录的学生
     */
    private Student getCurrentStudent(Authentication authentication) {
        if (!SecurityUtil.isStudent()) {
            return null;
        }
        return (Student) SecurityUtil.getCurrentUser(authentication);
    }
}