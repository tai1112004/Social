package com.taihoang.socialmobile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RealtimeEventsTest {
    private val me = 7L
    private val group = ApiConversation(id = 10L, name = "Nhom cu", avatarUrl = null, lastMessage = "", unreadCount = 0, type = "GROUP")
    private val direct = ApiConversation(id = 11L, name = "Ban", avatarUrl = null, lastMessage = "", unreadCount = 0)

    @Test
    fun renameAndAvatarOnlyTouchTheirConversation() {
        val renamed = applyGroupEvent(listOf(group, direct), event("GROUP_NAME_UPDATED", name = "Nhom moi"), me)
        assertEquals(listOf("Nhom moi", "Ban"), renamed.conversations.map { it.name })

        val avatar = applyGroupEvent(renamed.conversations, event("GROUP_AVATAR_UPDATED", avatarUrl = "https://cdn/a.png"), me)
        assertEquals("https://cdn/a.png", avatar.conversations.first().avatarUrl)
        assertEquals(null, avatar.conversations.last().avatarUrl)
    }

    @Test
    fun removingCurrentUserDropsTheConversation() {
        val removed = applyGroupEvent(listOf(group, direct), event("GROUP_MEMBER_REMOVED", targets = listOf(me)), me)
        assertEquals(listOf(11L), removed.conversations.map { it.id })

        val left = event("GROUP_MEMBER_LEFT", targets = listOf(me))
        assertTrue(left.removesUser(me))
    }

    @Test
    fun removingSomeoneElseKeepsTheConversation() {
        val result = applyGroupEvent(listOf(group), event("GROUP_MEMBER_REMOVED", targets = listOf(99L)), me)
        assertEquals(listOf(group), result.conversations)
        assertFalse(event("GROUP_MEMBER_REMOVED", targets = listOf(99L)).removesUser(me))
    }

    @Test
    fun beingAddedToAnUnknownGroupRequestsReload() {
        assertTrue(applyGroupEvent(listOf(direct), event("GROUP_MEMBERS_ADDED", targets = listOf(me)), me).reloadConversations)
        assertFalse(applyGroupEvent(listOf(group), event("GROUP_MEMBERS_ADDED", targets = listOf(me)), me).reloadConversations)
        assertFalse(applyGroupEvent(listOf(direct), event("GROUP_MEMBERS_ADDED", targets = listOf(99L)), me).reloadConversations)
    }

    @Test
    fun storyReactionEventsAdjustOnlyTheirStory() {
        val mine = story(id = 1L, reactions = 2L)
        val other = story(id = 2L, reactions = 5L)
        val events = listOf(reaction(1L, "ADD", 1), reaction(1L, "UPDATE", 2), reaction(1L, "ADD", 3), reaction(2L, "REMOVE", 4))

        assertEquals(4L, mine.withReactionEvents(events).reactionCount)
        assertEquals(4L, other.withReactionEvents(events).reactionCount)
    }

    @Test
    fun storyReactionCountNeverGoesNegative() {
        assertEquals(0L, story(id = 1L, reactions = 0L).withReactionEvents(listOf(reaction(1L, "REMOVE", 1))).reactionCount)
    }

    @Test
    fun newerThanSkipsEventsAlreadyApplied() {
        val events = listOf(reaction(1L, "ADD", 1), reaction(1L, "ADD", 2), reaction(1L, "ADD", 3))
        assertEquals(listOf(2L, 3L), events.newerThan(1).map { it.nonce })
    }

    private fun event(type: String, targets: List<Long> = emptyList(), name: String? = null, avatarUrl: String? = null) =
        GroupConversationEvent(type, conversationId = 10L, actorUserId = 1L, targetUserIds = targets, name = name, avatarUrl = avatarUrl)

    private fun reaction(storyId: Long, action: String, nonce: Long) =
        StoryReactionEvent(storyId, actorId = 3L, actorName = "Lan", type = "LOVE", action = action, nonce = nonce)

    private fun story(id: Long, reactions: Long) = ApiStory(
        id = id, authorId = me, authorName = "Tai", authorAvatarUrl = null, type = "TEXT", privacy = "PUBLIC",
        media = null, text = "hello", backgroundColor = null, textColor = null, seen = true, myReaction = null,
        reactionCount = reactions, createdAt = null, expiresAt = null
    )
}
