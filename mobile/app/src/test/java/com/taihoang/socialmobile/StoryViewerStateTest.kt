package com.taihoang.socialmobile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StoryViewerStateTest {
    private fun user(id: Long, count: Int) = ApiStoryUser(
        authorId = id,
        authorName = "User $id",
        avatarUrl = null,
        hasUnseenStory = true,
        stories = (0 until count).map { index ->
            ApiStory(id * 100 + index, id, "User $id", null, "TEXT", null, null, "Story", null, null, false, null, 0, null, null)
        }
    )

    @Test
    fun nextMovesWithinUserThenSkipsEmptyUsers() {
        val users = listOf(user(1, 2), user(2, 0), user(3, 1))
        assertEquals(StoryViewerPosition(0, 1), nextStoryPosition(users, StoryViewerPosition(0, 0)))
        assertEquals(StoryViewerPosition(2, 0), nextStoryPosition(users, StoryViewerPosition(0, 1)))
        assertNull(nextStoryPosition(users, StoryViewerPosition(2, 0)))
    }

    @Test
    fun previousMovesToPreviousUsersLastStory() {
        val users = listOf(user(1, 2), user(2, 0), user(3, 1))
        assertEquals(StoryViewerPosition(0, 1), previousStoryPosition(users, StoryViewerPosition(2, 0)))
        assertNull(previousStoryPosition(users, StoryViewerPosition(0, 0)))
    }
}
