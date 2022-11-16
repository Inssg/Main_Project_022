package com.backend.fitchallenge.domain.post.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.backend.fitchallenge.domain.member.entity.QMember.member;
import static com.backend.fitchallenge.domain.post.entity.QPost.post;
import static org.springframework.util.ObjectUtils.isEmpty;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements  PostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * post + 댓글수 조회
     * member(작성자) fetchJoin
     * @return 마지막 postId보다 작은 Id의 게시물들을 최신순으로 반환
     */
    public List<Tuple> findList(Long lastPostId, Pageable pageable) {

       return  jpaQueryFactory
                .select(post, post.postComments.size())
                .from(post)
                .leftJoin(post.member, member).fetchJoin()
                .where(ltPostId(lastPostId)) //no -offset 페이징 처리
                .limit(pageable.getPageSize()+1) //마지막 페이지인지 여부 확인하기위해 +1 해서 조회
                .orderBy(post.id.desc()) // 최신순
                .fetch();

    }

    // no -offset 방식 처리 메서드
    private BooleanExpression ltPostId(Long postId) {
        return isEmpty(postId) ? null : post.id.lt(postId);
    }




}
