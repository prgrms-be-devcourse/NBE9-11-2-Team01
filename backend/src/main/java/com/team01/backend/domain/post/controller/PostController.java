package com.team01.backend.domain.post.controller;

import com.team01.backend.domain.post.dto.PostDetailResponseDto;

import com.team01.backend.domain.post.dto.PostDto;
import com.team01.backend.domain.post.dto.PostResponseDto;
import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.post.service.PostService;
import com.team01.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    // 게시판별 글 목록 조회
    @GetMapping("/boards/{boardId}/posts")
    public ResponseEntity<ApiResponse<List<PostResponseDto>>> getPostsByBoardId(
            @PathVariable Long boardId
    ) {
        List<PostResponseDto> posts = postService.getPostsByBoardId(boardId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(posts));
    }

    // 게시글 상세 조회
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponseDto>> getPostById(
            @PathVariable Long postId
            // TODO: 인증 구현 후 추가
            // User user
    ) {
        // TODO: 로그인한 사용자만 접근 가능하도록 제한 필요
        PostDetailResponseDto post = postService.getPostById(postId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(post));
    }


    record PostWriteReqBody(
            @Size(min = 2, message = "제목은 2자 이상이어야 합니다.")
            @NotBlank(message = "제목은 공백일 수 없습니다.")
            String title,

            @Size(min = 2, message = "내용은 2자 이상이어야 합니다.")
            @NotBlank(message = "내용은 공백일 수 없습니다.")
            String content
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
        Post post = postService.write(reqBody.title, reqBody.content);

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
            String content
    ) {
    }

    record PostModifyResBody(
            PostDto postDto
    ) {
    }

    // 글 수정 api
    @PutMapping("posts/{id}")
    public ResponseEntity<ApiResponse<PostModifyResBody>> modify(
            @PathVariable("id") Long id,
            @RequestBody @Valid PostModifyReqBody reqBody
    ) {
        // 유저 정보 생기면 사용
//        User actor = rq.getActor();
//
//        Post post = postService.findById(id).get();
//        post.checkModify(actor);
//
//        postService.modify(id, reqBody.title, reqBody.content);

        Post post = postService.modify(id, reqBody.title(), reqBody.content());

        return ResponseEntity.ok(
                ApiResponse.ofSuccess(
                        new PostModifyResBody(
                                new PostDto(post)
                        )
                )
        );
    }
}
