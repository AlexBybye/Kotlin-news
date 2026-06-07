package com.example.homework.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.homework.R
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.data.repository.NewsRepository
import com.example.homework.model.NewsCategory
import com.example.homework.ui.auth.AuthActivity

/**
 * 定时刷新推荐新闻的后台任务。
 *
 * 拉取最新推荐新闻写入本地缓存，并在拿到内容后发送一条提醒通知，
 * 引导用户回到应用查看热点。对应加分项「定时刷新 + 通知提醒」。
 */
class NewsRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val repository = NewsRepository.createDefault(applicationContext)
        return when (val result = repository.getNews(NewsCategory.RECOMMEND)) {
            is ResultWrapper.Success -> {
                val topArticle = result.data.value.firstOrNull()
                if (topArticle != null) {
                    showNotification(
                        title = applicationContext.getString(R.string.news_refresh_notification_title),
                        content = topArticle.title
                    )
                }
                Result.success()
            }

            is ResultWrapper.Error -> Result.retry()
        }
    }

    private fun showNotification(title: String, content: String) {
        createChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = applicationContext.checkSelfPermission(
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val intent = Intent(applicationContext, AuthActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(NOTIFICATION_ID, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                applicationContext.getString(R.string.news_refresh_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = applicationContext.getString(R.string.news_refresh_channel_desc)
            }
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "news_refresh_work"
        private const val CHANNEL_ID = "news_refresh_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
