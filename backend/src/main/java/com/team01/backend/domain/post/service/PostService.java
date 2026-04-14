package com.team01.backend.domain.post.service;

import com.team01.backend.domain.post.dto.PostResponseDto;
import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

//    @Transactional
//    public Post write(User author, String title, String content) {
//        Post post = new Post(author, title, content);
//        return postRepository.save(post);
//    }


    public Post write(String title, String content) {
        Post post = new Post(title, content);
        return postRepository.save(post);
    }

    public long count() {
        return postRepository.count();
    }

    public List<PostResponseDto> getPostsByBoardId(Long boardId) {
        return postRepository.findByBoardIdAndIsDeletedFalse(boardId)
                .stream()
                .map(PostResponseDto::new)
                .toList();
    }

    public Optional<Post> findById(Long id) {return postRepository.findById(id);}

    public Post modify(Long id, String title, String content) {
        Post post = postRepository.findById(id).get();
        post.update(title, content);

        return post;

    }

}
