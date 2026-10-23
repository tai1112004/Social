package com.taihoang.socialmobile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PresenceStatusTest {
    private val now = "2026-10-21T12:00:00Z"

    @Test
    fun onlinePresenceUsesActiveLabel() {
        assertEquals("Dang hoat dong", presenceActivityLabel(ApiPresence(8L, "ONLINE", 1, null), now))
    }

    @Test
    fun recentOfflinePresenceShowsMinutes() {
        val presence = ApiPresence(8L, "OFFLINE", 0, "2026-10-21T11:42:00Z")

        assertEquals("Hoat dong 18 phut truoc", presenceActivityLabel(presence, now))
    }

    @Test
    fun presenceOlderThanOneHourIsHidden() {
        val presence = ApiPresence(8L, "OFFLINE", 0, "2026-10-21T10:59:59Z")

        assertNull(presenceActivityLabel(presence, now))
    }

    // tai2 is friends with three accounts all named "vu"; only vu 32 is online (22/10).
    private val sameNameFriends = listOf(
        ApiFriend(34L, "vu", null, null), ApiFriend(33L, "vu", null, null), ApiFriend(32L, "vu", null, null)
    )
    private val presenceByUser = mapOf(
        32L to ApiPresence(32L, "ONLINE", 1, null),
        33L to ApiPresence(33L, "OFFLINE", 0, null),
        34L to ApiPresence(34L, "OFFLINE", 0, null)
    )

    @Test
    fun directChatUsesThePeerIdNotTheSharedName() {
        val chatWith32 = ApiConversation(15L, "vu", null, "", 0, "private_chat", peerUserId = 32L)

        assertEquals(32L, presenceForConversation(chatWith32, sameNameFriends, presenceByUser)?.userId)
    }

    @Test
    fun ambiguousNameWithoutPeerIdShowsNoStatusInsteadOfAWrongOne() {
        val legacyChat = ApiConversation(15L, "vu", null, "", 0, "private_chat")

        assertNull(presenceForConversation(legacyChat, sameNameFriends, presenceByUser))
    }

    @Test
    fun chatPartnersWhoAreNotFriendsAreStillTracked() {
        val stranger = ApiConversation(20L, "lan", null, "", 0, "private_chat", peerUserId = 40L)
        val group = ApiConversation(11L, "Nhom", null, "", 0, "groups_chat")

        assertEquals(setOf(34L, 33L, 32L, 40L), presenceUserIds(sameNameFriends, listOf(stranger, group)))
    }
}
