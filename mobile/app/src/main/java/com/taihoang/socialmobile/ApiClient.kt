package com.taihoang.socialmobile

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

data class AuthSession(
    val token: String,
    val refreshToken: String?,
    val userId: Long?,
    val userName: String?,
    val email: String?,
    val avatarUrl: String? = null,
    val coverUrl: String? = null,
    val role: String? = null,
    val userSessionId: Long? = null,
    val riskScore: Int? = null,
    val approvalExpiresAt: String? = null,
    val loginStatus: String = "SUCCESS",
    val approvalToken: String? = null
)
data class LoginApprovalStatus(val status: String, val riskScore: Int, val expiresAt: String?, val auth: AuthSession?)
data class LoginOtpSendResult(val message: String, val expiresAt: String?, val debugOtp: String?)
data class ApiMedia(
    val id: Long?,
    val type: String,
    val url: String?,
    val originalFileName: String?,
    val uploadId: Long? = null,
    val sortOrder: Int? = null
)
data class ApiPost(
    val id: Long?,
    val authorId: Long?,
    val authorName: String,
    val authorAvatarUrl: String?,
    val content: String,
    val totalReactions: Int,
    val commentCount: Int,
    val shareCount: Int,
    val myReaction: String?,
    val reactionCounts: Map<String, Int> = emptyMap(),
    val media: List<ApiMedia>,
    val createdAt: String?,
    val originalPost: ApiOriginalPost? = null
)
data class ApiOriginalPost(
    val available: Boolean,
    val id: Long?,
    val authorId: Long?,
    val authorName: String,
    val authorAvatarUrl: String?,
    val content: String,
    val media: List<ApiMedia>,
    val createdAt: String?
)
data class ApiPostPage(val items: List<ApiPost>, val page: Int, val limit: Int, val totalItems: Long, val totalPages: Int)
data class ApiComment(
    val id: Long?, val postId: Long?, val parentCommentId: Long?, val authorId: Long?,
    val authorName: String, val authorAvatarUrl: String?, val content: String,
    val createdAt: String?, val updatedAt: String?
)
data class ApiCommentPage(
    val items: List<ApiComment>,
    val page: Int,
    val limit: Int,
    val totalItems: Long,
    val totalPages: Int
)
data class ApiReaction(val userId: Long?, val userName: String, val userAvatarUrl: String?, val type: String, val reactedAt: String?)
data class ApiShareResult(val shareId: Long?, val postId: Long?, val sharesCount: Int?)
data class ApiSavedPostPage(
    val items: List<ApiPost>,
    val page: Int,
    val limit: Int,
    val totalItems: Long,
    val totalPages: Int
)
data class ApiReport(
    val id: Long?,
    val targetType: String,
    val targetId: Long?,
    val reason: String,
    val description: String?,
    val status: String,
    val createdAt: String?
)
data class ApiReportPage(
    val items: List<ApiReport>,
    val page: Int,
    val limit: Int,
    val totalItems: Long,
    val totalPages: Int
)
data class ApiAdminOverview(
    val users: ApiAdminUserOverview,
    val posts: ApiAdminPostOverview,
    val comments: ApiAdminCommentOverview,
    val messages: ApiAdminMessageOverview,
    val stories: ApiAdminStoryOverview,
    val reports: ApiAdminReportOverview
)
data class ApiAdminUserOverview(val total: Long, val active: Long, val suspended: Long, val newToday: Long)
data class ApiAdminPostOverview(val total: Long, val active: Long, val deleted: Long, val newToday: Long)
data class ApiAdminCommentOverview(val total: Long, val newToday: Long)
data class ApiAdminMessageOverview(val total: Long, val today: Long)
data class ApiAdminStoryOverview(val total: Long, val activeNow: Long, val createdToday: Long)
data class ApiAdminReportOverview(val pending: Long, val reviewing: Long, val resolved: Long, val rejected: Long)
data class ApiAdminGrowth(
    val metric: String,
    val period: String,
    val from: String?,
    val to: String?,
    val total: Long,
    val items: List<ApiAdminDailyCount>
)
data class ApiAdminDailyCount(val date: String?, val count: Long)
data class ApiAdminTopPost(
    val postId: Long?,
    val authorName: String,
    val contentPreview: String,
    val reactionCount: Long,
    val commentCount: Long,
    val shareCount: Long,
    val engagementScore: Long,
    val createdAt: String?,
    val deleted: Boolean
)
data class ApiAdminActiveUser(
    val userId: Long?,
    val userName: String,
    val avatarUrl: String?,
    val posts: Long,
    val comments: Long,
    val messages: Long,
    val stories: Long,
    val activityScore: Long
)
data class ApiAdminReportStats(val total: Long, val byStatus: Map<String, Long>, val byTargetType: Map<String, Long>, val byReason: Map<String, Long>)
data class ApiAdminStoryStats(val totalStories: Long, val activeNow: Long, val expired: Long, val deleted: Long, val createdToday: Long, val totalViews: Long, val totalReactions: Long)
data class ApiAdminChatStats(val totalConversations: Long, val directConversations: Long, val groupConversations: Long, val totalMessages: Long, val messagesToday: Long, val messagesLast7Days: Long)
data class ApiAdminReport(
    val id: Long?,
    val reporterId: Long?,
    val reporterName: String,
    val targetType: String,
    val targetId: Long?,
    val reason: String,
    val description: String?,
    val status: String,
    val reviewedByName: String?,
    val reviewedAt: String?,
    val resolutionNote: String?,
    val createdAt: String?,
    val updatedAt: String?
)
data class ApiAdminReportPage(val items: List<ApiAdminReport>, val page: Int, val limit: Int, val total: Long, val totalPages: Int)
data class ApiProfile(val id: Long?, val userName: String, val email: String?, val avatarUrl: String?, val coverUrl: String?, val bio: String?, val dateOfBirth: String? = null, val gender: String? = null, val createdAt: String? = null)
data class ApiNotification(
    val id: Long?, val text: String, val read: Boolean, val type: String = "",
    val reactionType: String? = null,
    val actorId: Long? = null, val actorName: String? = null, val actorAvatarUrl: String? = null,
    val referenceId: Long? = null, val postId: Long? = null, val commentId: Long? = null,
    val storyId: Long? = null, val createdAt: String? = null
)
data class ApiNotificationPage(
    val items: List<ApiNotification>,
    val page: Int,
    val limit: Int,
    val totalItems: Long,
    val totalPages: Int
)
data class ApiPresence(val userId: Long?, val status: String, val activeSessionCount: Int, val lastSeenAt: String?)
data class ApiStoryMedia(val uploadId: Long?, val type: String?, val url: String?, val originalFileName: String?)
data class ApiStoryTextOverlay(
    val text: String,
    val x: Double = 0.5,
    val y: Double = 0.5,
    val fontSize: Double? = null,
    val color: String? = null,
    val fontStyle: String? = null,
    val rotation: Double? = null
)
data class ApiMusicTrack(val id: Long, val title: String, val artist: String?, val audioUrl: String, val coverUrl: String?, val durationMs: Long, val active: Boolean = true)
data class ApiMusicPage(val items: List<ApiMusicTrack>, val page: Int, val totalPages: Int, val hasMore: Boolean)
data class ApiStoryMusic(val id: Long, val title: String, val artist: String?, val audioUrl: String, val coverUrl: String?, val startMs: Long, val durationMs: Long, val volume: Double)
data class ApiStory(val id: Long?, val authorId: Long?, val authorName: String, val authorAvatarUrl: String?, val type: String, val privacy: String?, val media: ApiStoryMedia?, val text: String?, val backgroundColor: String?, val textColor: String?, val seen: Boolean, val myReaction: String?, val reactionCount: Long, val createdAt: String?, val expiresAt: String?, val music: ApiStoryMusic? = null, val textOverlays: List<ApiStoryTextOverlay> = emptyList())
data class ApiStoryUser(val authorId: Long?, val authorName: String, val avatarUrl: String?, val hasUnseenStory: Boolean, val stories: List<ApiStory>)
data class ApiStoryViewer(val userId: Long?, val userName: String, val avatarUrl: String?, val viewedAt: String?, val reactionType: String?)
data class ApiUpload(val uploadId: Long?, val type: String?, val url: String?, val originalFileName: String?, val contentType: String?, val fileSize: Long?)
data class ApiConversation(
    val id: Long,
    val name: String,
    val avatarUrl: String?,
    val lastMessage: String,
    val unreadCount: Int,
    val type: String = "DIRECT",
    val updatedAt: String? = null,
    val lastMessageId: Long? = null,
    val lastMessageSenderName: String? = null,
    /** The other person of a direct chat; names are not unique, so look people up by this id. */
    val peerUserId: Long? = null
)
data class ApiConversationMember(
    val userId: Long,
    val userName: String,
    val email: String?,
    val joinedAt: String?,
    val role: String
)
data class ApiConversationDetail(
    val id: Long,
    val type: String,
    val name: String,
    val avatarUrl: String?,
    val createdAt: String?,
    val currentUserRole: String,
    val members: List<ApiConversationMember>
)
data class ApiConversationPage(val items: List<ApiConversation>, val nextCursor: String?, val hasMore: Boolean)
data class ApiReplyMessage(val messageId: Long?, val senderId: Long?, val senderName: String, val content: String, val recalled: Boolean)
data class ApiMessageReaction(val userId: Long?, val userName: String, val type: String)
data class ApiMessageAttachment(val attachmentId: Long?, val type: String?, val url: String?, val originalFileName: String?, val contentType: String?, val fileSize: Long?)
data class ApiStoryMessageReference(val storyId: Long?, val available: Boolean, val authorId: Long?, val type: String?, val mediaUrl: String?, val text: String?, val createdAt: String?, val expiresAt: String?)
data class ApiMessage(
    val id: Long?, val conversationId: Long?, val senderId: Long?, val senderName: String,
    val content: String, val messageType: String, val sentAt: String?, val editedAt: String?,
    val recalledAt: String?, val seenAt: String?, val clientMessageId: String?,
    val sequenceNumber: Long? = null,
    val replyTo: ApiReplyMessage? = null,
    val reactions: List<ApiMessageReaction> = emptyList(),
    val attachments: List<ApiMessageAttachment> = emptyList(),
    val storyReference: ApiStoryMessageReference? = null,
    val deliveredAt: String? = null,
    /** Local-only state for an optimistic message; never sent to the backend. */
    val localStatus: String? = null,
    /** Upload IDs are different from persisted message attachment IDs. */
    val localUploadIds: List<Long> = emptyList(),
    /** Changes on every send attempt so an old timeout cannot fail a retry. */
    val localSendAttempt: Long = 0L,
    /** Local call-record metadata used when a call is rendered in the message timeline. */
    val callType: String? = null,
    val callStatus: String? = null,
    val callDurationSeconds: Long? = null,
    val callOutgoing: Boolean? = null
)
data class ApiMessagePage(val items: List<ApiMessage>, val nextBeforeSequence: Long?, val hasMore: Boolean)
data class ApiChatSyncPage(val items: List<ApiMessage>, val nextAfterMessageId: Long?, val hasMore: Boolean)
data class ApiFriend(val id: Long, val name: String, val avatarUrl: String?, val bio: String?)
data class ApiFriendRequest(val requestId: Long, val userId: Long, val userName: String, val avatarUrl: String?, val createdAt: String?)
data class ApiFriendStatus(val userId: Long, val status: String, val friendshipId: Long?)
data class ApiSearchUser(val id: Long, val name: String, val avatarUrl: String?, val bio: String?)
data class ApiUserSearchPage(val items: List<ApiSearchUser>, val page: Int, val limit: Int, val totalItems: Long, val totalPages: Int)
data class ApiSecuritySession(
    val id: Long, val userDeviceId: Long?, val deviceName: String?, val deviceType: String?,
    val browser: String?, val os: String?, val trusted: Boolean, val ipAddress: String?,
    val createdAt: String?, val lastActiveAt: String?, val expiresAt: String?, val status: String?, val current: Boolean
)
data class ApiPendingLoginApproval(
    val id: Long, val userDeviceId: Long?, val deviceName: String?, val deviceType: String?,
    val browser: String?, val os: String?, val ipAddress: String?, val riskScore: Int,
    val createdAt: String?, val expiresAt: String?, val status: String?
)

