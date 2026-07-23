package com.clipboardime.util

import android.content.ClipboardManager
import android.content.Context
import com.clipboardime.data.ClipboardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClipboardMonitor(
    private val context: Context,
    private val repository: ClipboardRepository
) : ClipboardManager.OnPrimaryClipChangedListener {

    private val scope = CoroutineScope(Dispatchers.IO)

    private fun getClipboardText(): String? {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
        val clip = cm.primaryClip ?: return null
        if (clip.itemCount == 0) return null
        return clip.getItemAt(0).coerceToText(context).toString().takeIf { it.isNotBlank() }
    }

    override fun onPrimaryClipChanged() {
        captureCurrentClipboard()
    }

    fun captureCurrentClipboard() {
        val text = getClipboardText() ?: return
        scope.launch {
            repository.addClipboardEntry(text)
        }
    }

    suspend fun captureAndAwait(): String? {
        val text = getClipboardText() ?: return null
        repository.addClipboardEntry(text)
        return text
    }

    fun copyToClipboard(text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = android.content.ClipData.newPlainText("clipboardime", text)
        cm.setPrimaryClip(clip)
    }
}
