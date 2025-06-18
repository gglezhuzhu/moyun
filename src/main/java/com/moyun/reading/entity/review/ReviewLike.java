package com.moyun.reading.entity.review;

import com.moyun.reading.entity.base.BaseEntity;
import com.moyun.reading.entity.base.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 书评点赞实体类
 * 
 * @author Moyun Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "review_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "review_id", "user_id" })
})
public class ReviewLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}