package eryaz.software.basewms.ui.dashboard.movement.routeList.chooseStep

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.databinding.FragmentChoosenVehicleBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.outbound.controlPoint.orderHeaderDialog.controlPointDetail.ControlPointDetailFragmentArgs
import eryaz.software.basewms.ui.dashboard.outbound.controlPoint.orderHeaderDialog.controlPointDetail.ControlPointDetailVM
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.zeus.zeusViewModel
import eryaz.software.zeus.params

class ChooseVehicleFragment : BaseFragment() {

    private val safeArgs by navArgs<ChooseVehicleFragmentArgs>()

    override val viewModel by zeusViewModel<ChooseVehicleVM> {
        params(safeArgs.routeId)
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentChoosenVehicleBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }

    override fun setClicks() {
        binding.downBtn.setOnSingleClickListener {
            findNavController().navigate(
                ChooseVehicleFragmentDirections.actionChooseVehicleFragmentToVehicleDownFragment(
                    1, viewModel.routeId
                )
            )
        }

        binding.upBtn.setOnClickListener {
            findNavController().navigate(
                ChooseVehicleFragmentDirections.actionChooseVehicleFragmentToVehicleUpFragment(
                    2, viewModel.routeId
                )
            )
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
}
