package eryaz.software.basewms.ui.dashboard.recording.recordBarcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.R
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.databinding.FragmentRecordingBarcodeBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.recording.dialog.ProductListDialogFragment
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.observe
import eryaz.software.basewms.util.extensions.parcelable
import eryaz.software.basewms.util.extensions.toast
import eryaz.software.zeus.zeusViewModel

class BarcodeRecordingFragment : BaseFragment() {
    override val viewModel by zeusViewModel<RecordBarcodeVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentRecordingBarcodeBinding.inflate(layoutInflater)
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
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.productCodeEdt.setOnEditorActionListener { _, actionId, _ ->
            val isValidBarcode = viewModel.searchProductCode.value.trim().isNotEmpty()

            if (((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_DONE)) || actionId == EditorInfo.IME_ACTION_DONE && isValidBarcode) {
                viewModel.getProductByCode()
            }

            hideSoftKeyboard()
            true
        }

        binding.searchProductCodeLyt.setEndIconOnClickListener {
           findNavController().navigate(
               BarcodeRecordingFragmentDirections.actionBarcodeRecordingFragmentToProductListDialogFragment()
           )
        }

        binding.createBtn.setOnSingleClickListener {
            viewModel.createBarcode()
        }
    }

    override fun subscribeToObservables() {

        setFragmentResultListener(ProductListDialogFragment.REQUEST_KEY) { _, bundle ->
            val dto = bundle.parcelable<ProductDto>(ProductListDialogFragment.ARG_PRODUCT_DTO)
            dto?.let {
                viewModel.setEnteredProduct(it)
            }
        }

        viewModel.showProductDetail.observe(this) {
            if (it) {
                binding.productBarcodeEdt.requestFocus()
            }
        }

        viewModel.createdBarcode.asLiveData()
            .observe(viewLifecycleOwner) {
                toast(getString(R.string.msg_process_success))
                binding.productCodeEdt.requestFocus()
            }

    }

    override fun onStart() {
        super.onStart()

        binding.productCodeEdt.requestFocus()
    }

    override fun hideActionBar() = false
}