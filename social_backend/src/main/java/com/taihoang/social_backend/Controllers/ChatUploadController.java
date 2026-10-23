package com.taihoang.social_backend.Controllers;

import com.taihoang.social_backend.Service.ChatUploadService;
import com.taihoang.social_backend.dto.ChatUploadResponse;
import com.taihoang.social_backend.security.AuthenticatedUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/user/chat/uploads")
@RequiredArgsConstructor
public class ChatUploadController {

    private final ChatUploadService
            chatUploadService;


    @PostMapping
    public ResponseEntity<ChatUploadResponse> upload(

            @RequestParam("file")
            MultipartFile file,

            @AuthenticationPrincipal
            AuthenticatedUserDetails currentUser
    ) {

        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chua dang nhap");
        }


        ChatUploadResponse response =
                chatUploadService.upload(

                        currentUser.getId(),

                        file
                );


        return ResponseEntity.ok(
                response
        );
    }
}
