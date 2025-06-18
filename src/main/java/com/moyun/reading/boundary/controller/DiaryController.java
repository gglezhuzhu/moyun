package com.moyun.reading.boundary.controller;

import com.moyun.reading.control.service.ReadingDiaryService;
import com.moyun.reading.control.service.BookService;
import com.moyun.reading.entity.diary.ReadingDiary;
import com.moyun.reading.entity.diary.DiaryComment;
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

/**
 * 读书日志控制器 - 边界类
 * 处理读书日志相关的所有功能
 * 
 * @author Moyun Team
 */
@Controller
@RequestMapping("/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final ReadingDiaryService readingDiaryService;
    private final BookService bookService;
    private static final Logger log = LoggerFactory.getLogger(DiaryController.class);

    /**
     * 日志首页
     */
    @GetMapping
    public String showDiaryList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String privacy,
            @RequestParam(required = false) Long bookId,
            Model model) {

        Pageable pageable = createPageable(page, size, sort);
        Page<ReadingDiary> diaries;

        if (keyword != null && !keyword.trim().isEmpty()) {
            diaries = readingDiaryService.searchPublicDiaries(keyword, pageable);
        } else if (bookId != null) {
            diaries = readingDiaryService.findBookDiaries(bookId, pageable);
            Book book = bookService.findById(bookId);
            model.addAttribute("selectedBook", book);
        } else if ("popular".equals(sort)) {
            diaries = readingDiaryService.findPopularDiaries(pageable);
        } else {
            diaries = readingDiaryService.findAllPublicDiaries(pageable);
        }

        model.addAttribute("diaries", diaries);
        model.addAttribute("currentSort", sort);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedPrivacy", privacy);
        model.addAttribute("readingMoods", ReadingDiary.ReadingMood.values());
        model.addAttribute("pageTitle", "读书日志");
        return "diary/list";
    }

    /**
     * 显示日志详情
     */
    @GetMapping("/{id}")
    public String showDiary(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        ReadingDiary diary = readingDiaryService.viewDiary(id);
        if (diary == null) {
            return "redirect:/diary?error=notfound";
        }

        User currentUser = SecurityUtil.getCurrentUser(authentication);
        if (!readingDiaryService.canAccessDiary(diary, currentUser)) {
            return "redirect:/diary?error=unauthorized";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DiaryComment> comments = readingDiaryService.findDiaryComments(id, pageable);
        long commentCount = readingDiaryService.getDiaryCommentCount(id);

        log.info("日志ID: {}, 评论数量: {}, 分页评论数量: {}", id, commentCount, comments.getTotalElements());

        model.addAttribute("diary", diary);
        model.addAttribute("comments", comments);
        model.addAttribute("commentCount", commentCount);
        model.addAttribute("pageTitle", diary.getTitle());
        return "diary/detail";
    }

    /**
     * 显示写日志页面
     */
    @GetMapping("/create")
    public String showCreateDiary(
            @RequestParam(required = false) Long bookId,
            Model model) {

        ReadingDiary diary = new ReadingDiary();
        if (bookId != null) {
            Book book = bookService.findById(bookId);
            diary.setBook(book);
        }

        model.addAttribute("diary", diary);
        model.addAttribute("readingMoods", ReadingDiary.ReadingMood.values());
        model.addAttribute("privacyOptions", ReadingDiary.DiaryPrivacy.values());
        model.addAttribute("books", bookService.findAllActiveBooks());
        model.addAttribute("pageTitle", "写日志");
        return "diary/create";
    }

    /**
     * 处理写日志
     */
    @PostMapping("/create")
    public String createDiary(
            @Valid @ModelAttribute ReadingDiary diary,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("diary", diary);
            model.addAttribute("readingMoods", ReadingDiary.ReadingMood.values());
            model.addAttribute("privacyOptions", ReadingDiary.DiaryPrivacy.values());
            model.addAttribute("books", bookService.findAllActiveBooks());
            model.addAttribute("pageTitle", "写日志");
            return "diary/create";
        }

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            diary.setUser(currentUser);

            ReadingDiary savedDiary = readingDiaryService.createDiary(diary);
            redirectAttributes.addFlashAttribute("successMessage", "日志发布成功！");
            return "redirect:/diary/" + savedDiary.getId();
        } catch (Exception e) {
            model.addAttribute("diary", diary);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("readingMoods", ReadingDiary.ReadingMood.values());
            model.addAttribute("privacyOptions", ReadingDiary.DiaryPrivacy.values());
            model.addAttribute("books", bookService.findAllActiveBooks());
            model.addAttribute("pageTitle", "写日志");
            return "diary/create";
        }
    }

    /**
     * 编辑日志页面
     */
    @GetMapping("/{id}/edit")
    public String showEditDiary(@PathVariable Long id, Model model, Authentication authentication) {
        ReadingDiary diary = readingDiaryService.findById(id);
        if (diary == null) {
            return "redirect:/diary?error=notfound";
        }

        User currentUser = SecurityUtil.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!diary.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/diary/" + id + "?error=unauthorized";
        }

        model.addAttribute("diary", diary);
        model.addAttribute("readingMoods", ReadingDiary.ReadingMood.values());
        model.addAttribute("privacyOptions", ReadingDiary.DiaryPrivacy.values());
        model.addAttribute("books", bookService.findAllActiveBooks());
        model.addAttribute("pageTitle", "编辑日志");
        return "diary/edit";
    }

    /**
     * 处理编辑日志
     */
    @PostMapping("/{id}/edit")
    public String updateDiary(
            @PathVariable Long id,
            @Valid @ModelAttribute ReadingDiary diary,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("diary", diary);
            model.addAttribute("readingMoods", ReadingDiary.ReadingMood.values());
            model.addAttribute("privacyOptions", ReadingDiary.DiaryPrivacy.values());
            model.addAttribute("books", bookService.findAllActiveBooks());
            model.addAttribute("pageTitle", "编辑日志");
            return "diary/edit";
        }

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            ReadingDiary existingDiary = readingDiaryService.findById(id);

            if (existingDiary == null || !existingDiary.getUser().getId().equals(currentUser.getId())) {
                return "redirect:/diary?error=unauthorized";
            }

            diary.setId(id);
            diary.setUser(currentUser);
            readingDiaryService.updateDiary(diary);

            redirectAttributes.addFlashAttribute("successMessage", "日志更新成功！");
            return "redirect:/diary/" + id;
        } catch (Exception e) {
            model.addAttribute("diary", diary);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("readingMoods", ReadingDiary.ReadingMood.values());
            model.addAttribute("privacyOptions", ReadingDiary.DiaryPrivacy.values());
            model.addAttribute("books", bookService.findAllActiveBooks());
            model.addAttribute("pageTitle", "编辑日志");
            return "diary/edit";
        }
    }

    /**
     * 删除日志
     */
    @PostMapping("/{id}/delete")
    public String deleteDiary(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            readingDiaryService.deleteDiary(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "日志删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败：" + e.getMessage());
        }

        return "redirect:/diary";
    }

    /**
     * 评论日志
     */
    @PostMapping("/{id}/comment")
    public String commentOnDiary(
            @PathVariable Long id,
            @RequestParam("content") String content,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "评论内容不能为空");
            return "redirect:/diary/" + id;
        }

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            ReadingDiary diary = readingDiaryService.findById(id);

            if (diary == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "日志不存在");
                return "redirect:/diary";
            }

            if (!readingDiaryService.canAccessDiary(diary, currentUser)) {
                redirectAttributes.addFlashAttribute("errorMessage", "无权访问此日志");
                return "redirect:/diary";
            }

            // 创建全新的评论对象
            DiaryComment comment = new DiaryComment();
            comment.setContent(content.trim());
            comment.setDiary(diary);
            comment.setUser(currentUser);

            readingDiaryService.createComment(comment);

            redirectAttributes.addFlashAttribute("successMessage", "评论成功！");
        } catch (Exception e) {
            log.error("评论失败", e);
            redirectAttributes.addFlashAttribute("errorMessage", "评论失败：" + e.getMessage());
        }

        return "redirect:/diary/" + id;
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
            readingDiaryService.deleteComment(commentId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "评论删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败：" + e.getMessage());
        }

        return "redirect:/diary";
    }

    /**
     * 我的日志
     */
    @GetMapping("/my")
    public String showMyDiaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        User currentUser = SecurityUtil.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReadingDiary> diaries = readingDiaryService.findUserDiaries(currentUser.getId(), pageable);

        model.addAttribute("diaries", diaries);
        model.addAttribute("pageTitle", "我的日志");
        return "diary/my-diaries";
    }

    /**
     * 创建分页对象
     */
    private Pageable createPageable(int page, int size, String sort) {
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createdAt";

        switch (sort) {
            case "popular":
                property = "likeCount";
                break;
            case "views":
                property = "viewCount";
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