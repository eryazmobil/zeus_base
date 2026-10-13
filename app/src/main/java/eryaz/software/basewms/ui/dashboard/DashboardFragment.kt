package eryaz.software.basewms.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.R
import eryaz.software.basewms.databinding.FragmentDashboardBinding
import eryaz.software.basewms.data.enums.DashboardPermissionType.RETURNING
import eryaz.software.basewms.data.enums.IconType
import eryaz.software.basewms.data.persistence.SessionManager.clearData
import eryaz.software.basewms.data.stepService.StepCountManager
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.util.adapter.dashboard.adapters.DashboardAdapter
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.dialogs.QuestionDialog
import eryaz.software.basewms.util.extensions.onBackPressedCallback
import eryaz.software.basewms.util.extensions.toast
import kotlinx.coroutines.launch
import eryaz.software.zeus.zeusViewModel

class DashboardFragment : BaseFragment() {
    override val viewModel by zeusViewModel<DashboardViewModel>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentDashboardBinding.inflate(layoutInflater)
    }

    private val activityRecognitionPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startStepCounter()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requestStepPermissionAndStart()
    }

    override fun onPause() {
        viewModel.syncStepCountToServer()
        super.onPause()
    }

    override fun subscribeToObservables() {
        viewModel.dashboardItemList
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.stepCount.collect { count: Int ->
                binding.stepTxt.visibility = View.VISIBLE
                binding.stepTxt.text = getString(R.string.step_count, count)
            }
        }
    }

    private val adapter by lazy {
        DashboardAdapter().also {
            it.also {
                binding.recyclerView.adapter = it
            }
        }
    }

    private fun requestStepPermissionAndStart() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            startStepCounter()
            return
        }
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startStepCounter()
            }
            else -> activityRecognitionPermission.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    private fun startStepCounter() {
        StepCountManager.start(requireContext())
    }

    override fun setClicks() {

        adapter.onItemClick = { dto ->
            when (dto.type) {

                RETURNING -> toast(getString(R.string.not_done_yet))

                else -> {
                    val title = requireActivity().getString(dto.titleRes)
                    findNavController().navigate(
                        DashboardFragmentDirections.actionMainFragmentToDashboardDetailFragment(
                            title, dto.type
                        )
                    )
                }
            }
        }

        binding.settingsBtn.setOnSingleClickListener {
            findNavController().navigate(
                DashboardFragmentDirections.actionMainFragmentToNavSettings()
            )
        }

        onBackPressedCallback {
            QuestionDialog(
                onNegativeClickListener = {
                },
                onPositiveClickListener = {
                    viewModel.syncStepCountToServer()
                    StepCountManager.stop()
                    clearData()
                    findNavController().navigateUp()
                },
                textHeader = resources.getString(R.string.exit),
                textMessage = resources.getString(R.string.are_you_sure),
                positiveBtnText = resources.getString(R.string.yes),
                negativeBtnText = resources.getString(R.string.no),
                singleBtnText = "",
                negativeBtnViewVisible = true,
                icType = IconType.Success.ordinal
            ).show(parentFragmentManager, "dialog")
        }
    }

    override fun hideActionBar() = true
}
