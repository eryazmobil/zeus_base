package eryaz.software.basewms.ui.dashboard.movement.routeList.chooseStep.vehicleUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.R
import eryaz.software.basewms.databinding.FragmentVehicleUpBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.util.adapter.movement.packageList.VehiclePackageAdapter
import eryaz.software.zeus.zeusViewModel
import eryaz.software.zeus.params

class VehicleUpFragment :BaseFragment() {

    private val safeArgs by navArgs<VehicleUpFragmentArgs>()

    override val viewModel by zeusViewModel<VehicleUpVM> {
        params(safeArgs.vehicleID, safeArgs.routeID)
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentVehicleUpBinding.inflate(layoutInflater)
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
        viewModel.vehicleDownSuccess.asLiveData().observe(this) {
            if (it) {
                binding.searchEdt.requestFocus()
                viewModel.getOrderHeaderRouteList()
                Toast.makeText(context, getString(R.string.unload_package), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        viewModel.packageList.asLiveData().observe(this) {
            adapter.submitList(it)
        }
    }

    private val adapter by lazy {
        VehiclePackageAdapter().also {
            binding.recyclerView.adapter = it
        }
    }

    override fun setClicks() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        adapter.onItemClick = {
            findNavController().navigate(
                VehicleUpFragmentDirections.actionVehicleUpFragmentToOrderDetailViewPagerFragment(
                    viewModel.routeID, it.orderHeaderId
                )
            )
        }
    }

    override fun onStart() {
        super.onStart()

        binding.searchEdt.requestFocus()
    }
}