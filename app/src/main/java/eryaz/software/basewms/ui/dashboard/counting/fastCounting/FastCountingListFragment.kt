package eryaz.software.basewms.ui.dashboard.counting.fastCounting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.databinding.FragmentFastCountingListBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.counting.adapter.CountingListAdapter
import eryaz.software.zeus.zeusViewModel

class FastCountingListFragment : BaseFragment() {
    override val viewModel by zeusViewModel<FastCountingListVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentFastCountingListBinding.inflate(layoutInflater)
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

        viewModel.fetchCountingWorkActivityList()
    }

    override fun subscribeToObservables() {

        viewModel.countingWorkActivityList.asLiveData().observe(viewLifecycleOwner) {
            val firstCountingList = it.filter { data ->
                data.stockTakingType?.id == FAST_COUNTING_WAREHOUSE
            }
            adapter.submitList(firstCountingList)
        }

        viewModel.assignedUser.asLiveData()
            .observe(viewLifecycleOwner) {
                if (it) {
                    findNavController().navigate(
                        FastCountingListFragmentDirections.actionFastCountingListFragmentToFastCountingDetailFragment(
                            viewModel.stockTackingHeaderId
                        )
                    )
                }
            }
    }

    override fun setClicks() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        adapter.onItemClick = {
            viewModel.stockTackingHeaderId = it.id
            viewModel.getAllAssignedToUser(it.id)
        }
    }

    private val adapter by lazy {
       CountingListAdapter().also {
            binding.recyclerView.adapter = it
        }
    }

    companion object {
        const val FAST_COUNTING_WAREHOUSE = 16
    }
}