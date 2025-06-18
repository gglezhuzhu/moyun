package com.moyun.reading.boundary.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 测试控制器
 */
@Controller
@RequestMapping("/test")
public class TestController {

    @GetMapping("/discussions")
    public String testDiscussions(Model model) {
        model.addAttribute("pageTitle", "测试讨论");
        model.addAttribute("message", "讨论功能测试");
        return "test/simple";
    }

    @GetMapping("/diary")
    public String testDiary(Model model) {
        model.addAttribute("pageTitle", "测试日志");
        model.addAttribute("message", "日志功能测试");
        return "test/simple";
    }
}