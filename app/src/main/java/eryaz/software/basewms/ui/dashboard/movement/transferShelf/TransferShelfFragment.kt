package eryaz.software.basewms.ui.dashboard.movement.transferShelf

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.R
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.databinding.FragmentTransferAddressBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.recording.dialog.ProductListDialogFragment
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.handleProgress
import eryaz.software.basewms.util.extensions.observe
import eryaz.software.basewms.util.extensions.onBarcodeOrShelfReadListener
import eryaz.software.basewms.util.extensions.parcelable
import eryaz.software.basewms.util.extensions.toast
import eryaz.software.zeus.zeusViewModel

class TransferShelfFragment : BaseFragment() {
    override val viewModel by zeusViewModel<TransferShelfVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentTransferAddressBinding.inflate(layoutInflater)
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

        binding.searchEdt.onBarcodeOrShelfReadListener {
            viewModel.getBarcodeByCode()
        }

        binding.exitShelfAddressEdt.onBarcodeOrShelfReadListener {

            viewModel.getShelfByCode(viewModel.exitShelfValue.value.trim(), false)

        }

        binding.enterShelfAddressEdt.onBarcodeOrShelfReadListener {
            viewModel.getShelfByCode(viewModel.enterShelfValue.value.trim(), true)

        }


        binding.transferBtn.setOnSingleClickListener {
            viewModel.createAddressMovement()
        }

        binding.searchProductBarcode.setEndIconOnClickListener {
            findNavController().navigate(
                TransferShelfFragmentDirections.actionTransferAddressFragmentToProductListDialogFragment()
            )
        }

        binding.shelfAddressOut.setEndIconOnClickListener {
            findNavController().navigate(
                TransferShelfFragmentDirections.actionTransferAddressFragmentToShelfStorageDialog3(
                    viewModel.productDetail.value?.id ?: 0
                )
            )
        }
    }

    override fun subscribeToObservables() {

        setFragmentResultListener(ProductListDialogFragment.REQUEST_KEY) { _, bundle ->
            val dto = bundle.parcelable<ProductDto>(ProductListDialogFragment.ARG_PRODUCT_DTO)
            dto?.let {
                viewModel.setEnteredProduct(it)
            }
        }

        viewModel.uiState.asLiveData().observe(viewLifecycleOwner) { data ->
            binding.transferBtn.handleProgress(data, progressColor = Color.WHITE)
        }

        viewModel.exitShelfId.asLiveData()
            .observe(viewLifecycleOwner) {
                if (it.toInt() != 0)
                    binding.edtQuantity.requestFocus()
            }

        viewModel.transferSuccess.asLiveData()
            .observe(viewLifecycleOwner) {
                toast(getString(R.string.transfer_success))
                binding.searchEdt.requestFocus()
            }

        viewModel.showProductDetail.observe(this) {
            if (it) {
                binding.exitShelfAddressEdt.requestFocus()
            }
        }

    }

    override fun onStart() {
        super.onStart()

        binding.searchEdt.requestFocus()
    }

    override fun hideActionBar() = false
}