package com.taihoang.socialmobile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max

private val AdminBackground = Color(0xFFF5F7FA)
private val AdminInk = Color(0xFF172033)
private val AdminMuted = Color(0xFF667085)
private val AdminBrand = Color(0xFF1877F2)
private val AdminDanger = Color(0xFFB42318)
private val AdminSuccess = Color(0xFF16803C)
private val AdminWarning = Color(0xFFB54708)

private fun formatAdminNumber(value: Long): String = String.format(Locale("vi", "VN"), "%,d", value)

@Composable
fun AdminScreen(api: ApiClient, onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    var overview by remember { mutableStateOf<ApiAdminOverview?>(null) }
    var reports by remember { mutableStateOf<List<ApiAdminReport>>(emptyList()) }
    var reportsPage by remember { mutableStateOf(0) }
    var reportsTotalPages by remember { mutableStateOf(0) }
    var reportsTotal by remember { mutableStateOf(0L) }
    var statusFilter by remember { mutableStateOf("ALL") }
    var targetFilter by remember { mutableStateOf("ALL") }
    var reasonFilter by remember { mutableStateOf("ALL") }
    var growthMetric by remember { mutableStateOf("USERS") }
    var period by remember { mutableStateOf("SEVEN_DAYS") }
    var growth by remember { mutableStateOf<ApiAdminGrowth?>(null) }
    var topPosts by remember { mutableStateOf<List<ApiAdminTopPost>>(emptyList()) }
    var activeUsers by remember { mutableStateOf<List<ApiAdminActiveUser>>(emptyList()) }
    var reportStats by remember { mutableStateOf<ApiAdminReportStats?>(null) }
    var storyStats by remember { mutableStateOf<ApiAdminStoryStats?>(null) }
    var chatStats by remember { mutableStateOf<ApiAdminChatStats?>(null) }
    // Each tab owns its request state; concurrent loads must not overwrite one another.
    var overviewLoading by remember { mutableStateOf(true) }
    var reportsLoading by remember { mutableStateOf(false) }
    var statsLoading by remember { mutableStateOf(true) }
    var overviewError by remember { mutableStateOf<String?>(null) }
    var reportsError by remember { mutableStateOf<String?>(null) }
    var statsError by remember { mutableStateOf<String?>(null) }
    var actionLoading by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var selectedReport by remember { mutableStateOf<ApiAdminReport?>(null) }
    var selectedReportLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadOverview() {
        scope.launch {
            overviewLoading = true
            overviewError = null
            runCatching { api.adminOverview() }
                .onSuccess { overview = it }
                .onFailure { overviewError = it.message ?: "Không thể tải tổng quan Admin" }
            overviewLoading = false
        }
    }

    fun loadReports(firstPage: Boolean = true) {
        scope.launch {
            if (reportsLoading) return@launch
            reportsLoading = true
            reportsError = null
            val page = if (firstPage) 0 else reportsPage + 1
            runCatching {
                api.adminReports(
                    status = statusFilter.takeUnless { it == "ALL" },
                    targetType = targetFilter.takeUnless { it == "ALL" },
                    reason = reasonFilter.takeUnless { it == "ALL" },
                    page = page
                )
            }.onSuccess { result ->
                reports = if (firstPage) result.items else reports + result.items
                reportsPage = result.page
                reportsTotalPages = result.totalPages
                reportsTotal = result.total
            }.onFailure { reportsError = it.message ?: "Không thể tải danh sách báo cáo" }
            reportsLoading = false
        }
    }

    fun loadStats() {
        scope.launch {
            statsLoading = true
            statsError = null
            runCatching {
                listOf(
                    api.adminGrowth(growthMetric, period),
                    api.adminTopPosts(period),
                    api.adminActiveUsers(period),
                    api.adminReportStats(),
                    api.adminStoryStats(),
                    api.adminChatStats()
                )
            }.onSuccess { values ->
                @Suppress("UNCHECKED_CAST")
                growth = values[0] as ApiAdminGrowth
                @Suppress("UNCHECKED_CAST")
                topPosts = values[1] as List<ApiAdminTopPost>
                @Suppress("UNCHECKED_CAST")
                activeUsers = values[2] as List<ApiAdminActiveUser>
                @Suppress("UNCHECKED_CAST")
                reportStats = values[3] as ApiAdminReportStats
                @Suppress("UNCHECKED_CAST")
                storyStats = values[4] as ApiAdminStoryStats
                @Suppress("UNCHECKED_CAST")
                chatStats = values[5] as ApiAdminChatStats
            }.onFailure { statsError = it.message ?: "Không thể tải thống kê" }
            statsLoading = false
        }
    }

    fun refreshAdminData() {
        // Moderation changes affect the overview and statistics as well as the report list.
        loadOverview()
        loadReports()
        loadStats()
    }

    LaunchedEffect(Unit) {
        loadOverview()
        loadReports()
        loadStats()
    }

    Scaffold(containerColor = AdminBackground) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AdminHeader(onBack, onRefresh = {
                when (selectedTab) {
                    0 -> loadOverview()
                    1 -> loadReports()
                    else -> loadStats()
                }
            })
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp, containerColor = Color.White) {
                listOf("Tổng quan", "Báo cáo", "Thống kê").forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) }, icon = {
                        Icon(when (index) { 0 -> Icons.Filled.Dashboard; 1 -> Icons.Filled.Flag; else -> Icons.Filled.BarChart }, null)
                    })
                }
            }
            HorizontalDivider(color = Color(0xFFE4E7EC))
            val selectedError = when (selectedTab) {
                0 -> overviewError
                1 -> reportsError
                else -> statsError
            }
            selectedError?.let { message ->
                ErrorBanner(message, onRetry = { if (selectedTab == 0) loadOverview() else if (selectedTab == 1) loadReports() else loadStats() })
            }
            actionError?.let { message -> ErrorBanner(message, onRetry = { actionError = null }) }
            when (selectedTab) {
                0 -> AdminOverviewTab(overview, overviewLoading, topPosts, activeUsers)
                1 -> AdminReportsTab(
                    reports = reports,
                    total = reportsTotal,
                    page = reportsPage,
                    totalPages = reportsTotalPages,
                    loading = reportsLoading,
                    statusFilter = statusFilter,
                    targetFilter = targetFilter,
                    reasonFilter = reasonFilter,
                    onStatusChange = { statusFilter = it },
                    onTargetChange = { targetFilter = it },
                    onReasonChange = { reasonFilter = it },
                    onApply = { loadReports() },
                    onLoadMore = { loadReports(false) },
                    onSelect = { report ->
                        selectedReport = report
                        val reportId = report.id
                        if (reportId != null) {
                            selectedReportLoading = true
                            scope.launch {
                                runCatching { api.adminReport(reportId) }
                                    .onSuccess { detail ->
                                        if (selectedReport?.id == reportId) selectedReport = detail
                                    }
                                    .onFailure {
                                        if (selectedReport?.id == reportId) {
                                            actionError = it.message ?: "Không thể tải chi tiết báo cáo"
                                        }
                                    }
                                if (selectedReport?.id == reportId) selectedReportLoading = false
                            }
                        } else selectedReportLoading = false
                    }
                )
                else -> AdminStatsTab(
                    period = period,
                    metric = growthMetric,
                    growth = growth,
                    topPosts = topPosts,
                    activeUsers = activeUsers,
                    reportStats = reportStats,
                    storyStats = storyStats,
                    chatStats = chatStats,
                    loading = statsLoading,
                    onPeriodChange = { period = it },
                    onMetricChange = { growthMetric = it },
                    onApply = { loadStats() }
                )
            }
        }
    }

    selectedReport?.let { report ->
        AdminReportDialog(
            report = report,
            loading = selectedReportLoading,
            busy = actionLoading,
            onDismiss = { selectedReport = null; selectedReportLoading = false },
            onAction = { action, note ->
                scope.launch {
                    if (actionLoading) return@launch
                    actionLoading = true
                    actionError = null
                    runCatching {
                        when (action) {
                            "REVIEW" -> api.adminReviewReport(requireNotNull(report.id) { "Báo cáo không hợp lệ" })
                            "RESOLVE" -> api.adminResolveReport(requireNotNull(report.id) { "Báo cáo không hợp lệ" }, note)
                            "REJECT" -> api.adminRejectReport(requireNotNull(report.id) { "Báo cáo không hợp lệ" }, note)
                            "DELETE_POST" -> { api.adminRemovePost(requireNotNull(report.targetId) { "Bài viết không hợp lệ" }); api.adminResolveReport(requireNotNull(report.id) { "Báo cáo không hợp lệ" }, "Đã gỡ bài viết vi phạm.") }
                            "DELETE_COMMENT" -> { api.adminRemoveComment(requireNotNull(report.targetId) { "Bình luận không hợp lệ" }); api.adminResolveReport(requireNotNull(report.id) { "Báo cáo không hợp lệ" }, "Đã gỡ bình luận vi phạm.") }
                            "SUSPEND_USER" -> { api.adminSuspendUser(requireNotNull(report.targetId) { "Người dùng không hợp lệ" }, note); api.adminResolveReport(requireNotNull(report.id) { "Báo cáo không hợp lệ" }, "Đã khóa tài khoản bị báo cáo.") }
                            "UNSUSPEND_USER" -> api.adminUnsuspendUser(requireNotNull(report.targetId) { "Người dùng không hợp lệ" })
                            else -> report
                        }
                    }.onSuccess {
                        selectedReport = null
                        selectedReportLoading = false
                        refreshAdminData()
                    }.onFailure { actionError = it.message ?: "Không thể thực hiện thao tác" }
                    actionLoading = false
                }
            }
        )
    }
}

