package com.moyun.reading.boundary.controller;

import com.moyun.reading.control.service.BookService;
import com.moyun.reading.control.service.ReviewService;
import com.moyun.reading.entity.book.Book;
import com.moyun.reading.entity.review.Review;
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

import java.util.List;

/**
 * 图书控制器 - 边界类
 * 处理图书相关的所有功能
 * 
 * @author Moyun Team
 */
@Slf4j
@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final ReviewService reviewService;

    /**
     * 图书首页
     */
    @GetMapping
    public String showBookList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Long categoryId,
            Model model) {

        Pageable pageable = createPageable(page, size, sort);
        Page<Book> books;

        if (keyword != null && !keyword.trim().isEmpty()) {
            books = bookService.searchBooks(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else if (author != null && !author.trim().isEmpty()) {
            books = bookService.searchByAuthor(author, pageable);
            model.addAttribute("author", author);
        } else if (categoryId != null) {
            books = bookService.findBooksByCategory(categoryId, pageable);
            model.addAttribute("selectedCategoryId", categoryId);
        } else {
            books = bookService.findAllActiveBooks(pageable);
        }

        model.addAttribute("books", books);
        model.addAttribute("currentSort", sort);
        model.addAttribute("pageTitle", "图书库");
        return "books/list";
    }

    /**
     * 显示图书详情
     */
    @GetMapping("/{id}")
    public String showBook(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Book book = bookService.viewBook(id);
        if (book == null || !book.isAvailable()) {
            return "redirect:/books?error=notfound";
        }

        // 获取该书的书评
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewService.findBookReviews(id, pageable);

        model.addAttribute("book", book);
        model.addAttribute("reviews", reviews);
        model.addAttribute("pageTitle", book.getTitle());
        return "books/detail";
    }

    /**
     * 显示添加图书页面
     */
    @GetMapping("/add")
    public String showAddBook(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("pageTitle", "添加图书");
        return "books/add";
    }

    /**
     * 处理添加图书
     */
    @PostMapping("/add")
    public String addBook(
            @Valid @ModelAttribute Book book,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("开始添加图书，标题: {}", book.getTitle());

        if (bindingResult.hasErrors()) {
            log.warn("图书验证失败，错误数量: {}", bindingResult.getErrorCount());
            model.addAttribute("pageTitle", "添加图书");
            return "books/add";
        }

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            log.info("当前用户: {}", currentUser != null ? currentUser.getUsername() : "null");

            if (currentUser == null) {
                log.error("用户未登录或获取当前用户失败");
                model.addAttribute("errorMessage", "用户未登录或登录已过期，请重新登录");
                model.addAttribute("pageTitle", "添加图书");
                return "books/add";
            }

            book.setAddedByUser(currentUser);
            book.setStatus(Book.BookStatus.ACTIVE);

            log.info("准备保存图书到数据库");
            Book savedBook = bookService.createBook(book);
            log.info("图书保存成功，ID: {}", savedBook.getId());

            redirectAttributes.addFlashAttribute("successMessage", "图书添加成功！");
            return "redirect:/books/" + savedBook.getId();
        } catch (Exception e) {
            log.error("添加图书失败", e);
            model.addAttribute("errorMessage", "添加图书失败：" + e.getMessage());
            model.addAttribute("pageTitle", "添加图书");
            return "books/add";
        }
    }

    /**
     * 编辑图书页面
     */
    @GetMapping("/{id}/edit")
    public String showEditBook(@PathVariable Long id, Model model, Authentication authentication) {
        Book book = bookService.findById(id);
        if (book == null) {
            return "redirect:/books?error=notfound";
        }

        User currentUser = SecurityUtil.getCurrentUser(authentication);
        if (!bookService.canEditBook(book, currentUser)) {
            return "redirect:/books/" + id + "?error=unauthorized";
        }

        model.addAttribute("book", book);
        model.addAttribute("pageTitle", "编辑图书");
        return "books/edit";
    }

    /**
     * 处理编辑图书
     */
    @PostMapping("/{id}/edit")
    public String updateBook(
            @PathVariable Long id,
            @Valid @ModelAttribute Book book,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "编辑图书");
            return "books/edit";
        }

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            Book existingBook = bookService.findById(id);

            if (existingBook == null || !bookService.canEditBook(existingBook, currentUser)) {
                return "redirect:/books?error=unauthorized";
            }

            book.setId(id);
            book.setAddedByUser(existingBook.getAddedByUser());
            bookService.updateBook(book);

            redirectAttributes.addFlashAttribute("successMessage", "图书信息更新成功！");
            return "redirect:/books/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "编辑图书");
            return "books/edit";
        }
    }

    /**
     * 删除图书
     */
    @PostMapping("/{id}/delete")
    public String deleteBook(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = SecurityUtil.getCurrentUser(authentication);
            bookService.deleteBook(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "图书删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败：" + e.getMessage());
        }

        return "redirect:/books";
    }

    /**
     * 我的图书
     */
    @GetMapping("/my")
    public String showMyBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Authentication authentication,
            Model model) {

        User currentUser = SecurityUtil.getCurrentUser(authentication);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Book> books = bookService.findUserBooks(currentUser.getId(), pageable);

        model.addAttribute("books", books);
        model.addAttribute("pageTitle", "我的图书");
        return "books/my-books";
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
            case "rating":
                property = "ratingSum";
                break;
            case "title":
                property = "title";
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

    // ==================== API端点 ====================

    /**
     * 搜索图书API（用于推荐功能）
     */
    @GetMapping("/api/books/search")
    @ResponseBody
    public List<BookSearchResult> searchBooksApi(@RequestParam String keyword) {
        try {
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "viewCount"));
            Page<Book> books = bookService.searchBooks(keyword, pageable);

            return books.getContent().stream()
                    .map(book -> new BookSearchResult(
                            book.getId(),
                            book.getTitle(),
                            book.getAuthor(),
                            book.getPublisher(),
                            book.getDescription(),
                            book.getCoverImageUrl()))
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("搜索图书API失败", e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 图书搜索结果DTO
     */
    public static class BookSearchResult {
        private Long id;
        private String title;
        private String author;
        private String publisher;
        private String description;
        private String coverImageUrl;

        public BookSearchResult(Long id, String title, String author, String publisher, String description,
                String coverImageUrl) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.publisher = publisher;
            this.description = description;
            this.coverImageUrl = coverImageUrl;
        }

        // Getters
        public Long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public String getPublisher() {
            return publisher;
        }

        public String getDescription() {
            return description;
        }

        public String getCoverImageUrl() {
            return coverImageUrl;
        }
    }
}