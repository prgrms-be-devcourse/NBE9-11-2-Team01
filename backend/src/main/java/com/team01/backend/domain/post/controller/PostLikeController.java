package com.team01.backend.domain.post.controller;

import com.team01.backend.domain.post.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;
}
