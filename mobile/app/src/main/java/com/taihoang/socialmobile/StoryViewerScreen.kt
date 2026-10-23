package com.taihoang.socialmobile

import android.app.Activity
import android.graphics.Color as AndroidColor
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.OffsetDateTime
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StoryViewerScreen(
    selection: StoryViewerSelection,
    api: ApiClient,
    onClose: () -> Unit
) {
    val users = remember(selection) { selection.users.filter { it.stories.isNotEmpty() } }
    if (users.isEmpty()) {
        LaunchedEffect(Unit) { onClose() }
        return
    }

    var position by remember(selection) {
        mutableStateOf(
            StoryViewerPosition(
                selection.userIndex.coerceIn(0, users.lastIndex),
                0
            )
        )
    }
    val currentUser = users[position.userIndex]
    val story = currentUser.stories[position.storyIndex.coerceIn(0, currentUser.stories.lastIndex)]
    val storyKey = story.id?.toString() ?: "${position.userIndex}:${position.storyIndex}:${story.createdAt}"
    var pausedByTouch by remember { mutableStateOf(false) }
    var inputFocused by remember { mutableStateOf(false) }
    var dragging by remember { mutableStateOf(false) }
    var progressByStory by remember(selection) { mutableStateOf<Map<String, Float>>(emptyMap()) }
    var completedStories by remember(selection) { mutableStateOf<Set<String>>(emptySet()) }
    var dragOffset by remember { mutableStateOf(0f) }
    var dismissed by remember { mutableStateOf(false) }
    var viewers by remember { mutableStateOf<List<ApiStoryViewer>?>(null) }
    var showViewers by remember { mutableStateOf(false) }
    var reaction by remember(storyKey) { mutableStateOf(story.myReaction) }
    var reply by remember(storyKey) { mutableStateOf("") }
    var error by remember(storyKey) { mutableStateOf<String?>(null) }
    var showReactions by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val touchPaused = pausedByTouch || inputFocused || dragging || dismissed
    val currentProgress = progressByStory[storyKey] ?: 0f
    val activity = LocalContext.current as? Activity
    val latestClose by rememberUpdatedState(onClose)

    DisposableEffect(Unit) {
        val window = activity?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = AndroidColor.TRANSPARENT
            window.navigationBarColor = AndroidColor.TRANSPARENT
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = false
        }
        onDispose {
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                window.statusBarColor = AndroidColor.BLACK
                window.navigationBarColor = AndroidColor.BLACK
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = true
            }
        }
    }

    fun finishWithAnimation() {
        if (dismissed) return
        dismissed = true
        scope.launch {
            dragOffset = 900f
            delay(180)
            latestClose()
        }
    }

    fun goNext() {
        completedStories = completedStories + storyKey
        progressByStory = progressByStory + (storyKey to 1f)
        nextStoryPosition(users, position)?.let { position = it } ?: finishWithAnimation()
    }

    fun goPrevious() {
        previousStoryPosition(users, position)?.let { position = it }
    }

    LaunchedEffect(storyKey) {
        story.id?.let { id -> runCatching { api.viewStory(id) } }
        progressByStory = progressByStory + (storyKey to 0f)
    }

    LaunchedEffect(storyKey, touchPaused) {
        if (touchPaused) return@LaunchedEffect
        var previousFrame = 0L
        while (!dismissed) {
            withFrameNanos { now ->
                if (previousFrame == 0L) previousFrame = now
                val delta = (now - previousFrame).coerceAtMost(100_000_000L)
                previousFrame = now
                val next = ((progressByStory[storyKey] ?: 0f) + delta / 5_000_000_000f).coerceAtMost(1f)
                progressByStory = progressByStory + (storyKey to next)
            }
            if ((progressByStory[storyKey] ?: 0f) >= 1f) {
                completedStories = completedStories + storyKey
                goNext()
                break
            }
        }
    }

    LaunchedEffect(showReactions) {
        if (showReactions) {
            delay(1_400)
            showReactions = false
        }
    }

    BackHandler(onBack = ::finishWithAnimation)
    StoryMusicController(story, touchPaused)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .offset { IntOffset(0, dragOffset.roundToInt()) }
    ) {
        StoryViewerContent(story, !touchPaused)

        Box(
            Modifier
                .fillMaxSize()
                .padding(top = 82.dp, bottom = 104.dp)
                .pointerInput(storyKey) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var totalX = 0f
                        var totalY = 0f
                        var moved = false
                        pausedByTouch = true
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            val delta = change.positionChange()
                            totalX += delta.x
                            totalY += delta.y
                            if (abs(totalX) > 18f || abs(totalY) > 18f) moved = true
                            if (moved && totalY > 0f) {
                                dragging = true
                                dragOffset = totalY.coerceAtLeast(0f)
                            }
                            if (!change.pressed) break
                        }
                        pausedByTouch = false
                        val isSwipeDown = totalY > 120f && abs(totalY) > abs(totalX)
                        dragging = false
                        if (isSwipeDown) finishWithAnimation()
                        else if (!moved) {
                            if (down.position.x < size.width / 3f) goPrevious() else goNext()
                        } else {
                            dragOffset = 0f
                        }
                    }
                }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .72f), Color.Transparent)))
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .align(Alignment.TopCenter)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    currentUser.stories.forEachIndexed { index, item ->
                        val itemKey = item.id?.toString() ?: "$index:${item.createdAt}"
                        val value = when {
                            index < position.storyIndex || itemKey in completedStories -> 1f
                            index > position.storyIndex -> .4f
                            else -> currentProgress
                        }
                        Box(
                            Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(Color.White.copy(alpha = .22f), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(value.coerceIn(0f, 1f))
                                    .height(3.dp)
                                    .background(Color.White.copy(alpha = if (index > position.storyIndex) .4f else 1f), RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RemoteAvatar(currentUser.authorName, currentUser.avatarUrl, Color(0xFF30343B), 36.dp)
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text(currentUser.authorName, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(storyRelativeTime(story.createdAt), color = Color.White.copy(alpha = .72f), fontSize = 12.sp)
                    }
                    IconButton(onClick = {}) { Icon(Icons.Filled.MoreHoriz, "Tùy chọn", tint = Color.White) }
                    IconButton(onClick = ::finishWithAnimation) { Icon(Icons.Filled.Close, "Đóng", tint = Color.White) }
                }
            }
        }

        AnimatedVisibility(
            visible = showReactions,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 116.dp),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
        ) {
            Row(
                Modifier
                    .background(Color.Black.copy(alpha = .56f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                listOf("LIKE" to "👍", "LOVE" to "❤️", "HAHA" to "😂", "WOW" to "😮", "SAD" to "😢", "ANGRY" to "😡").forEach { (type, emoji) ->
                    Text(emoji, fontSize = 24.sp, modifier = Modifier.clickable {
                        story.id?.let { id ->
                            scope.launch {
                                val previous = reaction
                                runCatching {
                                    if (previous == type) api.removeStoryReaction(id) else api.reactStory(id, type)
                                }.onSuccess { reaction = if (previous == type) null else type }
                                    .onFailure { error = it.message }
                            }
                        }
                        showReactions = false
                    })
                }
            }
        }

        if (currentUser.authorId != null && currentUser.authorId == api.currentUserId()) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 14.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, Color.White.copy(alpha = .72f), RoundedCornerShape(28.dp))
                    .clickable {
                        showViewers = true
                        story.id?.let { id -> scope.launch { runCatching { api.storyViewers(id) }.onSuccess { viewers = it }.onFailure { error = it.message } } }
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Text("Người xem (${viewers?.size ?: 0})", color = Color.White, modifier = Modifier.padding(horizontal = 18.dp), fontWeight = FontWeight.SemiBold)
            }
        } else {
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 12.dp, vertical = 14.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BasicTextField(
                    value = reply,
                    onValueChange = { reply = it.take(5000) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(1.dp, Color.White.copy(alpha = .72f), RoundedCornerShape(28.dp))
                        .onFocusChanged { inputFocused = it.isFocused },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Send),
                    decorationBox = { field ->
                        Row(Modifier.padding(start = 18.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f)) {
                                if (reply.isBlank()) Text("Gửi tin nhắn...", color = Color.White.copy(alpha = .72f), fontSize = 15.sp)
                                field()
                            }
                            if (inputFocused && reply.isNotBlank()) {
                                IconButton(onClick = {
                                    val id = story.id ?: return@IconButton
                                    scope.launch {
                                        runCatching { api.replyStory(id, reply.trim()) }
                                            .onSuccess { reply = ""; keyboard?.hide(); inputFocused = false }
                                            .onFailure { error = it.message }
                                    }
                                }) { Icon(Icons.Filled.Send, "Gửi", tint = Color.White) }
                            }
                        }
                    }
                )
                IconButton(
                    onClick = { showReactions = true },
                    modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha = .5f), CircleShape)
                ) {
                    Icon(if (reaction == "LIKE" || reaction == "LOVE") Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, "Cảm xúc", tint = if (reaction != null) Color(0xFFFF5C73) else Color.White)
                }
            }
        }

        error?.let { message ->
            Text(message, color = Color(0xFFFFB4AB), fontSize = 12.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 74.dp))
        }
    }

    if (showViewers) {
        ModalBottomSheet(
            onDismissRequest = { showViewers = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                Text("Người xem", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                val list = viewers.orEmpty()
                if (list.isEmpty()) Text("Chưa có người xem", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                list.forEach { viewer ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        RemoteAvatar(viewer.userName, viewer.avatarUrl, Color(0xFFE8EDF2), 38.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(viewer.userName, fontWeight = FontWeight.SemiBold)
                        viewer.reactionType?.let { Text("  $it", color = Color.Gray, fontSize = 12.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryViewerContent(story: ApiStory, active: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val media = story.media?.url
        if (!media.isNullOrBlank()) {
            Box(Modifier.fillMaxSize().padding(vertical = 80.dp).clip(RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                if (story.media?.type?.contains("VIDEO", true) == true) {
                    RemotePostVideo(media, Modifier.fillMaxWidth(), videoId = "story-viewer:$media", active = active)
                } else {
                    RemoteImage(media, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                }
                story.textOverlays.forEach { overlay ->
                    Text(
                        overlay.text,
                        color = runCatching { Color(AndroidColor.parseColor(overlay.color ?: "#FFFFFF")) }.getOrDefault(Color.White),
                        fontSize = (overlay.fontSize ?: 22.0).coerceIn(10.0, 64.0).sp,
                        fontWeight = if (overlay.fontStyle.equals("BOLD", true)) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.align(Alignment.Center).background(Color.Black.copy(alpha = .2f)).padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            val first = runCatching { Color(AndroidColor.parseColor(story.backgroundColor ?: "#E94B77")) }.getOrDefault(Color(0xFFE94B77))
            val second = Color(0xFFFF8A3D)
            Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(first, second))), contentAlignment = Alignment.Center) {
                Text(story.text.orEmpty(), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 30.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

@Composable
private fun StoryMusicController(story: ApiStory, paused: Boolean) {
    val music = story.music ?: return
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    val latestPaused by rememberUpdatedState(paused)
    var prepared by remember(story.id) { mutableStateOf(false) }
    var pauseForViewer by remember(story.id) { mutableStateOf(false) }
    var pauseMusic by remember(story.id) { mutableStateOf<(() -> Unit)?>(null) }
    var resumeMusic by remember(story.id) { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(story.id, paused) {
        if (paused) pauseMusic?.invoke() else resumeMusic?.invoke()
    }

    DisposableEffect(story.id, music.audioUrl, lifecycleOwner) {
        val handler = Handler(Looper.getMainLooper())
        val state = StoryMusicPlaybackState()
        var player: MediaPlayer? = null
        var disposed = false
        var loopTask: Runnable? = null

        fun scheduleLoop() {
            loopTask?.let(handler::removeCallbacks)
            val current = player ?: return
            val task = object : Runnable {
                override fun run() {
                    if (disposed) return
                    runCatching {
                        if (!current.isPlaying) return@runCatching
                        current.pause()
                        current.seekTo(music.startMs.coerceAtLeast(0L).coerceAtMost((current.duration - 1).coerceAtLeast(0).toLong()).toInt())
                        current.start()
                        handler.postDelayed(this, music.durationMs.coerceAtLeast(1000L))
                    }
                }
            }
            loopTask = task
            handler.postDelayed(task, music.durationMs.coerceAtLeast(1000L))
        }

        fun start() {
            if (!latestPaused && prepared) runCatching { player?.start(); pauseForViewer = false; scheduleLoop() }
        }

        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                    val playing = runCatching { player?.isPlaying == true }.getOrDefault(false)
                    state.onStop(playing)
                    if (playing) runCatching { player?.pause() }
                    loopTask?.let(handler::removeCallbacks)
                }
                androidx.lifecycle.Lifecycle.Event.ON_START -> if (state.onStart()) start()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        player = runCatching {
            MediaPlayer().apply {
                setDataSource(music.audioUrl)
                setVolume(music.volume.toFloat().coerceIn(0f, 1f), music.volume.toFloat().coerceIn(0f, 1f))
                setOnPreparedListener { current ->
                    prepared = true
                    current.seekTo(music.startMs.coerceAtLeast(0L).coerceAtMost((current.duration - 1).coerceAtLeast(0).toLong()).toInt())
                    if (state.onPrepared()) start()
                }
                setOnErrorListener { _, _, _ -> true }
                prepareAsync()
            }
        }.getOrNull()
        pauseMusic = {
            runCatching {
                if (player?.isPlaying == true) {
                    player?.pause()
                    pauseForViewer = true
                    loopTask?.let(handler::removeCallbacks)
                }
            }
        }
        resumeMusic = { if (pauseForViewer) start() }
        onDispose {
            disposed = true
            state.dispose()
            pauseMusic = null
            resumeMusic = null
            lifecycleOwner.lifecycle.removeObserver(observer)
            loopTask?.let(handler::removeCallbacks)
            runCatching { player?.stop() }
            runCatching { player?.release() }
        }
    }
}

private fun storyRelativeTime(value: String?): String {
    if (value.isNullOrBlank()) return "Vừa xong"
    val created = runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull() ?: return "Vừa xong"
    val minutes = Duration.between(created, java.time.Instant.now()).toMinutes().coerceAtLeast(0)
    return when {
        minutes < 1 -> "Vừa xong"
        minutes < 60 -> "$minutes phút"
        minutes < 24 * 60 -> "${minutes / 60} giờ"
        else -> "${minutes / (24 * 60)} ngày"
    }
}
