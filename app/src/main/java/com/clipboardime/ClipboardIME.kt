package com.clipboardime

import android.inputmethodservice.InputMethodService
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.clipboardime.data.ClipboardDatabase
import com.clipboardime.data.ClipboardEntity
import com.clipboardime.data.ClipboardRepository
import com.clipboardime.ui.KeyboardViewManager
import com.clipboardime.util.ClipboardMonitor
import com.clipboardime.viewmodel.SearchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClipboardIME : InputMethodService() {

    private lateinit var rootView: LinearLayout
    private lateinit var searchHeader: View
    private lateinit var searchEditText: EditText
    private lateinit var resultsContainer: LinearLayout
    private lateinit var keyboardContainer: LinearLayout
    private lateinit var keyboardManager: KeyboardViewManager
    private lateinit var searchKeyboardManager: KeyboardViewManager
    private lateinit var emptyHint: TextView

    private lateinit var repository: ClipboardRepository
    private lateinit var monitor: ClipboardMonitor
    private lateinit var viewModel: SearchViewModel

    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isSearchMode = false
    private var searchJob: Job? = null

    override fun onCreate() {
        super.onCreate()

        val db = ClipboardDatabase.getInstance(this)
        repository = ClipboardRepository(db.clipboardDao())
        viewModel = SearchViewModel(repository)
        monitor = ClipboardMonitor(this, repository)

        val cm = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        cm.addPrimaryClipChangedListener(monitor)
    }

    override fun onCreateInputView(): View {
        val inflater = LayoutInflater.from(this)

        rootView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Search header (hidden by default)
        searchHeader = inflater.inflate(R.layout.ime_search_header, rootView, false)
        searchEditText = searchHeader.findViewById(R.id.et_search)
        val btnBack: Button = searchHeader.findViewById(R.id.btn_search_back)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                performSearch(s?.toString() ?: "")
            }
        })

        btnBack.setOnClickListener { switchToNormalMode() }
        searchHeader.visibility = View.GONE
        rootView.addView(searchHeader)

        // Results area (hidden by default)
        resultsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                200 * resources.displayMetrics.density.toInt()
            )
            setBackgroundColor(0xFFFFFFFF.toInt())
            visibility = View.GONE
        }

        emptyHint = TextView(this).apply {
            text = getString(R.string.no_results)
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 32)
            setTextColor(0x99000000.toInt())
        }
        resultsContainer.addView(emptyHint)
        rootView.addView(resultsContainer)

        // Keyboard area
        keyboardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        keyboardManager = KeyboardViewManager(
            context = this,
            onKeyPress = { handleKeyPress(it) },
            onSearchClick = { switchToSearchMode() },
            onDeleteClick = { handleDelete() }
        )

        searchKeyboardManager = KeyboardViewManager(
            context = this,
            onKeyPress = { handleKeyPress(it) },
            onSearchClick = { switchToNormalMode() },
            onDeleteClick = { handleDelete() },
            isSearchMode = true
        )

        keyboardContainer.addView(keyboardManager.buildKeyboard())
        rootView.addView(keyboardContainer)

        return rootView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (isSearchMode) {
            switchToNormalMode()
        }
    }

    override fun onDestroy() {
        val cm = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        cm.removePrimaryClipChangedListener(monitor)
        mainScope.cancel()
        super.onDestroy()
    }

    private fun handleKeyPress(key: String) {
        val ic = currentInputConnection ?: return
        ic.commitText(key, 1)
    }

    private fun handleDelete() {
        val ic = currentInputConnection ?: return
        val textBefore = ic.getTextBeforeCursor(1, 0) ?: return
        if (textBefore.isNotEmpty()) {
            ic.deleteSurroundingText(1, 0)
        }
    }

    private fun switchToSearchMode() {
        isSearchMode = true
        searchHeader.visibility = View.VISIBLE
        resultsContainer.visibility = View.VISIBLE
        searchEditText.setText("")
        searchEditText.requestFocus()

        // Show all history initially
        searchJob = mainScope.launch {
            repository.getAll().collectLatest { items ->
                showResults(items)
            }
        }

        // Switch to search keyboard (with closing action)
        keyboardContainer.removeAllViews()
        keyboardContainer.addView(searchKeyboardManager.buildKeyboard())
    }

    private fun switchToNormalMode() {
        isSearchMode = false
        searchJob?.cancel()
        searchHeader.visibility = View.GONE
        resultsContainer.visibility = View.GONE

        keyboardContainer.removeAllViews()
        keyboardContainer.addView(keyboardManager.buildKeyboard())
    }

    private fun performSearch(keyword: String) {
        searchJob?.cancel()
        searchJob = mainScope.launch {
            if (keyword.isBlank()) {
                repository.getAll().collectLatest { items ->
                    showResults(items)
                }
            } else {
                repository.searchByKeyword(keyword.trim()).collectLatest { items ->
                    showResults(items)
                }
            }
        }
    }

    private fun showResults(items: List<ClipboardEntity>) {
        // Remove all result views except the empty hint
        for (i in resultsContainer.childCount - 1 downTo 0) {
            val child = resultsContainer.getChildAt(i)
            if (child !== emptyHint) {
                resultsContainer.removeViewAt(i)
            }
        }

        if (items.isEmpty()) {
            emptyHint.visibility = View.VISIBLE
        } else {
            emptyHint.visibility = View.GONE
            val inflater = LayoutInflater.from(this)
            val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

            items.forEach { item ->
                val itemView = inflater.inflate(R.layout.item_clipboard_result, resultsContainer, false)
                val tvContent: TextView = itemView.findViewById(R.id.tv_content)
                val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
                val btnPaste: Button = itemView.findViewById(R.id.btn_paste)

                tvContent.text = item.content
                tvTimestamp.text = dateFormat.format(Date(item.timestamp))

                itemView.setOnClickListener {
                    val ic = currentInputConnection ?: return@setOnClickListener
                    ic.commitText(item.content, 1)
                }

                btnPaste.setOnClickListener {
                    val ic = currentInputConnection ?: return@setOnClickListener
                    ic.commitText(item.content, 1)
                }

                resultsContainer.addView(itemView)
            }
        }
    }
}
