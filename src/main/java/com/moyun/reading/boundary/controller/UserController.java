package com.moyun.reading.boundary.controller;

import com.moyun.reading.boundary.dto.UserRegistrationDto;
import com.moyun.reading.control.service.UserService;
import com.moyun.reading.control.service.BookService;
import com.moyun.reading.dto.UserProfileUpdateDto;
import com.moyun.reading.entity.base.User;
import com.moyun.reading.utility.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 用户控制器 - 边界类
 * 处理用户注册、登录、个人信息等功能
 * 
 * @author Moyun Team
 */
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookService bookService;

    /**
     * 显示登录页面
     */
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("pageTitle", "用户登录");
        return "user/login";
    }

    /**
     * 显示注册页面
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userRegistrationDto", new UserRegistrationDto());
        model.addAttribute("pageTitle", "用户注册");
        return "user/register";
    }

    /**
     * 处理用户注册
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute UserRegistrationDto registrationDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "用户注册");
            return "user/register";
        }

        try {
            userService.registerUser(registrationDto);
            redirectAttributes.addFlashAttribute("successMessage", "注册成功！请登录。");
            return "redirect:/user/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "用户注册");
            return "user/register";
        }
    }

    /**
     * 用户个人中心
     */
    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        User currentUser = SecurityUtil.getCurrentUser(authentication);

        // 从数据库重新获取最新的用户信息
        User latestUser = userService.findById(currentUser.getId())
                .orElse(currentUser);

        System.out.println("=== 显示个人中心 ===");
        System.out.println("用户ID: " + latestUser.getId());
        System.out.println("用户名: " + latestUser.getUsername());
        System.out.println("真实姓名: " + latestUser.getRealName());
        System.out.println("手机: " + latestUser.getPhone());

        model.addAttribute("user", latestUser);
        model.addAttribute("pageTitle", "个人中心");
        return "user/profile";
    }

    /**
     * 显示编辑个人信息页面
     */
    @GetMapping("/profile/edit")
    public String showEditProfile(Model model, Authentication authentication) {
        User currentUser = SecurityUtil.getCurrentUser(authentication);

        // 从数据库重新获取最新的用户信息
        User latestUser = userService.findById(currentUser.getId())
                .orElse(currentUser);

        System.out.println("=== 显示编辑页面 ===");
        System.out.println("用户ID: " + latestUser.getId());
        System.out.println("用户名: " + latestUser.getUsername());
        System.out.println("真实姓名: " + latestUser.getRealName());
        System.out.println("手机: " + latestUser.getPhone());

        // 使用DTO而不是User实体
        UserProfileUpdateDto userDto = UserProfileUpdateDto.fromUser(latestUser);
        model.addAttribute("user", userDto);
        model.addAttribute("pageTitle", "编辑个人信息");
        return "user/edit-profile";
    }

    /**
     * 处理个人信息更新
     */
    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute UserProfileUpdateDto userProfileUpdateDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        System.out.println("=== 开始处理个人信息更新 ===");
        System.out.println("接收到的用户数据:");
        System.out.println("ID: " + userProfileUpdateDto.getId());
        System.out.println("用户名: " + userProfileUpdateDto.getUsername());
        System.out.println("邮箱: " + userProfileUpdateDto.getEmail());
        System.out.println("真实姓名: " + userProfileUpdateDto.getRealName());
        System.out.println("手机: " + userProfileUpdateDto.getPhone());
        System.out.println("性别: " + userProfileUpdateDto.getGender());
        System.out.println("个人简介: " + userProfileUpdateDto.getBio());

        if (bindingResult.hasErrors()) {
            System.out.println("表单验证失败:");
            bindingResult.getAllErrors().forEach(error -> System.out.println("- " + error.getDefaultMessage()));
            model.addAttribute("pageTitle", "编辑个人信息");
            return "user/edit-profile";
        }

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            System.out.println("当前登录用户: " + currentUser.getUsername() + " (ID: " + currentUser.getId() + ")");

            // 确保ID正确传递
            if (userProfileUpdateDto.getId() == null) {
                userProfileUpdateDto.setId(currentUser.getId());
                System.out.println("设置用户ID为: " + userProfileUpdateDto.getId());
            }

            User updatedUser = userService.updateUserProfile(currentUser.getId(), userProfileUpdateDto);
            System.out.println("更新后的用户信息:");
            System.out.println("真实姓名: " + updatedUser.getRealName());

            redirectAttributes.addFlashAttribute("successMessage", "个人信息更新成功！");
            return "redirect:/user/profile";
        } catch (Exception e) {
            System.out.println("更新失败: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "更新失败: " + e.getMessage());
            model.addAttribute("pageTitle", "编辑个人信息");
            return "user/edit-profile";
        }
    }

    /**
     * 显示修改密码页面
     */
    @GetMapping("/change-password")
    public String showChangePassword(Model model) {
        model.addAttribute("pageTitle", "修改密码");
        return "user/change-password";
    }

    /**
     * 处理密码修改
     */
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 验证新密码和确认密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "新密码和确认密码不一致");
            model.addAttribute("pageTitle", "修改密码");
            return "user/change-password";
        }

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            userService.changePassword(currentUser.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "密码修改成功！");
            return "redirect:/user/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "修改密码");
            return "user/change-password";
        }
    }

    /**
     * 用户图书管理页面
     */
    @GetMapping("/books")
    public String showUserBooks(Model model, Authentication authentication) {
        User currentUser = SecurityUtil.getCurrentUser(authentication);
        Pageable pageable = PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("userBooks", bookService.findUserBooks(currentUser.getId(), pageable).getContent());
        model.addAttribute("pageTitle", "我的图书");
        return "user/books";
    }

    /**
     * 显示忘记密码页面
     */
    @GetMapping("/forgot-password")
    public String showForgotPassword(Model model) {
        model.addAttribute("pageTitle", "忘记密码");
        return "user/forgot-password";
    }

    /**
     * 处理忘记密码请求
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String username,
            @RequestParam String email,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            User user = userService.validateUserForPasswordReset(username, email);
            // 将用户信息存储到session中，用于重置密码
            redirectAttributes.addFlashAttribute("resetUserId", user.getId());
            redirectAttributes.addFlashAttribute("resetUsername", user.getUsername());
            redirectAttributes.addFlashAttribute("successMessage",
                    "身份验证成功！请设置您的新密码。");
            return "redirect:/user/reset-password-direct";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "忘记密码");
            return "user/forgot-password";
        }
    }

    /**
     * 显示重置密码页面
     */
    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam String token, Model model) {
        try {
            // 验证token是否有效
            userService.validateResetToken(token);
            model.addAttribute("token", token);
            model.addAttribute("pageTitle", "重置密码");
            return "user/reset-password";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "重置链接无效或已过期");
            model.addAttribute("pageTitle", "重置密码");
            return "user/reset-password";
        }
    }

    /**
     * 处理密码重置
     */
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 验证新密码和确认密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "新密码和确认密码不一致");
            model.addAttribute("token", token);
            model.addAttribute("pageTitle", "重置密码");
            return "user/reset-password";
        }

        try {
            userService.resetPassword(token, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "密码重置成功！请使用新密码登录。");
            return "redirect:/user/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("token", token);
            model.addAttribute("pageTitle", "重置密码");
            return "user/reset-password";
        }
    }

    /**
     * 显示直接重置密码页面（用于忘记密码后的直接重置）
     */
    @GetMapping("/reset-password-direct")
    public String showResetPasswordDirect(Model model,
            @ModelAttribute("resetUserId") Long resetUserId,
            @ModelAttribute("resetUsername") String resetUsername) {
        if (resetUserId == null || resetUsername == null) {
            model.addAttribute("errorMessage", "会话已过期，请重新验证身份");
            return "user/forgot-password";
        }

        model.addAttribute("resetUserId", resetUserId);
        model.addAttribute("resetUsername", resetUsername);
        model.addAttribute("pageTitle", "重置密码");
        return "user/reset-password-direct";
    }

    /**
     * 处理直接密码重置（不需要token）
     */
    @PostMapping("/reset-password-direct")
    public String processResetPasswordDirect(@RequestParam Long resetUserId,
            @RequestParam String resetUsername,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 验证新密码和确认密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "新密码和确认密码不一致");
            model.addAttribute("resetUserId", resetUserId);
            model.addAttribute("resetUsername", resetUsername);
            model.addAttribute("pageTitle", "重置密码");
            return "user/reset-password-direct";
        }

        try {
            userService.resetPasswordDirect(resetUserId, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "密码重置成功！请使用新密码登录。");
            return "redirect:/user/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("resetUserId", resetUserId);
            model.addAttribute("resetUsername", resetUsername);
            model.addAttribute("pageTitle", "重置密码");
            return "user/reset-password-direct";
        }
    }
}