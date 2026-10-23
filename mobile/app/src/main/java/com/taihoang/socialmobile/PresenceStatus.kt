package com.taihoang.socialmobile

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal fun presenceIsOnline(presence: ApiPresence?): Boolean =
    presence?.status.equals("ONLINE", ignoreCase = true) || (presence?.activeSessionCount ?: 0) > 0

internal fun presenceActivityLabel(presence: ApiPresence?, now: String = Instant.now().toString()): String? {
    if (presence == null) return null
    if (presenceIsOnline(presence)) return "Dang hoat dong"
    val lastSeen = parsePresenceInstant(presence.lastSeenAt) ?: return null
    val reference = parsePresenceInstant(now) ?: Instant.now()
    val minutes = Duration.between(lastSeen, reference).toMinutes().coerceAtLeast(0)
    if (minutes >= 60) return null
    return if (minutes == 0L) "Hoat dong vua xong" else "Hoat dong $minutes phut truoc"
}

/** Status of the other person in a direct chat; groups have none. */
internal fun presenceForConversation(
    conversation: ApiConversation,
    friends: List<ApiFriend>,
    presenceByUser: Map<Long, ApiPresence>
): ApiPresence? {
    conversation.peerUserId?.let { return presenceByUser[it] }
    if (isGroupConversationType(conversation.type)) return null
    // Backends older than peerUserId only send the name, which several accounts can share:
    // trust it only when exactly one friend has it.
    return friends.singleOrNull { it.name == conversation.name }?.let { presenceByUser[it.id] }
}

/** Everyone whose status the app shows: friends and the other person of each direct chat. */
internal fun presenceUserIds(friends: List<ApiFriend>, conversations: List<ApiConversation>): Set<Long> =
    (friends.map { it.id } + conversations.mapNotNull { it.peerUserId }).toSet()

private fun parsePresenceInstant(value: String?): Instant? {
    if (value.isNullOrBlank()) return null
    return runCatching { Instant.parse(value) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(value).toInstant(ZoneOffset.systemDefault().rules.getOffset(LocalDateTime.now())) }.getOrNull()
}
