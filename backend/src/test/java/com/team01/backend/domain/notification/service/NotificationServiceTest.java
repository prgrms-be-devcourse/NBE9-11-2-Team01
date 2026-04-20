package com.team01.backend.domain.notification.service;

import com.team01.backend.domain.comment.dto.CommentRequestDto;
import com.team01.backend.domain.comment.service.CommentService;
import com.team01.backend.domain.notification.entity.Notification;
import com.team01.backend.domain.notification.repository.NotificationRepository;
import com.team01.backend.domain.notification.repository.SseEmitterRepository;
import com.team01.backend.domain.post.entity.Post;
import com.team01.backend.domain.post.service.PostService;
import com.team01.backend.domain.user.entity.Role;
import com.team01.backend.domain.user.entity.User;
import com.team01.backend.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
class NotificationServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostService postService;

    @MockitoBean
    private SseEmitterRepository emitterRepository;

    @Test
    @Commit
    void shouldNotifyPostOwnerWhenOtherUserComments() {

        User commenter = userRepository.save(
                new User("commenter@test.com", "pw", "commenter", null, Role.USER)
        );


        Post post = postService.write( "title", "content", 1L, 2L); //user1

        SseEmitter emitter = mock(SseEmitter.class);

        when(emitterRepository.findByUserId(1L))
                .thenReturn(List.of(emitter));

        commentService.writeComment(1L,new CommentRequestDto("comment1",null), "commenter@test.com");

        // then
        List<Notification> notifications =
                notificationRepository.findByReceiverIdOrderByCreatedAtDesc(1L);

        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getReceiverId())
                .isEqualTo(1L);
    }

}