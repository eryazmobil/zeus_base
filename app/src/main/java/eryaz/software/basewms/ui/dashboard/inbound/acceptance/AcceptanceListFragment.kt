package eryaz.software.basewms.ui.dashboard.inbound.acceptance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.data.enums.UiState
import eryaz.software.basewms.data.persistence.TemporaryCashManager
import eryaz.software.basewms.databinding.FragmentAcceptanceListBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.util.adapter.inbound.adapter.WorkActivityAdapter
import eryaz.software.zeus.zeusViewModel

class AcceptanceListFragment : BaseFragment() {
    override val viewModel by zeusViewModel<AcceptanceListVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentAcceptanceListBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.acceptanceViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        viewModel.getWaybillWorkActivityList()
    }

    override fun subscribeToObservables() {

        viewModel.acceptanceList
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }

        viewModel.uiState.asLiveData().observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = UiState.LOADING == it
        }

        viewModel.searchList()
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }

        viewModel.workActionDto
            .asLiveData()
            .observe(viewLifecycleOwner) {
                findNavController().navigate(
                    AcceptanceListFragmentDirections.actionAcceptanceListFragmentToAcceptanceProcessFragment()
                )
            }
    }

    override fun setClicks() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getWaybillWorkActivityList()
        }

        adapter.onItemClick = {
            TemporaryCashManager.getInstance().workActivity = it
            viewModel.getWorkActionForPda()
        }
    }

    private val adapter by lazy {
        WorkActivityAdapter().also {
            binding.recyclerView.adapter = it
        }
    }
}