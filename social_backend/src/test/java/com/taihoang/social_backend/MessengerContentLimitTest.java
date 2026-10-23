package com.taihoang.social_backend;

import com.taihoang.social_backend.Entity.Messenger;
import com.taihoang.social_backend.dto.EditMessageRequest;
import jakarta.persistence.Column;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessengerContentLimitTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void contentColumnFitsTheLongestAllowedMessage() throws NoSuchFieldException {
        Column column = Messenger.class.getDeclaredField("content").getAnnotation(Column.class);

        assertEquals(5000, column.length());
    }

    @Test
    void editRequestUsesTheSameLimit() {
        assertTrue(validator.validate(new EditMessageRequest(1L, "a".repeat(5000))).isEmpty());
        assertFalse(validator.validate(new EditMessageRequest(1L, "a".repeat(5001))).isEmpty());
    }
}