@Composable
private fun AdminHeader(onBack: () -> Unit, onRefresh: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Quay lại", tint = AdminInk) }
        Column(Modifier.weight(1f)) {
            Text("Khu vực quản trị", color = AdminInk, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Dữ liệu trực tiếp từ backend Admin", color = AdminMuted, fontSize = 12.sp)
        }
        IconButton(onClick = onRefresh) { Icon(Icons.Filled.Refresh, "Tải lại", tint = AdminBrand) }
    }
}

@Composable
private fun AdminOverviewTab(
    overview: ApiAdminOverview?,
    loading: Boolean,
    topPosts: List<ApiAdminTopPost>,
    activeUsers: List<ApiAdminActiveUser>
) {
    if (loading && overview == null) return LoadingState("Đang tải tổng quan...")
    if (overview == null) return EmptyState("Chưa có dữ liệu tổng quan")
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Sức khỏe hệ thống", color = AdminInk, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminMetricCard("Người dùng", overview.users.total, Icons.Filled.People, AdminBrand, Modifier.weight(1f))
                AdminMetricCard("Bài viết", overview.posts.total, Icons.Filled.Article, Color(0xFF7C3AED), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminMetricCard("Tin nhắn", overview.messages.total, Icons.Filled.Chat, Color(0xFF16803C), Modifier.weight(1f))
                AdminMetricCard("Báo cáo chờ", overview.reports.pending, Icons.Filled.Report, AdminWarning, Modifier.weight(1f))
            }
        }
        item { AdminSectionTitle("Người dùng") }
        item { AdminDetailCard(listOf("Đang hoạt động" to overview.users.active, "Bị khóa" to overview.users.suspended, "Mới hôm nay" to overview.users.newToday)) }
        item { AdminSectionTitle("Nội dung và tương tác") }
        item { AdminDetailCard(listOf("Bài đang hiển thị" to overview.posts.active, "Bài đã gỡ" to overview.posts.deleted, "Bài mới hôm nay" to overview.posts.newToday, "Bình luận" to overview.comments.total)) }
        item { AdminSectionTitle("Báo cáo") }
        item { AdminDetailCard(listOf("Đang chờ" to overview.reports.pending, "Đang xem xét" to overview.reports.reviewing, "Đã giải quyết" to overview.reports.resolved, "Từ chối" to overview.reports.rejected)) }
        item { AdminSectionTitle("Bài viết nổi bật · 7 ngày") }
        if (topPosts.isEmpty()) item { EmptyState("Chưa có dữ liệu bài viết nổi bật") }
        items(topPosts.take(5), key = { it.postId ?: it.hashCode().toLong() }) { post ->
            AdminRankRow(
                title = "${post.authorName} · ${post.contentPreview.ifBlank { "Không có nội dung" }}",
                detail = "${post.reactionCount} cảm xúc · ${post.commentCount} bình luận · ${post.shareCount} chia sẻ",
                score = post.engagementScore
            )
        }
        item { AdminSectionTitle("Người dùng hoạt động · 7 ngày") }
        if (activeUsers.isEmpty()) item { EmptyState("Chưa có dữ liệu người dùng hoạt động") }
        items(activeUsers.take(5), key = { it.userId ?: it.hashCode().toLong() }) { user ->
            AdminRankRow(
                title = user.userName,
                detail = "${user.posts} bài · ${user.comments} bình luận · ${user.messages} tin nhắn",
                score = user.activityScore
            )
        }
    }
}

