package com.team01.backend.global.initData;

import com.team01.backend.domain.board.entity.Board;
import com.team01.backend.domain.board.repository.BoardRepository;
import com.team01.backend.domain.board.service.BoardService;
import com.team01.backend.domain.category.entity.Category;
import com.team01.backend.domain.category.repository.CategoryRepository;
import com.team01.backend.domain.category.service.CategoryService;
import com.team01.backend.domain.comment.service.CommentService;
import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.post.entity.PostLike;
import com.team01.backend.domain.post.repository.PostLikeRepository;
import com.team01.backend.domain.post.repository.PostRepository;
import com.team01.backend.domain.post.service.PostService;
import com.team01.backend.domain.user.dto.SignUpRequest;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.domain.user.repository.UserRepository;
import com.team01.backend.domain.user.service.AuthService;
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
    @Autowired
    private AuthService authService;
    @Autowired
    private PostLikeRepository postLikeRepository;

    @Bean
    public ApplicationRunner initData() {
        return args -> {
            self.setMember();
            self.setBoard();
            self.setCategory();
            self.setPost();
            self.setComment();
            self.setPostLike();
        };
    }

    // 유저 데이터 생성
    @Transactional
    public void setMember() {

        if (userRepository.count() > 0) return;
        authService.signUp(SignUpRequest.builder().email("ujin3261@gmail.com").password("password1234").nickname("유저1asfd").build());
        authService.signUp(SignUpRequest.builder().email("user1@test.com").password("password1234").nickname("유저1").build());
        authService.signUp(SignUpRequest.builder().email("user2@test.com").password("password1234").nickname("유저2").build());
        authService.signUp(SignUpRequest.builder().email("admin@admin.com").password("passworda12345").nickname("admin").admin(true).adminToken("user_admin-2026").build());

        // 테스트용 유저 10명
        for (int i = 1; i <= 30; i++) {
            authService.signUp(SignUpRequest.builder()
                    .email("test" + i + "@test.com")
                    .password("password1234")
                    .nickname("테스터" + i)
                    .build());
        }
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

        User author1 = userRepository.findByEmail("user1@test.com").orElseThrow();
        User author2 = userRepository.findByEmail("user2@test.com").orElseThrow();

        Board board = boardRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        Category category = categoryRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Post post1 = new Post(author1, "첫 번째 게시글입니다.", "내용 1", board, category);
        postRepository.save(post1);

        Post post2 = new Post(author2, "두 번째 게시글입니다.", "내용 2", board, category);
        postRepository.save(post2);


        Post post3 = new Post(author2, "세 번째 게시글입니다.", "내용 3", board, category);
        postRepository.save(post3);
    }

    @Transactional
    public void setComment() {
        if (commentService.count() > 0) return;

        User author1 = userRepository.findByEmail("user1@test.com").orElseThrow();
        User author2 = userRepository.findByEmail("user2@test.com").orElseThrow();

        // 일반 댓글 — parentId 자리에 null
        commentService.writeInitComment(1L, author1,"첫 번째 댓글입니다", null);
        commentService.writeInitComment(1L, author2,"두 번째 댓글입니다", null);
        commentService.writeInitComment(1L, author1,"세 번째 댓글입니다", null);

        // 대댓글 — parentId 자리에 id 값
        commentService.writeInitComment(1L, author2,"첫 번째 대댓글입니다", 1L);
        commentService.writeInitComment(1L, author1,"두 번째 대댓글입니다", 2L);

        commentService.writeInitComment(2L, author1,"첫 번째 댓글입니다", null);
        commentService.writeInitComment(2L, author2,"두 번째 댓글입니다", null);

        commentService.writeInitComment(2L, author2,"첫 번째 대댓글입니다", 6L);
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

    @Transactional
    public void setPostLike() {
        if (postLikeRepository.count() > 0) return;

        Post post1 = postRepository.findById(1L).orElseThrow();
        Post post2 = postRepository.findById(2L).orElseThrow();
        Post post3 = postRepository.findById(3L).orElseThrow();

        // post1 좋아요 12개
        for (long i = 3; i <= 14; i++) {
            User user = userRepository.findById(i).orElseThrow();
            postLikeRepository.save(new PostLike(user, post1));
            postRepository.increaseLikeCount(post1.getId());
        }

        // post2 좋아요 30개
        for (long i = 3; i <= 32; i++) {
            User user = userRepository.findById(i).orElseThrow();
            postLikeRepository.save(new PostLike(user, post2));
            postRepository.increaseLikeCount(post2.getId());
        }

        // post3 좋아요 20개
        for (long i = 3; i <= 22; i++) {
            User user = userRepository.findById(i).orElseThrow();
            postLikeRepository.save(new PostLike(user, post3));
            postRepository.increaseLikeCount(post3.getId());
        }
    }
}
