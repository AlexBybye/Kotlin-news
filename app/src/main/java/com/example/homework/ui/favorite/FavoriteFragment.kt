package com.example.homework.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.homework.R
import com.example.homework.databinding.FragmentFavoriteBinding
import com.example.homework.model.NewsArticle
import com.example.homework.ui.detail.NewsDetailFragment
import com.example.homework.ui.home.NewsAdapter
import com.google.android.material.snackbar.Snackbar

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoriteViewModel by viewModels()
    private val favoriteAdapter by lazy { NewsAdapter(::openDetail) }
    private val historyAdapter by lazy { NewsAdapter(::openDetail) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        binding.retryButton.setOnClickListener {
            viewModel.loadData()
        }

        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            render(uiState)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }

    private fun setupRecyclerViews() {
        binding.favoriteRecyclerView.apply {
            adapter = favoriteAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        binding.historyRecyclerView.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun render(uiState: FavoriteUiState) {
        favoriteAdapter.submitList(uiState.favorites)
        historyAdapter.submitList(uiState.histories)

        binding.loadingLayout.isVisible = uiState.isLoading
        binding.errorLayout.isVisible = uiState.errorMessage != null && !uiState.isLoading
        binding.contentScrollView.isVisible =
            !uiState.isLoading && uiState.errorMessage == null && (uiState.favorites.isNotEmpty() || uiState.histories.isNotEmpty())
        binding.emptyLayout.isVisible =
            !uiState.isLoading && uiState.errorMessage == null && uiState.favorites.isEmpty() && uiState.histories.isEmpty()

        binding.favoriteRecyclerView.isVisible = uiState.favorites.isNotEmpty()
        binding.favoriteEmptyText.isVisible = uiState.favorites.isEmpty()
        binding.historyRecyclerView.isVisible = uiState.histories.isNotEmpty()
        binding.historyEmptyText.isVisible = uiState.histories.isEmpty()

        if (binding.errorLayout.isVisible) {
            binding.errorText.text = uiState.errorMessage ?: getString(R.string.favorite_error_default)
        }
    }

    private fun openDetail(article: NewsArticle) {
        if (article.id.isBlank()) {
            Snackbar.make(
                binding.root,
                getString(R.string.favorite_detail_unavailable),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        findNavController().navigate(
            R.id.action_favoriteFragment_to_newsDetailFragment,
            Bundle().apply {
                putString(NewsDetailFragment.ARG_NEWS_ID, article.id)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
