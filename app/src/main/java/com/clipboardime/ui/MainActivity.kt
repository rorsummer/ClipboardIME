package com.clipboardime.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.clipboardime.R
import com.clipboardime.data.ClipboardDatabase
import com.clipboardime.data.ClipboardEntity
import com.clipboardime.data.ClipboardRepository
import com.clipboardime.util.ClipboardMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var repository: ClipboardRepository
    private lateinit var monitor: ClipboardMonitor
    private lateinit var adapter: ClipboardAdapter
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = ClipboardDatabase.getInstance(this)
        repository = ClipboardRepository(db.clipboardDao())
        monitor = ClipboardMonitor(this, repository)

        setupRecyclerView()
        setupSearch()
        setupFab()

        // Register clipboard listener
        val cm = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        cm.addPrimaryClipChangedListener(monitor)

        // Capture existing clipboard first, then observe
        lifecycleScope.launch {
            monitor.captureAndAwait()
            observeClipboard()
        }
    }

    override fun onResume() {
        super.onResume()
        monitor.captureCurrentClipboard()
    }

    override fun onDestroy() {
        val cm = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        cm.removePrimaryClipChangedListener(monitor)
        super.onDestroy()
    }

    private fun setupRecyclerView() {
        adapter = ClipboardAdapter { item -> copyAndNotify(item) }

        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_results).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupSearch() {
        findViewById<android.widget.EditText>(R.id.et_search).addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    performSearch(s?.toString() ?: "")
                }
            }
        )
    }

    private fun setupFab() {
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.fab_capture
        ).setOnClickListener {
            lifecycleScope.launch {
                monitor.captureAndAwait()
                Toast.makeText(this@MainActivity, "已保存当前剪贴板内容", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performSearch(keyword: String) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            if (keyword.isBlank()) {
                repository.getAll().collectLatest { updateList(it) }
            } else {
                repository.searchByKeyword(keyword.trim()).collectLatest { updateList(it) }
            }
        }
    }

    private fun observeClipboard() {
        lifecycleScope.launch {
            repository.getAll().collectLatest { updateList(it) }
        }
    }

    private fun updateList(items: List<ClipboardEntity>) {
        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_results)
        val emptyLayout = findViewById<View>(R.id.layout_empty)
        val tvCount = findViewById<android.widget.TextView>(R.id.tv_count)

        if (items.isEmpty()) {
            rv.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
            tvCount.text = ""
        } else {
            rv.visibility = View.VISIBLE
            emptyLayout.visibility = View.GONE
            tvCount.text = "${items.size} 条"
            adapter.submitList(items)
        }
    }

    private fun copyAndNotify(item: ClipboardEntity) {
        monitor.copyToClipboard(item.content)
        Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }
}
