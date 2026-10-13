package eryaz.software.basewms.ui.dashboard.outbound.datControl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.R
import eryaz.software.basewms.data.enums.UiState
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.persistence.TemporaryCashManager
import eryaz.software.basewms.databinding.FragmentControlPointTransferListBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.util.adapter.outbound.dat.ControlPointTransferListAdapter
import eryaz.software.zeus.zeusViewModel

class ControlPointTransferListFragment : BaseFragment() {
    override val viewModel by zeusViewModel<ControlPointTransferListVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentControlPointTransferListBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.getTransferWorkActivityList()
    }

    override fun subscribeToObservables() {

        viewModel.workActivityList
            .asLiveData()
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }

        viewModel.uiState.asLiveData().observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = UiState.LOADING == it
        }
    }

    override fun setClicks() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getTransferWorkActivityList()
        }

        adapter.onItemClick = {
            if (it.isLocked) {
                errorDialog.show(
                    context,
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.locked_work_activity
                    )
                )
            } else {
                TemporaryCashManager.getInstance().workActivity = it
                findNavController().navigate(
                    ControlPointTransferListFragmentDirections.actionControlPointTransferListFragmentToTransferControlPointDetailFragment(
                        it.workActivityId,
                        it.workActivityCode
                    )
                )

            }
        }
    }

    private val adapter by lazy {
        ControlPointTransferListAdapter().also {
            binding.recyclerView.adapter = it
        }
    }
}