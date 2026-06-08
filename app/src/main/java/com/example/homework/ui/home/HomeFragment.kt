package com.example.homework.ui.home

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
import com.example.homework.databinding.FragmentHomeBinding
import com.example.homework.model.NewsCategory
import com.example.homework.ui.detail.NewsDetailFragment
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private val newsAdapter by lazy {
        NewsAdapter { article ->
            if (article.id.isBlank()) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.home_detail_unavailable),
                    Snackbar.LENGTH_SHORT
                ).show()
                return@NewsAdapter
            }

            findNavController().navigate(
                R.id.action_homeFragment_to_newsDetailFragment,
                Bundle().apply {
                    putString(NewsDetailFragment.ARG_NEWS_ID, article.id)
                }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupInteractions()
        setupCategorySelection()

        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            render(uiState)
        }

        viewModel.weather.observe(viewLifecycleOwner) { weather ->
            renderWeather(weather)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadWeather()
    }

    private fun renderWeather(weather: com.example.homework.model.WeatherNow?) {
        if (weather == null) {
            binding.weatherCard.isVisible = false
            return
        }
        binding.weatherCard.isVisible = true
        binding.weatherIconText.text =
            com.example.homework.util.WeatherIconMapper.toEmoji(weather.iconCode)
        binding.weatherCityText.text = weather.cityName
        binding.weatherTempText.text = getString(R.string.home_weather_temp_format, weather.temperature)
        binding.weatherDescText.text = getString(
            R.string.home_weather_desc_format,
            weather.text,
            weather.feelsLike,
            weather.humidity
        )
    }

    private fun setupRecyclerView() {
        binding.newsRecyclerView.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.home_chip_text_checked)
    }

    private fun setupInteractions() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
            viewModel.loadWeather()
        }

        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }

        binding.searchCard.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }

    private fun setupCategorySelection() {
        binding.categoryChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val category = when (checkedId) {
                R.id.chipRecommend -> NewsCategory.RECOMMEND
                R.id.chipTechnology -> NewsCategory.TECHNOLOGY
                R.id.chipSports -> NewsCategory.SPORTS
                R.id.chipCampus -> NewsCategory.CAMPUS
                R.id.chipInternational -> NewsCategory.INTERNATIONAL
                else -> null
            }

            category?.let(viewModel::onCategorySelected)
        }
    }

    private fun render(uiState: HomeUiState) {
        binding.swipeRefreshLayout.isRefreshing = uiState.isRefreshing
        newsAdapter.submitList(uiState.articles)

        binding.cacheTipText.isVisible = uiState.isFromCache && uiState.articles.isNotEmpty()
        binding.loadingLayout.isVisible = uiState.isLoading
        binding.newsRecyclerView.isVisible = uiState.articles.isNotEmpty()
        binding.emptyLayout.isVisible = uiState.isEmpty
        binding.errorLayout.isVisible =
            uiState.errorMessage != null && uiState.articles.isEmpty() && !uiState.isLoading

        if (binding.errorLayout.isVisible) {
            binding.errorText.text = uiState.errorMessage ?: getString(R.string.home_error_default)
        }

        updateSelectedCategory(uiState.selectedCategory)
    }

    private fun updateSelectedCategory(category: NewsCategory) {
        val checkedId = when (category) {
            NewsCategory.RECOMMEND -> R.id.chipRecommend
            NewsCategory.TECHNOLOGY -> R.id.chipTechnology
            NewsCategory.SPORTS -> R.id.chipSports
            NewsCategory.CAMPUS -> R.id.chipCampus
            NewsCategory.INTERNATIONAL -> R.id.chipInternational
        }

        if (binding.categoryChipGroup.checkedChipId != checkedId) {
            binding.categoryChipGroup.check(checkedId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