data class ApiCallHistory(
    val id: Long,
    val callId: String,
    val conversationId: Long,
    val otherUserId: Long,
    val otherUserName: String,
    val otherUserAvatarUrl: String?,
    val callType: String,
    val status: String,
    val createdAt: String?,
    val answeredAt: String?,
    val endedAt: String?,
    val durationSeconds: Long?,
    val outgoing: Boolean
)

class ApiException(val status: Int, override val message: String) : IOException(message)

class SessionStore(context: Context) : SessionHolder {
    private val prefs = context.getSharedPreferences("social_session", Context.MODE_PRIVATE)
    override fun get(): AuthSession? = prefs.getString("token", null)?.let {
        AuthSession(it, prefs.getString("refreshToken", null), prefs.getLong("userId", -1L).takeIf { id -> id >= 0 }, prefs.getString("userName", null), prefs.getString("email", null), prefs.getString("avatarUrl", null), prefs.getString("coverUrl", null), prefs.getString("role", null), prefs.getLong("userSessionId", -1L).takeIf { id -> id >= 0 })
    }
    override fun save(session: AuthSession) = prefs.edit().putString("token", session.token).putString("refreshToken", session.refreshToken).putLong("userId", session.userId ?: -1L).putString("userName", session.userName).putString("email", session.email).putString("avatarUrl", session.avatarUrl).putString("coverUrl", session.coverUrl).putString("role", session.role).putLong("userSessionId", session.userSessionId ?: -1L).apply()
    override fun clear() = prefs.edit()
        .remove("token")
        .remove("refreshToken")
        .remove("userId")
        .remove("userName")
        .remove("email")
        .remove("avatarUrl")
        .remove("coverUrl")
        .remove("role")
        .remove("userSessionId")
        .apply()
    fun isTrustPromptShown() = prefs.getBoolean("trustPromptShown", false)
    fun markTrustPromptShown() = prefs.edit().putBoolean("trustPromptShown", true).apply()
    fun deviceId(): String = prefs.getString("deviceId", null) ?: UUID.randomUUID().toString().also { prefs.edit().putString("deviceId", it).apply() }
    fun selectedBackendEndpointId(defaultBaseUrl: String = LOCAL_BACKEND_ENDPOINT.baseUrl): String {
        val defaultId = backendEndpointIdForUrl(defaultBaseUrl)
        return backendEndpointForId(prefs.getString("backendEndpointId", null))?.id
            ?: backendEndpointForId(defaultId)?.id
            ?: LOCAL_BACKEND_ENDPOINT.id
    }
    fun saveBackendEndpoint(endpointId: String) {
        if (backendEndpointForId(endpointId) != null) {
            prefs.edit().putString("backendEndpointId", endpointId).apply()
        }
    }
}

class ApiClient(initialBaseUrl: String, private val sessionStore: SessionStore) {
    var baseUrl: String = normalizeBackendUrl(initialBaseUrl)
        private set

    fun updateBaseUrl(newBaseUrl: String) {
        baseUrl = normalizeBackendUrl(newBaseUrl)
    }

    private val tokenRefresher = TokenRefresher(sessionStore) { refreshToken ->
        executeJson("POST", "/auth/refresh", JSONObject().put("refreshToken", refreshToken), token = null).let(::authSession)
    }

    /** Called (off the main thread) after the stored session was replaced by a refreshed one. */
    var onSessionRefreshed: ((AuthSession) -> Unit)?
        get() = tokenRefresher.onRefreshed
        set(value) { tokenRefresher.onRefreshed = value }

    /** Called (off the main thread) after the refresh token was rejected and the stored session cleared. */
    var onSessionExpired: (() -> Unit)?
        get() = tokenRefresher.onExpired
        set(value) { tokenRefresher.onExpired = value }

    fun currentUserId(): Long? = sessionStore.get()?.userId
    fun currentToken(): String? = sessionStore.get()?.token

    /** Renews the session after [rejectedToken] was refused, e.g. by the STOMP CONNECT handshake. */
    suspend fun refreshSession(rejectedToken: String): AuthSession? = withContext(Dispatchers.IO) {
        tokenRefresher.refreshAfterRejected(rejectedToken)
    }
    suspend fun login(email: String, password: String, trustDevice: Boolean = false): AuthSession {
        require(email.isNotBlank()) { "Email không được để trống" }
        require(password.isNotBlank()) { "Mật khẩu không được để trống" }
        return requestJson("POST", "/auth/login", JSONObject().put("email", email.trim()).put("password", password).put("deviceId", sessionStore.deviceId()).put("deviceType", "ANDROID").put("deviceName", "Android").put("os", "Android").put("trustDevice", trustDevice)).let(::authSession)
    }
    suspend fun register(userName: String, email: String, password: String): AuthSession = requestJson("POST", "/auth/register", JSONObject().put("userName", userName).put("email", email).put("password", password)).let(::authSession)
    suspend fun registerAndLogin(userName: String, email: String, password: String, trustDevice: Boolean = false): AuthSession {
        register(userName, email, password)
        // Registration currently returns a JWT without a persisted user session;
        // login creates the session required by JwtAuthenticationFilter.
        return login(email, password, trustDevice)
    }
    suspend fun logout() { requestJson("POST", "/auth/logout", JSONObject().put("refreshToken", sessionStore.get()?.refreshToken)); sessionStore.clear() }
    suspend fun loginApprovalStatus(approvalToken: String): LoginApprovalStatus = requestJson("GET", "/auth/login-approvals/${URLEncoder.encode(approvalToken, "UTF-8")}/status").toLoginApprovalStatus()
    suspend fun sendLoginOtp(approvalToken: String): LoginOtpSendResult = requestJson("POST", "/auth/login-approvals/${URLEncoder.encode(approvalToken, "UTF-8")}/otp/send").let { LoginOtpSendResult(it.optStringOr("message", ""), it.optStringOrNull("expiresAt"), it.optStringOrNull("debugOtp")) }
    suspend fun verifyLoginOtp(approvalToken: String, otp: String, trustDevice: Boolean): LoginApprovalStatus = requestJson("POST", "/auth/login-approvals/${URLEncoder.encode(approvalToken, "UTF-8")}/otp/verify", JSONObject().put("otp", otp).put("trustDevice", trustDevice)).toLoginApprovalStatus()

