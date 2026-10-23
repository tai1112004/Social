package com.taihoang.socialmobile

import org.junit.Assert.assertEquals
import org.junit.Test

class PostReactionPickerTest {
    @Test
    fun pickerOffersTheFiveRequestedPostReactions() {
        assertEquals(
            listOf("LIKE", "LOVE", "HAHA", "SAD", "WOW"),
            POST_REACTION_TYPES
        )
    }
}
