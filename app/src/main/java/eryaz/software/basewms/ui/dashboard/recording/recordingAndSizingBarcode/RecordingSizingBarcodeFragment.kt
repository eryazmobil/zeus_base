package eryaz.software.basewms.ui.dashboard.recording.recordingAndSizingBarcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.R
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.databinding.FragmentRecordingSizingBarcodeBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.recording.dialog.ProductListDialogFragment
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.observe
import eryaz.software.basewms.util.extensions.parcelable
import eryaz.software.zeus.zeusViewModel

class RecordingSizingBarcodeFragment : BaseFragment() {

    override val viewModel by zeusViewModel<RecordingSizingBarcodeVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentRecordingSizingBarcodeBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }

    override fun subscribeToObservables() {
        super.subscribeToObservables()

        setFragmentResultListener(ProductListDialogFragment.REQUEST_KEY) { _, bundle ->
            val dto = bundle.parcelable<ProductDto>(ProductListDialogFragment.ARG_PRODUCT_DTO)
            dto?.let {
                viewModel.setEnteredProduct(it)
            }
        }

        viewModel.showProductDetail.observe(this) {
            if (it) {
                binding.sizeWidth.requestFocus()
            } else {
                binding.productCodeEdt.requestFocus()
            }
        }
    }

    override fun setClicks() {
        super.setClicks()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.productCodeEdt.setOnEditorActionListener { _, actionId, _ ->
            val isValidBarcode = viewModel.searchProductCode.value.trim().isNotEmpty()

            if (((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_DONE)) && isValidBarcode) {
                viewModel.getProductByCode()
            }

            hideSoftKeyboard()
            true
        }

        binding.createBtn.setOnSingleClickListener {

            viewModel.updateProductDimensions()

        }



        binding.searchProductCodeLyt.setEndIconOnClickListener {
            findNavController().navigate(
                RecordingSizingBarcodeFragmentDirections.actionRecordingSizingBarcodeFragmentToProductListDialogFragment()
            )
        }
    }

    override fun onStart() {
        super.onStart()

        binding.productCodeEdt.requestFocus()
    }


}