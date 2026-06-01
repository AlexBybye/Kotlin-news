package com.example.homework.data.repository

data class CacheAwareData<T>(
    val value: T,
    val isFromCache: Boolean
)
