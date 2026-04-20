package com.team01.backend.domain.post.controller;

import com.team01.backend.domain.post.dto.PostDetailResponseDto;
import com.team01.backend.domain.post.dto.PostDto;
import com.team01.backend.domain.post.dto.PostPageResponseDto;
import com.team01.backend.domain.post.dto.PostSummaryDto;
import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.post.service.PostService;
import com.team01.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    // 게시판별 글 목록 조회
    @GetMapping("/boards/{boardId}/posts")
    public ResponseEntity<ApiResponse<PostPageResponseDto>> getPostsByBoardId(
            @PathVariable Long boardId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        if (page < 1) throw new IllegalArgumentException("페이지 번호는 1 이상이어야 합니다.");
        if (keyword != null && keyword.length() > 50) throw new IllegalArgumentException("검색어는 50자 이하이어야 합니다.");

        PostPageResponseDto posts = postService.getPostsByBoardId(boardId, page, keyword, categoryId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(posts));
    }

    // 게시판별, 카테고리별 글 목록 조회
    @GetMapping("/boards/{boardId}/categories/{categoryId}/posts")
    public ResponseEntity<ApiResponse<PostPageResponseDto>> getPostsByCategory(
            @PathVariable Long boardId,
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword
    ) {
        // 페이지 번호 유효성 검증 (1 이상)
        if (page < 1) throw new IllegalArgumentException("페이지 번호는 1 이상이어야 합니다.");
        // 검색어 길이 검증 (50자 이하)
        if (keyword != null && keyword.length() > 50) throw new IllegalArgumentException("검색어는 50자 이하이어야 합니다.");

        PostPageResponseDto posts = postService.getPostsByBoardAndCategory(boardId, categoryId, page, keyword);
        return ResponseEntity.ok(ApiResponse.ofSuccess(posts));
    }


    // 게시글 상세 조회
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponseDto>> getPostById(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        PostDetailResponseDto post = postService.getPostById(postId, email);
        return ResponseEntity.ok(ApiResponse.ofSuccess(post));
    }


    record PostWriteReqBody(
            @Size(min = 2, message = "제목은 2자 이상이어야 합니다.")
            @NotBlank(message = "제목은 공백일 수 없습니다.")
            String title,

            @Size(min = 2, message = "내용은 2자 이상이어야 합니다.")
            @NotBlank(message = "내용은 공백일 수 없습니다.")
            String content,

            @NotNull(message = "게시판 선택은 필수입니다.")
            Long boardId,

            @NotNull(message = "카테고리 선택은 필수입니다.")
            Long categoryId
    ){
    }

    record PostWriteResBody(
            PostDto postDto,
            long postsCount
    ) {
    }

    // 글 작성 api
    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<PostWriteResBody>> write(
            @RequestBody @Valid PostWriteReqBody reqBody
    ) {
        // User actor = rq.getActor();

        //Post post = postService.write(actor, reqBody.title, reqBody.content);
        Post post = postService.write(
                reqBody.title,
                reqBody.content,
                reqBody.boardId,
                reqBody.categoryId
        );

        long postsCount = postService.count();

        return ResponseEntity.ok(
                ApiResponse.ofSuccess(
                        new PostWriteResBody(
                                new PostDto(post),
                                postsCount
                        )
                )
        );
    }

    record PostModifyReqBody(
            @Size(min = 2, message = "제목은 2자 이상이어야 합니다.")
            @NotBlank(message = "제목은 공백일 수 없습니다.")
            String title,

            @Size(min = 2, message = "내용은 2자 이상이어야 합니다.")
            @NotBlank(message = "내용은 공백일 수 없습니다.")
            String content,

            @NotNull(message = "카테고리 선택은 필수입니다.")
            Long categoryId
    ) {
    }

    record PostModifyResBody(
            PostDto postDto
    ) {
    }

    // 글 수정 api
    @PutMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<PostModifyResBody>> modify(
            @PathVariable("postId") Long postId,
            @RequestBody @Valid PostModifyReqBody reqBody
    ) {
        // 유저 정보 생기면 사용
//        User actor = rq.getActor();
//
//        Post post = postService.findById(postId).get();
//        post.checkModify(actor);
//
//        postService.modify(postId, reqBody.title, reqBody.content);

        Post post = postService.modify(postId, reqBody.title(), reqBody.content(), reqBody.categoryId);

        return ResponseEntity.ok(
                ApiResponse.ofSuccess(
                        new PostModifyResBody(
                                new PostDto(post)
                        )
                )
        );
    }

    // 글 삭제 api
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("postId") Long postId
    ) {
        // 유저 인증처리 생기면 사용
//        User actor = rq.getActor();
//        postService.delete(postId, actor);


        postService.delete(postId);

        return ResponseEntity.ok(
                ApiResponse.ofSuccess(null)
        );
    }
}
