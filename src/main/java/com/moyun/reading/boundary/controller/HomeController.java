package com.moyun.reading.boundary.controller;

import com.moyun.reading.control.service.BookService;
import com.moyun.reading.control.service.ReviewService;
import com.moyun.reading.control.service.StatisticsService;
import com.moyun.reading.entity.book.Book;
import com.moyun.reading.entity.review.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 首页控制器 - 边界类
 * 处理用户界面请求和响应
 * 
 * @author Moyun Team
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final BookService bookService;
    private final ReviewService reviewService;
    private final StatisticsService statisticsService;

    /**
     * 首页
     */
    @GetMapping("/")
    public String index(Model model) {
        // 获取最新图书
        Page<Book> latestBooks = bookService.findLatestBooks(6);

        // 获取热门图书
        Page<Book> popularBooks = bookService.findPopularBooks(6);

        // 获取最新书评
        Page<Review> latestReviews = reviewService.findLatestReviews(5);

        // 获取平台统计数据
        model.addAttribute("statistics", statisticsService.getPlatformStatistics());

        model.addAttribute("latestBooks", latestBooks.getContent());
        model.addAttribute("popularBooks", popularBooks.getContent());
        model.addAttribute("latestReviews", latestReviews.getContent());
        model.addAttribute("pageTitle", "墨韵读书平台 - 首页");

        return "index";
    }

    /**
     * 搜索页面
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "all") String type,
            @PageableDefault(size = 12) Pageable pageable,
            Model model) {

        if (keyword != null && !keyword.trim().isEmpty()) {
            Page<Book> searchResults = null;

            switch (type) {
                case "book":
                    searchResults = bookService.searchBooks(keyword, pageable);
                    break;
                case "author":
                    searchResults = bookService.searchByAuthor(keyword, pageable);
                    break;
                default:
                    searchResults = bookService.searchBooks(keyword, pageable);
            }

            model.addAttribute("searchResults", searchResults);
            model.addAttribute("keyword", keyword);
            model.addAttribute("type", type);
            model.addAttribute("pageTitle", "搜索结果: " + keyword);
        } else {
            model.addAttribute("pageTitle", "搜索图书");
        }

        return "search";
    }

}