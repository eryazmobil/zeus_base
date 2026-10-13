package eryaz.software.basewms.ui.dashboard.movement.supply.createSupplyWorkActivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.data.enums.ReplenishStatusEnum
import eryaz.software.basewms.databinding.FragmentSelectingWorkActivityStatusListBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.util.adapter.movement.supply.replenishmentTypeAdapter.ReplenishmentTypeAdapter
import eryaz.software.zeus.zeusViewModel


class SelectingWorkActivityStatusListFragment : BaseFragment() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentSelectingWorkActivityStatusListBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }

    override fun subscribeToObservables() {

        adapter.submitList(ReplenishStatusEnum.entries.toList())

    }

    override fun setClicks() {

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        adapter.onItemSelected = {
            val action = SelectingWorkActivityStatusListFragmentDirections
                .actionCreateSupplyWorkActivityFragmentToCreateSupplyWorkActivityAndShelfListInformation()
            action.supplyStatus = it.status
            findNavController().navigate(action)
        }

    }

    private val adapter by lazy {
        ReplenishmentTypeAdapter().also {
            binding.replenishmentRecycler.adapter = it
        }
    }


}