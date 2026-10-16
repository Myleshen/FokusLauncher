package com.lu4p.fokuslauncher.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherFontPreferencesTest {

    @Test
    fun `isCustomFont detects custom prefix`() {
        assertTrue(LauncherFontPreferences.isCustomFont(LauncherFontPreferences.CUSTOM_FONT_STORAGE))
        assertFalse(LauncherFontPreferences.isCustomFont("Roboto"))
        assertFalse(LauncherFontPreferences.isCustomFont(""))
    }

    @Test
    fun `customFontRelativePath returns file name for custom storage`() {
        assertEquals(
                LauncherFontPreferences.CUSTOM_FONT_ACTIVE_FILE,
                LauncherFontPreferences.customFontRelativePath(
                        LauncherFontPreferences.CUSTOM_FONT_STORAGE
                ),
        )
        assertEquals(null, LauncherFontPreferences.customFontRelativePath("sans-serif"))
    }

    @Test
    fun `customFontRelativePath rejects values that could escape the font directory`() {
        // A hand-edited preference or an imported backup must not read outside the font directory.
        assertEquals(null, LauncherFontPreferences.customFontRelativePath("custom:../outside.ttf"))
        assertEquals(null, LauncherFontPreferences.customFontRelativePath("custom:/etc/passwd"))
        assertEquals(null, LauncherFontPreferences.customFontRelativePath("custom:sub/dir.ttf"))
        assertEquals(null, LauncherFontPreferences.customFontRelativePath("custom:"))
        assertEquals(
                "font-123.otf",
                LauncherFontPreferences.customFontRelativePath("custom:font-123.otf"),
        )
    }

    @Test
    fun `displayLabelFromFontFileName strips ttf and otf extensions as fallback`() {
        assertEquals("Roboto", LauncherFontPreferences.displayLabelFromFontFileName("Roboto.ttf"))
        assertEquals("My Font", LauncherFontPreferences.displayLabelFromFontFileName("My Font.TTF"))
        assertEquals("Inter", LauncherFontPreferences.displayLabelFromFontFileName("Inter.otf"))
        assertEquals("readme.txt", LauncherFontPreferences.displayLabelFromFontFileName("readme.txt"))
    }

    @Test
    fun `normalizeFontFamilyFromStorage maps legacy values`() {
        assertEquals("", LauncherFontPreferences.normalizeFontFamilyFromStorage("DEFAULT"))
        assertEquals("sans-serif", LauncherFontPreferences.normalizeFontFamilyFromStorage("SANS_SERIF"))
        assertEquals(
                LauncherFontPreferences.CUSTOM_FONT_STORAGE,
                LauncherFontPreferences.normalizeFontFamilyFromStorage(
                        LauncherFontPreferences.CUSTOM_FONT_STORAGE
                ),
        )
    }
}
