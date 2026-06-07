package com.example.homework.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.homework.R
import com.example.homework.databinding.FragmentDiscoverBinding
import com.example.homework.model.NewsArticle
import com.example.homework.ui.detail.NewsDetailFragment
import com.example.homework.ui.home.NewsAdapter
import com.example.homework.ui.search.SearchFragment
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DiscoverViewModel by viewModels()
    private val trendingAdapter by lazy { NewsAdapter(::openDetail) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.trendingRecyclerView.apply {
            adapter = trendingAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        viewModel.uiState.observe(viewLifecycleOwner) { render(it) }
    }

    private fun render(state: DiscoverUiState) {
        binding.loadingBar.isVisible = state.isLoading
        binding.contentScrollView.isVisible = !state.isLoading

        renderHotKeywords(state.hotKeywords)
        trendingAdapter.submitList(state.trendingArticles)

        binding.errorText.isVisible =
            state.errorMessage != null && state.trendingArticles.isEmpty()
        binding.errorText.text = state.errorMessage.orEmpty()
    }

    private fun renderHotKeywords(keywords: List<String>) {
        binding.hotKeywordChipGroup.removeAllViews()
        keywords.forEach { keyword ->
            val chip = Chip(requireContext()).apply {
                text = keyword
                isCheckable = false
                isClickable = true
                setEnsureMinTouchTargetSize(false)
                setChipBackgroundColorResource(R.color.home_category_chip_background)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.home_chip_text_checked))
                setOnClickListener { openSearch(keyword) }
            }
            binding.hotKeywordChipGroup.addView(chip)
        }
    }

    private fun openSearch(keyword: String) {
        findNavController().navigate(
            R.id.action_discoverFragment_to_searchFragment,
            Bundle().apply { putString(SearchFragment.ARG_QUERY, keyword) }
        )
    }

    private fun openDetail(article: NewsArticle) {
        if (article.id.isBlank()) {
            Snackbar.make(
                binding.root,
                getString(R.string.discover_detail_unavailable),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        findNavController().navigate(
            R.id.action_discoverFragment_to_newsDetailFragment,
            Bundle().apply { putString(NewsDetailFragment.ARG_NEWS_ID, article.id) }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
