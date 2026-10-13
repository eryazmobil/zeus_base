package eryaz.software.basewms.ui.auth

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.databinding.FragmentLoginBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.util.extensions.handleProgress
import eryaz.software.zeus.zeusViewModel

class LoginFragment : BaseFragment() {
    override val viewModel by zeusViewModel<LoginViewModel>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentLoginBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }

    override fun subscribeToObservables() {
        binding.edtUserName.requestFocus()

        viewModel.uiState.asLiveData().observe(viewLifecycleOwner) { data ->
            binding.loginBtn.handleProgress(data, progressColor = Color.WHITE)
        }

        viewModel.navigateToMain.asLiveData().observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToMainFragment()
                )
            }
        }
    }

    override fun hideActionBar() = true
    override fun getStatusBarColor() = Color.TRANSPARENT
}
