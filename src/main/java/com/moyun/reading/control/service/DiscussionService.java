package com.moyun.reading.control.service;

import com.moyun.reading.entity.discussion.Discussion;
import com.moyun.reading.entity.discussion.DiscussionReply;
import com.moyun.reading.entity.base.User;
import com.moyun.reading.entity.user.Teacher;
import com.moyun.reading.repository.DiscussionRepository;
import com.moyun.reading.repository.DiscussionReplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 讨论服务类 - 控制类
 * 
 * @author Moyun Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DiscussionService {

    private final DiscussionRepository discussionRepository;
    private final DiscussionReplyRepository discussionReplyRepository;

    /**
     * 获取最新讨论
     */
    @Transactional(readOnly = true)
    public Page<Discussion> findLatestDiscussions(int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return discussionRepository.findByDeletedFalseAndStatus(Discussion.DiscussionStatus.OPEN, pageable);
    }

    /**
     * 获取所有讨论（分页）
     */
    @Transactional(readOnly = true)
    public Page<Discussion> findAllDiscussions(Pageable pageable) {
        return discussionRepository.findByDeletedFalseAndStatus(Discussion.DiscussionStatus.OPEN, pageable);
    }

    /**
     * 获取指定类型的讨论
     */
    @Transactional(readOnly = true)
    public Page<Discussion> findDiscussionsByType(Discussion.DiscussionType type, Pageable pageable) {
        return discussionRepository.findByDeletedFalseAndTypeAndStatus(
                type, Discussion.DiscussionStatus.OPEN, pageable);
    }

    /**
     * 搜索讨论
     */
    @Transactional(readOnly = true)
    public Page<Discussion> searchDiscussions(String keyword, Pageable pageable) {
        return discussionRepository.searchDiscussions(keyword, pageable);
    }

    /**
     * 获取热门讨论
     */
    @Transactional(readOnly = true)
    public Page<Discussion> findHotDiscussions(Pageable pageable) {
        return discussionRepository.findHotDiscussions(Discussion.DiscussionStatus.OPEN, pageable);
    }

    /**
     * 通过ID查找讨论
     */
    @Transactional(readOnly = true)
    public Discussion findById(Long id) {
        return discussionRepository.findById(id).orElse(null);
    }

    /**
     * 创建新讨论
     */
    public Discussion createDiscussion(Discussion discussion) {
        log.info("创建新讨论: {}", discussion.getTitle());
        return discussionRepository.save(discussion);
    }

    /**
     * 更新讨论
     */
    public Discussion updateDiscussion(Discussion discussion) {
        log.info("更新讨论: {}", discussion.getId());
        return discussionRepository.save(discussion);
    }

    /**
     * 删除讨论（软删除）
     */
    public void deleteDiscussion(Long id, User currentUser) {
        Discussion discussion = findById(id);
        if (discussion != null && canDeleteDiscussion(discussion, currentUser)) {
            discussion.delete();
            discussionRepository.save(discussion);
            log.info("删除讨论: {}", id);
        }
    }

    /**
     * 查看讨论（增加浏览次数）
     */
    public Discussion viewDiscussion(Long id) {
        Discussion discussion = findById(id);
        if (discussion != null) {
            discussion.incrementViewCount();
            discussionRepository.save(discussion);
        }
        return discussion;
    }

    /**
     * 获取用户的讨论
     */
    @Transactional(readOnly = true)
    public Page<Discussion> findUserDiscussions(Long userId, Pageable pageable) {
        return discussionRepository.findByUserIdAndDeletedFalse(userId, pageable);
    }

    /**
     * 获取讨论的回复
     */
    @Transactional(readOnly = true)
    public Page<DiscussionReply> findDiscussionReplies(Long discussionId, Pageable pageable) {
        return discussionReplyRepository.findByDiscussionIdAndDeletedFalse(discussionId, pageable);
    }

    /**
     * 获取讨论的回复数量
     */
    @Transactional(readOnly = true)
    public long getDiscussionReplyCount(Long discussionId) {
        return discussionReplyRepository.countByDiscussionIdAndDeletedFalse(discussionId);
    }

    /**
     * 创建回复
     */
    public DiscussionReply createReply(DiscussionReply reply) {
        log.info("创建讨论回复: 讨论ID={}", reply.getDiscussion().getId());
        return discussionReplyRepository.save(reply);
    }

    /**
     * 删除回复
     */
    public void deleteReply(Long replyId, User currentUser) {
        DiscussionReply reply = discussionReplyRepository.findById(replyId).orElse(null);
        if (reply != null && canDeleteReply(reply, currentUser)) {
            reply.delete();
            discussionReplyRepository.save(reply);
            log.info("删除讨论回复: {}", replyId);
        }
    }

    /**
     * 标记回复为最佳答案（导师功能）
     */
    public void markReplyAsAnswer(Long replyId, User currentUser) {
        DiscussionReply reply = discussionReplyRepository.findById(replyId).orElse(null);
        if (reply == null) {
            throw new IllegalArgumentException("回复不存在");
        }

        Discussion discussion = reply.getDiscussion();

        // 检查权限：只有导师和管理员可以标记最佳答案
        if (!isTeacherOrAdmin(currentUser)) {
            throw new IllegalArgumentException("只有导师和管理员可以标记最佳答案");
        }

        // 取消其他回复的最佳答案标记
        Page<DiscussionReply> existingAnswers = discussionReplyRepository
                .findByDiscussionIdAndDeletedFalseAndIsAnswerTrue(discussion.getId(),
                        PageRequest.of(0, 100));

        for (DiscussionReply existingAnswer : existingAnswers) {
            existingAnswer.unmarkAsAnswer();
            discussionReplyRepository.save(existingAnswer);
        }

        // 标记新的最佳答案
        reply.markAsAnswer();
        discussionReplyRepository.save(reply);

        // 标记讨论为已解决
        discussion.markAsAnswered();
        discussionRepository.save(discussion);

        log.info("导师 {} 标记回复 {} 为最佳答案", currentUser.getDisplayName(), replyId);
    }

    /**
     * 取消最佳答案标记（导师功能）
     */
    public void unmarkReplyAsAnswer(Long replyId, User currentUser) {
        DiscussionReply reply = discussionReplyRepository.findById(replyId).orElse(null);
        if (reply == null) {
            throw new IllegalArgumentException("回复不存在");
        }

        // 检查权限
        if (!isTeacherOrAdmin(currentUser)) {
            throw new IllegalArgumentException("只有导师和管理员可以取消最佳答案标记");
        }

        reply.unmarkAsAnswer();
        discussionReplyRepository.save(reply);

        // 检查是否还有其他最佳答案，如果没有则标记讨论为未解决
        Page<DiscussionReply> remainingAnswers = discussionReplyRepository
                .findByDiscussionIdAndDeletedFalseAndIsAnswerTrue(reply.getDiscussion().getId(),
                        PageRequest.of(0, 1));

        if (remainingAnswers.isEmpty()) {
            Discussion discussion = reply.getDiscussion();
            discussion.setIsAnswered(false);
            discussionRepository.save(discussion);
        }

        log.info("导师 {} 取消回复 {} 的最佳答案标记", currentUser.getDisplayName(), replyId);
    }

    /**
     * 获取需要导师回答的问题（未解决的问题类型讨论）
     */
    @Transactional(readOnly = true)
    public Page<Discussion> findQuestionsNeedingAnswer(Pageable pageable) {
        return discussionRepository.findByDeletedFalseAndTypeAndIsAnsweredAndStatus(
                Discussion.DiscussionType.QUESTION,
                false,
                Discussion.DiscussionStatus.OPEN,
                pageable);
    }

    /**
     * 获取导师已回答的问题
     */
    @Transactional(readOnly = true)
    public Page<Discussion> findAnsweredQuestions(Pageable pageable) {
        return discussionRepository.findByDeletedFalseAndTypeAndIsAnsweredAndStatus(
                Discussion.DiscussionType.QUESTION,
                true,
                Discussion.DiscussionStatus.OPEN,
                pageable);
    }

    /**
     * 获取导师学生的问题咨询（所有状态）
     */
    @Transactional(readOnly = true)
    public Page<Discussion> findStudentQuestionsForTeacher(Teacher teacher, Pageable pageable) {
        return discussionRepository.findStudentQuestionsForTeacher(
                teacher.getId(),
                Discussion.DiscussionType.QUESTION,
                Discussion.DiscussionStatus.OPEN,
                pageable);
    }

    /**
     * 获取导师学生的未解决问题咨询
     */
    @Transactional(readOnly = true)
    public Page<Discussion> findUnansweredStudentQuestionsForTeacher(Teacher teacher, Pageable pageable) {
        return discussionRepository.findUnansweredStudentQuestionsForTeacher(
                teacher.getId(),
                Discussion.DiscussionType.QUESTION,
                false,
                Discussion.DiscussionStatus.OPEN,
                pageable);
    }

    /**
     * 获取导师学生的已解决问题咨询
     */
    @Transactional(readOnly = true)
    public Page<Discussion> findAnsweredStudentQuestionsForTeacher(Teacher teacher, Pageable pageable) {
        return discussionRepository.findAnsweredStudentQuestionsForTeacher(
                teacher.getId(),
                Discussion.DiscussionType.QUESTION,
                true,
                Discussion.DiscussionStatus.OPEN,
                pageable);
    }

    /**
     * 统计导师学生的未解决问题数量
     */
    @Transactional(readOnly = true)
    public long countUnansweredStudentQuestionsForTeacher(Teacher teacher) {
        return discussionRepository.countUnansweredStudentQuestionsForTeacher(
                teacher.getId(),
                Discussion.DiscussionType.QUESTION,
                false,
                Discussion.DiscussionStatus.OPEN);
    }

    /**
     * 统计导师学生的已解决问题数量
     */
    @Transactional(readOnly = true)
    public long countAnsweredStudentQuestionsForTeacher(Teacher teacher) {
        return discussionRepository.countAnsweredStudentQuestionsForTeacher(
                teacher.getId(),
                Discussion.DiscussionType.QUESTION,
                true,
                Discussion.DiscussionStatus.OPEN);
    }

    /**
     * 导师标记讨论为已回答（不需要最佳答案）
     */
    public void markDiscussionAsAnswered(Long discussionId, User currentUser) {
        Discussion discussion = discussionRepository.findById(discussionId).orElse(null);
        if (discussion == null) {
            throw new IllegalArgumentException("讨论不存在");
        }

        // 检查权限：只有导师和管理员可以标记为已回答
        if (!isTeacherOrAdmin(currentUser)) {
            throw new IllegalArgumentException("只有导师和管理员可以标记为已回答");
        }

        // 标记讨论为已回答
        discussion.markAsAnswered();
        discussionRepository.save(discussion);

        log.info("导师 {} 标记讨论 {} 为已回答", currentUser.getDisplayName(), discussionId);
    }

    /**
     * 导师取消讨论的已回答状态
     */
    public void unmarkDiscussionAsAnswered(Long discussionId, User currentUser) {
        Discussion discussion = discussionRepository.findById(discussionId).orElse(null);
        if (discussion == null) {
            throw new IllegalArgumentException("讨论不存在");
        }

        // 检查权限
        if (!isTeacherOrAdmin(currentUser)) {
            throw new IllegalArgumentException("只有导师和管理员可以取消已回答状态");
        }

        // 取消已回答状态
        discussion.setIsAnswered(false);
        discussionRepository.save(discussion);

        log.info("导师 {} 取消讨论 {} 的已回答状态", currentUser.getDisplayName(), discussionId);
    }

    /**
     * 检查是否可以删除讨论
     */
    private boolean canDeleteDiscussion(Discussion discussion, User currentUser) {
        // 用户可以删除自己的讨论，管理员可以删除任何讨论
        return discussion.getUser().getId().equals(currentUser.getId()) ||
                isAdmin(currentUser);
    }

    /**
     * 检查是否可以删除回复
     */
    private boolean canDeleteReply(DiscussionReply reply, User currentUser) {
        // 用户可以删除自己的回复，管理员可以删除任何回复
        return reply.getUser().getId().equals(currentUser.getId()) ||
                isAdmin(currentUser);
    }

    /**
     * 检查是否为管理员
     */
    private boolean isAdmin(User user) {
        return "ADMIN".equals(user.getUserRole().name());
    }

    /**
     * 检查是否为导师或管理员
     */
    private boolean isTeacherOrAdmin(User user) {
        String role = user.getUserRole().name();
        return "TEACHER".equals(role) || "ADMIN".equals(role);
    }
}