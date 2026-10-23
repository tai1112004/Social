package com.taihoang.socialmobile

import org.junit.Assert.assertEquals
import org.junit.Test

class CallAudioPolicyTest {
    @Test
    fun togglesFromEarpieceToSpeaker() {
        assertEquals(CallAudioRoute.SPEAKER, nextAudioRoute(CallAudioRoute.EARPIECE))
    }

    @Test
    fun togglesFromSpeakerToEarpiece() {
        assertEquals(CallAudioRoute.EARPIECE, nextAudioRoute(CallAudioRoute.SPEAKER))
    }
}
