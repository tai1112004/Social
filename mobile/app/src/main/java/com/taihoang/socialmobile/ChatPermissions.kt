package com.taihoang.socialmobile

internal fun isGroupConversationType(type: String?): Boolean =
    type.equals("GROUP", ignoreCase = true) || type.equals("groups_chat", ignoreCase = true)

internal fun canManageGroup(role: String?): Boolean =
    role.equals("ADMIN", ignoreCase = true)
