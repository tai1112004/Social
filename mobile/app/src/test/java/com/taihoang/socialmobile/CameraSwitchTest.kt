package com.taihoang.socialmobile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CameraSwitchTest {
    @Test
    fun switchesFromFrontToBack() {
        assertEquals("back", nextCameraName("front", "front", "back"))
    }

    @Test
    fun switchesFromBackToFront() {
        assertEquals("front", nextCameraName("back", "front", "back"))
    }

    @Test
    fun reportsNoSwitchWhenDeviceHasOnlyOneCamera() {
        assertNull(nextCameraName("front", "front", null))
    }
}
