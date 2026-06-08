package com.example.homework.ui.weather

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homework.data.location.DeviceLocationProvider
import com.example.homework.data.remote.network.ResultWrapper
import com.example.homework.data.repository.WeatherRepository
import com.example.homework.data.settings.AppSettings
import com.example.homework.data.settings.SettingsManager
import com.example.homework.model.WeatherCity
import kotlinx.coroutines.launch

class WeatherCityViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager.getInstance(application)
    private val locationProvider = DeviceLocationProvider(application)
    private val weatherRepository = WeatherRepository.createDefault(application)

    private val _settings = MutableLiveData(AppSettings())
    val settings: LiveData<AppSettings> = _settings

    private val _isLocating = MutableLiveData(false)
    val isLocating: LiveData<Boolean> = _isLocating

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    init {
        viewModelScope.launch {
            _settings.value = settingsManager.current()
        }
    }

    fun selectCity(city: WeatherCity) {
        viewModelScope.launch {
            settingsManager.setWeatherLocation(city.id, city.name)
            _settings.value = settingsManager.current()
            _message.value = "天气城市已切换为 ${city.name}。"
        }
    }

    fun useCurrentLocation() {
        if (_isLocating.value == true) return

        viewModelScope.launch {
            _isLocating.value = true
            when (val locationResult = locationProvider.getCurrentLocation()) {
                is ResultWrapper.Success -> resolveAndSaveCity(
                    latitude = locationResult.data.latitude,
                    longitude = locationResult.data.longitude
                )

                is ResultWrapper.Error -> _message.value = locationResult.message
            }
            _isLocating.value = false
        }
    }

    private suspend fun resolveAndSaveCity(latitude: Double, longitude: Double) {
        when (val cityResult = weatherRepository.resolveCityByCoordinates(latitude, longitude)) {
            is ResultWrapper.Success -> {
                val city = cityResult.data
                settingsManager.setWeatherLocation(city.id, city.displayName)
                _settings.value = settingsManager.current()
                _message.value = "已根据当前位置切换为 ${city.displayName}。"
            }

            is ResultWrapper.Error -> _message.value = cityResult.message
        }
    }

    fun onMessageConsumed() {
        _message.value = null
    }
}
