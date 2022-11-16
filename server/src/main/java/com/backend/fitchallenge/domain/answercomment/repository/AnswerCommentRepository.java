package com.backend.fitchallenge.domain.answercomment.repository;

import com.backend.fitchallenge.domain.answercomment.entity.AnswerComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AnswerCommentRepository extends JpaRepository<AnswerComment, Long> {

    @Query("select ac.member.id from AnswerComment ac where ac.id = :answerCommentId")
    Long findMemberIdByAnswerCommentId(Long answerCommentId);
}
