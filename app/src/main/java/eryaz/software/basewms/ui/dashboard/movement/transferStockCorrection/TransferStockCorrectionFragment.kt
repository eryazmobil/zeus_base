package eryaz.software.basewms.ui.dashboard.movement.transferStockCorrection

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.R
import eryaz.software.basewms.data.models.dto.StockTypeDto
import eryaz.software.basewms.data.models.dto.StorageDto
import eryaz.software.basewms.databinding.FragmentTransferStockCorrectionBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.movement.transferStockCorrection.stockType.StockTypeFragment
import eryaz.software.basewms.ui.dashboard.movement.transferStockCorrection.storageList.StorageListDialogFragment
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.handleProgress
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.parcelable
import eryaz.software.basewms.util.extensions.toast
import eryaz.software.zeus.zeusViewModel

class TransferStockCorrectionFragment : BaseFragment() {
    override val viewModel by zeusViewModel<TransferStockCorrectionVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentTransferStockCorrectionBinding.inflate(layoutInflater)
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

        binding.searchProductEdt.setOnEditorActionListener { _, actionId, _ ->
            val isValidBarcode = viewModel.searchProduct.value.trim().isNotEmpty()

            if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_DONE) && isValidBarcode) {
                viewModel.getBarcodeByCode()
            }

            hideSoftKeyboard()
            true
        }

        binding.shelfAddressEdt.setOnEditorActionListener { _, actionId, _ ->
            val isValidAddress = viewModel.enterShelfValue.value.trim().isNotEmpty()

            if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_DONE) && isValidAddress) {

                viewModel.getShelfByCode(viewModel.enterShelfValue.value.trim(), false)
            }

            hideSoftKeyboard()
            true
        }

        binding.choiceStorage.setOnSingleClickListener {
            findNavController().navigate(
                TransferStockCorrectionFragmentDirections.actionTransferStockCorrectionFragmentToStorageListDialogFragment(
                    false
                )
            )
        }

        binding.deleteProduct.setOnSingleClickListener {
            findNavController().navigate(
                TransferStockCorrectionFragmentDirections.actionTransferStockCorrectionFragmentToStockTypeFragment()
            )
        }

        binding.transferBtn.setOnSingleClickListener {
            viewModel.createStorageMovement()
        }
    }

    override fun subscribeToObservables() {


        setFragmentResultListener(StockTypeFragment.REQUEST_KEY) { _, bundle ->
            val dto = bundle.parcelable<StockTypeDto>(StockTypeFragment.ARG_STORAGE_DTO)

            dto?.let {
                viewModel.setStockType(it)
            }
        }

        setFragmentResultListener(StorageListDialogFragment.REQUEST_KEY) { _, bundle ->
            val dto = bundle.parcelable<StorageDto>(StorageListDialogFragment.ARG_STORAGE_DTO)

            dto?.let {
                binding.edtQuantity.requestFocus()
                viewModel.setStorage(it)
            }
        }

        viewModel.uiState.asLiveData().observe(viewLifecycleOwner) { data ->
            binding.transferBtn.handleProgress(data, progressColor = Color.WHITE)
        }

        viewModel.showProductDetail.asLiveData().observe(viewLifecycleOwner){
            binding.shelfAddressEdt.requestFocus()
        }

        viewModel.exitShelfId.asLiveData()
            .observe(viewLifecycleOwner) {
                if (it.toInt() != 0)
                    binding.edtQuantity.requestFocus()
            }

        viewModel.transferSuccess.asLiveData()
            .observe(viewLifecycleOwner) {
                if (it) {
                    toast(getString(R.string.transfer_success))
                }
            }

    }

    override fun onResume() {
        super.onResume()
        binding.searchProductEdt.requestFocus()

    }

    override fun hideActionBar() = false
}