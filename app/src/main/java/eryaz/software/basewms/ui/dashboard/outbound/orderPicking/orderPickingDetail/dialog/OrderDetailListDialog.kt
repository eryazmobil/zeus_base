package eryaz.software.basewms.ui.dashboard.outbound.orderPicking.orderPickingDetail.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.databinding.OrderDetailDialogListBinding
import eryaz.software.basewms.ui.base.BaseDialogFragment
import eryaz.software.basewms.util.adapter.outbound.OrderDetailListDialogAdapter
import eryaz.software.zeus.zeusViewModel
import eryaz.software.zeus.params

class OrderDetailListDialog : BaseDialogFragment() {
    private val safeArgs by navArgs<OrderDetailListDialogArgs>()

    override val viewModel by zeusViewModel<OrderDetailListDialogVM>() {
        params(safeArgs.orderDetailList.toList())
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        OrderDetailDialogListBinding.inflate(layoutInflater)
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

        val displayMetrics = resources.displayMetrics
        val widthPercentage = 0.8
        val heightPercentage = 0.9

        val width = (displayMetrics.widthPixels * widthPercentage).toInt()
        val height = (displayMetrics.heightPixels * heightPercentage).toInt()

        dialog?.window?.setLayout(width, height)

        adapter.submitList(viewModel.orderDetailList)


        viewModel.searchList()
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
    }

    private val adapter by lazy {
        OrderDetailListDialogAdapter().also {
            binding.recyclerView.adapter = it
        }
    }
}