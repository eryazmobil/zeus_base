package eryaz.software.basewms.ui.dashboard.inbound.acceptance.acceptanceProcess.createBarcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.R
import eryaz.software.basewms.data.enums.IconType
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.databinding.CreateBarcodeDialogBinding
import eryaz.software.basewms.ui.base.BaseDialogFragment
import eryaz.software.basewms.ui.dashboard.recording.dialog.ProductListDialogFragment
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.dialogs.QuestionDialog
import eryaz.software.basewms.util.extensions.onBarcodeOrShelfReadListener
import eryaz.software.basewms.util.extensions.parcelable
import eryaz.software.zeus.zeusViewModel

class CreateBarcodeDialog : BaseDialogFragment() {
    override val viewModel by zeusViewModel<CreateBarcodeDialogVM>()

    private val safeArgs by navArgs<CreateBarcodeDialogArgs>()

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

    override fun setClicks() {

        binding.inputProductCode.onBarcodeOrShelfReadListener {
            viewModel.getProductByCode()

        }

        binding.saveBtn.setOnSingleClickListener {
            viewModel.createBarcode()
        }

        binding.searchProductBarcode.setEndIconOnClickListener {
            findNavController().navigate(
                CreateBarcodeDialogDirections.actionCreateBarcodeDialog3ToProductListDialogFragment()
            )
        }
    }

    override fun subscribeToObservables() {

        binding.inputProductCode.requestFocus()

        binding.searchTxt.setText(safeArgs.barcode)

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

        viewModel.showErrorProduct
            .asLiveData()
            .observe(viewLifecycleOwner) {
                errorDialog.show(
                    requireContext(),
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.msg_no_no_product
                    )
                )

            }

        viewModel.showProductViewDetail
            .asLiveData()
            .observe(viewLifecycleOwner) {
                binding.quantityEdt.requestFocus()
            }

        setFragmentResultListener(ProductListDialogFragment.REQUEST_KEY) { _, bundle ->
            val dto = bundle.parcelable<ProductDto>(ProductListDialogFragment.ARG_PRODUCT_DTO)
            dto?.let {
                viewModel.setEnteredProduct(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.inputProductCode.requestFocus()

    }


}
