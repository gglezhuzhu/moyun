package com.moyun.reading.entity.book;

import com.moyun.reading.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 图书分类实体类
 * 
 * @author Moyun Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "book_categories")
public class BookCategory extends BaseEntity {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    @Size(max = 200, message = "分类描述长度不能超过200个字符")
    @Column(name = "description", length = 200)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private BookCategory parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookCategory> children;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Book> books;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * 是否为顶级分类
     */
    public boolean isTopLevel() {
        return parent == null;
    }

    /**
     * 是否有子分类
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * 获取分类路径
     */
    public String getCategoryPath() {
        if (parent == null) {
            return name;
        }
        return parent.getCategoryPath() + " > " + name;
    }

    /**
     * 获取图书数量
     */
    public int getBookCount() {
        return books != null ? books.size() : 0;
    }
}