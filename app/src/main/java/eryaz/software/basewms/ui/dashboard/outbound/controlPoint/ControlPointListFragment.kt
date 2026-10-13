package eryaz.software.basewms.ui.dashboard.outbound.controlPoint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.data.enums.UiState
import eryaz.software.basewms.databinding.FragmentControlPointListBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.util.adapter.outbound.ControlPointListAdapter
import eryaz.software.zeus.zeusViewModel

class ControlPointListFragment : BaseFragment() {
    override val viewModel by zeusViewModel<ControlPointListVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentControlPointListBinding.inflate(layoutInflater)
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

    override fun onStart() {
        super.onStart()
        viewModel.fetchControlPointList()
    }

    override fun subscribeToObservables() {

        viewModel.controlPointList.asLiveData()
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }

        viewModel.uiState.asLiveData().observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = UiState.LOADING == it
        }

        viewModel.orderHeaderList
            .asLiveData()
            .observe(viewLifecycleOwner) { list ->
                if (list.isNotEmpty()) {
                    findNavController().navigate(
                        ControlPointListFragmentDirections.actionControlPointListFragmentToOrderHeaderListDialog(
                            list.toTypedArray()
                        )
                    )
                }
            }
    }

    override fun setClicks() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchControlPointList()
        }

        adapter.onItemClick = {
            viewModel.controlPointId = it.id
            viewModel.getOrderHeaderListByControlPointId()
        }
    }

    private val adapter by lazy {
        ControlPointListAdapter().also {
            binding.recyclerView.adapter = it
        }
    }
}