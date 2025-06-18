package com.moyun.reading.control.service;

import com.moyun.reading.entity.review.Review;
import com.moyun.reading.entity.review.ReviewComment;
import com.moyun.reading.entity.review.ReviewLike;
import com.moyun.reading.entity.base.User;
import com.moyun.reading.repository.ReviewRepository;
import com.moyun.reading.repository.ReviewCommentRepository;
import com.moyun.reading.repository.ReviewLikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 书评服务类 - 控制类
 * 
 * @author Moyun Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    /**
     * 获取最新书评
     */
    @Transactional(readOnly = true)
    public Page<Review> findLatestReviews(int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findByDeletedFalseAndStatus(Review.ReviewStatus.PUBLISHED, pageable);
    }

    /**
     * 获取所有书评（分页）
     */
    @Transactional(readOnly = true)
    public Page<Review> findAllReviews(Pageable pageable) {
        return reviewRepository.findByDeletedFalseAndStatus(Review.ReviewStatus.PUBLISHED, pageable);
    }

    /**
     * 通过ID查找书评
     */
    @Transactional(readOnly = true)
    public Review findById(Long id) {
        return reviewRepository.findById(id).orElse(null);
    }

    /**
     * 保存书评
     */
    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    /**
     * 搜索书评
     */
    @Transactional(readOnly = true)
    public Page<Review> searchReviews(String keyword, Pageable pageable) {
        // 这里需要在ReviewRepository中添加搜索方法
        return reviewRepository.findByDeletedFalseAndStatus(Review.ReviewStatus.PUBLISHED, pageable);
    }

    /**
     * 获取某本书的书评
     */
    @Transactional(readOnly = true)
    public Page<Review> findBookReviews(Long bookId, Pageable pageable) {
        return reviewRepository.findByBookIdAndDeletedFalseAndStatus(bookId, Review.ReviewStatus.PUBLISHED, pageable);
    }

    /**
     * 查看书评（增加浏览次数）
     */
    public Review viewReview(Long id) {
        Review review = findById(id);
        if (review != null) {
            review.incrementViewCount();
            reviewRepository.save(review);
        }
        return review;
    }

    /**
     * 获取书评的评论
     */
    @Transactional(readOnly = true)
    public Page<ReviewComment> findReviewComments(Long reviewId, Pageable pageable) {
        Page<ReviewComment> comments = reviewCommentRepository.findByReviewIdAndDeletedFalse(reviewId, pageable);
        log.info("查询书评 {} 的评论，总数: {}, 当前页: {}, 每页大小: {}, 当前页评论数: {}",
                reviewId, comments.getTotalElements(), comments.getNumber(), comments.getSize(),
                comments.getNumberOfElements());
        return comments;
    }

    /**
     * 获取书评的评论数量
     */
    @Transactional(readOnly = true)
    public long getReviewCommentCount(Long reviewId) {
        long count = reviewCommentRepository.countByReviewIdAndDeletedFalse(reviewId);
        log.info("书评 {} 的评论总数: {}", reviewId, count);
        return count;
    }

    /**
     * 检查用户是否已点赞书评
     */
    @Transactional(readOnly = true)
    public boolean isUserLikedReview(Long reviewId, Long userId) {
        return reviewLikeRepository.existsByReviewIdAndUserIdAndDeletedFalse(reviewId, userId);
    }

    /**
     * 创建新书评
     */
    public Review createReview(Review review) {
        log.info("创建新书评: {}", review.getTitle());
        return reviewRepository.save(review);
    }

    /**
     * 更新书评
     */
    public Review updateReview(Review review) {
        log.info("更新书评: {}", review.getId());
        return reviewRepository.save(review);
    }

    /**
     * 删除书评（软删除）
     */
    public void deleteReview(Long id, User currentUser) {
        Review review = findById(id);
        if (review != null && canDeleteReview(review, currentUser)) {
            review.delete();
            reviewRepository.save(review);
            log.info("删除书评: {}", id);
        }
    }

    /**
     * 切换点赞状态
     */
    public boolean toggleLike(Long reviewId, Long userId) {
        log.info("开始处理点赞切换: reviewId={}, userId={}", reviewId, userId);

        Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewIdAndUserIdAndDeletedFalse(reviewId,
                userId);

        if (existingLike.isPresent()) {
            // 已点赞，取消点赞
            ReviewLike like = existingLike.get();
            like.delete();
            reviewLikeRepository.save(like);
            log.info("取消点赞书评: reviewId={}, userId={}", reviewId, userId);
            return false;
        } else {
            // 未点赞，添加点赞
            Review review = findById(reviewId);
            if (review == null) {
                log.error("书评不存在: reviewId={}", reviewId);
                throw new IllegalArgumentException("书评不存在");
            }

            User user = new User();
            user.setId(userId);

            ReviewLike like = new ReviewLike();
            like.setReview(review);
            like.setUser(user);
            reviewLikeRepository.save(like);
            log.info("点赞书评: reviewId={}, userId={}", reviewId, userId);
            return true;
        }
    }

    /**
     * 创建评论
     */
    @Transactional
    public ReviewComment createComment(ReviewComment comment) {
        log.info("开始创建书评评论: 书评ID={}, 用户ID={}, 内容={}",
                comment.getReview().getId(), comment.getUser().getId(), comment.getContent());

        // 确保评论对象没有ID（避免更新现有记录）
        comment.setId(null);

        ReviewComment savedComment = reviewCommentRepository.save(comment);
        log.info("评论创建成功: 评论ID={}, 内容={}", savedComment.getId(), savedComment.getContent());

        // 验证评论是否真的被保存
        long totalComments = reviewCommentRepository.countByReviewIdAndDeletedFalse(comment.getReview().getId());
        log.info("书评 {} 现在总共有 {} 条评论", comment.getReview().getId(), totalComments);

        return savedComment;
    }

    /**
     * 删除评论
     */
    public Long deleteComment(Long commentId, User currentUser) {
        ReviewComment comment = reviewCommentRepository.findById(commentId).orElse(null);
        if (comment != null && canDeleteComment(comment, currentUser)) {
            Long reviewId = comment.getReview().getId();
            comment.delete();
            reviewCommentRepository.save(comment);
            log.info("删除书评评论: {}", commentId);
            return reviewId;
        }
        return null;
    }

    /**
     * 获取用户的书评
     */
    @Transactional(readOnly = true)
    public Page<Review> findUserReviews(Long userId, Pageable pageable) {
        return reviewRepository.findByUserIdAndDeletedFalse(userId, pageable);
    }

    /**
     * 检查是否可以删除书评
     */
    private boolean canDeleteReview(Review review, User currentUser) {
        // 用户可以删除自己的书评，管理员可以删除任何书评
        return review.getUser().getId().equals(currentUser.getId()) ||
                isAdmin(currentUser);
    }

    /**
     * 检查是否可以删除评论
     */
    private boolean canDeleteComment(ReviewComment comment, User currentUser) {
        // 用户可以删除自己的评论，管理员可以删除任何评论
        return comment.getUser().getId().equals(currentUser.getId()) ||
                isAdmin(currentUser);
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin(User user) {
        return "ADMIN".equals(user.getUserRole().name());
    }
}