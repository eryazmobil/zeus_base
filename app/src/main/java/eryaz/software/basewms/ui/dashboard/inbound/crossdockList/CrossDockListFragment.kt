package eryaz.software.basewms.ui.dashboard.inbound.crossdockList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.data.enums.UiState
import eryaz.software.basewms.databinding.FragmentCrossDockListBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.util.adapter.inbound.adapter.CrossDockListAdapter
import eryaz.software.zeus.zeusViewModel

class CrossDockListFragment : BaseFragment() {

    override val viewModel by zeusViewModel<CrossDockListVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentCrossDockListBinding.inflate(layoutInflater)
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
        viewModel.crossDockList
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
            viewModel.fetchCrossDockList()
        }
    }

    private val adapter by lazy {
        CrossDockListAdapter().also {
            binding.recyclerView.adapter = it
        }
    }
}