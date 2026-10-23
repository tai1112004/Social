package com.taihoang.socialmobile

import org.junit.Assert.assertEquals
import org.junit.Test

class CallQualityPolicyTest {
    @Test
    fun classifiesHealthyConnection() {
        assertEquals(CallQualityLevel.GOOD, classifyCallQuality(60, 0, 4))
    }

    @Test
    fun classifiesUnstableConnection() {
        assertEquals(CallQualityLevel.FAIR, classifyCallQuality(160, 2, 18))
    }

    @Test
    fun classifiesPoorConnection() {
        assertEquals(CallQualityLevel.POOR, classifyCallQuality(320, 7, 40))
    }
}
