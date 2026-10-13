package eryaz.software.basewms.ui.dashboard.counting.fastCounting.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.databinding.DialogEditProductQuantityBinding
import eryaz.software.basewms.ui.base.BaseDialogFragment
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.zeus.zeusViewModel
import eryaz.software.zeus.params

class EditedQuantityDialog : BaseDialogFragment() {
    private val safeArgs by navArgs<EditedQuantityDialogArgs>()

    override val viewModel by zeusViewModel<EditQuantityVM> {
        params(safeArgs.productCode,safeArgs.productQuantity, safeArgs.productId)
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        DialogEditProductQuantityBinding.inflate(layoutInflater)
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
        binding.changeBtn.setOnSingleClickListener {
            setFragmentResult(
                REQUEST_KEY, bundleOf(
                    ARG_PRODUCT_QUANTITY to viewModel.quantity.value.toInt(),
                    ARG_PRODUCT_ID to safeArgs.productId
                )
            )
            findNavController().navigateUp()
        }
    }

    companion object {
        const val REQUEST_KEY = "EditedQuantityDialog"
        const val ARG_PRODUCT_QUANTITY = "NewQuantity"
        const val ARG_PRODUCT_ID = "id"
    }
}