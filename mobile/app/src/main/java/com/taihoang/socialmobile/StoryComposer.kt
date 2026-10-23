package com.taihoang.socialmobile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

private enum class StoryComposerMode { CHOOSER, TEXT, MEDIA }

private val StoryCanvas = Color(0xFFF7F8FA)
private val StoryInk = Color(0xFF182230)
private val StoryMuted = Color(0xFF6B7280)
private val StoryBlue = Color(0xFF1877F2)
private val StoryBlueSoft = Color(0xFFEAF2FF)

@Composable
internal fun StoryComposer(api: ApiClient, onDismiss: () -> Unit, onCreated: () -> Unit) {
    var mode by remember { mutableStateOf(StoryComposerMode.CHOOSER) }
    var storyText by remember { mutableStateOf("") }
    var privacy by remember { mutableStateOf("PUBLIC") }
    var backgroundIndex by remember { mutableStateOf(0) }
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedMusic by remember { mutableStateOf<ApiStoryMusicSelection?>(null) }
    var showMusicPicker by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            selectedUris = uris.take(10)
            mode = StoryComposerMode.MEDIA
            error = null
        }
    }

    fun publish() {
        if (busy) return
        scope.launch {
            busy = true
            error = null
            runCatching {
                val music = selectedMusic
                val background = listOf("#F9C5D1", "#BDE0FE", "#CDECCF", "#DCCEF9")[backgroundIndex]
                if (mode == StoryComposerMode.MEDIA && selectedUris.isNotEmpty()) {
                    selectedUris.forEachIndexed { index, uri ->
                        val mime = context.contentResolver.getType(uri) ?: "image/*"
                        require(mime.startsWith("image/") || mime.startsWith("video/")) { "Chi co the chon anh hoac video" }
                        val bytes = readValidatedUpload(context, uri, mime)
                        val upload = api.uploadMedia("story-${System.currentTimeMillis()}-$index", mime, bytes)
                        api.createStory(
                            type = if (mime.startsWith("video/")) "VIDEO" else "IMAGE",
                            text = storyText.trim().ifBlank { null },
                            privacy = privacy,
                            uploadId = upload.uploadId,
                            textOverlays = emptyList(),
                            musicTrackId = music?.track?.id,
                            musicStartMs = music?.startMs,
                            musicDurationMs = music?.durationMs,
                            musicVolume = music?.volume
                        )
                    }
                } else {
                    api.createStory(
                        type = "TEXT",
                        text = storyText.trim(),
                        privacy = privacy,
                        backgroundColor = background,
                        textColor = "#182230",
                        musicTrackId = music?.track?.id,
                        musicStartMs = music?.startMs,
                        musicDurationMs = music?.durationMs,
                        musicVolume = music?.volume
                    )
                }
            }.onSuccess { onCreated() }.onFailure { error = it.message ?: "Khong the dang tin" }
            busy = false
        }
    }

    Dialog(onDismissRequest = { if (!busy) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = StoryCanvas) {
            when (mode) {
                StoryComposerMode.CHOOSER -> StoryChooser(
                    selectedUris = selectedUris,
                    selectedMusic = selectedMusic,
                    privacy = privacy,
                    busy = busy,
                    error = error,
                    onClose = onDismiss,
                    onText = { mode = StoryComposerMode.TEXT },
                    onMusic = { showMusicPicker = true },
                    onMedia = { mediaPicker.launch("*/*") },
                    onPrivacy = { privacy = it },
                    onPublish = ::publish
                )
                StoryComposerMode.TEXT -> StoryTextEditor(
                    text = storyText,
                    backgroundIndex = backgroundIndex,
                    selectedMusic = selectedMusic,
                    privacy = privacy,
                    busy = busy,
                    error = error,
                    onTextChanged = { storyText = it.take(1000) },
                    onBack = { mode = StoryComposerMode.CHOOSER },
                    onBackground = { backgroundIndex = (backgroundIndex + 1) % 4 },
                    onMusic = { showMusicPicker = true },
                    onPrivacy = { privacy = it },
                    onPublish = ::publish
                )
                StoryComposerMode.MEDIA -> StoryMediaEditor(
                    uris = selectedUris,
                    text = storyText,
                    selectedMusic = selectedMusic,
                    privacy = privacy,
                    busy = busy,
                    error = error,
                    onTextChanged = { storyText = it.take(1000) },
                    onBack = { mode = StoryComposerMode.CHOOSER },
                    onAddMedia = { mediaPicker.launch("*/*") },
                    onMusic = { showMusicPicker = true },
                    onPrivacy = { privacy = it },
                    onRemove = { uri -> selectedUris = selectedUris - uri },
                    onPublish = ::publish
                )
            }
        }
    }

    if (showMusicPicker) {
        MusicPickerDialog(
            api = api,
            initialSelection = selectedMusic,
            onSelect = { selectedMusic = it; showMusicPicker = false },
            onDismiss = { showMusicPicker = false }
        )
    }
}

@Composable
private fun StoryHeader(title: String, onBack: () -> Unit, action: @Composable () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) { Icon(Icons.Filled.Close, "Dong", tint = StoryInk) }
        Text(title, color = StoryInk, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { action() }
    }
}

