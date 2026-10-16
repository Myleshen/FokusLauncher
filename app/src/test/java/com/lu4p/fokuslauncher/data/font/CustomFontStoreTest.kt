package com.lu4p.fokuslauncher.data.font

import android.content.Context
import android.net.Uri
import com.lu4p.fokuslauncher.data.model.LauncherFontPreferences
import java.io.ByteArrayInputStream
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class, sdk = [28])
class CustomFontStoreTest {

    private lateinit var context: Context
    private lateinit var store: CustomFontStore

    private val fontsDir: File
        get() = File(context.filesDir, CustomFontStore.FONTS_DIR_NAME)

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        fontsDir.deleteRecursively()
        store = CustomFontStore(context)
    }

    @After
    fun tearDown() {
        fontsDir.deleteRecursively()
    }

    @Test
    fun `importing a file with an otf name succeeds`() = runBlocking {
        // Used to be rejected on the file name before anything read the content.
        val result = store.importFromUri(fontUri("MyFont.otf", payload = 1))

        assertTrue("expected success but was $result", result is CustomFontImportResult.Success)
        assertEquals(
                "otf",
                (result as CustomFontImportResult.Success).storageValue.substringAfterLast('.'),
        )
    }

    @Test
    fun `reimporting a font changes the stored value so the launcher re-resolves`() = runBlocking {
        // Regression: every import used to store the same value, so nothing re-resolved and the
        // previous face stayed on screen.
        val first = store.importFromUri(fontUri("First.otf", payload = 1))
        val second = store.importFromUri(fontUri("Second.ttf", payload = 2))

        val firstValue = (first as CustomFontImportResult.Success).storageValue
        val secondValue = (second as CustomFontImportResult.Success).storageValue
        assertNotEquals(firstValue, secondValue)
        assertTrue(firstValue.startsWith(LauncherFontPreferences.CUSTOM_FONT_PREFIX))
        assertNotNull(store.resolveFile(secondValue))
        // The replaced font is gone, so the directory keeps exactly one candidate.
        assertEquals(1, fontsDir.listFiles()!!.count { it.isFile })
    }

    @Test
    fun `rejects a name that would escape the font directory`() {
        assertTrue(store.resolveFile("custom:../outside.ttf") == null)
    }

    private fun fontUri(name: String, payload: Int): Uri {
        val bytes = byteArrayOf(0x4F, 0x54, 0x54, 0x4F) + ByteArray(64) { payload.toByte() }
        val uri = Uri.parse("content://test.fonts/$name")
        Shadows.shadowOf(context.contentResolver)
                .registerInputStream(uri, ByteArrayInputStream(bytes))
        return uri
    }
}
