package com.moyun.reading.boundary.controller;

import com.moyun.reading.control.service.UserService;
import com.moyun.reading.control.service.ReadingDiaryService;
import com.moyun.reading.control.service.DiscussionService;
import com.moyun.reading.control.service.BookService;
import com.moyun.reading.control.service.ReviewService;
import com.moyun.reading.control.service.CircleService;
import com.moyun.reading.entity.base.User;
import com.moyun.reading.entity.user.UserRole;
import com.moyun.reading.entity.diary.ReadingDiary;
import com.moyun.reading.entity.discussion.Discussion;
import com.moyun.reading.entity.book.Book;
import com.moyun.reading.entity.review.Review;
import com.moyun.reading.entity.circle.Circle;
import com.moyun.reading.utility.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 管理员控制器 - 边界类
 * 处理管理员功能，包括用户管理和内容管理
 * 
 * @author Moyun Team
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ReadingDiaryService readingDiaryService;
    private final DiscussionService discussionService;
    private final BookService bookService;
    private final ReviewService reviewService;
    private final CircleService circleService;

    /**
     * 管理员首页 - 重定向到主页
     */
    @GetMapping
    public String adminHome() {
        return "redirect:/";
    }

    /**
     * 管理员控制台
     */
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("pageTitle", "管理员控制台");
        return "admin/dashboard";
    }

    /**
     * 用户管理页面
     */
    @GetMapping("/users")
    public String userManagement(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.findAllUsers(pageable, search);

        model.addAttribute("users", users);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "用户管理");
        model.addAttribute("userRoles", UserRole.values());

        return "admin/users";
    }

    /**
     * 日志管理页面
     */
    @GetMapping("/diaries")
    public String diaryManagement(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReadingDiary> diaries;

        if (keyword != null && !keyword.trim().isEmpty()) {
            diaries = readingDiaryService.searchPublicDiaries(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            diaries = readingDiaryService.findAllPublicDiaries(pageable);
        }

        model.addAttribute("diaries", diaries);
        model.addAttribute("pageTitle", "日志管理");
        return "admin/diaries";
    }

    /**
     * 讨论管理页面
     */
    @GetMapping("/discussions")
    public String discussionManagement(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Discussion> discussions;

        if (keyword != null && !keyword.trim().isEmpty()) {
            discussions = discussionService.searchDiscussions(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            discussions = discussionService.findAllDiscussions(pageable);
        }

        model.addAttribute("discussions", discussions);
        model.addAttribute("pageTitle", "讨论管理");
        return "admin/discussions";
    }

    /**
     * 书籍管理页面
     */
    @GetMapping("/books")
    public String bookManagement(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Book> books;

        if (keyword != null && !keyword.trim().isEmpty()) {
            books = bookService.searchBooks(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            books = bookService.findAllActiveBooks(pageable);
        }

        model.addAttribute("books", books);
        model.addAttribute("pageTitle", "书籍管理");
        return "admin/books";
    }

    /**
     * 书评管理页面
     */
    @GetMapping("/reviews")
    public String reviewManagement(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews;

        if (keyword != null && !keyword.trim().isEmpty()) {
            reviews = reviewService.searchReviews(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            reviews = reviewService.findAllReviews(pageable);
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("pageTitle", "书评管理");
        return "admin/reviews";
    }

    /**
     * 圈子管理页面
     */
    @GetMapping("/circles")
    public String circleManagement(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Circle> circles;

        if (keyword != null && !keyword.trim().isEmpty()) {
            circles = circleService.searchCircles(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            circles = circleService.findAllActiveCircles(pageable);
        }

        model.addAttribute("circles", circles);
        model.addAttribute("pageTitle", "圈子管理");
        return "admin/circles";
    }

    /**
     * 更新用户角色
     */
    @PostMapping("/users/{userId}/role")
    public String updateUserRole(@PathVariable Long userId,
            @RequestParam UserRole role,
            RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserRole(userId, role);
            redirectAttributes.addFlashAttribute("successMessage", "用户角色更新成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "更新失败：" + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    /**
     * 启用/禁用用户
     */
    @PostMapping("/users/{userId}/toggle")
    public String toggleUserStatus(@PathVariable Long userId,
            RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(userId);
            redirectAttributes.addFlashAttribute("successMessage", "用户状态更新成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "更新失败：" + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    /**
     * 删除用户
     */
    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable Long userId,
            RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "用户删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败：" + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    /**
     * 管理员删除日志
     */
    @PostMapping("/diaries/{diaryId}/delete")
    public String deleteDiary(@PathVariable Long diaryId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            readingDiaryService.deleteDiary(diaryId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "日志删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败：" + e.getMessage());
        }

        return "redirect:/admin/diaries";
    }

    /**
     * 管理员删除讨论
     */
    @PostMapping("/discussions/{discussionId}/delete")
    public String deleteDiscussion(@PathVariable Long discussionId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            discussionService.deleteDiscussion(discussionId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "讨论删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败：" + e.getMessage());
        }

        return "redirect:/admin/discussions";
    }

    /**
     * 管理员删除书籍
     */
    @PostMapping("/books/{bookId}/delete")
    public String deleteBook(@PathVariable Long bookId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            bookService.deleteBook(bookId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "书籍删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败：" + e.getMessage());
        }

        return "redirect:/admin/books";
    }

    /**
     * 管理员删除书评
     */
    @PostMapping("/reviews/{reviewId}/delete")
    public String deleteReview(@PathVariable Long reviewId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            reviewService.deleteReview(reviewId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "书评删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败：" + e.getMessage());
        }

        return "redirect:/admin/reviews";
    }

    /**
     * 管理员删除圈子
     */
    @PostMapping("/circles/{circleId}/delete")
    public String deleteCircle(@PathVariable Long circleId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            circleService.deleteCircle(circleId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "圈子删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败：" + e.getMessage());
        }

        return "redirect:/admin/circles";
    }
}