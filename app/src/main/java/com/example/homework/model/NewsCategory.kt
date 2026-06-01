package com.example.homework.model

enum class NewsCategory(val displayName: String, val apiValue: String) {
    RECOMMEND("推荐", "recommend"),
    TECHNOLOGY("科技", "technology"),
    SPORTS("体育", "sports"),
    CAMPUS("校园", "campus"),
    INTERNATIONAL("国际", "international");

    companion object {
        fun fromApiValue(value: String?): NewsCategory {
            return entries.firstOrNull { it.apiValue == value } ?: RECOMMEND
        }
    }
}
