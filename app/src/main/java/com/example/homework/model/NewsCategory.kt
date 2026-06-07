package com.example.homework.model

enum class NewsCategory(val displayName: String, val apiValue: String, val juheType: String) {
    RECOMMEND("推荐", "recommend", "top"),
    TECHNOLOGY("科技", "technology", "keji"),
    SPORTS("体育", "sports", "tiyu"),
    CAMPUS("校园", "campus", "shehui"),
    INTERNATIONAL("国际", "international", "guoji");

    companion object {
        fun fromApiValue(value: String?): NewsCategory {
            return entries.firstOrNull { it.apiValue == value } ?: RECOMMEND
        }
    }
}
