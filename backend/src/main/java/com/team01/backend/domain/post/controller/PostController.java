package com.team01.backend.domain.post.controller;


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
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
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

