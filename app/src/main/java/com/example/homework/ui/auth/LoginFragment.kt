package com.example.homework.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homework.R
import com.example.homework.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            viewModel.login(
                username = binding.usernameEditText.text?.toString().orEmpty(),
                password = binding.passwordEditText.text?.toString().orEmpty()
            )
        }

        binding.goRegisterText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    private fun render(state: AuthUiState) {
        binding.loadingBar.isVisible = state.isLoading
        binding.loginButton.isEnabled = !state.isLoading

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