@Composable
private fun StoryChooser(
    selectedUris: List<Uri>, selectedMusic: ApiStoryMusicSelection?, privacy: String, busy: Boolean, error: String?, onClose: () -> Unit,
    onText: () -> Unit, onMusic: () -> Unit, onMedia: () -> Unit, onPrivacy: (String) -> Unit, onPublish: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        StoryHeader("Tao tin", onClose) { Icon(Icons.Filled.Settings, "Cai dat", tint = StoryMuted) }
        LazyColumn(Modifier.weight(1f).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Text("Chia se khoanh khac cua ban", color = StoryInk, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("Chon mot cach de bat dau tin moi", color = StoryMuted, modifier = Modifier.padding(top = 4.dp))
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StoryModeCard("Van ban", "Tao tin bang chu", Icons.Filled.TextFields, Color(0xFFFFD9E2), onText, Modifier.weight(1f))
                    StoryModeCard("Nhac", "Them bai hat", Icons.Filled.LibraryMusic, Color(0xFFE5DAFF), onMusic, Modifier.weight(1f))
                    StoryModeCard("Nhom anh", "Chon tu thu vien", Icons.Filled.Collections, Color(0xFFD5F2DF), onMedia, Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.PhotoLibrary, null, tint = StoryBlue)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Thu vien", color = StoryInk, fontWeight = FontWeight.Bold)
                        Text("Anh va video tren thiet bi", color = StoryMuted, fontSize = 12.sp)
                    }
                    OutlinedButton(onClick = onMedia) { Text("Chon nhieu file", color = StoryBlue, maxLines = 1) }
                }
            }
            if (selectedUris.isNotEmpty()) {
                item {
                    Text("Da chon ${selectedUris.size} tep", color = StoryInk, fontWeight = FontWeight.Bold)
                    StoryUriGrid(selectedUris)
                }
            } else {
                item { EmptyLibraryCard(onClick = onMedia) }
            }
            selectedMusic?.let { music ->
                item { SelectedMusicChip(music, onClick = onMusic) }
            }
            item {
                PrivacyPicker(privacy, onPrivacy)
                error?.let { Text(it, color = Color(0xFFB3261E), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
            }
        }
        ComposerBottomAction(
            label = if (selectedUris.isNotEmpty()) "Dang ${selectedUris.size} tin" else "Dang tin",
            enabled = selectedUris.isNotEmpty() || selectedMusic != null,
            busy = busy,
            onClick = onPublish
        )
    }
}

