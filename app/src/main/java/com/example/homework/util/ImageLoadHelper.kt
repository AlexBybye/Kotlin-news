package com.example.homework.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.ImageView
import coil.load
import com.example.homework.R
import com.example.homework.data.settings.SettingsManager

/**
 * 统一的新闻封面加载入口。
 *
 * 根据「仅 Wi-Fi 加载大图」设置与当前网络类型决定是否加载远程图片，
 * 在移动网络且开启该设置时只展示占位图以节省流量。
 */
object ImageLoadHelper {

    fun loadCover(imageView: ImageView, url: String?) {
        val context = imageView.context
        if (url.isNullOrBlank() || shouldSkipRemoteImage(context)) {
            imageView.setImageResource(R.drawable.bg_news_cover_placeholder)
            return
        }
        imageView.load(url) {
            crossfade(true)
            placeholder(R.drawable.bg_news_cover_placeholder)
            error(R.drawable.bg_news_cover_placeholder)
        }
    }

    private fun shouldSkipRemoteImage(context: Context): Boolean {
        val wifiOnly = SettingsManager.getInstance(context).wifiOnlyImagesSync()
        if (!wifiOnly) return false
        return !isOnWifi(context)
    }

    private fun isOnWifi(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}
