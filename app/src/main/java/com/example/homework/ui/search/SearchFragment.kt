package com.example.homework.ui.search

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.homework.R
import com.example.homework.databinding.FragmentSearchBinding
import com.example.homework.model.NewsArticle
import com.example.homework.model.SearchHistory
import com.example.homework.ui.detail.NewsDetailFragment
import com.example.homework.ui.home.NewsAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()
    private val resultAdapter by lazy {
        NewsAdapter { article ->
            openDetail(article)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupInteractions()
        observeUiState()
        consumeInitialQuery(savedInstanceState)
    }

    private fun consumeInitialQuery(savedInstanceState: Bundle?) {
        // 仅在首次进入时根据传入关键词自动发起搜索，避免旋转重建后重复执行。
        if (savedInstanceState != null) return
        val initialQuery = arguments?.getString(ARG_QUERY)
        if (!initialQuery.isNullOrBlank()) {
            viewModel.onHotKeywordClicked(initialQuery)
        }
    }

    private fun setupRecyclerView() {
        binding.resultRecyclerView.apply {
            adapter = resultAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupInteractions() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.searchButton.setOnClickListener {
            performSearch()
        }

        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }

        binding.clearHistoryText.setOnClickListener {
            viewModel.clearHistory()
        }

        binding.searchEditText.doAfterTextChanged { text ->
            viewModel.updateKeyword(text?.toString().orEmpty())
        }

        binding.searchEditText.setOnEditorActionListener { _, actionId, event ->
            val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnterDown = event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                event.action == KeyEvent.ACTION_DOWN
            if (isSearchAction || isEnterDown) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun observeUiState() {
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            render(uiState)
        }
    }

    private fun render(uiState: SearchUiState) {
        if (binding.searchEditText.text?.toString() != uiState.keyword) {
            binding.searchEditText.setText(uiState.keyword)
            binding.searchEditText.setSelection(uiState.keyword.length)
        }

        renderHistory(uiState.recentHistory)
        renderHotKeywords(uiState.hotKeywords)

        resultAdapter.submitList(uiState.results)

        val showDiscovery = !uiState.hasSearched && !uiState.isLoading && uiState.errorMessage == null
        val showResults = uiState.hasSearched || uiState.isLoading || uiState.errorMessage != null

        binding.discoveryScrollView.isVisible = showDiscovery
        binding.resultContainer.isVisible = showResults
        binding.loadingLayout.isVisible = uiState.isLoading
        binding.errorLayout.isVisible = uiState.errorMessage != null && !uiState.isLoading
        binding.resultRecyclerView.isVisible =
            uiState.results.isNotEmpty() && !uiState.isLoading && uiState.errorMessage == null
        binding.emptyLayout.isVisible =
            uiState.hasSearched && uiState.results.isEmpty() && !uiState.isLoading &&
                uiState.errorMessage == null

        if (binding.errorLayout.isVisible) {
            binding.errorText.text = uiState.errorMessage ?: getString(R.string.search_error_default)
        }
    }

    private fun renderHistory(historyList: List<SearchHistory>) {
        binding.historyChipGroup.removeAllViews()
        binding.clearHistoryText.isVisible = historyList.isNotEmpty()
        binding.emptyHistoryText.isVisible = historyList.isEmpty()

        historyList.forEach { history ->
            binding.historyChipGroup.addView(
                createKeywordChip(history.keyword) {
                    viewModel.onHistoryClicked(history.keyword)
                }
            )
        }
    }

    private fun renderHotKeywords(hotKeywords: List<String>) {
        binding.hotKeywordChipGroup.removeAllViews()
        hotKeywords.forEach { keyword ->
            binding.hotKeywordChipGroup.addView(
                createKeywordChip(keyword) {
                    viewModel.onHotKeywordClicked(keyword)
                }
            )
        }
    }

    private fun createKeywordChip(label: String, onClick: () -> Unit): Chip {
        return Chip(requireContext()).apply {
            text = label
            isCheckable = false
            isClickable = true
            setEnsureMinTouchTargetSize(false)
            setChipBackgroundColorResource(R.color.home_category_chip_background)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.home_chip_text_checked))
            setOnClickListener { onClick() }
        }
    }

    private fun performSearch() {
        val keyword = binding.searchEditText.text?.toString().orEmpty()
        if (keyword.isBlank()) {
            Snackbar.make(
                binding.root,
                getString(R.string.search_empty_keyword),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        viewModel.submitSearch(keyword)
    }

    private fun openDetail(article: NewsArticle) {
        if (article.id.isBlank()) {
            Snackbar.make(
                binding.root,
                getString(R.string.search_detail_unavailable),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        findNavController().navigate(
            R.id.action_searchFragment_to_newsDetailFragment,
            Bundle().apply {
                putString(NewsDetailFragment.ARG_NEWS_ID, article.id)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_QUERY = "query"
    }
}
