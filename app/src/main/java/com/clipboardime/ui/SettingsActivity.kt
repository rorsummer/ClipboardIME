package com.clipboardime.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.clipboardime.R
import com.clipboardime.data.ClipboardDatabase
import com.clipboardime.data.ClipboardRepository
import com.clipboardime.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var repository: ClipboardRepository
    private lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val db = ClipboardDatabase.getInstance(this)
        repository = ClipboardRepository(db.clipboardDao())
        viewModel = SearchViewModel(repository)

        val btnOpenImeSettings: Button = findViewById(R.id.btn_open_ime_settings)
        val btnClearHistory: Button = findViewById(R.id.btn_clear_history)
        val tvHistoryCount: TextView = findViewById(R.id.tv_history_count)

        btnOpenImeSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            startActivity(intent)
        }

        btnClearHistory.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.clear_all)
                .setMessage(R.string.clear_confirm)
                .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                    lifecycleScope.launch {
                        repository.deleteAll()
                        Toast.makeText(
                            this@SettingsActivity,
                            "已清空所有记录",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        // Observe history count
        lifecycleScope.launch {
            repository.getAll().collectLatest { items ->
                tvHistoryCount.text = getString(R.string.history_count, items.size)
            }
        }
    }
}
