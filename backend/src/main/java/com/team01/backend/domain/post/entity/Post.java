package com.team01.backend.domain.post.entity;

import com.team01.backend.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Post extends BaseEntity {

    private String title;
    private String content;

    private Long boardId;

    // DB에는 authorId 컬럼이 자동으로 생성됨
//    @ManyToOne(fetch = FetchType.LAZY)
//    private User author;

    private int likeCount;

    // 그러면 boardId도 사용 가능?
//    @ManyToOne(fetch = FetchType.LAZY)
//    private Category category;

    private boolean isDeleted;

//    @OneToMany(mappedBy = "post",
//            cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
//            fetch = FetchType.LAZY,
//            orphanRemoval = false)  // (소프트 딜리트) : service 에서 delete 로직 짤 때 상태값만 바꾸고, 부모 리스트와의 관계를 끊는 코드를 직접 작성
//    private List<Comment> comments = new ArrayList<>();

//    public Post(User author, String title, String content) {
//        this.author = author;
//        this.title = title;
//        this.content = content;
//    }

    //Comment테스트
    public void delete() {
        this.isDeleted = true;
    }

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // 유저 정보 생기면 사용
//    public void checkModify(User actor) {
//
//        if (!this.getAuthor().equals(actor)) {
//            throw new IllegalArgumentException("수정 권한이 없습니다.");
//        }
//    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
