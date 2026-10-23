package com.taihoang.socialmobile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatPermissionsTest {
    @Test
    fun recognizesBackendGroupType() {
        assertTrue(isGroupConversationType("groups_chat"))
        assertTrue(isGroupConversationType("GROUP"))
        assertFalse(isGroupConversationType("private_chat"))
    }

    @Test
    fun onlyAdminCanManageGroup() {
        assertTrue(canManageGroup("ADMIN"))
        assertTrue(canManageGroup("admin"))
        assertFalse(canManageGroup("MEMBER"))
        assertFalse(canManageGroup(null))
    }

}
