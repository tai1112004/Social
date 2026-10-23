package com.taihoang.social_backend;

import com.taihoang.social_backend.Entity.Post;
import com.taihoang.social_backend.Entity.PostPrivacy;
import com.taihoang.social_backend.Entity.User;
import com.taihoang.social_backend.Repository.ChatUploadRepository;
import com.taihoang.social_backend.Repository.FriendshipRepository;
import com.taihoang.social_backend.Repository.PostCommentRepository;
import com.taihoang.social_backend.Repository.PostMediaRepository;
import com.taihoang.social_backend.Repository.PostMentionRepository;
import com.taihoang.social_backend.Repository.PostReactionRepository;
import com.taihoang.social_backend.Repository.PostRepository;
import com.taihoang.social_backend.Repository.UserRepository;
import com.taihoang.social_backend.Service.NotificationService;
import com.taihoang.social_backend.Service.PostAccessService;
import com.taihoang.social_backend.Service.PostResponseMapper;
import com.taihoang.social_backend.Service.Implements.PostServiceImpl;
import com.taihoang.social_backend.dto.SharePostRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostServiceImplTest {
    @Test
    void directSharePersistsBlankCaptionAsAnEmptyString() {
        PostRepository postRepository = mock(PostRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PostAccessService postAccessService = mock(PostAccessService.class);
        User sharer = user(1L, "sharer");
        User author = user(2L, "author");
        Post original = new Post();
        original.setId(10L);
        original.setAuthor(author);
        original.setPrivacy(PostPrivacy.PUBLIC);
        PostServiceImpl service = new PostServiceImpl(
                postRepository,
                userRepository,
                mock(FriendshipRepository.class),
                mock(ChatUploadRepository.class),
                mock(PostMediaRepository.class),
                mock(PostReactionRepository.class),
                mock(PostCommentRepository.class),
                mock(PostResponseMapper.class),
                postAccessService,
                mock(PostMentionRepository.class),
                mock(NotificationService.class)
        );
        when(postRepository.findActiveById(10L)).thenReturn(Optional.of(original));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sharer));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(postRepository.countBySharedPost_IdAndDeletedFalse(10L)).thenReturn(1L);

        service.sharePost(1L, 10L, new SharePostRequest("   ", PostPrivacy.PUBLIC, "FEED", List.of()));

        ArgumentCaptor<Post> sharedPost = ArgumentCaptor.forClass(Post.class);
        org.mockito.Mockito.verify(postRepository).save(sharedPost.capture());
        assertEquals("", sharedPost.getValue().getContent());
    }

    private static User user(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setUserName(name);
        return user;
    }
}
