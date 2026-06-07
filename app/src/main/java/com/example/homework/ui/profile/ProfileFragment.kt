package com.example.homework.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.homework.R
import com.example.homework.databinding.FragmentProfileBinding
import com.example.homework.ui.auth.AuthActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingsEntryCard.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        binding.logoutButton.setOnClickListener {
            viewModel.logout()
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    private fun render(state: ProfileUiState) {
        val user = state.user
        if (user != null) {
            binding.nicknameText.text = user.nickname
            binding.usernameText.text = getString(R.string.profile_username_format, user.username)
            binding.avatarText.text = user.nickname.take(1).uppercase()
        }
        binding.favoriteCountText.text = state.favoriteCount.toString()
        binding.historyCountText.text = state.historyCount.toString()

        if (state.loggedOut) {
            val intent = Intent(requireContext(), AuthActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
