package eryaz.software.basewms.ui.dashboard.counting.firstCounting.firstCountingDetail.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.R
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.databinding.CreateBarcodeDialogBinding
import eryaz.software.basewms.ui.base.BaseDialogFragment
import eryaz.software.basewms.ui.dashboard.inbound.acceptance.acceptanceProcess.createBarcode.CreateBarcodeDialogVM
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.onBarcodeOrShelfReadListener
import eryaz.software.zeus.zeusViewModel

class CreateBarcodeDialog : BaseDialogFragment() {
    override val viewModel by zeusViewModel<CreateBarcodeDialogVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        CreateBarcodeDialogBinding.inflate(layoutInflater)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.inputProductCode.requestFocus()

        binding.inputProductCode.onBarcodeOrShelfReadListener {
            viewModel.getProductByCode()
        }

        subscribe()

        binding.saveBtn.setOnSingleClickListener {
            viewModel.createBarcode()
        }

        viewModel.showErrorProductMessage
            .asLiveData()
            .observe(viewLifecycleOwner) {

                errorDialog.show(requireContext(),
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        message = it
                    )
                )
            }

        viewModel.showSuccess
            .asLiveData()
            .observe(viewLifecycleOwner) {
                findNavController().navigateUp()
            }

    }

    private fun subscribe() {
        viewModel.showErrorProduct
            .asLiveData()
            .observe(viewLifecycleOwner) {
                errorDialog.show(requireContext(),
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.msg_no_no_product
                    )
                )
            }

        viewModel.showProductViewDetail
            .asLiveData()
            .observe(viewLifecycleOwner) {
                binding.inputProductBarcode.requestFocus()
            }
    }

}
