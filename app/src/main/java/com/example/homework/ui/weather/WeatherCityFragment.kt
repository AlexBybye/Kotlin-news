package com.example.homework.ui.weather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.homework.R
import com.example.homework.data.settings.AppSettings
import com.example.homework.databinding.FragmentWeatherCityBinding
import com.example.homework.model.WeatherCity
import com.google.android.material.snackbar.Snackbar

class WeatherCityFragment : Fragment() {

    private var _binding: FragmentWeatherCityBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeatherCityViewModel by viewModels()
    private var isBinding = false

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                viewModel.useCurrentLocation()
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.weather_city_permission_denied),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherCityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        setupCitySelection()
        binding.useCurrentLocationRow.setOnClickListener { locateOrRequestPermission() }

        viewModel.settings.observe(viewLifecycleOwner) { bindSettings(it) }
        viewModel.isLocating.observe(viewLifecycleOwner) { isLocating ->
            binding.locatingProgressBar.isVisible = isLocating
            binding.locateActionText.isVisible = !isLocating
            binding.useCurrentLocationRow.isEnabled = !isLocating
        }
        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message.isNullOrBlank()) return@observe
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            viewModel.onMessageConsumed()
        }
    }

    private fun setupCitySelection() {
        binding.cityChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (isBinding) return@setOnCheckedStateChangeListener
            val city = cityOptions[checkedIds.firstOrNull()] ?: return@setOnCheckedStateChangeListener
            viewModel.selectCity(city)
        }
    }

    private fun locateOrRequestPermission() {
        if (hasLocationPermission()) {
            viewModel.useCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun bindSettings(settings: AppSettings) {
        isBinding = true
        binding.currentCityText.text = settings.weatherCityName
        binding.currentCitySubtitleText.text =
            getString(R.string.weather_city_id_format, settings.weatherLocationId)

        val selectedChipId = cityOptions.entries
            .firstOrNull { it.value.id == settings.weatherLocationId }
            ?.key
        if (selectedChipId != null) {
            binding.cityChipGroup.check(selectedChipId)
        } else {
            binding.cityChipGroup.clearCheck()
        }
        isBinding = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        val cityOptions = mapOf(
            R.id.chipWeatherGuangzhou to WeatherCity(id = "101280101", name = "广州"),
            R.id.chipWeatherBeijing to WeatherCity(id = "101010100", name = "北京"),
            R.id.chipWeatherShanghai to WeatherCity(id = "101020100", name = "上海"),
            R.id.chipWeatherShenzhen to WeatherCity(id = "101280601", name = "深圳"),
            R.id.chipWeatherHangzhou to WeatherCity(id = "101210101", name = "杭州"),
            R.id.chipWeatherNanjing to WeatherCity(id = "101190101", name = "南京"),
            R.id.chipWeatherChengdu to WeatherCity(id = "101270101", name = "成都"),
            R.id.chipWeatherWuhan to WeatherCity(id = "101200101", name = "武汉"),
            R.id.chipWeatherXian to WeatherCity(id = "101110101", name = "西安"),
            R.id.chipWeatherChongqing to WeatherCity(id = "101040100", name = "重庆")
        )
    }
}
