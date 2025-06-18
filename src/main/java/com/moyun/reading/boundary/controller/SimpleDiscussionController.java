package com.moyun.reading.boundary.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 简化的讨论控制器
 */
@Controller
@RequestMapping("/simple-discussions")
public class SimpleDiscussionController {

    @GetMapping
    public String showDiscussions(Model model) {
        model.addAttribute("pageTitle", "讨论区");
        return "discussion/simple-list";
    }
}