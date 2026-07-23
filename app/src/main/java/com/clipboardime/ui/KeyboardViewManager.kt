package com.clipboardime.ui

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout

class KeyboardViewManager(
    private val context: Context,
    private val onKeyPress: (String) -> Unit,
    private val onSearchClick: () -> Unit,
    private val onDeleteClick: () -> Unit,
    private val isSearchMode: Boolean = false
) {
    companion object {
        private val ROW_1 = arrayOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
        private val ROW_2 = arrayOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
        private val ROW_3 = arrayOf("z", "x", "c", "v", "b", "n", "m")
    }

    fun buildKeyboard(): LinearLayout {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(0xFFE8E8E8.toInt())
            setPadding(4, 4, 4, 4)
        }

        container.addView(buildRow(ROW_1, fullWidth = true))
        container.addView(buildRow(ROW_2, fullWidth = false))
        container.addView(buildActionRow())
        container.addView(buildBottomRow())

        return container
    }

    private fun buildRow(keys: Array<String>, fullWidth: Boolean): LinearLayout {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            setPadding(2, 2, 2, 2)
        }

        keys.forEach { key ->
            row.addView(createKeyButton(key, 1f))
        }

        return row
    }

    private fun buildActionRow(): LinearLayout {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            setPadding(2, 2, 2, 2)
        }

        row.addView(createKeyButton("⇧", 1.2f))

        val keys = arrayOf("z", "x", "c", "v", "b", "n", "m")
        keys.forEach { key -> row.addView(createKeyButton(key, 1f)) }

        row.addView(createKeyButton("⌫", 1.2f) { onDeleteClick() })

        return row
    }

    private fun buildBottomRow(): LinearLayout {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            setPadding(2, 4, 2, 2)
        }

        row.addView(createKeyButton("123", 1.2f))
        row.addView(createKeyButton(",", 1f))
        row.addView(createKeyButton("", 5f) {
            onKeyPress(" ")
        })
        row.addView(createKeyButton(".", 1f))
        row.addView(createKeyButton("🔍", 1.3f) { onSearchClick() })

        return row
    }

    private fun createKeyButton(label: String, weight: Float, onClick: (() -> Unit)? = null): Button {
        return Button(context).apply {
            text = label
            textSize = if (label.length > 2) 11f else 14f
            setTextColor(0xFF1F1F1F.toInt())
            gravity = Gravity.CENTER
            minWidth = 0
            minHeight = 0
            setPadding(2, 12, 2, 12)
            setBackgroundResource(com.clipboardime.R.drawable.key_bg)

            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight).apply {
                setMargins(2, 3, 2, 3)
            }

            setOnClickListener {
                onClick?.invoke() ?: onKeyPress(label)
            }
        }
    }
}
