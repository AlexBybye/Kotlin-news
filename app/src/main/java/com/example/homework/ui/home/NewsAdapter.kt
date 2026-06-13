package com.example.homework.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.homework.R
import com.example.homework.databinding.ItemNewsArticleBinding
import com.example.homework.model.NewsArticle
import com.example.homework.util.ImageLoadHelper

class NewsAdapter(
    private val onArticleClick: (NewsArticle) -> Unit
) : ListAdapter<NewsArticle, NewsAdapter.NewsViewHolder>(NewsDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsArticleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NewsViewHolder(binding, onArticleClick)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NewsViewHolder(
        private val binding: ItemNewsArticleBinding,
        private val onArticleClick: (NewsArticle) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: NewsArticle) {
            binding.topTagText.isVisible = article.isTop
            binding.titleText.text = article.title
            binding.summaryText.text = article.summary
            binding.summaryText.isVisible = article.summary.isNotBlank()
            binding.sourceText.text = article.source
            binding.timeText.text = article.publishTime

            if (article.coverImageUrl.isNullOrBlank()) {
                binding.coverImageView.setImageResource(R.drawable.bg_news_cover_placeholder)
            } else {
                ImageLoadHelper.loadCover(binding.coverImageView, article.coverImageUrl)
            }

            binding.root.setOnClickListener {
                onArticleClick(article)
            }
        }
    }

    private object NewsDiffCallback : DiffUtil.ItemCallback<NewsArticle>() {
        override fun areItemsTheSame(oldItem: NewsArticle, newItem: NewsArticle): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NewsArticle, newItem: NewsArticle): Boolean {
            return oldItem == newItem
        }
    }
}
