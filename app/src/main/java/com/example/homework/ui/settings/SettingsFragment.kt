package com.example.homework.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.homework.R
import com.example.homework.data.settings.AppSettings
import com.example.homework.data.settings.DarkMode
import com.example.homework.data.settings.FontScale
import com.example.homework.databinding.FragmentSettingsBinding
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    /** 防止程序化设置选中态时回调触发写入。 */
    private var isBinding = false

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* 用户选择即可，无需额外处理 */ }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        setupListeners()

        viewModel.settings.observe(viewLifecycleOwner) { bindSettings(it) }
        viewModel.cacheCount.observe(viewLifecycleOwner) { count ->
            binding.cacheCountText.text = getString(R.string.settings_cache_count_format, count)
        }
        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message.isNullOrBlank()) return@observe
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            viewModel.onMessageConsumed()
        }
    }

    private fun setupListeners() {
        binding.darkModeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (isBinding) return@setOnCheckedStateChangeListener
            val mode = when (checkedIds.firstOrNull()) {
                R.id.chipDarkLight -> DarkMode.LIGHT
                R.id.chipDarkDark -> DarkMode.DARK
                else -> DarkMode.FOLLOW_SYSTEM
            }
            viewModel.onDarkModeSelected(mode)
        }

        binding.fontScaleChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (isBinding) return@setOnCheckedStateChangeListener
            val scale = when (checkedIds.firstOrNull()) {
                R.id.chipFontSmall -> FontScale.SMALL
                R.id.chipFontLarge -> FontScale.LARGE
                R.id.chipFontExtraLarge -> FontScale.EXTRA_LARGE
                else -> FontScale.STANDARD
            }
            viewModel.onFontScaleSelected(scale)
        }

        binding.wifiOnlySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isBinding) viewModel.onWifiOnlyImagesChanged(isChecked)
        }
        binding.autoRefreshSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isBinding) {
                viewModel.onAutoRefreshChanged(isChecked)
                if (isChecked) requestNotificationPermissionIfNeeded()
            }
        }
        binding.clearCacheRow.setOnClickListener { viewModel.clearCache() }
        binding.weatherCityRow.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_weatherCityFragment)
        }

        binding.useBackendSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isBinding) viewModel.onUseBackendChanged(isChecked)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun bindSettings(settings: AppSettings) {
        isBinding = true

        val darkChipId = when (settings.darkMode) {
            DarkMode.FOLLOW_SYSTEM -> R.id.chipDarkFollowSystem
            DarkMode.LIGHT -> R.id.chipDarkLight
            DarkMode.DARK -> R.id.chipDarkDark
        }
        binding.darkModeChipGroup.check(darkChipId)

        val fontChipId = when (settings.fontScale) {
            FontScale.SMALL -> R.id.chipFontSmall
            FontScale.STANDARD -> R.id.chipFontStandard
            FontScale.LARGE -> R.id.chipFontLarge
            FontScale.EXTRA_LARGE -> R.id.chipFontExtraLarge
        }
        binding.fontScaleChipGroup.check(fontChipId)

        binding.wifiOnlySwitch.isChecked = settings.wifiOnlyImages
        binding.autoRefreshSwitch.isChecked = settings.autoRefresh
        binding.useBackendSwitch.isChecked = settings.useBackend
        binding.weatherCityValueText.text = getString(
            R.string.settings_weather_city_value_format,
            settings.weatherCityName,
            settings.weatherLocationId
        )

        isBinding = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
