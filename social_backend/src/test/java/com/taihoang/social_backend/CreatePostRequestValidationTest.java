package com.taihoang.social_backend;

import com.taihoang.social_backend.dto.CreatePostRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CreatePostRequestValidationTest {

    private static Validator validator;
    private static AutoCloseable validatorFactory;

    @BeforeAll
    static void setUpValidator() {
        var factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        validatorFactory = factory;
    }

    @AfterAll
    static void closeValidator() throws Exception {
        validatorFactory.close();
    }

    @Test
    void acceptsMediaOnlyPostAtRequestBoundary() {
        var request = new CreatePostRequest(null, null, List.of(42L), List.of());

        assertTrue(validator.validate(request).isEmpty());
    }
}
