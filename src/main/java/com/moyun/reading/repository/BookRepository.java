package com.moyun.reading.repository;

import com.moyun.reading.entity.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 图书仓库接口
 * 
 * @author Moyun Team
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * 查找未删除的图书
     */
    Page<Book> findByDeletedFalse(Pageable pageable);

    /**
     * 按标题搜索图书
     */
    Page<Book> findByTitleContainingIgnoreCaseAndDeletedFalse(String title, Pageable pageable);

    /**
     * 按作者搜索图书
     */
    Page<Book> findByAuthorContainingIgnoreCaseAndDeletedFalse(String author, Pageable pageable);

    /**
     * 按分类查找图书
     */
    Page<Book> findByCategoryIdAndDeletedFalse(Long categoryId, Pageable pageable);

    /**
     * 查找指定状态的图书
     */
    Page<Book> findByDeletedFalseAndStatus(Book.BookStatus status, Pageable pageable);

    /**
     * 查找用户添加的图书
     */
    Page<Book> findByAddedByUserIdAndDeletedFalse(Long userId, Pageable pageable);
}