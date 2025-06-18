package com.moyun.reading.entity.book;

import com.moyun.reading.entity.base.BaseEntity;
import com.moyun.reading.entity.base.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 图书实体类
 * 
 * @author Moyun Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "books")
public class Book extends BaseEntity {

    @NotBlank(message = "图书标题不能为空")
    @Size(max = 200, message = "图书标题长度不能超过200个字符")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Size(max = 100, message = "作者姓名长度不能超过100个字符")
    @Column(name = "author", length = 100)
    private String author;

    @Size(max = 50, message = "ISBN长度不能超过50个字符")
    @Column(name = "isbn", length = 50, unique = true)
    private String isbn;

    @Size(max = 100, message = "出版社名称长度不能超过100个字符")
    @Column(name = "publisher", length = 100)
    private String publisher;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Size(max = 2000, message = "图书描述长度不能超过2000个字符")
    @Column(name = "description", length = 2000)
    private String description;

    @Size(max = 500, message = "封面图片URL长度不能超过500个字符")
    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private BookCategory category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "added_by_user_id", nullable = false)
    private User addedByUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookStatus status = BookStatus.ACTIVE;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "rating_sum", nullable = false)
    private Long ratingSum = 0L;

    @Column(name = "rating_count", nullable = false)
    private Long ratingCount = 0L;

    /**
     * 图书状态枚举
     */
    public enum BookStatus {
        ACTIVE("正常"),
        INACTIVE("禁用"),
        PENDING("待审核");

        private final String displayName;

        BookStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 增加浏览次数
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 添加评分
     */
    public void addRating(int rating) {
        this.ratingSum += rating;
        this.ratingCount++;
    }

    /**
     * 获取平均评分
     */
    public double getAverageRating() {
        if (ratingCount == 0) {
            return 0.0;
        }
        return (double) ratingSum / ratingCount;
    }

    /**
     * 获取显示用的评分（保留一位小数）
     */
    public String getDisplayRating() {
        return String.format("%.1f", getAverageRating());
    }

    /**
     * 是否可用
     */
    public boolean isAvailable() {
        return BookStatus.ACTIVE.equals(status);
    }

    /**
     * 获取出版年份（用于表单）
     */
    public Integer getPublishYear() {
        return publishDate != null ? publishDate.getYear() : null;
    }

    /**
     * 设置出版年份（用于表单）
     */
    public void setPublishYear(Integer year) {
        if (year != null) {
            this.publishDate = LocalDate.of(year, 1, 1);
        } else {
            this.publishDate = null;
        }
    }

    /**
     * 重写toString方法，避免访问可能导致懒加载问题的关联对象
     */
    @Override
    public String toString() {
        return "Book{" +
                "id=" + getId() +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", publisher='" + publisher + '\'' +
                ", publishDate=" + publishDate +
                ", pageCount=" + pageCount +
                ", price=" + price +
                ", status=" + status +
                ", viewCount=" + viewCount +
                ", ratingCount=" + ratingCount +
                '}';
    }
}