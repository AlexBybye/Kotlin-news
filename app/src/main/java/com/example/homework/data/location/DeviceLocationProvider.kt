package com.example.homework.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.homework.data.remote.network.ResultWrapper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 原生定位能力封装，不引入额外定位 SDK。
 */
class DeviceLocationProvider(private val context: Context) {

    private val locationManager: LocationManager? by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): ResultWrapper<Location> {
        if (!hasLocationPermission()) {
            return ResultWrapper.Error("请先授权定位权限。")
        }

        val manager = locationManager ?: return ResultWrapper.Error("当前设备不支持定位服务。")
        val providers = enabledProviders(manager)
        if (providers.isEmpty()) {
            return ResultWrapper.Error("请先在系统设置中开启定位服务。")
        }

        latestKnownLocation(manager, providers)?.let {
            return ResultWrapper.Success(it)
        }

        return requestSingleLocation(manager, providers.first())
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun enabledProviders(manager: LocationManager): List<String> {
        return listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { provider ->
                runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false)
            }
    }

    @SuppressLint("MissingPermission")
    private fun latestKnownLocation(
        manager: LocationManager,
        providers: List<String>
    ): Location? {
        return providers
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleLocation(
        manager: LocationManager,
        provider: String
    ): ResultWrapper<Location> = suspendCancellableCoroutine { continuation ->
        val handler = Handler(Looper.getMainLooper())
        var completed = false
        lateinit var timeout: Runnable

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (completed) return
                completed = true
                handler.removeCallbacks(timeout)
                manager.removeUpdates(this)
                continuation.resume(ResultWrapper.Success(location))
            }

            @Deprecated("Deprecated in Android framework")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

            override fun onProviderEnabled(provider: String) = Unit

            override fun onProviderDisabled(provider: String) = Unit
        }

        timeout = Runnable {
            if (completed) return@Runnable
            completed = true
            manager.removeUpdates(listener)
            continuation.resume(ResultWrapper.Error("暂时无法获取当前位置，请稍后重试。"))
        }

        continuation.invokeOnCancellation {
            completed = true
            handler.removeCallbacks(timeout)
            manager.removeUpdates(listener)
        }

        runCatching {
            manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            handler.postDelayed(timeout, LOCATION_TIMEOUT_MILLIS)
        }.getOrElse {
            completed = true
            handler.removeCallbacks(timeout)
            manager.removeUpdates(listener)
            continuation.resume(ResultWrapper.Error(it.message ?: "定位请求失败。"))
        }
    }

    private companion object {
        const val LOCATION_TIMEOUT_MILLIS = 10_000L
    }
}
