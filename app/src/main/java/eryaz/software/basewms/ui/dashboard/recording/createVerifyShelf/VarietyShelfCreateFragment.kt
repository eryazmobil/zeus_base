package eryaz.software.basewms.ui.dashboard.recording.createVerifyShelf

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
import eryaz.software.basewms.databinding.FragmentVarietyShelfCreateBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.recording.dialog.ProductListDialogFragment
import eryaz.software.basewms.ui.dashboard.recording.recordBarcode.BarcodeRecordingFragmentDirections
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.observe
import eryaz.software.basewms.util.extensions.parcelable
import eryaz.software.basewms.util.extensions.toast
import eryaz.software.zeus.zeusViewModel

class VarietyShelfCreateFragment : BaseFragment() {
    override val viewModel by zeusViewModel<VarietyShelfCreateVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentVarietyShelfCreateBinding.inflate(layoutInflater)
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

        binding.productBarcodeEdt.setOnEditorActionListener { _, actionId, _ ->
            val isValidBarcode = viewModel.productBarcode.value.trim().isNotEmpty()

            if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_DONE) && isValidBarcode) {
                viewModel.getBarcodeByCode()
            }

            hideSoftKeyboard()
            true
        }

        binding.shelfAddressEdt.setOnEditorActionListener { _, actionId, _ ->
            val isValidBarcode = viewModel.shelfAddress.value.trim().isNotEmpty()

            if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_DONE) && isValidBarcode) {
                viewModel.getShelfAddressFromApi()
            }

            hideSoftKeyboard()
            true
        }

        binding.productBarcodeLyt.setEndIconOnClickListener {
            findNavController().navigate(
                VarietyShelfCreateFragmentDirections.actionVarietyShelfCreateFragmentToProductListDialogFragment()
            )
        }

        binding.createBtn.setOnSingleClickListener {
            viewModel.createShelfInsert()
        }

        binding.updateBtn.setOnSingleClickListener {
            viewModel.updateProductShelf()
        }
        binding.deleteBtn.setOnSingleClickListener {
            viewModel.deleteProductShelf()
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

        viewModel.shelfSuccess.observe(this) {
            if (it) {
                binding.amountLyt.requestFocus()
            }
        }

        viewModel.createSuccess.asLiveData()
            .observe(viewLifecycleOwner) {
                if (it) {
                    toast(getString(R.string.msg_process_success))
                    binding.productBarcodeEdt.requestFocus()
                }
            }

        viewModel.showProductDetail.observe(this) {
            if (it) {
                binding.shelfAddressEdt.requestFocus()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        binding.productBarcodeEdt.requestFocus()
    }

    override fun hideActionBar() = false
}