@Composable
private fun AdminReportsTab(
    reports: List<ApiAdminReport>, total: Long, page: Int, totalPages: Int, loading: Boolean,
    statusFilter: String, targetFilter: String, reasonFilter: String,
    onStatusChange: (String) -> Unit, onTargetChange: (String) -> Unit, onReasonChange: (String) -> Unit,
    onApply: () -> Unit, onLoadMore: () -> Unit, onSelect: (ApiAdminReport) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Kiểm duyệt báo cáo", color = AdminInk, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        item { Text("$total báo cáo", color = AdminMuted, fontSize = 13.sp) }
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminFilterMenu("Trạng thái", statusFilter, listOf("ALL", "PENDING", "REVIEWING", "RESOLVED", "REJECTED"), onStatusChange)
                AdminFilterMenu("Đối tượng", targetFilter, listOf("ALL", "POST", "COMMENT", "USER"), onTargetChange)
                AdminFilterMenu("Lý do", reasonFilter, listOf("ALL", "SPAM", "HARASSMENT", "HATE_SPEECH", "VIOLENCE", "SEXUAL_CONTENT", "SCAM", "FALSE_INFORMATION", "OTHER"), onReasonChange)
            }
        }
        item { Button(onClick = onApply, enabled = !loading, colors = ButtonDefaults.buttonColors(AdminBrand), shape = RoundedCornerShape(10.dp)) { Text("Áp dụng bộ lọc") } }
        if (loading && reports.isEmpty()) item { LoadingState("Đang tải báo cáo...") }
        else if (!loading && reports.isEmpty()) item { EmptyState("Không có báo cáo phù hợp") }
        items(reports, key = { it.id ?: it.hashCode().toLong() }) { report -> AdminReportRow(report, onClick = { onSelect(report) }) }
        if (page + 1 < totalPages) item { OutlinedButton(onClick = onLoadMore, enabled = !loading, modifier = Modifier.fillMaxWidth()) { Text(if (loading) "Đang tải..." else "Tải thêm báo cáo") } }
    }
}

