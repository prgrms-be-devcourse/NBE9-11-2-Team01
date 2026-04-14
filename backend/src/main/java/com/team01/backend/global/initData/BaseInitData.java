package com.team01.backend.global.initData;

import com.team01.backend.domain.board.service.BoardService;
import com.team01.backend.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {

    @Autowired
    @Lazy
    private BaseInitData self;
    @Autowired
    private BoardService boardService;
    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;

    @Autowired
    private CategoryService categoryService;

    @Bean
    public ApplicationRunner initData() {
        return args -> {
            self.setBoard();
            self.setCategory();
            self.setPost();
            self.setComment();
        };
    }

    // 게시판 생성
    @Transactional
    public void setBoard(){
        if(boardService.count() > 0){
            return;
        }
        boardService.createBoard("name1", "description1");
        boardService.createBoard("name2", "description2");
        boardService.createBoard("name3", "description3");
    }

    // 게시글 생성
    @Transactional
    public void setPost(){
        if(postService.count() > 0){
            return;
        }
        // 1번 게시판에 글 3개
        postService.write("게시글 1", "내용 1");
        postService.write("게시글 2", "내용 2");
        postService.write("게시글 3", "내용 3");
    }

    @Transactional
    public void setComment() {
        if (commentService.count() > 0) return;

        // 일반 댓글 — parentId 자리에 null
        commentService.writeInitComment(1L, "첫 번째 댓글입니다", null);
        commentService.writeInitComment(1L, "두 번째 댓글입니다", null);
        commentService.writeInitComment(1L, "세 번째 댓글입니다", null);

        // 대댓글 — parentId 자리에 id 값
        commentService.writeInitComment(1L, "첫 번째 대댓글입니다", 1L);
        commentService.writeInitComment(1L, "두 번째 대댓글입니다", 2L);

        commentService.writeInitComment(2L, "첫 번째 댓글입니다", null);
        commentService.writeInitComment(2L, "두 번째 댓글입니다", null);

        commentService.writeInitComment(2L, "첫 번째 대댓글입니다", 6L);
    }
    @Transactional
    public void setCategory(){
        if(categoryService.count() > 0){
            return;
        }
        // 1번 게시판에 글 3개
        categoryService.create(1L, "카테고리 1");
        categoryService.create(1L, "카테고리 2");
        categoryService.create(1L, "카테고리 3");

        categoryService.create(2L, "카테고리 1");
        categoryService.create(2L, "카테고리 2");
    }
}
