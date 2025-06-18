package com.moyun.reading.repository;

import com.moyun.reading.entity.discussion.DiscussionReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 讨论回复仓库接口
 * 
 * @author Moyun Team
 */
@Repository
public interface DiscussionReplyRepository extends JpaRepository<DiscussionReply, Long> {

    /**
     * 查找指定讨论的回复
     */
    Page<DiscussionReply> findByDiscussionIdAndDeletedFalse(Long discussionId, Pageable pageable);

    /**
     * 查找指定用户的回复
     */
    Page<DiscussionReply> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    /**
     * 查找某个回复的子回复
     */
    Page<DiscussionReply> findByParentIdAndDeletedFalse(Long parentId, Pageable pageable);

    /**
     * 查找最佳答案
     */
    Page<DiscussionReply> findByDiscussionIdAndDeletedFalseAndIsAnswerTrue(Long discussionId, Pageable pageable);

    /**
     * 统计指定讨论的回复数量
     */
    long countByDiscussionIdAndDeletedFalse(Long discussionId);
}