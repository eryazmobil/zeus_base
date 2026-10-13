package eryaz.software.basewms.ui.dashboard.counting.firstCounting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.databinding.FragmentFirstCountingListBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.zeus.zeusViewModel

class FirstCountingListFragment : BaseFragment() {
    override val viewModel by zeusViewModel<FirstCountingListVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentFirstCountingListBinding.inflate(layoutInflater)
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
                data.stockTakingType!!.id == FIRST_COUNTING_WAREHOUSE
            }
            adapter.submitList(firstCountingList)
        }
    }

    override fun setClicks() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        adapter.onItemClick = {
            findNavController().navigate(
                FirstCountingListFragmentDirections.actionWarehouseCountingListFragmentToFirstCountingDetailFragment(
                    it.id
                )
            )
        }
    }

    private val adapter by lazy {
        eryaz.software.basewms.ui.dashboard.counting.adapter.CountingListAdapter().also {
            binding.recyclerView.adapter = it
        }
    }

    companion object {
        const val FIRST_COUNTING_WAREHOUSE = 7
    }
}