@Composable
private fun AdminReportRow(report: ApiAdminReport, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(report.status)
                Spacer(Modifier.width(8.dp))
                Text("#${report.id ?: "-"} · ${adminTargetLabel(report.targetType)}", color = AdminInk, fontWeight = FontWeight.Bold)
            }
            Text(report.description?.ifBlank { null } ?: "Không có mô tả bổ sung", color = AdminInk, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("${adminReasonLabel(report.reason)} · ${report.reporterName} · ${report.createdAt ?: ""}", color = AdminMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AdminStatsTab(
    period: String, metric: String, growth: ApiAdminGrowth?, topPosts: List<ApiAdminTopPost>, activeUsers: List<ApiAdminActiveUser>,
    reportStats: ApiAdminReportStats?, storyStats: ApiAdminStoryStats?, chatStats: ApiAdminChatStats?, loading: Boolean,
    onPeriodChange: (String) -> Unit, onMetricChange: (String) -> Unit, onApply: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Thống kê", color = AdminInk, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminFilterMenu("Khoảng thời gian", period, listOf("SEVEN_DAYS", "THIRTY_DAYS", "NINETY_DAYS"), onPeriodChange)
                AdminFilterMenu("Chỉ số", metric, listOf("USERS", "POSTS", "COMMENTS", "MESSAGES", "STORIES", "REPORTS"), onMetricChange)
                Button(onClick = onApply, enabled = !loading, colors = ButtonDefaults.buttonColors(AdminBrand), shape = RoundedCornerShape(10.dp)) { Text("Cập nhật") }
            }
        }
        if (loading && growth == null) item { LoadingState("Đang tải thống kê...") }
        growth?.let { data ->
            item { AdminGrowthCard(data) }
        }
        item { AdminSectionTitle("Bài viết nổi bật") }
        if (topPosts.isEmpty()) item { EmptyState("Chưa có dữ liệu bài viết") }
        items(topPosts, key = { it.postId ?: it.hashCode().toLong() }) { post ->
            AdminRankRow("${post.authorName} · ${post.contentPreview}", "${post.reactionCount} cảm xúc · ${post.commentCount} bình luận · ${post.shareCount} chia sẻ", post.engagementScore)
        }
        item { AdminSectionTitle("Người dùng hoạt động") }
        if (activeUsers.isEmpty()) item { EmptyState("Chưa có dữ liệu người dùng") }
        items(activeUsers, key = { it.userId ?: it.hashCode().toLong() }) { user ->
            AdminRankRow(user.userName, "${user.posts} bài · ${user.comments} bình luận · ${user.messages} tin nhắn", user.activityScore)
        }
        reportStats?.let { stats ->
            item { AdminSectionTitle("Tổng hợp báo cáo") }
            item { AdminDetailCard(listOf("Tổng" to stats.total, "Đang chờ" to (stats.byStatus["PENDING"] ?: 0L), "Đã giải quyết" to (stats.byStatus["RESOLVED"] ?: 0L), "Từ chối" to (stats.byStatus["REJECTED"] ?: 0L))) }
        }
        storyStats?.let { stats ->
            item { AdminSectionTitle("Story") }
            item { AdminDetailCard(listOf("Tổng story" to stats.totalStories, "Đang hoạt động" to stats.activeNow, "Lượt xem" to stats.totalViews, "Cảm xúc" to stats.totalReactions)) }
        }
        chatStats?.let { stats ->
            item { AdminSectionTitle("Chat") }
            item { AdminDetailCard(listOf("Cuộc trò chuyện" to stats.totalConversations, "Tin nhắn" to stats.totalMessages, "Hôm nay" to stats.messagesToday, "7 ngày" to stats.messagesLast7Days)) }
        }
    }
}

