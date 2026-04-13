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

    @Bean
    public ApplicationRunner initData() {
        return args -> {
            self.setBoard();
            self.setPost();
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
}
