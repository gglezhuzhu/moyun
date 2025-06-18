package com.moyun.reading.control.service;

import com.moyun.reading.entity.book.Book;
import com.moyun.reading.entity.base.User;
import com.moyun.reading.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 图书服务类 - 控制类
 * 
 * @author Moyun Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final BookRepository bookRepository;

    /**
     * 获取最新图书
     */
    @Transactional(readOnly = true)
    public Page<Book> findLatestBooks(int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return bookRepository.findByDeletedFalse(pageable);
    }

    /**
     * 获取热门图书
     */
    @Transactional(readOnly = true)
    public Page<Book> findPopularBooks(int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "viewCount"));
        return bookRepository.findByDeletedFalse(pageable);
    }

    /**
     * 搜索图书
     */
    @Transactional(readOnly = true)
    public Page<Book> searchBooks(String keyword, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCaseAndDeletedFalse(keyword, pageable);
    }

    /**
     * 按作者搜索
     */
    @Transactional(readOnly = true)
    public Page<Book> searchByAuthor(String author, Pageable pageable) {
        return bookRepository.findByAuthorContainingIgnoreCaseAndDeletedFalse(author, pageable);
    }

    /**
     * 通过ID查找图书
     */
    @Transactional(readOnly = true)
    public Book findById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    /**
     * 保存图书
     */
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    /**
     * 获取所有可用图书
     */
    @Transactional(readOnly = true)
    public List<Book> findAllActiveBooks() {
        Pageable pageable = PageRequest.of(0, 1000, Sort.by(Sort.Direction.ASC, "title"));
        Page<Book> page = bookRepository.findByDeletedFalseAndStatus(Book.BookStatus.ACTIVE, pageable);
        return page.getContent();
    }

    /**
     * 获取所有可用图书（分页）
     */
    @Transactional(readOnly = true)
    public Page<Book> findAllActiveBooks(Pageable pageable) {
        return bookRepository.findByDeletedFalseAndStatus(Book.BookStatus.ACTIVE, pageable);
    }

    /**
     * 按分类查找图书
     */
    @Transactional(readOnly = true)
    public Page<Book> findBooksByCategory(Long categoryId, Pageable pageable) {
        return bookRepository.findByCategoryIdAndDeletedFalse(categoryId, pageable);
    }

    /**
     * 查看图书（增加浏览次数）
     */
    public Book viewBook(Long id) {
        Book book = findById(id);
        if (book != null) {
            book.incrementViewCount();
            bookRepository.save(book);
        }
        return book;
    }

    /**
     * 创建新图书
     */
    public Book createBook(Book book) {
        log.info("创建新图书: {}", book.getTitle());
        return bookRepository.save(book);
    }

    /**
     * 更新图书
     */
    public Book updateBook(Book book) {
        log.info("更新图书: {}", book.getId());
        return bookRepository.save(book);
    }

    /**
     * 删除图书（软删除）
     */
    public void deleteBook(Long id, User currentUser) {
        Book book = findById(id);
        if (book != null && canEditBook(book, currentUser)) {
            book.delete();
            bookRepository.save(book);
            log.info("删除图书: {}", id);
        }
    }

    /**
     * 获取用户添加的图书
     */
    @Transactional(readOnly = true)
    public Page<Book> findUserBooks(Long userId, Pageable pageable) {
        return bookRepository.findByAddedByUserIdAndDeletedFalse(userId, pageable);
    }

    /**
     * 检查是否可以编辑图书
     */
    public boolean canEditBook(Book book, User currentUser) {
        // 用户可以编辑自己添加的图书，管理员可以编辑任何图书
        return book.getAddedByUser().getId().equals(currentUser.getId()) ||
                isAdmin(currentUser);
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin(User user) {
        return "ADMIN".equals(user.getUserRole().name());
    }
}