    suspend fun feed(page: Int = 0, limit: Int = 20): List<ApiPost> = requestJson("GET", "/user/feed?page=$page&limit=$limit").optJSONArray("items").toPosts()
    suspend fun profile(): ApiProfile = requestJson("GET", "/user/profile/me").toProfile()
    suspend fun userProfile(userId: Long): ApiProfile = requestJson("GET", "/user/profile/$userId").toProfile()
    suspend fun updateProfile(userName: String?, bio: String?, avatarUrl: String?, coverUrl: String?, dateOfBirth: String?, gender: String?): ApiProfile = requestJson("PUT", "/user/profile/me", JSONObject().apply {
        putNullable("userName", userName); putNullable("bio", bio); putNullable("avatarUrl", avatarUrl); putNullable("coverUrl", coverUrl); putNullable("dateOfBirth", dateOfBirth); putNullable("gender", gender)
    }).toProfile()
    suspend fun notifications(page: Int = 0, limit: Int = 20): ApiNotificationPage = requestJson("GET", "/user/notifications?page=$page&limit=$limit").toNotificationPage()
    suspend fun unreadNotifications(): Int = requestJson("GET", "/user/notifications/unread-count")
        .optLong("unreadCount", 0L).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
    suspend fun markNotificationRead(notificationId: Long) = requestJson("PATCH", "/user/notifications/$notificationId/read")
    suspend fun markAllNotificationsRead() { requestJson("PATCH", "/user/notifications/read-all") }
    suspend fun presence(userId: Long): ApiPresence = requestJson("GET", "/presence/$userId").let { ApiPresence(it.optLong("userId").takeIf { id -> id != 0L }, it.optStringOr("status", "OFFLINE"), it.optInt("activeSessionCount", 0), it.optStringOrNull("lastSeenAt")) }
    suspend fun iceServers(): JSONArray = requestJson("GET", "/user/calls/ice-servers").optJSONArray("items") ?: JSONArray()
    suspend fun callHistory(limit: Int = 50): List<ApiCallHistory> = requestJson("GET", "/user/calls/history?limit=$limit").optJSONArray("items").toCallHistory()

    // Remaining REST capabilities are exposed here so screens can be added without duplicating auth/error handling.
    suspend fun conversationPage(cursor: String? = null, limit: Int = 20): ApiConversationPage {
        val cursorPart = cursor?.let { "&cursor=${URLEncoder.encode(it, "UTF-8")}" }.orEmpty()
        return requestJson("GET", "/user/conversations?limit=$limit$cursorPart").toConversationPage()
    }
    suspend fun conversations(): List<ApiConversation> = conversationPage(limit = 50).items
    suspend fun messagePage(conversationId: Long, beforeSequence: Long? = null, limit: Int = 30): ApiMessagePage {
        val cursor = beforeSequence?.let { "&beforeSequence=$it" }.orEmpty()
        return requestJson("GET", "/user/conversations/$conversationId/messages?limit=$limit$cursor").toMessagePage()
    }
    suspend fun messages(conversationId: Long, beforeSequence: Long? = null, limit: Int = 50): List<ApiMessage> {
        return messagePage(conversationId, beforeSequence, limit).items
    }
    suspend fun createDirectConversation(recipientId: Long): JSONObject = requestJson("POST", "/user/conversations/direct", JSONObject().put("recipientId", recipientId))
    suspend fun createGroupConversation(name: String, memberIds: List<Long>): JSONObject = requestJson("POST", "/user/conversations/group", JSONObject().put("name", name).put("memberIds", JSONArray(memberIds)))
    suspend fun conversationDetail(conversationId: Long): JSONObject = requestJson("GET", "/user/conversations/$conversationId")
    suspend fun markConversationSeen(conversationId: Long) { requestJson("POST", "/user/conversations/$conversationId/seen") }
    suspend fun chatSyncPage(afterMessageId: Long? = null, limit: Int = 100): ApiChatSyncPage {
        val cursor = afterMessageId?.let { "&afterMessageId=$it" }.orEmpty()
        return requestJson("GET", "/user/chat/sync?limit=$limit$cursor").toChatSyncPage()
    }
    suspend fun syncChat(afterMessageId: Long? = null, limit: Int = 100): List<ApiMessage> = chatSyncPage(afterMessageId, limit).items
    suspend fun seenConversation(conversationId: Long) { requestJson("POST", "/user/conversations/$conversationId/seen") }
    suspend fun addGroupMembers(conversationId: Long, memberIds: List<Long>) = requestJson("POST", "/user/conversations/$conversationId/members", JSONObject().put("memberIds", JSONArray(memberIds)))
    suspend fun removeGroupMember(conversationId: Long, memberId: Long) { requestJson("DELETE", "/user/conversations/$conversationId/members/$memberId") }
    suspend fun leaveGroup(conversationId: Long) { requestJson("DELETE", "/user/conversations/$conversationId/members/me") }
    suspend fun renameGroup(conversationId: Long, name: String) = requestJson("PATCH", "/user/conversations/$conversationId/name", JSONObject().put("name", name))
    suspend fun updateGroupAvatar(conversationId: Long, uploadId: Long) = requestJson("PATCH", "/user/conversations/$conversationId/avatar", JSONObject().put("uploadId", uploadId))
    suspend fun updateGroupMemberRole(conversationId: Long, memberId: Long, role: String) = requestJson("PATCH", "/user/conversations/$conversationId/members/$memberId/role", JSONObject().put("role", role))
    suspend fun friends(): List<ApiFriend> = requestJson("GET", "/user/friends?page=0&limit=50").optJSONArray("items").toFriends()
    suspend fun friendRequests(): List<ApiFriendRequest> = requestJson("GET", "/user/friends/requests/received").optJSONArray("items").toFriendRequests()
    suspend fun sentFriendRequests(): List<ApiFriendRequest> = requestJson("GET", "/user/friends/requests/sent").optJSONArray("items").toFriendRequests()
    suspend fun cancelFriendRequest(requestId: Long) { requestJson("DELETE", "/user/friends/requests/$requestId") }
    suspend fun friendStatus(userId: Long): ApiFriendStatus = requestJson("GET", "/user/friends/status/$userId").let { ApiFriendStatus(it.optLong("userId"), it.optStringOr("status", "NONE"), it.optLong("friendshipId").takeIf { id -> id != 0L }) }
    suspend fun userFriends(userId: Long, page: Int = 0, limit: Int = 50): List<ApiFriend> = requestJson("GET", "/user/friends/$userId/friends?page=$page&limit=$limit").optJSONArray("items").toFriends()
    suspend fun musicTracks(page: Int = 0, limit: Int = 20): ApiMusicPage = requestJson("GET", "/user/music/tracks?page=$page&limit=$limit").toMusicPage()
    suspend fun searchMusicTracks(query: String, page: Int = 0, limit: Int = 20): ApiMusicPage = requestJson("GET", "/user/music/tracks/search?q=${URLEncoder.encode(query, "UTF-8")}&page=$page&limit=$limit").toMusicPage()
    suspend fun musicTrack(trackId: Long): ApiMusicTrack = requestJson("GET", "/user/music/tracks/$trackId").toMusicTrack()
    suspend fun stories(): List<ApiStoryUser> = requestJson("GET", "/user/stories/feed").optJSONArray("items").toStoryUsers()
    suspend fun myStories(): List<ApiStory> = requestJson("GET", "/user/stories/me").optJSONArray("items").toStories()
    suspend fun story(storyId: Long): ApiStory = requestJson("GET", "/user/stories/$storyId").toStoryWithMusic()
    suspend fun searchUsersPage(query: String, page: Int = 0, limit: Int = 20): ApiUserSearchPage = requestJson("GET", "/user/profile/search?q=${URLEncoder.encode(query, "UTF-8")}&page=$page&limit=$limit").toUserSearchPage()
    suspend fun searchUsers(query: String): List<ApiSearchUser> = searchUsersPage(query).items
    suspend fun react(postId: Long, reactionType: String) = requestJson("POST", "/user/feed/$postId/reactions", JSONObject().put("type", reactionType))
    suspend fun removeReaction(postId: Long) { requestJson("DELETE", "/user/feed/$postId/reactions") }
    suspend fun sharePost(postId: Long, content: String = "", privacy: String = "PUBLIC", mentionedUserIds: List<Long> = emptyList()): ApiShareResult = requestJson("POST", "/user/posts/$postId/share", JSONObject().put("content", content).put("privacy", privacy).put("target", "FEED").put("mentionedUserIds", JSONArray(mentionedUserIds))).let { response ->
        ApiShareResult(
            response.optLong("shareId", response.optLong("id")).takeIf { it != 0L },
            response.optLong("postId", response.optLong("originalPostId")).takeIf { it != 0L },
            response.optInt("sharesCount").takeIf { response.has("sharesCount") }
        )
    }

