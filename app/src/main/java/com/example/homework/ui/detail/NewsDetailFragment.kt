package com.example.homework.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.homework.R
import com.example.homework.databinding.FragmentNewsDetailBinding
import com.example.homework.model.NewsArticle
import com.example.homework.model.NewsDetail
import com.google.android.material.snackbar.Snackbar

class NewsDetailFragment : Fragment() {

    private var _binding: FragmentNewsDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewsDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInteractions()
        observeUiState()
        observeMessages()
        loadDetailFromArguments()
    }

    private fun setupInteractions() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }

        binding.collectButton.setOnClickListener {
            viewModel.toggleCollect()
        }

        binding.likeButton.setOnClickListener {
            viewModel.toggleLike()
        }

        binding.shareButton.setOnClickListener {
            shareCurrentArticle()
        }

        binding.originButton.setOnClickListener {
            openOriginalArticle()
        }
    }

    private fun shareCurrentArticle() {
        val detail = viewModel.uiState.value?.detail ?: return
        val shareText = buildString {
            append(detail.title)
            append('\n')
            append(detail.summary)
            detail.contentUrl?.let {
                append('\n')
                append(it)
            }
        }
        val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, detail.title)
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        }
        startActivity(
            android.content.Intent.createChooser(
                sendIntent,
                getString(R.string.detail_share_chooser_title)
            )
        )
    }

    private fun openOriginalArticle() {
        val detail = viewModel.uiState.value?.detail ?: return
        val url = detail.contentUrl
        if (url.isNullOrBlank()) {
            Snackbar.make(
                binding.root,
                getString(R.string.detail_origin_unavailable),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        findNavController().navigate(
            R.id.action_newsDetailFragment_to_webViewFragment,
            Bundle().apply {
                putString(com.example.homework.ui.web.WebViewFragment.ARG_URL, url)
                putString(com.example.homework.ui.web.WebViewFragment.ARG_TITLE, detail.title)
            }
        )
    }

    private fun observeUiState() {
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            render(uiState)
        }
    }

    private fun observeMessages() {
        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message.isNullOrBlank()) return@observe
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            viewModel.onMessageConsumed()
        }
    }

    private fun loadDetailFromArguments() {
        val newsId = arguments?.getString(ARG_NEWS_ID)
        if (newsId.isNullOrBlank()) {
            render(
                NewsDetailUiState(
                    isLoading = false,
                    errorMessage = getString(R.string.detail_missing_id_error)
                )
            )
            return
        }

        viewModel.loadDetail(newsId)
    }

    private fun render(uiState: NewsDetailUiState) {
        binding.loadingLayout.isVisible = uiState.isLoading
        binding.errorLayout.isVisible = uiState.errorMessage != null && uiState.detail == null
        binding.detailScrollView.isVisible = uiState.detail != null
        binding.cacheTipText.isVisible = uiState.isFromCache && uiState.detail != null
        binding.errorText.text = uiState.errorMessage ?: getString(R.string.detail_error_default)

        uiState.detail?.let(::bindDetail)
    }

    private fun bindDetail(detail: NewsDetail) {
        binding.titleText.text = detail.title
        binding.sourceText.text = detail.source
        binding.timeText.text = detail.publishTime
        binding.categoryText.text = detail.category.displayName
        binding.summaryText.text = detail.summary
        binding.collectButton.text = getString(
            if (detail.isCollected) R.string.detail_action_collected else R.string.detail_action_collect
        )
        binding.likeButton.text = getString(
            if (detail.isLiked) R.string.detail_action_liked else R.string.detail_action_like
        )

        binding.coverImageView.let { imageView ->
            com.example.homework.util.ImageLoadHelper.loadCover(imageView, detail.coverImageUrl)
        }

        val paragraphs = detail.content
        bindParagraph(binding.contentParagraph1Text, paragraphs.getOrNull(0))
        bindParagraph(binding.contentParagraph2Text, paragraphs.getOrNull(1))
        bindParagraph(binding.contentParagraph3Text, paragraphs.getOrNull(2))

        val relatedArticles = detail.relatedArticles
        binding.relatedCard1.isVisible = relatedArticles.getOrNull(0) != null
        binding.relatedCard2.isVisible = relatedArticles.getOrNull(1) != null
        binding.relatedTitleText.isVisible = relatedArticles.isNotEmpty()

        relatedArticles.getOrNull(0)?.let { article ->
            binding.relatedTitle1Text.text = article.title
            binding.relatedMeta1Text.text = getString(
                R.string.detail_related_meta_format,
                article.source,
                article.publishTime
            )
            binding.relatedCard1.setOnClickListener {
                openRelatedDetail(article)
            }
        }

        relatedArticles.getOrNull(1)?.let { article ->
            binding.relatedTitle2Text.text = article.title
            binding.relatedMeta2Text.text = getString(
                R.string.detail_related_meta_format,
                article.source,
                article.publishTime
            )
            binding.relatedCard2.setOnClickListener {
                openRelatedDetail(article)
            }
        }

        if (relatedArticles.getOrNull(0) == null) {
            binding.relatedCard1.setOnClickListener(null)
        }

        if (relatedArticles.getOrNull(1) == null) {
            binding.relatedCard2.setOnClickListener(null)
        }
    }

    private fun bindParagraph(textView: View, content: String?) {
        if (textView !is android.widget.TextView) return
        textView.isVisible = !content.isNullOrBlank()
        textView.text = content.orEmpty()
    }

    private fun openRelatedDetail(article: NewsArticle) {
        if (article.id.isBlank()) {
            Snackbar.make(
                binding.root,
                getString(R.string.detail_related_unavailable),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        findNavController().navigate(
            R.id.action_newsDetailFragment_self,
            Bundle().apply {
                putString(ARG_NEWS_ID, article.id)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_NEWS_ID = "newsId"
    }
}
