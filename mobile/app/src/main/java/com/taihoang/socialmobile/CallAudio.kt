package com.taihoang.socialmobile

enum class CallAudioRoute {
    EARPIECE,
    SPEAKER
}

internal fun nextAudioRoute(current: CallAudioRoute): CallAudioRoute = when (current) {
    CallAudioRoute.EARPIECE -> CallAudioRoute.SPEAKER
    CallAudioRoute.SPEAKER -> CallAudioRoute.EARPIECE
}