    suspend fun createPost(content: String, privacy: String = "PUBLIC", mediaIds: List<Long> = emptyList(), mentionedUserIds: List<Long> = emptyList()) = requestJson("POST", "/user/posts", JSONObject().put("content", content).put("privacy", privacy).put("mediaIds", JSONArray(mediaIds)).put("mentionedUserIds", JSONArray(mentionedUserIds)))
    suspend fun updatePost(postId: Long, content: String, privacy: String = "PUBLIC", mediaIds: List<Long> = emptyList(), mentionedUserIds: List<Long> = emptyList()) = requestJson("PUT", "/user/posts/$postId", JSONObject().put("content", content).put("privacy", privacy).put("mediaIds", JSONArray(mediaIds)).put("mentionedUserIds", JSONArray(mentionedUserIds)))
    suspend fun deletePost(postId: Long) { requestJson("DELETE", "/user/posts/$postId") }
    suspend fun post(postId: Long): ApiPost = requestJson("GET", "/user/posts/$postId").toPost()
    suspend fun userPostsPage(userId: Long, page: Int = 0, limit: Int = 20): ApiPostPage = requestJson("GET", "/user/posts/user/$userId?page=$page&limit=$limit").toPostPage()
    suspend fun userPosts(userId: Long, page: Int = 0, limit: Int = 20): List<ApiPost> = userPostsPage(userId, page, limit).items
    suspend fun savedPostsPage(page: Int = 0, limit: Int = 20): ApiSavedPostPage = requestJson("GET", "/user/posts/saved?page=$page&limit=$limit").toSavedPostPage()
    suspend fun savedPosts(page: Int = 0, limit: Int = 20): List<ApiPost> = savedPostsPage(page, limit).items
    suspend fun savePost(postId: Long) = requestJson("POST", "/user/posts/$postId/save")
    suspend fun unsavePost(postId: Long) { requestJson("DELETE", "/user/posts/$postId/save") }
    suspend fun hidePost(postId: Long) = requestJson("POST", "/user/posts/$postId/hide")
    suspend fun unhidePost(postId: Long) { requestJson("DELETE", "/user/posts/$postId/hide") }
    suspend fun videoFeed(page: Int = 0, limit: Int = 20): List<ApiPost> = requestJson("GET", "/user/feed/videos?page=$page&limit=$limit").optJSONArray("items").toPosts()
    suspend fun postReactions(postId: Long, page: Int = 0, limit: Int = 20): List<ApiReaction> = requestJson("GET", "/user/feed/$postId/reactions?page=$page&limit=$limit").optJSONArray("items").toReactions()
    suspend fun comments(postId: Long, page: Int = 0, limit: Int = 20): ApiCommentPage = requestJson("GET", "/user/posts/$postId/comments?page=$page&limit=$limit").toCommentPage()
    suspend fun comment(postId: Long, content: String): ApiComment = requestJson("POST", "/user/posts/$postId/comments", JSONObject().put("content", content)).toComment()
    suspend fun updateComment(commentId: Long, content: String): ApiComment = requestJson("PUT", "/user/posts/comments/$commentId", JSONObject().put("content", content)).toComment()
    suspend fun deleteComment(commentId: Long) { requestJson("DELETE", "/user/posts/comments/$commentId") }
    suspend fun replyToComment(commentId: Long, content: String): ApiComment = requestJson("POST", "/user/posts/comments/$commentId/replies", JSONObject().put("content", content)).toComment()
    suspend fun reportComment(commentId: Long, reason: String, description: String? = null) = requestJson("POST", "/user/reports/comments/$commentId", JSONObject().apply { put("reason", reason); putNullable("description", description) })
    suspend fun sendFriendRequest(userId: Long): Long = requestJson("POST", "/user/friends/requests/$userId").optLong("id")
    suspend fun acceptFriendRequest(requestId: Long) = requestJson("POST", "/user/friends/requests/$requestId/accept")
    suspend fun rejectFriendRequest(requestId: Long) = requestJson("POST", "/user/friends/requests/$requestId/reject")
    suspend fun removeFriend(userId: Long) { requestJson("DELETE", "/user/friends/$userId") }
    suspend fun createStory(type: String = "TEXT", text: String? = null, privacy: String = "PUBLIC", uploadId: Long? = null, backgroundColor: String? = null, textColor: String? = null, textOverlays: List<ApiStoryTextOverlay> = emptyList(), musicTrackId: Long? = null, musicStartMs: Long? = null, musicDurationMs: Long? = null, musicVolume: Double? = null): ApiStory = requestJson("POST", "/user/stories", JSONObject().apply {
        put("type", type); put("privacy", privacy); putNullable("text", text)
        if (uploadId != null) put("uploadId", uploadId)
        putNullable("backgroundColor", backgroundColor); putNullable("textColor", textColor)
        put("textOverlays", JSONArray().apply { textOverlays.take(10).forEach { overlay -> put(JSONObject().apply {
            put("text", overlay.text); put("x", overlay.x); put("y", overlay.y)
            overlay.fontSize?.let { put("fontSize", it) }; putNullable("color", overlay.color)
            putNullable("fontStyle", overlay.fontStyle); overlay.rotation?.let { put("rotation", it) }
        }) } })
        if (musicTrackId != null) put("musicTrackId", musicTrackId)
        if (musicStartMs != null) put("musicStartMs", musicStartMs)
        if (musicDurationMs != null) put("musicDurationMs", musicDurationMs)
        if (musicVolume != null) put("musicVolume", musicVolume)
    }).toStoryWithMusic()
    suspend fun viewStory(storyId: Long) = requestJson("POST", "/user/stories/$storyId/view")
    suspend fun storyViewers(storyId: Long, page: Int = 0, limit: Int = 50): List<ApiStoryViewer> = requestJson("GET", "/user/stories/$storyId/viewers?page=$page&limit=$limit").optJSONArray("items").toStoryViewers()
    suspend fun reactStory(storyId: Long, reactionType: String) = requestJson("POST", "/user/stories/$storyId/reaction", JSONObject().put("type", reactionType))
    suspend fun removeStoryReaction(storyId: Long) { requestJson("DELETE", "/user/stories/$storyId/reaction") }
    suspend fun replyStory(storyId: Long, content: String, clientMessageId: String = UUID.randomUUID().toString()) = requestJson("POST", "/user/stories/$storyId/reply", JSONObject().put("clientMessageId", clientMessageId).put("content", content))
    suspend fun deleteStory(storyId: Long) { requestJson("DELETE", "/user/stories/$storyId") }
    suspend fun uploadMedia(fileName: String, contentType: String, bytes: ByteArray): ApiUpload = withContext(Dispatchers.IO) {
        withAuthRetry("/user/chat/uploads") { token -> executeUpload(fileName, contentType, bytes, token) }
    }
    private fun executeUpload(fileName: String, contentType: String, bytes: ByteArray, token: String?): ApiUpload {
        val boundary = "----SocialMobile${ThreadLocalRandom.current().nextLong()}"
        val connection = (URL(baseUrl.trimEnd('/') + "/user/chat/uploads").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; doOutput = true; connectTimeout = 15_000; readTimeout = 30_000
            setRequestProperty("Accept", "application/json"); setRequestProperty("Authorization", "Bearer ${token.orEmpty()}")
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }
        return try {
            val safeFileName = fileName.substringAfterLast('/').substringAfterLast('\\')
                .replace(Regex("[\\r\\n\\\"]"), "_")
                .ifBlank { "upload.bin" }
            connection.outputStream.use { out ->
                out.write("--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"$safeFileName\"\r\nContent-Type: $contentType\r\n\r\n".toByteArray(Charsets.UTF_8))
                out.write(bytes); out.write("\r\n--$boundary--\r\n".toByteArray())
            }
            val status = connection.responseCode
            val text = (if (status in 200..299) connection.inputStream else connection.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) throw ApiException(status, parseError(text, status))
            JSONObject(text).let { ApiUpload(it.optLong("uploadId").takeIf { id -> id != 0L }, it.optStringOrNull("type"), it.optStringOrNull("url"), it.optStringOrNull("originalFileName"), it.optStringOrNull("contentType"), it.optLong("fileSize").takeIf { size -> size != 0L }) }
        } finally { connection.disconnect() }
    }
    suspend fun securitySessions(): List<ApiSecuritySession> = requestJson("GET", "/user/security/sessions").optJSONArray("items").toSecuritySessions()
    suspend fun trustCurrentDevice() {
        val sessions = securitySessions()
        sessions.firstOrNull { it.current && (it.userDeviceId ?: 0L) > 0 }?.let { session ->
            requestJson("PATCH", "/user/security/devices/${session.userDeviceId}/trust")
            return
        }
        throw ApiException(404, "Không tìm thấy thiết bị hiện tại")
    }
    suspend fun trustDevice(userDeviceId: Long) { requestJson("PATCH", "/user/security/devices/$userDeviceId/trust") }
    suspend fun untrustDevice(userDeviceId: Long) { requestJson("PATCH", "/user/security/devices/$userDeviceId/untrust") }
    suspend fun revokeOtherSessions() { requestJson("DELETE", "/user/security/sessions/others") }
    suspend fun approveLogin(approvalRequestId: Long) { requestJson("POST", "/user/security/login-approvals/$approvalRequestId/approve") }
    suspend fun rejectLogin(approvalRequestId: Long) { requestJson("POST", "/user/security/login-approvals/$approvalRequestId/reject") }
    suspend fun pendingLoginApprovals(): List<ApiPendingLoginApproval> = requestJson("GET", "/user/security/login-approvals/pending").optJSONArray("items").toPendingApprovals()
    suspend fun revokeSession(sessionId: Long) { requestJson("DELETE", "/user/security/sessions/$sessionId") }
    suspend fun reportPost(postId: Long, reason: String, description: String? = null) = requestJson("POST", "/user/reports/posts/$postId", JSONObject().apply { put("reason", reason); putNullable("description", description) })
    suspend fun reportUser(userId: Long, reason: String, description: String? = null) = requestJson("POST", "/user/reports/users/$userId", JSONObject().apply { put("reason", reason); putNullable("description", description) })
    suspend fun myReportsPage(page: Int = 0, limit: Int = 20): ApiReportPage = requestJson("GET", "/user/reports?page=$page&limit=$limit").toReportPage()

