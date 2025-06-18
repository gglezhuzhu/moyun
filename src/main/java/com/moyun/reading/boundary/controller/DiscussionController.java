package com.moyun.reading.boundary.controller;

import com.moyun.reading.control.service.DiscussionService;
import com.moyun.reading.entity.discussion.Discussion;
import com.moyun.reading.entity.discussion.DiscussionReply;
import com.moyun.reading.entity.base.User;
import com.moyun.reading.utility.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * 讨论控制器 - 边界类
 * 处理讨论相关的所有功能
 * 
 * @author Moyun Team
 */
@Slf4j
@Controller
@RequestMapping("/discussions")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;

    /**
     * 讨论区首页
     */
    @GetMapping
    public String showDiscussionList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            Model model) {

        try {
            Pageable pageable = createPageable(page, size, sort);
            Page<Discussion> discussions = discussionService.findAllDiscussions(pageable);

            model.addAttribute("discussions", discussions);
            model.addAttribute("discussionTypes", Discussion.DiscussionType.values());
            model.addAttribute("currentSort", sort);
            model.addAttribute("selectedType", type);
            model.addAttribute("keyword", keyword);
            model.addAttribute("pageTitle", "讨论区");
            return "discussion/list";
        } catch (Exception e) {
            log.error("显示讨论列表失败", e);
            model.addAttribute("errorMessage", "加载讨论列表失败：" + e.getMessage());
            model.addAttribute("discussions", Page.empty());
            model.addAttribute("discussionTypes", Discussion.DiscussionType.values());
            model.addAttribute("currentSort", sort);
            model.addAttribute("selectedType", type);
            model.addAttribute("keyword", keyword);
            model.addAttribute("pageTitle", "讨论区");
            return "discussion/list";
        }
    }

    /**
     * 显示讨论详情
     */
    @GetMapping("/{id}")
    public String showDiscussion(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        Discussion discussion = discussionService.viewDiscussion(id);
        if (discussion == null) {
            return "redirect:/discussions?error=notfound";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<DiscussionReply> replies = discussionService.findDiscussionReplies(id, pageable);
        long replyCount = discussionService.getDiscussionReplyCount(id);

        log.info("讨论ID: {}, 回复数量: {}, 分页回复数量: {}", id, replyCount, replies.getTotalElements());

        model.addAttribute("discussion", discussion);
        model.addAttribute("replies", replies);
        model.addAttribute("replyCount", replyCount);
        model.addAttribute("newReply", new DiscussionReply());
        model.addAttribute("pageTitle", discussion.getTitle());
        return "discussion/detail";
    }

    /**
     * 显示发起讨论页面
     */
    @GetMapping("/create")
    public String showCreateDiscussion(Model model) {
        model.addAttribute("discussion", new Discussion());
        model.addAttribute("discussionTypes", Discussion.DiscussionType.values());
        model.addAttribute("pageTitle", "发起讨论");
        return "discussion/create";
    }

    /**
     * 处理发起讨论
     */
    @PostMapping("/create")
    public String createDiscussion(
            @Valid @ModelAttribute Discussion discussion,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("discussionTypes", Discussion.DiscussionType.values());
            model.addAttribute("pageTitle", "发起讨论");
            return "discussion/create";
        }

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            discussion.setUser(currentUser);

            Discussion savedDiscussion = discussionService.createDiscussion(discussion);
            redirectAttributes.addFlashAttribute("successMessage", "讨论发起成功！");
            return "redirect:/discussions/" + savedDiscussion.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("discussionTypes", Discussion.DiscussionType.values());
            model.addAttribute("pageTitle", "发起讨论");
            return "discussion/create";
        }
    }

    /**
     * 编辑讨论页面
     */
    @GetMapping("/{id}/edit")
    public String showEditDiscussion(@PathVariable Long id, Model model, Authentication authentication) {
        Discussion discussion = discussionService.findById(id);
        if (discussion == null) {
            return "redirect:/discussions?error=notfound";
        }

        User currentUser = SecurityUtil.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!discussion.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/discussions/" + id + "?error=unauthorized";
        }

        model.addAttribute("discussion", discussion);
        model.addAttribute("discussionTypes", Discussion.DiscussionType.values());
        model.addAttribute("pageTitle", "编辑讨论");
        return "discussion/edit";
    }

    /**
     * 处理编辑讨论
     */
    @PostMapping("/{id}/edit")
    public String updateDiscussion(
            @PathVariable Long id,
            @Valid @ModelAttribute Discussion discussion,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("discussionTypes", Discussion.DiscussionType.values());
            model.addAttribute("pageTitle", "编辑讨论");
            return "discussion/edit";
        }

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            Discussion existingDiscussion = discussionService.findById(id);

            if (existingDiscussion == null || !existingDiscussion.getUser().getId().equals(currentUser.getId())) {
                return "redirect:/discussions?error=unauthorized";
            }

            discussion.setId(id);
            discussion.setUser(currentUser);
            discussionService.updateDiscussion(discussion);

            redirectAttributes.addFlashAttribute("successMessage", "讨论更新成功！");
            return "redirect:/discussions/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("discussionTypes", Discussion.DiscussionType.values());
            model.addAttribute("pageTitle", "编辑讨论");
            return "discussion/edit";
        }
    }

    /**
     * 删除讨论
     */
    @PostMapping("/{id}/delete")
    public String deleteDiscussion(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            discussionService.deleteDiscussion(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "讨论删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败：" + e.getMessage());
        }

        return "redirect:/discussions";
    }

    /**
     * 回复讨论
     */
    @PostMapping("/{id}/reply")
    public String replyToDiscussion(
            @PathVariable Long id,
            @Valid @ModelAttribute DiscussionReply reply,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "回复内容不能为空");
            return "redirect:/discussions/" + id;
        }

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            Discussion discussion = discussionService.findById(id);

            if (discussion == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "讨论不存在");
                return "redirect:/discussions";
            }

            reply.setDiscussion(discussion);
            reply.setUser(currentUser);
            discussionService.createReply(reply);

            redirectAttributes.addFlashAttribute("successMessage", "回复成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "回复失败：" + e.getMessage());
        }

        return "redirect:/discussions/" + id;
    }

    /**
     * 删除回复
     */
    @PostMapping("/reply/{replyId}/delete")
    public String deleteReply(
            @PathVariable Long replyId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            discussionService.deleteReply(replyId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "回复删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败：" + e.getMessage());
        }

        return "redirect:/discussions";
    }

    /**
     * 我的讨论
     */
    @GetMapping("/my")
    public String showMyDiscussions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {

        User currentUser = SecurityUtil.getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Discussion> discussions = discussionService.findUserDiscussions(currentUser.getId(), pageable);

        model.addAttribute("discussions", discussions);
        model.addAttribute("pageTitle", "我的讨论");
        return "discussion/my-discussions";
    }

    /**
     * 创建分页对象
     */
    private Pageable createPageable(int page, int size, String sort) {
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createdAt";

        switch (sort) {
            case "hot":
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