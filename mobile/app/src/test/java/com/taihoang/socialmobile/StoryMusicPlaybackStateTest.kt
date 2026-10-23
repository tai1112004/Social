package com.taihoang.socialmobile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoryMusicPlaybackStateTest {
    @Test
    fun pausesWhenAppStopsAndResumesWhenItStartsAgain() {
        val state = StoryMusicPlaybackState()

        assertTrue(state.onPrepared())
        state.onStop(currentlyPlaying = true)

        assertTrue(state.onStart())
    }

    @Test
    fun remembersToPlayWhenAppStopsBeforeMediaIsPrepared() {
        val state = StoryMusicPlaybackState()

        state.onStop(currentlyPlaying = false)
        assertFalse(state.onPrepared())
        assertTrue(state.onStart())
    }

    @Test
    fun disposeCancelsAnyPendingResume() {
        val state = StoryMusicPlaybackState()

        state.onPrepared()
        state.onStop(currentlyPlaying = true)
        state.dispose()

        assertFalse(state.onStart())
    }

    @Test
    fun manualPauseDoesNotResumeAfterBackground() {
        val state = StoryMusicPlaybackState()

        assertTrue(state.onPrepared())
        state.onManualPause()
        state.onStop(currentlyPlaying = false)

        assertFalse(state.onStart())
        assertTrue(state.onManualStart())
    }
}
