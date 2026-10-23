package com.taihoang.socialmobile

internal data class StoryViewerSelection(
    val users: List<ApiStoryUser>,
    val userIndex: Int
)

internal data class StoryViewerPosition(
    val userIndex: Int,
    val storyIndex: Int
)

internal fun nextStoryPosition(users: List<ApiStoryUser>, position: StoryViewerPosition): StoryViewerPosition? {
    val current = users.getOrNull(position.userIndex) ?: return null
    if (position.storyIndex + 1 < current.stories.size) {
        return position.copy(storyIndex = position.storyIndex + 1)
    }
    for (userIndex in position.userIndex + 1 until users.size) {
        if (users[userIndex].stories.isNotEmpty()) return StoryViewerPosition(userIndex, 0)
    }
    return null
}

internal fun previousStoryPosition(users: List<ApiStoryUser>, position: StoryViewerPosition): StoryViewerPosition? {
    val current = users.getOrNull(position.userIndex) ?: return null
    if (position.storyIndex > 0) return position.copy(storyIndex = position.storyIndex - 1)
    for (userIndex in position.userIndex - 1 downTo 0) {
        val stories = users[userIndex].stories
        if (stories.isNotEmpty()) return StoryViewerPosition(userIndex, stories.lastIndex)
    }
    return null
}
