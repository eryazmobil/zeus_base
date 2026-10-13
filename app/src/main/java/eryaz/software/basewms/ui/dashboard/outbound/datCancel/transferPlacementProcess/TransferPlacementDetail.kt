package eryaz.software.basewms.ui.dashboard.outbound.datCancel.transferPlacementProcess

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.R
import eryaz.software.basewms.data.models.dto.ButtonDto
import eryaz.software.basewms.data.models.dto.ConfirmationDialogDto
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.data.persistence.TemporaryCashManager
import eryaz.software.basewms.databinding.FragmentTransferPlacementDetailBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.recording.dialog.ProductListDialogFragment
import eryaz.software.basewms.util.adapter.inbound.adapter.SuggestionShelfListAdapter
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.copyToClipboard
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.onBackPressedCallback
import eryaz.software.basewms.util.extensions.parcelable
import eryaz.software.zeus.zeusViewModel

class TransferPlacementDetail : BaseFragment() {

    override val viewModel by zeusViewModel<TransferPlacementDetailVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentTransferPlacementDetailBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.placementViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }


    override fun subscribeToObservables() {

        setFragmentResultListener(ProductListDialogFragment.REQUEST_KEY) { _, bundle ->
            val dto = bundle.parcelable<ProductDto>(ProductListDialogFragment.ARG_PRODUCT_DTO)
            dto?.let {
                viewModel.setEnteredProduct(it)
            }
        }

        binding.searchEdt.setOnEditorActionListener { _, actionId, _ ->

            val isValidBarcode = viewModel.searchProduct.value.trim().isNotEmpty()

            if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_DONE) && isValidBarcode) {
                viewModel.getBarcodeByCode()
            }

            hideSoftKeyboard()
            true
        }

        binding.shelfAddressEdt.setOnEditorActionListener { _, actionId, _ ->

            val isValidBarcode = viewModel.searchShelf.value.trim().isNotEmpty()

            if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_DONE) && isValidBarcode) {
                viewModel.getShelfByCode()
            }

            hideSoftKeyboard()
            true
        }

        viewModel.hasProductToReplacement.asLiveData().observe(viewLifecycleOwner){
            binding.searchProductBarcodePlacement.requestFocus()
        }

        viewModel.suggestionShelfList.asLiveData().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.showProductDetailView.asLiveData().observe(viewLifecycleOwner) {
            if (it) binding.quantityEdt.requestFocus() else binding.searchEdt.requestFocus()
        }

        viewModel.controlSuccess.asLiveData().observe(viewLifecycleOwner) {
            binding.searchEdt.setText("")
            binding.quantityEdt.setText("")
            binding.shelfAddressEdt.setText("")
            binding.searchEdt.requestFocus()
        }

        viewModel.checkProductInPlacementList.asLiveData().observe(viewLifecycleOwner){
            if (it) {
                backToPage()
            }
        }

    }

    override fun setClicks() {

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.searchProductBarcodePlacement.setEndIconOnClickListener {
            findNavController().navigate(
                TransferPlacementDetailDirections.actionTransferPlacementDetailToProductListDialogFragment()
            )
        }
        onBackPressedCallback {
            showConditionDialog(
                ConfirmationDialogDto(
                    title = getString(R.string.exit),
                    message = getString(R.string.are_you_sure),
                    positiveButton = ButtonDto(text = R.string.yes, onClickListener = {
                        backToPage()
                    }),
                    negativeButton = ButtonDto(
                        text = R.string.no,
                        onClickListener = { confirmationDialog.dismiss() })
                )
            )
        }

        binding.toolbar.setNavigationOnClickListener {
            backToPage()
        }

        binding.controlBtn.setOnSingleClickListener{
            viewModel.updateTransferReplacementAddQuantityForPda()
        }

        adapter.onItemClick = {
            context?.copyToClipboard(it.shelfAddress)
            Toast.makeText(context, getString(R.string.copied), Toast.LENGTH_SHORT).show()
        }

    }

    private fun backToPage() {
        viewModel.finishWorkAction()
        viewModel.actionIsFinished.asLiveData().observe(viewLifecycleOwner) {
            findNavController().navigateUp()
            TemporaryCashManager.getInstance().workActivity = null
            TemporaryCashManager.getInstance().workAction = null
        }
    }

    private val adapter by lazy {
        SuggestionShelfListAdapter().also {
            binding.suggestionShelfRecycleView.adapter = it
        }
    }

}