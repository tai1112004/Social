package com.taihoang.social_backend;

import com.taihoang.social_backend.Controllers.ChatUploadController;
import com.taihoang.social_backend.Service.ChatUploadService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChatUploadControllerTest {

    @Test
    void uploadWithoutAuthenticatedPrincipalReturnsUnauthorized() {
        ChatUploadService service = (userId, file) -> null;
        ChatUploadController controller = new ChatUploadController(service);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.upload(null, null)
        );

        assertEquals(401, exception.getStatusCode().value());
    }
}
