package eryaz.software.basewms.ui.dashboard.outbound.controlPoint.orderHeaderDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.databinding.OrderHeaderListBinding
import eryaz.software.basewms.ui.base.BaseDialogFragment
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.observe
import eryaz.software.basewms.util.extensions.orZero
import eryaz.software.zeus.zeusViewModel
import eryaz.software.zeus.params

class OrderHeaderListDialog : BaseDialogFragment() {
    private val safeArgs by navArgs<OrderHeaderListDialogArgs>()

    override val viewModel by zeusViewModel<OrderHeaderListVM> {
        params(safeArgs.orderHeaderList.toList())
    }
    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        OrderHeaderListBinding.inflate(layoutInflater)
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

    override fun setClicks() {
        binding.continuedBtn.setOnSingleClickListener {
            viewModel.orderHeaderDto.observe(this) {
                findNavController().navigate(
                    OrderHeaderListDialogDirections.actionOrderHeaderListDialogToControlPointDetailFragment(
                        it?.workActivity?.workActivityCode.orEmpty(), it?.id.orZero()
                    )
                )
            }
        }
    }
}