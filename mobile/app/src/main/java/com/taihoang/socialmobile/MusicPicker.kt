package com.taihoang.socialmobile

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max
import kotlin.math.min

data class ApiStoryMusicSelection(
    val track: ApiMusicTrack,
    val startMs: Long,
    val durationMs: Long,
    val volume: Double
)

private const val MusicPageSize = 20
private const val DefaultMusicClipMs = 15_000L
private val MusicBackground = Color(0xFFF7F8FA)
private val MusicInk = Color(0xFF182230)
private val MusicMuted = Color(0xFF6B7280)
private val MusicBlue = Color(0xFF1877F2)

@Composable
fun MusicPickerDialog(
    api: ApiClient,
    initialSelection: ApiStoryMusicSelection? = null,
    onSelect: (ApiStoryMusicSelection) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var tracks by remember { mutableStateOf(emptyList<ApiMusicTrack>()) }
    var page by remember { mutableStateOf(0) }
    var totalPages by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf(initialSelection?.track) }
    var startMs by remember { mutableStateOf(initialSelection?.startMs ?: 0L) }
    var durationMs by remember { mutableStateOf(initialSelection?.durationMs ?: DefaultMusicClipMs) }
    var volume by remember { mutableStateOf(initialSelection?.volume ?: 1.0) }
    var playingId by remember { mutableStateOf<Long?>(null) }
    var activeTab by remember { mutableStateOf("FOR_YOU") }
    val scope = rememberCoroutineScope()
    val player = remember { MediaPlayer() }
    // Invalidates callbacks from a preview that was stopped/reset while preparing.
    val previewGeneration = remember { AtomicLong(0L) }
    val lifecycleOwner = LocalLifecycleOwner.current

    fun stopPreview() {
        previewGeneration.incrementAndGet()
        runCatching { if (player.isPlaying) player.stop() }
        runCatching { player.reset() }
        playingId = null
    }

    fun preview(track: ApiMusicTrack) {
        if (playingId == track.id) {
            stopPreview()
            return
        }
        stopPreview()
        if (track.audioUrl.isBlank()) return
        val generation = previewGeneration.get()
        runCatching {
            player.setDataSource(track.audioUrl)
            player.setVolume(0.8f, 0.8f)
            player.setOnPreparedListener { prepared ->
                if (previewGeneration.get() == generation) {
                    prepared.start()
                    playingId = track.id
                }
            }
            player.setOnCompletionListener { if (previewGeneration.get() == generation) playingId = null }
            player.setOnErrorListener { _, _, _ -> if (previewGeneration.get() == generation) playingId = null; true }
            player.prepareAsync()
        }.onFailure { playingId = null }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { player.release() }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) stopPreview()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(query) {
        delay(350)
        debouncedQuery = query.trim()
    }

    LaunchedEffect(debouncedQuery) {
        loading = true
        error = null
        runCatching {
            if (debouncedQuery.isBlank()) api.musicTracks(0, MusicPageSize)
            else api.searchMusicTracks(debouncedQuery, 0, MusicPageSize)
        }.onSuccess { result ->
            tracks = result.items.distinctBy { it.id }
            page = result.page
            totalPages = result.totalPages
        }.onFailure { error = it.message ?: "Khong the tai danh sach nhac" }
        loading = false
    }

    fun loadMore() {
        if (loading || loadingMore || page >= totalPages - 1) return
        scope.launch {
            loadingMore = true
            runCatching {
                if (debouncedQuery.isBlank()) api.musicTracks(page + 1, MusicPageSize)
                else api.searchMusicTracks(debouncedQuery, page + 1, MusicPageSize)
            }.onSuccess { result ->
                tracks = (tracks + result.items).distinctBy { it.id }
                page = result.page
                totalPages = result.totalPages
            }.onFailure { error = it.message ?: "Khong the tai them nhac" }
            loadingMore = false
        }
    }

    Dialog(onDismissRequest = { stopPreview(); onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MusicBackground) {
            Column {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { stopPreview(); onDismiss() }) { Icon(Icons.Filled.ArrowBack, "Quay lai", tint = MusicInk) }
                    Text("Chon nhac", color = MusicInk, fontSize = 21.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    IconButton(onClick = { }) { Icon(Icons.Filled.BookmarkBorder, "Da luu", tint = MusicMuted) }
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it.take(100) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, "Tim nhac", tint = MusicMuted) },
                    placeholder = { Text("Tim bai hat hoac nghe si", color = MusicMuted) },
                    shape = RoundedCornerShape(14.dp)
                )
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    MusicTab("Dành cho bạn", activeTab == "FOR_YOU") { activeTab = "FOR_YOU" }
                    MusicTab("Thịnh hành", activeTab == "TRENDING") { activeTab = "TRENDING" }
                }
                selected?.let { track ->
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (track.coverUrl.isNullOrBlank()) {
                                Box(Modifier.size(42.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE7F3FF)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.MusicNote, "Ban nhac", tint = MusicBlue, modifier = Modifier.size(24.dp))
                                }
                            } else {
                                RemoteImage(track.coverUrl, Modifier.size(42.dp).clip(RoundedCornerShape(8.dp)))
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(track.title, color = MusicInk, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(track.artist ?: "Khong ro nghe si", color = MusicMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { preview(track) }) { Icon(if (playingId == track.id) Icons.Filled.Pause else Icons.Filled.PlayArrow, "Nghe thu", tint = MusicBlue) }
                        }
                        val trackDuration = max(1L, track.durationMs)
                        val safeDuration = min(durationMs.coerceAtLeast(1000L), trackDuration)
                        Text("Bat dau: ${formatMusicMs(startMs)}", color = MusicMuted, fontSize = 12.sp)
                        Slider(value = startMs.coerceIn(0L, max(0L, trackDuration - safeDuration)).toFloat(), onValueChange = { startMs = it.toLong() }, valueRange = 0f..max(1L, trackDuration - safeDuration).toFloat())
                        Text("Do dai: ${formatMusicMs(safeDuration)}", color = MusicMuted, fontSize = 12.sp)
                        Slider(value = safeDuration.toFloat(), onValueChange = { durationMs = it.toLong() }, valueRange = 1000f..trackDuration.toFloat())
                        Text("Am luong: ${(volume * 100).toInt()}%", color = MusicMuted, fontSize = 12.sp)
                        Slider(value = volume.toFloat(), onValueChange = { volume = it.toDouble() }, valueRange = 0f..1f)
                        Button(onClick = {
                            val safeStart = startMs.coerceIn(0L, max(0L, trackDuration - safeDuration))
                            stopPreview()
                            onSelect(ApiStoryMusicSelection(track, safeStart, safeDuration, volume))
                        }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Check, "Chon", modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Chon doan nhac")
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                if (loading && tracks.isEmpty()) Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MusicBlue) }
                else if (error != null && tracks.isEmpty()) Text(error.orEmpty(), color = Color(0xFFB3261E), modifier = Modifier.padding(16.dp))
                else LazyColumn(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(tracks, key = { it.id }) { track ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White).clickable { selected = track; startMs = 0; durationMs = min(DefaultMusicClipMs, max(1000L, track.durationMs)); volume = 1.0 }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (track.coverUrl.isNullOrBlank()) {
                                Box(Modifier.size(42.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE7F3FF)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.MusicNote, "Ban nhac", tint = MusicBlue, modifier = Modifier.size(24.dp))
                                }
                            } else {
                                RemoteImage(track.coverUrl, Modifier.size(42.dp).clip(RoundedCornerShape(8.dp)))
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(track.title, color = MusicInk, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${track.artist ?: "Khong ro nghe si"} · ${formatMusicMs(track.durationMs)}", color = MusicMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { preview(track) }) { Icon(if (playingId == track.id) Icons.Filled.Pause else Icons.Filled.PlayArrow, "Nghe thu", tint = MusicBlue) }
                            TextButton(onClick = { selected = track; startMs = 0; durationMs = min(DefaultMusicClipMs, max(1000L, track.durationMs)); volume = 1.0 }) { Text("Chon", color = MusicBlue, fontWeight = FontWeight.Bold) }
                        }
                    }
                    if (page < totalPages - 1) item { TextButton(onClick = ::loadMore, enabled = !loadingMore, modifier = Modifier.fillMaxWidth()) { if (loadingMore) CircularProgressIndicator(Modifier.size(16.dp), color = MusicBlue) else Text("Xem them", color = MusicBlue) } }
                }
                if (!loading && tracks.isEmpty() && error == null) Text("Chua co ban nhac kha dung", color = MusicMuted, modifier = Modifier.padding(20.dp))
            }
        }
    }
}

@Composable
private fun MusicTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = if (selected) MusicBlue else MusicMuted, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.width(if (selected) 54.dp else 0.dp).height(2.dp).background(MusicBlue))
    }
}

private fun formatMusicMs(value: Long): String {
    val totalSeconds = max(0L, value) / 1000
    return "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"
}
