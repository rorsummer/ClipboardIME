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

    override fun onPrimaryClipChanged() {
        captureCurrentClipboard()
    }

    fun captureCurrentClipboard() {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = cm.primaryClip ?: return
        if (clip.itemCount == 0) return
        val text = clip.getItemAt(0).coerceToText(context).toString()
        if (text.isBlank()) return
        scope.launch {
            repository.addClipboardEntry(text)
        }
    }

    fun copyToClipboard(text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = android.content.ClipData.newPlainText("clipboardime", text)
        cm.setPrimaryClip(clip)
    }
}
