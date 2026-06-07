package com.example.homework.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homework.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.registerButton.setOnClickListener {
            viewModel.register(
                username = binding.usernameEditText.text?.toString().orEmpty(),
                nickname = binding.nicknameEditText.text?.toString().orEmpty(),
                password = binding.passwordEditText.text?.toString().orEmpty(),
                confirmPassword = binding.confirmPasswordEditText.text?.toString().orEmpty()
            )
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    private fun render(state: AuthUiState) {
        binding.loadingBar.isVisible = state.isLoading
        binding.registerButton.isEnabled = !state.isLoading

        state.errorMessage?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            viewModel.onErrorConsumed()
        }

        if (state.authenticatedUser != null) {
            viewModel.onNavigationConsumed()
            (activity as? AuthActivity)?.navigateToMain()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
