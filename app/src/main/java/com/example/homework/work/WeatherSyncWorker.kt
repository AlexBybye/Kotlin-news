package com.example.homework.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.data.repository.WeatherRepository

/**
 * 后台定时同步天气的任务。
 *
 * 周期性拉取最新天气并写入本地缓存（由 WeatherRepository 负责持久化），
 * 使首页打开时可立即展示最近一次天气。对应作业「使用 Service」要求
 * （WorkManager 底层基于 JobScheduler / 系统服务）。
 */
class WeatherSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val repository = WeatherRepository.createDefault(applicationContext)
        return when (repository.getCurrentWeather()) {
            is ResultWrapper.Success -> Result.success()
            is ResultWrapper.Error -> Result.retry()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "weather_sync_work"
    }
}
