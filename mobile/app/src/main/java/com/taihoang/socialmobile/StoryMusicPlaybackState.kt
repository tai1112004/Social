package com.taihoang.socialmobile

/**
 * Keeps Story audio lifecycle decisions independent from MediaPlayer callbacks.
 * It prevents audio from resuming after the viewer has been disposed.
 */
internal class StoryMusicPlaybackState {
    private var active = true
    private var prepared = false
    private var wantsPlayback = true
    private var resumeWhenActive = false

    fun onPrepared(): Boolean {
        if (!active) {
            prepared = true
            resumeWhenActive = wantsPlayback
            return false
        }
        prepared = true
        return wantsPlayback
    }

    fun onStop(currentlyPlaying: Boolean) {
        active = false
        resumeWhenActive = currentlyPlaying || (!prepared && wantsPlayback)
    }

    fun onStart(): Boolean {
        active = true
        val shouldResume = prepared && resumeWhenActive
        resumeWhenActive = false
        return shouldResume
    }

    fun onManualPause() {
        wantsPlayback = false
        resumeWhenActive = false
    }

    fun onManualStart(): Boolean {
        wantsPlayback = true
        return active && prepared
    }

    fun dispose() {
        active = false
        prepared = false
        wantsPlayback = false
        resumeWhenActive = false
    }
}
