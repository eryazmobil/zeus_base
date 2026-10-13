package eryaz.software.basewms.ui.dashboard.outbound.datPicking

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
import eryaz.software.basewms.databinding.FragmentDatPickingListBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.outbound.orderPicking.adapter.OrderWorkActivityListAdapter
import eryaz.software.zeus.zeusViewModel

class DatPickingListFragment : BaseFragment() {
    override val viewModel by zeusViewModel<DatPickingListVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentDatPickingListBinding.inflate(layoutInflater)
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

    override fun subscribeToObservables() {
        viewModel.getActiveWorkAction()

        viewModel.orderPickingList
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }

        viewModel.searchList()
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }

        viewModel.uiState.asLiveData().observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = UiState.LOADING == it
        }

        viewModel.navigateToDetail
            .asLiveData()
            .observe(viewLifecycleOwner) {
                if (it) {
                    findNavController().navigate(
                        DatPickingListFragmentDirections.actionDatPickingListFragmentToDatPickingDetailFragment()
                    )
                }
            }
    }

    override fun setClicks() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getActiveWorkAction()
        }

        adapter.onItemClick = {
            if (it.isLocked) {
                errorDialog.show(
                    context,
                    ErrorDialogDto(
                        titleRes = R.string.warning,
                        messageRes = R.string.locked_work_activity
                    )
                )
            }
            TemporaryCashManager.getInstance().workActivity = it
            viewModel.getWorkActionForPda()
        }
    }

    private val adapter by lazy {
        OrderWorkActivityListAdapter().also {
            binding.recyclerView.adapter = it
        }
    }
}