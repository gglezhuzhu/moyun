package com.moyun.reading.boundary.controller;

import com.moyun.reading.control.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 信息页面控制器 - 边界类
 * 处理关于我们、联系我们等信息页面请求
 * 
 * @author Moyun Team
 */
@Controller
@RequiredArgsConstructor
public class InfoController {

    private final StatisticsService statisticsService;

    /**
     * 帮助中心页面
     */
    @GetMapping("/help")
    public String help(Model model) {
        model.addAttribute("pageTitle", "帮助中心");
        return "info/help";
    }

    /**
     * 关于我们页面
     */
    @GetMapping("/about")
    public String about(Model model) {
        // 获取平台统计数据
        model.addAttribute("statistics", statisticsService.getPlatformStatistics());
        model.addAttribute("pageTitle", "关于我们");
        return "info/about";
    }

    /**
     * 联系我们页面
     */
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "联系我们");
        return "info/contact";
    }
}