@Composable
private fun StoryModeCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(Color.White).clickable(onClick = onClick).padding(12.dp)) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) { Icon(icon, title, tint = StoryInk) }
        Text(title, color = StoryInk, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
        Text(subtitle, color = StoryMuted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun EmptyLibraryCard(onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).clickable(onClick = onClick).padding(vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.PhotoLibrary, null, tint = StoryBlue, modifier = Modifier.size(34.dp))
        Text("Chua chon anh hoac video", color = StoryInk, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
        Text("Cham de mo thu vien tren dien thoai", color = StoryMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun StoryUriGrid(uris: List<Uri>) {
    LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxWidth().height(172.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(uris, key = { it.toString() }) { uri ->
            Box(Modifier.aspectRatio(1f).clip(RoundedCornerShape(10.dp)).background(StoryBlueSoft), contentAlignment = Alignment.Center) {
                Text(uri.lastPathSegment?.takeLast(12) ?: "Media", color = StoryMuted, fontSize = 10.sp, maxLines = 2, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun StoryTextEditor(text: String, backgroundIndex: Int, selectedMusic: ApiStoryMusicSelection?, privacy: String, busy: Boolean, error: String?, onTextChanged: (String) -> Unit, onBack: () -> Unit, onBackground: () -> Unit, onMusic: () -> Unit, onPrivacy: (String) -> Unit, onPublish: () -> Unit) {
    val backgrounds = listOf(Color(0xFFFFC6D5), Color(0xFFBFE3FF), Color(0xFFCDECCF), Color(0xFFE0D1FF))
    Column(Modifier.fillMaxSize().background(backgrounds[backgroundIndex])) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, enabled = !busy) { Icon(Icons.Filled.ArrowBack, "Quay lai", tint = StoryInk) }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onMusic, enabled = !busy) { Icon(Icons.Filled.MusicNote, "Them nhac", tint = StoryInk) }
            IconButton(onClick = onBackground, enabled = !busy) { Icon(Icons.Filled.Palette, "Doi mau", tint = StoryInk) }
            IconButton(onClick = { }, enabled = !busy) { Icon(Icons.Filled.TextFields, "Kieu chu", tint = StoryInk) }
            Button(onClick = onPublish, enabled = text.isNotBlank() && !busy, colors = ButtonDefaults.buttonColors(containerColor = StoryInk), shape = RoundedCornerShape(12.dp)) { Text(if (busy) "..." else "Xong", color = Color.White) }
        }
        Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 28.dp), contentAlignment = Alignment.Center) {
            BasicTextField(
                value = text,
                onValueChange = onTextChanged,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = StoryInk, fontSize = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 38.sp),
                decorationBox = { field ->
                    if (text.isBlank()) Text("Nhap hoac @gan the", color = StoryInk.copy(alpha = .55f), fontSize = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    field()
                }
            )
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf("😀", "😂", "😍", "🔥", "🎉", "❤️", "👍").forEach { emoji ->
                Text(emoji, fontSize = 26.sp, modifier = Modifier.clickable { onTextChanged(text + emoji) })
            }
            IconButton(onClick = { }) { Icon(Icons.Filled.EmojiEmotions, "Emoji", tint = StoryInk) }
            IconButton(onClick = { }) { Icon(Icons.Filled.Keyboard, "Ban phim", tint = StoryInk) }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            PrivacyPicker(privacy, onPrivacy, compact = true)
            selectedMusic?.let { Text(it.track.title, color = StoryInk, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(150.dp)) }
        }
        error?.let { Text(it, color = Color(0xFFB3261E), fontSize = 12.sp, modifier = Modifier.padding(16.dp)) }
    }
}

@Composable
private fun StoryMediaEditor(uris: List<Uri>, text: String, selectedMusic: ApiStoryMusicSelection?, privacy: String, busy: Boolean, error: String?, onTextChanged: (String) -> Unit, onBack: () -> Unit, onAddMedia: () -> Unit, onMusic: () -> Unit, onPrivacy: (String) -> Unit, onRemove: (Uri) -> Unit, onPublish: () -> Unit) {
    Column(Modifier.fillMaxSize().background(StoryCanvas)) {
        StoryHeader("Nhom anh", onBack) { Icon(Icons.Filled.GridView, "Bo cuc", tint = StoryMuted) }
        LazyColumn(Modifier.weight(1f).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Text("Chinh sua tin", color = StoryInk, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Them chu, nhac va chia se nhieu khoanh khac", color = StoryMuted, modifier = Modifier.padding(top = 4.dp))
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    uris.take(3).forEach { uri ->
                        Box(Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(14.dp)).background(StoryBlueSoft), contentAlignment = Alignment.Center) {
                            Text(uri.lastPathSegment?.takeLast(10) ?: "Anh", color = StoryMuted, fontSize = 10.sp, textAlign = TextAlign.Center)
                            IconButton(onClick = { onRemove(uri) }, modifier = Modifier.align(Alignment.TopEnd)) { Icon(Icons.Filled.Close, "Xoa", tint = StoryInk) }
                        }
                    }
                    if (uris.size < 3) Box(Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(14.dp)).border(1.dp, StoryBlue, RoundedCornerShape(14.dp)).clickable(onClick = onAddMedia), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Add, "Them", tint = StoryBlue) }
                }
            }
            item {
                BasicTextField(value = text, onValueChange = onTextChanged, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White).padding(16.dp), textStyle = TextStyle(color = StoryInk, fontSize = 16.sp), decorationBox = { field -> if (text.isBlank()) Text("Them mo ta cho tin", color = StoryMuted); field() })
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onMusic, modifier = Modifier.weight(1f)) { Icon(Icons.Filled.MusicNote, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(selectedMusic?.track?.title ?: "Them nhac", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    OutlinedButton(onClick = onAddMedia, modifier = Modifier.weight(1f)) { Icon(Icons.Filled.PhotoLibrary, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Them anh") }
                }
                PrivacyPicker(privacy, onPrivacy)
                error?.let { Text(it, color = Color(0xFFB3261E), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
            }
        }
        ComposerBottomAction("Dang tin", uris.isNotEmpty() && !busy, busy, onPublish)
    }
}

@Composable
private fun PrivacyPicker(value: String, onChange: (String) -> Unit, compact: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Filled.Public, null, tint = StoryMuted, modifier = Modifier.size(if (compact) 16.dp else 20.dp))
        Text("Doi tuong", color = StoryMuted, fontSize = if (compact) 12.sp else 13.sp)
        TextButton(onClick = { onChange(if (value == "PUBLIC") "FRIENDS" else "PUBLIC") }) { Text(if (value == "PUBLIC") "Cong khai" else "Ban be", color = StoryBlue, fontWeight = FontWeight.Bold, fontSize = if (compact) 12.sp else 13.sp) }
    }
}

@Composable
private fun SelectedMusicChip(selection: ApiStoryMusicSelection, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFF0EAFE)).clickable(onClick = onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.MusicNote, null, tint = Color(0xFF6B4CC2))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(selection.track.title, color = StoryInk, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(selection.track.artist ?: "Khong ro nghe si", color = StoryMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Filled.ArrowForward, "Mo nhac", tint = StoryMuted)
    }
}

@Composable
private fun ComposerBottomAction(label: String, enabled: Boolean, busy: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(Color.White).padding(16.dp), horizontalArrangement = Arrangement.End) {
        Button(onClick = onClick, enabled = enabled && !busy, colors = ButtonDefaults.buttonColors(containerColor = StoryBlue), shape = RoundedCornerShape(12.dp)) {
            if (busy) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp) else Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = Color.White)
        }
    }
}
