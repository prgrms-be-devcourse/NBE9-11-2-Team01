package com.team01.backend.global.initData;

import com.team01.backend.domain.board.entity.Board;
import com.team01.backend.domain.board.repository.BoardRepository;
import com.team01.backend.domain.board.service.BoardService;
import com.team01.backend.domain.category.entity.Category;
import com.team01.backend.domain.category.repository.CategoryRepository;
import com.team01.backend.domain.category.service.CategoryService;
import com.team01.backend.domain.comment.service.CommentService;
import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.post.repository.PostRepository;
import com.team01.backend.domain.post.service.PostService;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.domain.user.repository.UserRepository;
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
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BoardRepository boardRepository;

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

        //4번 게시판 삭제
        boardService.createBoard("name4", "description4");
        boardService.deleteBoard(4L);
    }

    // 게시글 생성
    @Transactional
    public void setPost(){
        if(postRepository.count() > 0) return;

        Board board = boardRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        Category category = categoryRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Post post1 = new Post("게시글 1", "내용 1", board, category);
        postRepository.save(post1);

        Post post2 = new Post("게시글 2", "내용 2", board, category);
        postRepository.save(post2);

        Post post3 = new Post("게시글 3", "내용 3", board, category);
        postRepository.save(post3);
    }

    @Transactional
    public void setComment() {
        if (commentService.count() > 0) return;

        // 임시 유저 생성
        User tempUser = userRepository.save(User.builder()
                .email("init@init.com")
                .nickname("유저")
                .password("1234")
                .build());

        // 일반 댓글 — parentId 자리에 null
        commentService.writeInitComment(1L, tempUser,"첫 번째 댓글입니다", null);
        commentService.writeInitComment(1L, tempUser,"두 번째 댓글입니다", null);
        commentService.writeInitComment(1L, tempUser,"세 번째 댓글입니다", null);

        // 대댓글 — parentId 자리에 id 값
        commentService.writeInitComment(1L, tempUser,"첫 번째 대댓글입니다", 1L);
        commentService.writeInitComment(1L, tempUser,"두 번째 대댓글입니다", 2L);

        commentService.writeInitComment(2L, tempUser,"첫 번째 댓글입니다", null);
        commentService.writeInitComment(2L, tempUser,"두 번째 댓글입니다", null);

        commentService.writeInitComment(2L, tempUser,"첫 번째 대댓글입니다", 6L);
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
