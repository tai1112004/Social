package com.taihoang.socialmobile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val NewChatBackground = Color.Black
private val NewChatSurface = Color(0xFF1C1C1E)
private val NewChatText = Color(0xFFF5F5F7)
private val NewChatMuted = Color(0xFFA1A1AA)
private val NewChatBlue = Color(0xFF0A84FF)

@Composable
internal fun NewConversationSurface(
    friends: List<ApiFriend>,
    busy: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onCreateDirect: (ApiFriend) -> Unit,
    onCreateGroup: (List<ApiFriend>) -> Unit
) {
    var groupMode by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val visibleFriends = friends.filter { query.isBlank() || it.name.contains(query, true) }
    val selectedFriends = friends.filter { it.id in selectedIds }

    Dialog(onDismissRequest = { if (!busy) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = NewChatBackground) {
            if (groupMode) {
                NewGroupMembersScreen(
                    friends = visibleFriends,
                    selectedFriends = selectedFriends,
                    query = query,
                    busy = busy,
                    error = error,
                    onBack = { if (!busy) { groupMode = false; query = "" } },
                    onQueryChanged = { query = it.take(80) },
                    onToggle = { friend -> selectedIds = if (friend.id in selectedIds) selectedIds - friend.id else selectedIds + friend.id },
                    onRemove = { friend -> selectedIds = selectedIds - friend.id },
                    onNext = { onCreateGroup(selectedFriends) }
                )
            } else {
                NewConversationHome(
                    friends = visibleFriends,
                    query = query,
                    busy = busy,
                    error = error,
                    onBack = onDismiss,
                    onQueryChanged = { query = it.take(80) },
                    onGroup = { groupMode = true; query = "" },
                    onDirect = onCreateDirect
                )
            }
        }
    }
}

@Composable
private fun NewConversationHome(
    friends: List<ApiFriend>,
    query: String,
    busy: Boolean,
    error: String?,
    onBack: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onGroup: () -> Unit,
    onDirect: (ApiFriend) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        NewConversationHeader("Tin nhan moi", onBack)
        NewChatSearch(query, "Hay nhap ten hoac nhom", onQueryChanged, enabled = !busy)
        Spacer(Modifier.height(20.dp))
        NewChatActionRow(Icons.Filled.Edit, "Ghi chu moi", "Viet dieu gi do cho ban than")
        NewChatActionRow(Icons.Filled.Group, "Nhom chat", "Tao mot cuoc tro chuyen voi ban be", onClick = onGroup)
        NewChatActionRow(Icons.Filled.ChatBubble, "Chat voi AI", "Hoi dap nhanh voi tro ly")
        Text("Goi y", color = NewChatMuted, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
        if (friends.isEmpty()) {
            Text("Chua co danh sach ban be", color = NewChatMuted, modifier = Modifier.padding(vertical = 24.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                items(friends, key = { it.id }) { friend ->
                    FriendSelectionRow(friend, selected = false, enabled = !busy, onClick = { onDirect(friend) })
                }
            }
        }
        error?.let { Text(it, color = Color(0xFFFF453A), fontSize = 13.sp, modifier = Modifier.padding(vertical = 10.dp)) }
    }
}

@Composable
private fun NewGroupMembersScreen(
    friends: List<ApiFriend>,
    selectedFriends: List<ApiFriend>,
    query: String,
    busy: Boolean,
    error: String?,
    onBack: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onToggle: (ApiFriend) -> Unit,
    onRemove: (ApiFriend) -> Unit,
    onNext: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, enabled = !busy) { Icon(Icons.Filled.ArrowBack, "Quay lai", tint = NewChatText) }
            Text("Nhom moi", color = NewChatText, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButtonLike("Tiep", enabled = selectedFriends.isNotEmpty() && !busy, onClick = onNext)
        }
        NewChatSearch(query, "Tim kiem", onQueryChanged, enabled = !busy)
        if (selectedFriends.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                selectedFriends.forEach { friend -> SelectedFriendChip(friend, onRemove = { onRemove(friend) }) }
            }
        }
        Text("Goi y", color = NewChatMuted, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp, bottom = 8.dp))
        if (friends.isEmpty()) {
            Text("Khong tim thay ban be", color = NewChatMuted, modifier = Modifier.padding(vertical = 24.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                items(friends, key = { it.id }) { friend ->
                    FriendSelectionRow(friend, selected = friend in selectedFriends, enabled = !busy, onClick = { onToggle(friend) })
                }
            }
        }
        if (busy) Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center) { CircularProgressIndicator(Modifier.size(20.dp), color = NewChatBlue, strokeWidth = 2.dp) }
        error?.let { Text(it, color = Color(0xFFFF453A), fontSize = 13.sp, modifier = Modifier.padding(vertical = 10.dp)) }
    }
}

@Composable
private fun NewConversationHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Quay lai", tint = NewChatText) }
        Text(title, color = NewChatText, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun NewChatSearch(value: String, placeholder: String, onValueChange: (String) -> Unit, enabled: Boolean) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(NewChatSurface).padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Search, "Tim kiem", tint = NewChatMuted)
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = NewChatText, fontSize = 17.sp),
            decorationBox = { field ->
                if (value.isBlank()) Text(placeholder, color = NewChatMuted, fontSize = 17.sp)
                field()
            }
        )
    }
}

@Composable
private fun NewChatActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(54.dp).clip(CircleShape).background(NewChatSurface), contentAlignment = Alignment.Center) { Icon(icon, title, tint = NewChatText, modifier = Modifier.size(26.dp)) }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, color = NewChatText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = NewChatMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun FriendSelectionRow(friend: ApiFriend, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick).padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        RemoteAvatar(friend.name, friend.avatarUrl, NewChatSurface, 52.dp)
        Spacer(Modifier.width(14.dp))
        Text(friend.name, color = NewChatText, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        SelectionCircle(selected)
    }
}

@Composable
private fun SelectionCircle(selected: Boolean) {
    Box(Modifier.size(26.dp).clip(CircleShape).then(if (selected) Modifier.background(NewChatBlue) else Modifier.border(2.dp, NewChatMuted, CircleShape)), contentAlignment = Alignment.Center) {
        if (selected) Icon(Icons.Filled.Check, "Da chon", tint = Color.White, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun SelectedFriendChip(friend: ApiFriend, onRemove: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
        Box(Modifier.size(60.dp), contentAlignment = Alignment.TopEnd) {
            RemoteAvatar(friend.name, friend.avatarUrl, NewChatSurface, 56.dp)
            IconButton(onClick = onRemove, modifier = Modifier.size(22.dp).background(NewChatSurface, CircleShape)) { Icon(Icons.Filled.Close, "Bo chon", tint = NewChatText, modifier = Modifier.size(15.dp)) }
        }
        Text(friend.name, color = NewChatText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TextButtonLike(label: String, enabled: Boolean, onClick: () -> Unit) {
    Text(label, color = if (enabled) NewChatBlue else NewChatMuted, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(enabled = enabled, onClick = onClick).padding(horizontal = 8.dp, vertical = 8.dp))
}