    suspend fun adminOverview(): ApiAdminOverview = requestJson("GET", "/admin/statistics/overview").toAdminOverview()
    suspend fun adminGrowth(metric: String, period: String = "SEVEN_DAYS"): ApiAdminGrowth = requestJson("GET", "/admin/statistics/growth?metric=$metric&period=$period").toAdminGrowth()
    suspend fun adminTopPosts(period: String = "SEVEN_DAYS", limit: Int = 10): List<ApiAdminTopPost> = requestJson("GET", "/admin/statistics/top-posts?period=$period&limit=$limit").optJSONArray("items").toAdminTopPosts()
    suspend fun adminActiveUsers(period: String = "SEVEN_DAYS", limit: Int = 10): List<ApiAdminActiveUser> = requestJson("GET", "/admin/statistics/active-users?period=$period&limit=$limit").optJSONArray("items").toAdminActiveUsers()
    suspend fun adminReportStats(): ApiAdminReportStats = requestJson("GET", "/admin/statistics/reports").toAdminReportStats()
    suspend fun adminStoryStats(): ApiAdminStoryStats = requestJson("GET", "/admin/statistics/stories").toAdminStoryStats()
    suspend fun adminChatStats(): ApiAdminChatStats = requestJson("GET", "/admin/statistics/chat").toAdminChatStats()
    suspend fun adminReports(status: String? = null, targetType: String? = null, reason: String? = null, page: Int = 0, limit: Int = 20): ApiAdminReportPage {
        require(page >= 0) { "Page không hợp lệ" }
        require(limit in 1..100) { "Limit phải từ 1 đến 100" }
        val params = listOfNotNull(
            status?.takeIf { it.isNotBlank() }?.let { "status=${URLEncoder.encode(it, "UTF-8")}" },
            targetType?.takeIf { it.isNotBlank() }?.let { "targetType=${URLEncoder.encode(it, "UTF-8")}" },
            reason?.takeIf { it.isNotBlank() }?.let { "reason=${URLEncoder.encode(it, "UTF-8")}" },
            "page=$page", "limit=$limit"
        ).joinToString("&")
        return requestJson("GET", "/admin/reports?$params").toAdminReportPage()
    }
    suspend fun adminReport(reportId: Long): ApiAdminReport = requestJson("GET", "/admin/reports/$reportId").toAdminReport()
    suspend fun adminReviewReport(reportId: Long): ApiAdminReport = requestJson("PATCH", "/admin/reports/$reportId/review").toAdminReport()
    suspend fun adminResolveReport(reportId: Long, resolutionNote: String = ""): ApiAdminReport = requestJson("PATCH", "/admin/reports/$reportId/resolve", JSONObject().put("resolutionNote", resolutionNote)).toAdminReport()
    suspend fun adminRejectReport(reportId: Long, resolutionNote: String = ""): ApiAdminReport = requestJson("PATCH", "/admin/reports/$reportId/reject", JSONObject().put("resolutionNote", resolutionNote)).toAdminReport()
    suspend fun adminRemovePost(postId: Long) { requestJson("DELETE", "/admin/posts/$postId") }
    suspend fun adminRemoveComment(commentId: Long) { requestJson("DELETE", "/admin/comments/$commentId") }
    suspend fun adminSuspendUser(userId: Long, reason: String = "") { requestJson("PATCH", "/admin/users/$userId/suspend", JSONObject().put("reason", reason)) }
    suspend fun adminUnsuspendUser(userId: Long) { requestJson("PATCH", "/admin/users/$userId/unsuspend") }

    private suspend fun requestJson(method: String, path: String, body: JSONObject? = null): JSONObject = withContext(Dispatchers.IO) {
        withAuthRetry(path) { token -> executeJson(method, path, body, token) }
    }

    /**
     * Runs [block] with the stored access token and, if the backend answers 401, once more with a
     * refreshed token. Requests rejected with 401 never reached the handler, so the retry is safe.
     */
    private suspend fun <T> withAuthRetry(path: String, block: (token: String?) -> T): T {
        val token = sessionStore.get()?.token
        return try {
            block(token)
        } catch (exception: ApiException) {
            if (exception.status != 401 || token.isNullOrBlank() || path.startsWith("/auth/")) throw exception
            val refreshed = tokenRefresher.refreshAfterRejected(token) ?: throw exception
            block(refreshed.token)
        }
    }

    private fun executeJson(method: String, path: String, body: JSONObject?, token: String?): JSONObject {
        val connection = (URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            token?.takeIf { it.isNotBlank() }?.let { setRequestProperty("Authorization", "Bearer $it") }
            if (body != null) { doOutput = true; setRequestProperty("Content-Type", "application/json") }
        }
        return try {
            body?.let { connection.outputStream.use { stream -> stream.write(it.toString().toByteArray()) } }
            val status = connection.responseCode
            val text = (if (status in 200..299) connection.inputStream else connection.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) throw ApiException(status, parseError(text, status))
            if (text.isBlank()) JSONObject()
            else if (text.trimStart().startsWith("[")) JSONObject().put("items", JSONArray(text))
            else JSONObject(text)
        } finally { connection.disconnect() }
    }

    private fun authSession(json: JSONObject): AuthSession = AuthSession(
        token = json.optStringOr("token", ""),
        refreshToken = json.optStringOrNull("refreshToken"),
        userId = json.optLong("userId").takeIf { it != 0L },
        userName = json.optStringOrNull("userName"),
        email = json.optStringOrNull("email"),
        avatarUrl = json.optStringOrNull("avatarUrl"),
        coverUrl = json.optStringOrNull("coverUrl"),
        role = json.optStringOrNull("role"),
        userSessionId = json.optLong("userSessionId").takeIf { it != 0L },
        riskScore = json.optInt("riskScore").takeIf { json.has("riskScore") },
        approvalExpiresAt = json.optStringOrNull("approvalExpiresAt"),
        loginStatus = json.optStringOr("loginStatus", "SUCCESS"),
        approvalToken = json.optStringOrNull("approvalToken")
    )
    private fun JSONObject.toLoginApprovalStatus() = LoginApprovalStatus(optStringOr("status", "PENDING"), optInt("riskScore", 0), optStringOrNull("expiresAt"), optJSONObject("auth")?.let(::authSession))
    private fun parseError(text: String, status: Int): String = runCatching {
        val json = JSONObject(text)
        // Spring's ApiErrorResponse uses message; older endpoints may return error or errors.
        json.optStringOr("message", "")
            .ifBlank { json.optStringOr("error", "") }
            .ifBlank { json.optJSONArray("errors")?.join(", ") ?: text }
    }.getOrDefault(text).trim().ifBlank { "Request failed ($status)" }
}

