package com.team01.backend.domain.post.entity;

import com.team01.backend.domain.board.entity.Board;
import com.team01.backend.domain.category.entity.Category;
import com.team01.backend.domain.comment.entity.Comment;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(indexes = {
        // 게시판별 게시글 목록 조회 최적화 (board_id + isDeleted 필터링, createdAt 정렬)
        @Index(name = "idx_post_board_deleted_created", columnList = "board_id, isDeleted, createdAt")
})
public class Post extends BaseEntity {

    private String title;
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    // DB에는 authorId 컬럼이 자동으로 생성됨
    @ManyToOne(fetch = FetchType.LAZY)
    private User author;

    private int likeCount;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    private boolean isDeleted = false;

    @OneToMany(mappedBy = "post",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
            fetch = FetchType.LAZY,
            orphanRemoval = false)  // (소프트 딜리트) : service 에서 delete 로직 짤 때 상태값만 바꾸고, 부모 리스트와의 관계를 끊는 코드를 직접 작성
    private List<Comment> comments = new ArrayList<>();


    public Post(User author, String title, String content, Board board, Category category) {
        this.title = title;
        this.content = content;
        this.board = board;
        this.author = author;
        this.category = category;
    }

    public void update(String title, String content, Category category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public void delete() {

        this.isDeleted = true;

    }

    // top5 조회를 위한 임시 메서드
    public void initLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
