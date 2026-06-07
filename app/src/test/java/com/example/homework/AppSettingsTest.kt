package com.example.homework

import com.example.homework.data.settings.DarkMode
import com.example.homework.data.settings.FontScale
import org.junit.Assert.assertEquals
import org.junit.Test

class AppSettingsTest {

    @Test
    fun darkMode_roundTripsThroughStorageValue() {
        DarkMode.entries.forEach { mode ->
            assertEquals(mode, DarkMode.fromStorageValue(mode.storageValue))
        }
    }

    @Test
    fun darkMode_unknownValueFallsBackToFollowSystem() {
        assertEquals(DarkMode.FOLLOW_SYSTEM, DarkMode.fromStorageValue(99))
        assertEquals(DarkMode.FOLLOW_SYSTEM, DarkMode.fromStorageValue(null))
    }

    @Test
    fun fontScale_roundTripsThroughStorageValue() {
        FontScale.entries.forEach { scale ->
            assertEquals(scale, FontScale.fromStorageValue(scale.storageValue))
        }
    }

    @Test
    fun fontScale_unknownValueFallsBackToStandard() {
        assertEquals(FontScale.STANDARD, FontScale.fromStorageValue(99))
        assertEquals(FontScale.STANDARD, FontScale.fromStorageValue(null))
    }
}