private fun JSONArray?.toPosts(): List<ApiPost> = if (this == null) emptyList() else (0 until length()).map { index ->
    getJSONObject(index).let { obj ->
        val engagement = obj.optJSONObject("engagement")
        val mediaJson = obj.optJSONArray("media")
        val media = if (mediaJson == null) emptyList() else (0 until mediaJson.length()).map { mediaIndex ->
            mediaJson.getJSONObject(mediaIndex).let { item ->
                ApiMedia(
                    id = item.optLong("id").takeIf { it != 0L },
                    type = item.optStringOr("type", "IMAGE"),
                    url = item.optStringOrNull("url"),
                    originalFileName = item.optStringOrNull("originalFileName"),
                    uploadId = item.optLong("uploadId").takeIf { it != 0L },
                    sortOrder = item.optInt("sortOrder").takeIf { item.has("sortOrder") }
                )
            }
        }
        ApiPost(
            id = obj.optLong("id").takeIf { it != 0L },
            authorId = obj.optLong("authorId").takeIf { it != 0L },
            authorName = obj.optStringOr("authorName", "Người dùng").ifBlank { "Người dùng" },
            authorAvatarUrl = obj.optStringOrNull("authorAvatarUrl"),
            content = obj.optStringOr("content", ""),
            totalReactions = engagement?.optInt("totalReactions", 0) ?: 0,
            commentCount = engagement?.optInt("commentCount", 0) ?: 0,
            shareCount = obj.optInt("shareCount", 0),
            myReaction = engagement?.optStringOrNull("myReaction"),
            reactionCounts = engagement?.optJSONObject("reactionCounts").toReactionCounts(),
            media = media,
            createdAt = obj.optStringOrNull("createdAt"),
            originalPost = obj.optJSONObject("originalPost")?.toOriginalPost()
        )
    }
}
private fun JSONObject.toOriginalPost(): ApiOriginalPost {
    val post = JSONArray().put(this).toPosts().first()
    return ApiOriginalPost(
        available = optBoolean("available", false),
        id = post.id,
        authorId = post.authorId,
        authorName = post.authorName,
        authorAvatarUrl = post.authorAvatarUrl,
        content = post.content,
        media = post.media,
        createdAt = post.createdAt
    )
}
internal fun parsePostPayload(payload: JSONObject): ApiPost = payload.toPost()
private fun JSONObject?.toReactionCounts(): Map<String, Int> {
    if (this == null) return emptyMap()
    val counts = mutableMapOf<String, Int>()
    keys().forEach { key -> counts[key.uppercase()] = optLong(key).coerceAtLeast(0).coerceAtMost(Int.MAX_VALUE.toLong()).toInt() }
    return counts
}
private fun JSONObject.toPost(): ApiPost = JSONArray().put(this).toPosts().first()
private fun JSONObject.toPostPage(): ApiPostPage {
    val items = optJSONArray("items").toPosts()
    return ApiPostPage(items, optInt("page", 0), optInt("limit", 20), optLong("totalItems", items.size.toLong()), optInt("totalPages", if (items.isEmpty()) 0 else 1))
}
private fun JSONObject.toSavedPostPage(): ApiSavedPostPage {
    val items = optJSONArray("items").toSavedPosts()
    return ApiSavedPostPage(
        items = items,
        page = optInt("page", 0),
        limit = optInt("limit", 20),
        totalItems = optLong("totalElements", items.size.toLong()),
        totalPages = optInt("totalPages", if (items.isEmpty()) 0 else 1)
    )
}
private fun JSONArray?.toSavedPosts(): List<ApiPost> = if (this == null) emptyList() else (0 until length()).mapNotNull { index -> optJSONObject(index)?.optJSONObject("post")?.let { it.toPost() } }
private fun JSONArray?.toNotifications(): List<ApiNotification> = if (this == null) emptyList() else (0 until length()).map { index -> getJSONObject(index).let { obj ->
    ApiNotification(
        id = obj.optLong("id").takeIf { id -> id != 0L }, text = obj.optStringOr("message", "Thông báo mới"),
        read = obj.optBoolean("read", obj.optBoolean("isRead", false)), type = obj.optStringOr("type", ""),
        reactionType = obj.optStringOrNull("reactionType"),
        actorId = obj.optLong("actorId").takeIf { id -> id != 0L }, actorName = obj.optStringOrNull("actorName"),
        actorAvatarUrl = obj.optStringOrNull("actorAvatarUrl"), referenceId = obj.optLong("referenceId").takeIf { id -> id != 0L },
        postId = obj.optLong("postId").takeIf { id -> id != 0L }, commentId = obj.optLong("commentId").takeIf { id -> id != 0L },
        storyId = obj.optLong("storyId").takeIf { id -> id != 0L }, createdAt = obj.optStringOrNull("createdAt")
    )
} }
private fun JSONObject.toNotificationPage(): ApiNotificationPage {
    val items = optJSONArray("items").toNotifications()
    return ApiNotificationPage(
        items = items,
        page = optInt("page", 0),
        limit = optInt("limit", 20),
        totalItems = optLong("totalItems", items.size.toLong()),
        totalPages = optInt("totalPages", if (items.isEmpty()) 0 else 1)
    )
}
private fun JSONArray?.toConversations(): List<ApiConversation> = if (this == null) emptyList() else (0 until length()).map { index -> getJSONObject(index).let { obj ->
    val last = obj.optJSONObject("lastMessage")
    ApiConversation(
        id = obj.optLong("id"),
        name = obj.optStringOr("name", "Cuộc trò chuyện").ifBlank { "Cuộc trò chuyện" },
        avatarUrl = obj.optStringOrNull("avatarUrl"),
        lastMessage = last?.optStringOr("content", "") ?: "",
        unreadCount = obj.optLong("unreadCount", 0L).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt(),
        type = obj.optStringOr("type", "DIRECT"),
        updatedAt = obj.optStringOrNull("updatedAt"),
        lastMessageId = last?.optLong("id")?.takeIf { it != 0L },
        lastMessageSenderName = last?.optStringOrNull("senderName"),
        peerUserId = obj.optLong("peerUserId").takeIf { it != 0L && !obj.isNull("peerUserId") }
    )
} }
private fun JSONObject.toConversationPage(): ApiConversationPage = ApiConversationPage(
    items = optJSONArray("items").toConversations(),
    nextCursor = optStringOrNull("nextCursor"),
    hasMore = optBoolean("hasMore", false)
)
internal fun JSONObject.toConversationDetail(): ApiConversationDetail = ApiConversationDetail(
    id = optLong("id"),
    type = optStringOr("type", "DIRECT"),
    name = optStringOr("name", "Cuộc trò chuyện").ifBlank { "Cuộc trò chuyện" },
    avatarUrl = optStringOrNull("avatarUrl"),
    createdAt = optStringOrNull("createdAt"),
    currentUserRole = optStringOr("currentUserRole", "MEMBER"),
    members = optJSONArray("members")?.let { array ->
        (0 until array.length()).mapNotNull { index ->
            array.optJSONObject(index)?.let { member ->
                ApiConversationMember(
                    userId = member.optLong("userId"),
                    userName = member.optStringOr("userName", "Người dùng"),
                    email = member.optStringOrNull("email"),
                    joinedAt = member.optStringOrNull("joinedAt"),
                    role = member.optStringOr("role", "MEMBER")
                )
            }
        }
    } ?: emptyList()
)
private fun JSONArray?.toMessages(): List<ApiMessage> = if (this == null) emptyList() else (0 until length()).mapNotNull { index -> optJSONObject(index)?.let { obj ->
    val reply = obj.optJSONObject("replyTo")?.let { r -> ApiReplyMessage(r.optLong("messageId").takeIf { it != 0L }, r.optLong("senderId").takeIf { it != 0L }, r.optStringOr("senderName", "Người dùng"), r.optStringOr("content", ""), r.optBoolean("recalled")) }
    val reactions = obj.optJSONArray("reactions")?.let { arr -> (0 until arr.length()).mapNotNull { i -> arr.optJSONObject(i)?.let { item -> ApiMessageReaction(item.optLong("userId").takeIf { it != 0L }, item.optStringOr("userName", "Người dùng"), item.optStringOr("type", "LIKE")) } } } ?: emptyList()
    val attachments = obj.optJSONArray("attachments")?.let { arr -> (0 until arr.length()).mapNotNull { i -> arr.optJSONObject(i)?.let { item -> ApiMessageAttachment(item.optLong("attachmentId").takeIf { it != 0L }, item.optStringOrNull("type"), item.optStringOrNull("url"), item.optStringOrNull("originalFileName"), item.optStringOrNull("contentType"), item.optLong("fileSize").takeIf { it != 0L }) } } } ?: emptyList()
    val story = obj.optJSONObject("storyReference")?.let { item -> ApiStoryMessageReference(item.optLong("storyId").takeIf { it != 0L }, item.optBoolean("available"), item.optLong("authorId").takeIf { it != 0L }, item.optStringOrNull("type"), item.optStringOrNull("mediaUrl"), item.optStringOrNull("text"), item.optStringOrNull("createdAt"), item.optStringOrNull("expiresAt")) }
    ApiMessage(
        id = obj.optLong("id").takeIf { it != 0L }, conversationId = obj.optLong("conversationId").takeIf { it != 0L },
        senderId = obj.optLong("senderId").takeIf { it != 0L }, senderName = obj.optStringOr("senderName", "Người dùng"),
        content = obj.optStringOr("content", ""), messageType = obj.optStringOr("messageType", "TEXT"),
        sentAt = obj.optStringOrNull("sentAt"), editedAt = obj.optStringOrNull("editedAt"),
        recalledAt = obj.optStringOrNull("recalledAt"), seenAt = obj.optStringOrNull("seenAt"),
        clientMessageId = obj.optStringOrNull("clientMessageId"),
        sequenceNumber = obj.optLong("sequenceNumber").takeIf { it != 0L }, replyTo = reply,
        reactions = reactions, attachments = attachments, storyReference = story,
        deliveredAt = obj.optStringOrNull("deliveredAt")
    )
} }
internal fun JSONObject.toApiMessage(): ApiMessage? = JSONArray().put(this).toMessages().firstOrNull()
internal fun JSONObject.toGroupConversationEvent(): GroupConversationEvent? {
    val conversationId = optLong("conversationId").takeIf { it != 0L } ?: return null
    val type = optStringOrNull("type") ?: return null
    val targets = optJSONArray("targetUserIds")?.let { array -> (0 until array.length()).mapNotNull { index -> array.optLong(index).takeIf { it != 0L } } } ?: emptyList()
    return GroupConversationEvent(type, conversationId, optLong("actorUserId").takeIf { it != 0L }, targets, optStringOrNull("name"), optStringOrNull("avatarUrl"))
}
private fun JSONObject.toMessagePage(): ApiMessagePage = ApiMessagePage(optJSONArray("items").toMessages(), optLong("nextBeforeSequence").takeIf { it != 0L }, optBoolean("hasMore", false))
private fun JSONObject.toChatSyncPage(): ApiChatSyncPage = ApiChatSyncPage(optJSONArray("items").toMessages(), optLong("nextAfterMessageId").takeIf { it != 0L }, optBoolean("hasMore", false))
private fun JSONArray?.toFriends(): List<ApiFriend> = if (this == null) emptyList() else (0 until length()).map { index -> getJSONObject(index).let { ApiFriend(it.optLong("userId"), it.optStringOr("userName", ""), it.optStringOrNull("avatarUrl"), it.optStringOrNull("bio")) } }
private fun JSONArray?.toCallHistory(): List<ApiCallHistory> = if (this == null) emptyList() else (0 until length()).mapNotNull { index -> optJSONObject(index)?.let { obj ->
    ApiCallHistory(
        id = obj.optLong("id"), callId = obj.optStringOr("callId", ""), conversationId = obj.optLong("conversationId"),
        otherUserId = obj.optLong("otherUserId"), otherUserName = obj.optStringOr("otherUserName", "Người dùng"),
        otherUserAvatarUrl = obj.optStringOrNull("otherUserAvatarUrl"), callType = obj.optStringOr("callType", "AUDIO"),
        status = obj.optStringOr("status", "FAILED"), createdAt = obj.optStringOrNull("createdAt"),
        answeredAt = obj.optStringOrNull("answeredAt"), endedAt = obj.optStringOrNull("endedAt"),
        durationSeconds = obj.optLong("durationSeconds").takeIf { !obj.isNull("durationSeconds") }, outgoing = obj.optBoolean("outgoing")
    )
} }
private fun JSONArray?.toFriendRequests(): List<ApiFriendRequest> = if (this == null) emptyList() else (0 until length()).map { index -> getJSONObject(index).let { obj -> ApiFriendRequest(obj.optLong("requestId", obj.optLong("id")), obj.optLong("userId", obj.optLong("requesterId")), obj.optStringOr("userName", obj.optStringOr("requesterName", "Nguoi dung")), obj.optStringOrNull("avatarUrl"), obj.optStringOrNull("createdAt")) } }
private fun JSONArray?.toSearchUsers(): List<ApiSearchUser> = if (this == null) emptyList() else (0 until length()).map { index -> getJSONObject(index).let { ApiSearchUser(it.optLong("id"), it.optStringOr("userName", ""), it.optStringOrNull("avatarUrl"), it.optStringOrNull("bio")) } }
private fun JSONArray?.toSecuritySessions(): List<ApiSecuritySession> = if (this == null) emptyList() else (0 until length()).map { index -> getJSONObject(index).let { obj ->
    ApiSecuritySession(
        id = obj.optLong("id"), userDeviceId = obj.optLong("userDeviceId").takeIf { it != 0L },
        deviceName = obj.optStringOrNull("deviceName"), deviceType = obj.optStringOrNull("deviceType"),
        browser = obj.optStringOrNull("browser"), os = obj.optStringOrNull("os"),
        trusted = obj.optBoolean("trusted"), ipAddress = obj.optStringOrNull("ipAddress"),
        createdAt = obj.optStringOrNull("createdAt"), lastActiveAt = obj.optStringOrNull("lastActiveAt"),
        expiresAt = obj.optStringOrNull("expiresAt"), status = obj.optStringOrNull("status"), current = obj.optBoolean("current")
    )
} }
private fun JSONArray?.toPendingApprovals(): List<ApiPendingLoginApproval> = if (this == null) emptyList() else (0 until length()).map { index -> getJSONObject(index).let { obj ->
    ApiPendingLoginApproval(obj.optLong("id"), obj.optLong("userDeviceId").takeIf { it != 0L }, obj.optStringOrNull("deviceName"), obj.optStringOrNull("deviceType"), obj.optStringOrNull("browser"), obj.optStringOrNull("os"), obj.optStringOrNull("ipAddress"), obj.optInt("riskScore", 0), obj.optStringOrNull("createdAt"), obj.optStringOrNull("expiresAt"), obj.optStringOrNull("status"))
} }
private fun JSONArray?.toComments(): List<ApiComment> = if (this == null) emptyList() else (0 until length()).map { index -> getJSONObject(index).let { obj ->
    ApiComment(obj.optLong("id").takeIf { it != 0L }, obj.optLong("postId").takeIf { it != 0L }, obj.optLong("parentCommentId").takeIf { it != 0L }, obj.optLong("authorId").takeIf { it != 0L }, obj.optStringOr("authorName", "Người dùng"), obj.optStringOrNull("authorAvatarUrl"), obj.optStringOr("content", ""), obj.optStringOrNull("createdAt"), obj.optStringOrNull("updatedAt"))
} }
private fun JSONObject.toCommentPage(): ApiCommentPage {
    val items = optJSONArray("items").toComments()
    return ApiCommentPage(
        items = items,
        page = optInt("page", 0),
        limit = optInt("limit", 20),
        totalItems = optLong("totalItems", items.size.toLong()),
        totalPages = optInt("totalPages", if (items.isEmpty()) 0 else 1)
    )
}
private fun JSONObject.toComment(): ApiComment = ApiComment(
    id = optLong("id").takeIf { it != 0L },
    postId = optLong("postId").takeIf { it != 0L },
    parentCommentId = optLong("parentCommentId").takeIf { it != 0L },
    authorId = optLong("authorId").takeIf { it != 0L },
    authorName = optStringOr("authorName", "Người dùng"),
    authorAvatarUrl = optStringOrNull("authorAvatarUrl"),
    content = optStringOr("content", ""),
    createdAt = optStringOrNull("createdAt"),
    updatedAt = optStringOrNull("updatedAt")
)
private fun JSONArray?.toReactions(): List<ApiReaction> = if (this == null) emptyList() else (0 until length()).map { index -> getJSONObject(index).let { obj ->
    ApiReaction(obj.optLong("userId").takeIf { it != 0L }, obj.optStringOr("userName", "Nguoi dung"), obj.optStringOrNull("userAvatarUrl"), obj.optStringOr("type", "LIKE"), obj.optStringOrNull("reactedAt"))
} }
private fun JSONObject.toProfile() = ApiProfile(optLong("id").takeIf { it != 0L }, optStringOr("userName", "Người dùng"), optStringOrNull("email"), optStringOrNull("avatarUrl"), optStringOrNull("coverUrl"), optStringOrNull("bio"), optStringOrNull("dateOfBirth"), optStringOrNull("gender"), optStringOrNull("createdAt"))
private fun JSONObject.toUserSearchPage(): ApiUserSearchPage {
    val items = optJSONArray("items").toSearchUsers()
    return ApiUserSearchPage(
        items = items,
        page = optInt("page", 0),
        limit = optInt("limit", 20),
        totalItems = optLong("totalItems", items.size.toLong()),
        totalPages = optInt("totalPages", if (items.isEmpty()) 0 else 1)
    )
}
private fun JSONArray?.toReports(): List<ApiReport> = if (this == null) emptyList() else (0 until length()).mapNotNull { index ->
    optJSONObject(index)?.let { obj ->
        ApiReport(
            id = obj.optLong("id").takeIf { it != 0L },
            targetType = obj.optStringOr("targetType", "UNKNOWN"),
            targetId = obj.optLong("targetId").takeIf { it != 0L },
            reason = obj.optStringOr("reason", "OTHER"),
            description = obj.optStringOrNull("description"),
            status = obj.optStringOr("status", "PENDING"),
            createdAt = obj.optStringOrNull("createdAt")
        )
    }
}
private fun JSONObject.toReportPage(): ApiReportPage {
    val items = optJSONArray("items").toReports()
    return ApiReportPage(
        items = items,
        page = optInt("page", 0),
        limit = optInt("limit", 20),
        totalItems = optLong("total", items.size.toLong()),
        totalPages = optInt("totalPages", if (items.isEmpty()) 0 else 1)
    )
}
private fun JSONObject.toAdminOverview(): ApiAdminOverview = ApiAdminOverview(
    users = optJSONObject("users").toAdminUserOverview(),
    posts = optJSONObject("posts").toAdminPostOverview(),
    comments = optJSONObject("comments").toAdminCommentOverview(),
    messages = optJSONObject("messages").toAdminMessageOverview(),
    stories = optJSONObject("stories").toAdminStoryOverview(),
    reports = optJSONObject("reports").toAdminReportOverview()
)
private fun JSONObject?.toAdminUserOverview() = ApiAdminUserOverview(this?.optLong("total") ?: 0L, this?.optLong("active") ?: 0L, this?.optLong("suspended") ?: 0L, this?.optLong("newToday") ?: 0L)
private fun JSONObject?.toAdminPostOverview() = ApiAdminPostOverview(this?.optLong("total") ?: 0L, this?.optLong("active") ?: 0L, this?.optLong("deleted") ?: 0L, this?.optLong("newToday") ?: 0L)
private fun JSONObject?.toAdminCommentOverview() = ApiAdminCommentOverview(this?.optLong("total") ?: 0L, this?.optLong("newToday") ?: 0L)
private fun JSONObject?.toAdminMessageOverview() = ApiAdminMessageOverview(this?.optLong("total") ?: 0L, this?.optLong("today") ?: 0L)
private fun JSONObject?.toAdminStoryOverview() = ApiAdminStoryOverview(this?.optLong("total") ?: 0L, this?.optLong("activeNow") ?: 0L, this?.optLong("createdToday") ?: 0L)
private fun JSONObject?.toAdminReportOverview() = ApiAdminReportOverview(this?.optLong("pending") ?: 0L, this?.optLong("reviewing") ?: 0L, this?.optLong("resolved") ?: 0L, this?.optLong("rejected") ?: 0L)
private fun JSONObject.toAdminGrowth(): ApiAdminGrowth = ApiAdminGrowth(
    metric = optStringOr("metric", ""), period = optStringOr("period", ""), from = optStringOrNull("from"), to = optStringOrNull("to"),
    total = optLong("total"), items = (optJSONArray("items") ?: JSONArray()).toAdminDailyCounts()
)
private fun JSONArray.toAdminDailyCounts(): List<ApiAdminDailyCount> = (0 until length()).mapNotNull { optJSONObject(it)?.let { item -> ApiAdminDailyCount(item.optStringOrNull("date"), item.optLong("count")) } }
private fun JSONArray?.toAdminTopPosts(): List<ApiAdminTopPost> = if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it)?.let { item ->
    ApiAdminTopPost(item.optLong("postId").takeIf { it != 0L }, item.optStringOr("authorName", "Người dùng"), item.optStringOr("contentPreview", ""), item.optLong("reactionCount"), item.optLong("commentCount"), item.optLong("shareCount"), item.optLong("engagementScore"), item.optStringOrNull("createdAt"), item.optBoolean("deleted"))
} }
private fun JSONArray?.toAdminActiveUsers(): List<ApiAdminActiveUser> = if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it)?.let { item ->
    ApiAdminActiveUser(item.optLong("userId").takeIf { it != 0L }, item.optStringOr("userName", "Người dùng"), item.optStringOrNull("avatarUrl"), item.optLong("posts"), item.optLong("comments"), item.optLong("messages"), item.optLong("stories"), item.optLong("activityScore"))
} }
private fun JSONObject?.toLongMap(): Map<String, Long> = if (this == null) emptyMap() else buildMap { keys().forEach { key -> put(key, optLong(key)) } }
private fun JSONObject.toAdminReportStats(): ApiAdminReportStats = ApiAdminReportStats(optLong("total"), optJSONObject("byStatus").toLongMap(), optJSONObject("byTargetType").toLongMap(), optJSONObject("byReason").toLongMap())
private fun JSONObject.toAdminStoryStats(): ApiAdminStoryStats = ApiAdminStoryStats(optLong("totalStories"), optLong("activeNow"), optLong("expired"), optLong("deleted"), optLong("createdToday"), optLong("totalViews"), optLong("totalReactions"))
private fun JSONObject.toAdminChatStats(): ApiAdminChatStats = ApiAdminChatStats(optLong("totalConversations"), optLong("directConversations"), optLong("groupConversations"), optLong("totalMessages"), optLong("messagesToday"), optLong("messagesLast7Days"))
private fun JSONObject.toAdminReport(): ApiAdminReport = ApiAdminReport(
    id = optLong("id").takeIf { it != 0L }, reporterId = optLong("reporterId").takeIf { it != 0L }, reporterName = optStringOr("reporterName", "Người dùng"),
    targetType = optStringOr("targetType", "UNKNOWN"), targetId = optLong("targetId").takeIf { it != 0L }, reason = optStringOr("reason", "OTHER"),
    description = optStringOrNull("description"), status = optStringOr("status", "PENDING"), reviewedByName = optStringOrNull("reviewedByName"),
    reviewedAt = optStringOrNull("reviewedAt"), resolutionNote = optStringOrNull("resolutionNote"),
    createdAt = optStringOrNull("createdAt"), updatedAt = optStringOrNull("updatedAt")
)
private fun JSONObject.toAdminReportPage(): ApiAdminReportPage {
    val items = (optJSONArray("items") ?: JSONArray()).let { array -> (0 until array.length()).mapNotNull { array.optJSONObject(it)?.toAdminReport() } }
    return ApiAdminReportPage(items, optInt("page", 0), optInt("limit", 20), optLong("total", items.size.toLong()), optInt("totalPages", if (items.isEmpty()) 0 else 1))
}
private fun JSONObject.toMusicPage(): ApiMusicPage {
    val tracks = (optJSONArray("content") ?: optJSONArray("items")).toMusicTracks()
    val page = optInt("number", 0)
    val totalPages = optInt("totalPages", if (tracks.isEmpty()) 0 else 1)
    return ApiMusicPage(tracks, page, totalPages, page < totalPages - 1)
}
private fun JSONArray?.toMusicTracks(): List<ApiMusicTrack> = if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it)?.toMusicTrack() }
private fun JSONObject.toMusicTrack(): ApiMusicTrack = ApiMusicTrack(
    id = optLong("id"),
    title = optStringOr("title", "Music track"),
    artist = optStringOrNull("artist"),
    audioUrl = optStringOr("audioUrl", ""),
    coverUrl = optStringOrNull("coverUrl"),
    durationMs = optLong("durationMs", 0L),
    active = optBoolean("active", true)
)
private fun JSONObject.putNullable(key: String, value: String?) { if (value == null) put(key, JSONObject.NULL) else put(key, value) }
private fun JSONArray?.toStoryUsers(): List<ApiStoryUser> = if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it)?.let { obj -> ApiStoryUser(obj.optLong("authorId").takeIf { id -> id != 0L }, obj.optStringOr("authorName", "Người dùng"), obj.optStringOrNull("avatarUrl"), obj.optBoolean("hasUnseenStory"), obj.optJSONArray("stories").toStories()) } }
private fun JSONArray?.toStories(): List<ApiStory> = if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it)?.toStoryWithMusic() }
private fun JSONObject.toStoryWithMusic(): ApiStory {
    val story = toStory()
    val track = optJSONObject("music") ?: return story
    return story.copy(music = ApiStoryMusic(
        id = track.optLong("id"),
        title = track.optStringOr("title", "Music track"),
        artist = track.optStringOrNull("artist"),
        audioUrl = track.optStringOr("audioUrl", ""),
        coverUrl = track.optStringOrNull("coverUrl"),
        startMs = track.optLong("startMs", 0L),
        durationMs = track.optLong("durationMs", 0L),
        volume = track.optDouble("volume", 1.0)
    ))
}
private fun JSONObject.toStory(): ApiStory {
    val author = optJSONObject("author")
    val media = optJSONObject("media")?.let { ApiStoryMedia(it.optLong("uploadId").takeIf { id -> id != 0L }, it.optStringOrNull("type"), it.optStringOrNull("url"), it.optStringOrNull("originalFileName")) }
    val overlays = (optJSONArray("textOverlays") ?: JSONArray()).let { array ->
        (0 until array.length()).mapNotNull { index -> array.optJSONObject(index)?.let { item ->
            ApiStoryTextOverlay(item.optStringOr("text", ""), item.optDouble("x", 0.5), item.optDouble("y", 0.5), item.optDouble("fontSize").takeIf { !item.isNull("fontSize") }, item.optStringOrNull("color"), item.optStringOrNull("fontStyle"), item.optDouble("rotation").takeIf { !item.isNull("rotation") })
        } }
    }
    return ApiStory(optLong("id").takeIf { it != 0L }, author?.optLong("id")?.takeIf { it != 0L }, author?.optStringOr("userName", "Người dùng") ?: "Người dùng", author?.optStringOrNull("avatarUrl"), optStringOr("type", "TEXT"), optStringOrNull("privacy"), media, optStringOrNull("text"), optStringOrNull("backgroundColor"), optStringOrNull("textColor"), optBoolean("seen"), optStringOrNull("myReaction"), optLong("reactionCount", 0), optStringOrNull("createdAt"), optStringOrNull("expiresAt"), textOverlays = overlays)
}
private fun JSONArray?.toStoryViewers(): List<ApiStoryViewer> = if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it)?.let { obj -> ApiStoryViewer(obj.optLong("userId").takeIf { id -> id != 0L }, obj.optStringOr("userName", "Người dùng"), obj.optStringOrNull("avatarUrl"), obj.optStringOrNull("viewedAt"), obj.optStringOrNull("reactionType")) } }
