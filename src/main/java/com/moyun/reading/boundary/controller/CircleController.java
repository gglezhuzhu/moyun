package com.moyun.reading.boundary.controller;

import com.moyun.reading.control.service.CircleService;
import com.moyun.reading.entity.circle.Circle;
import com.moyun.reading.entity.circle.CircleMember;
import com.moyun.reading.entity.recommendation.BookRecommendation;
import com.moyun.reading.entity.base.User;
import com.moyun.reading.repository.BookRecommendationRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 圈子控制器 - 边界类
 * 处理圈子相关的所有功能
 * 
 * @author Moyun Team
 */
@Controller
@RequestMapping("/circles")
@RequiredArgsConstructor
public class CircleController {

    private final CircleService circleService;
    private final BookRecommendationRepository bookRecommendationRepository;

    /**
     * 圈子首页
     */
    @GetMapping
    public String showCircleList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String keyword,
            Authentication authentication,
            Model model) {

        Pageable pageable = createPageable(page, size, sort);
        Page<Circle> circles;

        if (keyword != null && !keyword.trim().isEmpty()) {
            circles = circleService.searchCircles(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            circles = circleService.findAllActiveCircles(pageable);
        }

        User currentUser = SecurityUtil.getCurrentUser(authentication);
        model.addAttribute("circles", circles);
        model.addAttribute("currentSort", sort);
        model.addAttribute("canCreateCircle", currentUser != null &&
                (SecurityUtil.isTeacher() || SecurityUtil.isAdmin()));
        model.addAttribute("pageTitle", "圈子");
        return "circles/list";
    }

    /**
     * 显示圈子详情
     */
    @GetMapping("/{id}")
    public String showCircle(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        Circle circle = circleService.findById(id);
        if (circle == null || !circle.isActive()) {
            return "redirect:/circles?error=notfound";
        }

        User currentUser = SecurityUtil.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        boolean isMember = circleService.isCircleMember(id, currentUser.getId());
        if (!isMember) {
            return "redirect:/circles?error=notmember";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CircleMember> members = circleService.findCircleMembers(id, pageable);
        CircleMember userMembership = circleService.getUserMembership(id, currentUser.getId());

        List<BookRecommendation> recommendations = bookRecommendationRepository
                .findRecentRecommendationsByCircle(circle, PageRequest.of(0, 20));

        model.addAttribute("circle", circle);
        model.addAttribute("members", members);
        model.addAttribute("userMembership", userMembership);
        model.addAttribute("canManage", circleService.canManageCircle(circle, currentUser));
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("pageTitle", circle.getName());
        return "circles/detail";
    }

    /**
     * 显示创建圈子页面
     */
    @GetMapping("/create")
    public String showCreateCircle(Authentication authentication, Model model) {
        User currentUser = SecurityUtil.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        if (!SecurityUtil.isTeacher() && !SecurityUtil.isAdmin()) {
            return "redirect:/circles?error=unauthorized";
        }

        model.addAttribute("circle", new Circle());
        model.addAttribute("pageTitle", "创建圈子");
        return "circles/create";
    }

    /**
     * 处理创建圈子
     */
    @PostMapping("/create")
    public String createCircle(
            @Valid @ModelAttribute Circle circle,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("circle", circle);
            model.addAttribute("pageTitle", "创建圈子");
            return "circles/create";
        }

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            Circle savedCircle = circleService.createCircle(circle, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "圈子创建成功！");
            return "redirect:/circles/" + savedCircle.getId();
        } catch (Exception e) {
            model.addAttribute("circle", circle);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "创建圈子");
            return "circles/create";
        }
    }

    /**
     * 邀请用户加入圈子
     */
    @PostMapping("/{id}/invite")
    public String inviteUser(
            @PathVariable Long id,
            @RequestParam Long userId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            circleService.inviteUserToCircle(id, userId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "用户邀请成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "邀请失败：" + e.getMessage());
        }

        return "redirect:/circles/" + id;
    }

    /**
     * 移除圈子成员
     */
    @PostMapping("/{id}/remove")
    public String removeMember(
            @PathVariable Long id,
            @RequestParam Long userId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            circleService.removeMemberFromCircle(id, userId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "成员移除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "移除失败：" + e.getMessage());
        }

        return "redirect:/circles/" + id;
    }

    /**
     * 显示编辑圈子页面
     */
    @GetMapping("/{id}/edit")
    public String showEditCircle(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        Circle circle = circleService.findById(id);
        if (circle == null || !circle.isActive()) {
            return "redirect:/circles?error=notfound";
        }

        User currentUser = SecurityUtil.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        if (!circleService.canManageCircle(circle, currentUser)) {
            return "redirect:/circles/" + id + "?error=unauthorized";
        }

        model.addAttribute("circle", circle);
        model.addAttribute("pageTitle", "编辑圈子 - " + circle.getName());
        return "circles/edit";
    }

    /**
     * 处理编辑圈子
     */
    @PostMapping("/{id}/edit")
    public String editCircle(
            @PathVariable Long id,
            @Valid @ModelAttribute Circle circle,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("circle", circle);
            model.addAttribute("pageTitle", "编辑圈子 - " + circle.getName());
            return "circles/edit";
        }

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            circle.setId(id); // 确保ID正确
            Circle updatedCircle = circleService.updateCircle(circle, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "圈子信息更新成功！");
            return "redirect:/circles/" + updatedCircle.getId();
        } catch (Exception e) {
            model.addAttribute("circle", circle);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "编辑圈子 - " + circle.getName());
            return "circles/edit";
        }
    }

    /**
     * 获取可邀请的学生列表（AJAX接口）
     */
    @GetMapping("/{id}/available-students")
    @ResponseBody
    public List<Map<String, Object>> getAvailableStudents(
            @PathVariable Long id,
            Authentication authentication) {

        User currentUser = SecurityUtil.getCurrentUser(authentication);
        if (currentUser == null || !SecurityUtil.isTeacher()) {
            return new ArrayList<>();
        }

        Circle circle = circleService.findById(id);
        if (circle == null || !circleService.canManageCircle(circle, currentUser)) {
            return new ArrayList<>();
        }

        return circleService.getAvailableStudentsForInvite(id, currentUser.getId());
    }

    /**
     * 我的圈子
     */
    @GetMapping("/my")
    public String showMyCircles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "joined") String type,
            Authentication authentication,
            Model model) {

        User currentUser = SecurityUtil.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Circle> circles;

        if ("created".equals(type)) {
            circles = circleService.findUserCreatedCircles(currentUser.getId(), pageable);
        } else {
            circles = circleService.findUserCircles(currentUser.getId(), pageable);
        }

        model.addAttribute("circles", circles);
        model.addAttribute("currentType", type);
        model.addAttribute("pageTitle", "我的圈子");
        return "circles/my-circles";
    }

    /**
     * 创建分页对象
     */
    private Pageable createPageable(int page, int size, String sort) {
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createdAt";

        switch (sort) {
            case "popular":
                property = "memberCount";
                break;
            case "active":
                property = "postCount";
                break;
            case "oldest":
                direction = Sort.Direction.ASC;
                break;
            case "latest":
            default:
                break;
        }

        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}