@Composable
private fun AdminGrowthCard(data: ApiAdminGrowth) {
    val maxValue = max(1L, data.items.maxOfOrNull { it.count } ?: 1L)
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tăng trưởng ${data.metric}", color = AdminInk, fontWeight = FontWeight.Bold)
                Text("Tổng ${data.total}", color = AdminBrand, fontWeight = FontWeight.Bold)
            }
            data.items.takeLast(10).forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.date ?: "-", color = AdminMuted, fontSize = 11.sp, modifier = Modifier.width(78.dp))
                    LinearProgressIndicator(progress = { item.count.toFloat() / maxValue.toFloat() }, modifier = Modifier.weight(1f), color = AdminBrand, trackColor = Color(0xFFE4E7EC))
                    Text(item.count.toString(), color = AdminInk, fontSize = 11.sp, modifier = Modifier.width(38.dp).padding(start = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun AdminFilterMenu(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 10.dp)) {
            Text("$label: ${value.replace('_', ' ')}", fontSize = 12.sp, maxLines = 1)
            Icon(Icons.Filled.ExpandMore, null, modifier = Modifier.size(17.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option.replace('_', ' ')) }, onClick = { onSelect(option); expanded = false }) }
        }
    }
}

@Composable
private fun AdminReportDialog(
    report: ApiAdminReport,
    loading: Boolean,
    busy: Boolean,
    onDismiss: () -> Unit,
    onAction: (String, String) -> Unit
) {
    var note by remember(report.id) { mutableStateOf(report.resolutionNote.orEmpty()) }
    var confirmAction by remember { mutableStateOf<String?>(null) }
    val canModerate = !busy && report.status in setOf("PENDING", "REVIEWING")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Báo cáo #${report.id ?: "-"}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (loading) {
                    LoadingState("Đang tải chi tiết báo cáo...")
                }
                Row(verticalAlignment = Alignment.CenterVertically) { StatusPill(report.status); Spacer(Modifier.width(8.dp)); Text("${adminTargetLabel(report.targetType)} · ${adminReasonLabel(report.reason)}", color = AdminMuted, fontSize = 12.sp) }
                Text("Người báo cáo: ${report.reporterName}", color = AdminInk, fontSize = 13.sp)
                report.targetId?.let { Text("Đối tượng #$it", color = AdminMuted, fontSize = 12.sp) }
                Text(report.description?.ifBlank { null } ?: "Không có mô tả", color = AdminInk)
                OutlinedTextField(value = note, onValueChange = { note = it.take(2000) }, label = { Text("Ghi chú xử lý") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                if (report.status == "PENDING") TextButton(enabled = canModerate, onClick = { onAction("REVIEW", note) }) { Text("Bắt đầu xem xét", color = AdminBrand) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(enabled = canModerate, onClick = { confirmAction = "RESOLVE" }, colors = ButtonDefaults.buttonColors(AdminSuccess), shape = RoundedCornerShape(9.dp), modifier = Modifier.weight(1f)) { Text("Giải quyết") }
                    OutlinedButton(enabled = canModerate, onClick = { confirmAction = "REJECT" }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(9.dp)) { Text("Từ chối", color = AdminDanger) }
                }
                when (report.targetType) {
                    "POST" -> OutlinedButton(enabled = canModerate && report.targetId != null, onClick = { confirmAction = "DELETE_POST" }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Filled.Delete, null); Spacer(Modifier.width(5.dp)); Text("Gỡ bài viết vi phạm") }
                    "COMMENT" -> OutlinedButton(enabled = canModerate && report.targetId != null, onClick = { confirmAction = "DELETE_COMMENT" }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Filled.Comment, null); Spacer(Modifier.width(5.dp)); Text("Gỡ bình luận vi phạm") }
                    "USER" -> OutlinedButton(enabled = canModerate && report.targetId != null, onClick = { confirmAction = "SUSPEND_USER" }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Filled.PersonOff, null); Spacer(Modifier.width(5.dp)); Text("Khóa tài khoản") }
                }
                if (report.targetType == "USER") {
                    OutlinedButton(enabled = !busy && report.targetId != null, onClick = { confirmAction = "UNSUSPEND_USER" }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.CheckCircle, null)
                        Spacer(Modifier.width(5.dp))
                        Text("Khôi phục tài khoản")
                    }
                }
            }
        },
        confirmButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text("Đóng") } }
    )
    confirmAction?.let { action ->
        AlertDialog(onDismissRequest = { if (!busy) confirmAction = null }, title = { Text("Xác nhận thao tác") }, text = { Text("Thao tác này sẽ cập nhật dữ liệu thật trên backend.") }, confirmButton = { TextButton(enabled = !busy, onClick = { confirmAction = null; onAction(action, note) }) { Text("Xác nhận", color = AdminDanger) } }, dismissButton = { TextButton(enabled = !busy, onClick = { confirmAction = null }) { Text("Hủy") } })
    }
}

