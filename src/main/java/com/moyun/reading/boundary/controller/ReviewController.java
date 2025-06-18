package com.moyun.reading.boundary.controller;

import com.moyun.reading.control.service.ReviewService;
import com.moyun.reading.control.service.BookService;
import com.moyun.reading.entity.review.Review;
import com.moyun.reading.entity.review.ReviewComment;
import com.moyun.reading.entity.review.ReviewLike;
import com.moyun.reading.entity.base.User;
import com.moyun.reading.entity.book.Book;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * 书评控制器 - 边界类
 * 处理书评相关的所有功能
 * 
 * @author Moyun Team
 */
@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final BookService bookService;
    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    /**
     * 书评首页
     */
    @GetMapping
    public String showReviewList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long bookId,
            Model model) {

        Pageable pageable = createPageable(page, size, sort);
        Page<Review> reviews;

        if (keyword != null && !keyword.trim().isEmpty()) {
            reviews = reviewService.searchReviews(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else if (bookId != null) {
            reviews = reviewService.findBookReviews(bookId, pageable);
            Book book = bookService.findById(bookId);
            model.addAttribute("selectedBook", book);
        } else {
            reviews = reviewService.findAllReviews(pageable);
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("currentSort", sort);
        model.addAttribute("pageTitle", "书评");
        return "reviews/list";
    }

    /**
     * 显示书评详情
     */
    @GetMapping("/{id}")
    public String showReview(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        Review review = reviewService.viewReview(id);
        if (review == null || !review.isPublished()) {
            return "redirect:/reviews?error=notfound";
        }

        User currentUser = SecurityUtil.getCurrentUser(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewComment> comments = reviewService.findReviewComments(id, pageable);
        long commentCount = reviewService.getReviewCommentCount(id);

        log.info("书评ID: {}, 评论数量: {}, 分页评论数量: {}", id, commentCount, comments.getTotalElements());

        model.addAttribute("review", review);
        model.addAttribute("comments", comments);
        model.addAttribute("commentCount", commentCount);
        model.addAttribute("isLiked", currentUser != null && reviewService.isUserLikedReview(id, currentUser.getId()));
        model.addAttribute("pageTitle", review.getTitle());
        return "reviews/detail";
    }

    /**
     * 显示写书评页面
     */
    @GetMapping("/create")
    public String showCreateReview(
            @RequestParam(required = false) Long bookId,
            Model model) {

        Review review = new Review();
        if (bookId != null) {
            Book book = bookService.findById(bookId);
            review.setBook(book);
        }

        model.addAttribute("review", review);
        model.addAttribute("books", bookService.findAllActiveBooks());
        model.addAttribute("pageTitle", "写书评");
        return "reviews/create";
    }

    /**
     * 处理写书评
     */
    @PostMapping("/create")
    public String createReview(
            @Valid @ModelAttribute Review review,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("books", bookService.findAllActiveBooks());
            model.addAttribute("pageTitle", "写书评");
            return "reviews/create";
        }

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            review.setUser(currentUser);
            review.setStatus(Review.ReviewStatus.PUBLISHED);

            Review savedReview = reviewService.createReview(review);
            redirectAttributes.addFlashAttribute("successMessage", "书评发布成功！");
            return "redirect:/reviews/" + savedReview.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("books", bookService.findAllActiveBooks());
            model.addAttribute("pageTitle", "写书评");
            return "reviews/create";
        }
    }

    /**
     * 编辑书评页面
     */
    @GetMapping("/{id}/edit")
    public String showEditReview(@PathVariable Long id, Model model, Authentication authentication) {
        Review review = reviewService.findById(id);
        if (review == null) {
            return "redirect:/reviews?error=notfound";
        }

        User currentUser = SecurityUtil.getCurrentUser(authentication);
        if (!review.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/reviews/" + id + "?error=unauthorized";
        }

        model.addAttribute("review", review);
        model.addAttribute("books", bookService.findAllActiveBooks());
        model.addAttribute("pageTitle", "编辑书评");
        return "reviews/edit";
    }

    /**
     * 处理编辑书评
     */
    @PostMapping("/{id}/edit")
    public String updateReview(
            @PathVariable Long id,
            @Valid @ModelAttribute Review review,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("books", bookService.findAllActiveBooks());
            model.addAttribute("pageTitle", "编辑书评");
            return "reviews/edit";
        }

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            Review existingReview = reviewService.findById(id);

            if (existingReview == null || !existingReview.getUser().getId().equals(currentUser.getId())) {
                return "redirect:/reviews?error=unauthorized";
            }

            review.setId(id);
            review.setUser(currentUser);
            reviewService.updateReview(review);

            redirectAttributes.addFlashAttribute("successMessage", "书评更新成功！");
            return "redirect:/reviews/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("books", bookService.findAllActiveBooks());
            model.addAttribute("pageTitle", "编辑书评");
            return "reviews/edit";
        }
    }

    /**
     * 删除书评
     */
    @PostMapping("/{id}/delete")
    public String deleteReview(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            reviewService.deleteReview(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "书评删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败：" + e.getMessage());
        }

        return "redirect:/reviews";
    }

    /**
     * 点赞书评
     */
    @PostMapping("/{id}/like")
    @ResponseBody
    public String likeReview(@PathVariable Long id, Authentication authentication) {
        log.info("收到书评点赞请求: reviewId={}", id);
        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            if (currentUser == null) {
                log.warn("用户未登录，无法点赞");
                return "login_required";
            }

            log.info("用户 {} 尝试点赞书评 {}", currentUser.getUsername(), id);
            boolean liked = reviewService.toggleLike(id, currentUser.getId());
            log.info("点赞操作完成: reviewId={}, liked={}", id, liked);
            return liked ? "liked" : "unliked";
        } catch (Exception e) {
            log.error("点赞操作失败: reviewId={}, error={}", id, e.getMessage(), e);
            return "error";
        }
    }

    /**
     * 评论书评
     */
    @PostMapping("/{id}/comment")
    @Transactional
    public String commentOnReview(
            @PathVariable Long id,
            @RequestParam("content") String content,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "评论内容不能为空");
            return "redirect:/reviews/" + id;
        }

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            Review review = reviewService.findById(id);

            if (review == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "书评不存在");
                return "redirect:/reviews";
            }

            // 创建全新的评论对象
            ReviewComment comment = new ReviewComment();
            comment.setContent(content.trim());
            comment.setReview(review);
            comment.setUser(currentUser);

            reviewService.createComment(comment);

            redirectAttributes.addFlashAttribute("successMessage", "评论成功！");
        } catch (Exception e) {
            log.error("评论失败", e);
            redirectAttributes.addFlashAttribute("errorMessage", "评论失败：" + e.getMessage());
        }

        return "redirect:/reviews/" + id;
    }

    /**
     * 删除评论
     */
    @PostMapping("/comment/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long commentId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            Long reviewId = reviewService.deleteComment(commentId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "评论删除成功！");

            if (reviewId != null) {
                return "redirect:/reviews/" + reviewId;
            } else {
                return "redirect:/reviews";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败：" + e.getMessage());
            return "redirect:/reviews";
        }
    }

    /**
     * 我的书评
     */
    @GetMapping("/my")
    public String showMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        User currentUser = SecurityUtil.getCurrentUser(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewService.findUserReviews(currentUser.getId(), pageable);

        model.addAttribute("reviews", reviews);
        model.addAttribute("pageTitle", "我的书评");
        return "reviews/my-reviews";
    }

    /**
     * 创建分页对象
     */
    private Pageable createPageable(int page, int size, String sort) {
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createdAt";

        switch (sort) {
            case "popular":
                property = "viewCount";
                break;
            case "rating_high":
                property = "rating";
                break;
            case "rating_low":
                property = "rating";
                direction = Sort.Direction.ASC;
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