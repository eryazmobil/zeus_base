package eryaz.software.basewms.ui.dashboard.outbound.orderPicking.orderPickingDetail.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.databinding.OrderShelfListDialogBinding
import eryaz.software.basewms.ui.base.BaseDialogFragment
import eryaz.software.basewms.ui.dashboard.query.adapter.ShelfProductQuantityAdapter
import eryaz.software.zeus.zeusViewModel
import eryaz.software.zeus.params

class ShelfListDialog : BaseDialogFragment() {
    private val safeArgs by navArgs<ShelfListDialogArgs>()

    override val viewModel by zeusViewModel<ShelfListDialogVM> {
        params(safeArgs.productId)
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        OrderShelfListDialogBinding.inflate(layoutInflater)
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

        viewModel.shelfList.asLiveData()
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }

        val displayMetrics = resources.displayMetrics
        val widthPercentage = 0.8
        val heightPercentage = 0.6

        val width = (displayMetrics.widthPixels * widthPercentage).toInt()
        val height = (displayMetrics.heightPixels * heightPercentage).toInt()

        dialog?.window?.setLayout(width, height)
    }

    private val adapter by lazy {
        ShelfProductQuantityAdapter().also {
            binding.recyclerView.adapter = it
        }
    }
}