@Composable
private fun StatusPill(status: String) {
    val color = when (status) { "RESOLVED" -> AdminSuccess; "REJECTED" -> AdminDanger; "REVIEWING" -> AdminWarning; else -> AdminBrand }
    Surface(color = color.copy(alpha = .12f), shape = RoundedCornerShape(6.dp)) { Text(adminStatusLabel(status), color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
}

private fun adminStatusLabel(status: String): String = when (status) {
    "PENDING" -> "Đang chờ"
    "REVIEWING" -> "Đang xem xét"
    "RESOLVED" -> "Đã xử lý"
    "REJECTED" -> "Từ chối"
    else -> status
}

private fun adminTargetLabel(target: String): String = when (target) {
    "POST" -> "Bài viết"
    "COMMENT" -> "Bình luận"
    "USER" -> "Người dùng"
    else -> target
}

private fun adminReasonLabel(reason: String): String = when (reason) {
    "SPAM" -> "Spam"
    "HARASSMENT" -> "Quấy rối"
    "HATE_SPEECH" -> "Ngôn từ thù ghét"
    "VIOLENCE" -> "Bạo lực"
    "SEXUAL_CONTENT" -> "Nội dung nhạy cảm"
    "SCAM" -> "Lừa đảo"
    "FALSE_INFORMATION" -> "Thông tin sai"
    "OTHER" -> "Khác"
    else -> reason
}

@Composable
private fun AdminMetricCard(title: String, value: Long, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(14.dp)) { Column(Modifier.padding(14.dp)) { Icon(icon, null, tint = color, modifier = Modifier.size(24.dp)); Spacer(Modifier.height(10.dp)); Text(formatAdminNumber(value), color = AdminInk, fontSize = 23.sp, fontWeight = FontWeight.Bold); Text(title, color = AdminMuted, fontSize = 12.sp) } }
}

@Composable
private fun AdminDetailCard(values: List<Pair<String, Long>>) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) { values.forEachIndexed { index, (label, value) -> Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = AdminMuted, fontSize = 13.sp); Text(formatAdminNumber(value), color = AdminInk, fontWeight = FontWeight.Bold) }; if (index < values.lastIndex) HorizontalDivider(color = Color(0xFFE4E7EC)) } }
    }
}

@Composable
private fun AdminRankRow(title: String, detail: String, score: Long) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(12.dp)) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFE7F3FF)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.TrendingUp, null, tint = AdminBrand, modifier = Modifier.size(20.dp)) }; Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(title, color = AdminInk, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis); Text(detail, color = AdminMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }; Text(formatAdminNumber(score), color = AdminBrand, fontWeight = FontWeight.Bold) } }
}

@Composable
private fun AdminSectionTitle(title: String) { Text(title, color = AdminInk, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp)) }

@Composable
private fun LoadingState(message: String) { Column(Modifier.fillMaxWidth().padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(color = AdminBrand, modifier = Modifier.size(30.dp)); Spacer(Modifier.height(10.dp)); Text(message, color = AdminMuted, fontSize = 13.sp) } }

@Composable
private fun EmptyState(message: String) { Text(message, color = AdminMuted, modifier = Modifier.fillMaxWidth().padding(22.dp)) }

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) { Row(Modifier.fillMaxWidth().background(Color(0xFFFFF1F0)).padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Close, null, tint = AdminDanger, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(message, color = AdminDanger, fontSize = 12.sp, modifier = Modifier.weight(1f)); TextButton(onClick = onRetry) { Text("Thử lại", color = AdminDanger) } } }
