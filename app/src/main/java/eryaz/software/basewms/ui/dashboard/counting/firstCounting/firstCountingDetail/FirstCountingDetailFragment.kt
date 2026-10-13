package eryaz.software.basewms.ui.dashboard.counting.firstCounting.firstCountingDetail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.R
import eryaz.software.basewms.data.models.dto.ButtonDto
import eryaz.software.basewms.data.models.dto.ConfirmationDialogDto
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.databinding.FragmentFirstCountingDetailBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.recording.dialog.ProductListDialogFragment
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.handleProgress
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.observe
import eryaz.software.basewms.util.extensions.onBackPressedCallback
import eryaz.software.basewms.util.extensions.onBarcodeOrShelfReadListener
import eryaz.software.basewms.util.extensions.parcelable
import eryaz.software.basewms.util.extensions.toast
import eryaz.software.zeus.zeusViewModel
import eryaz.software.zeus.params

class FirstCountingDetailFragment : BaseFragment() {
    private val safeArgs by navArgs<FirstCountingDetailFragmentArgs>()

    override val viewModel by zeusViewModel<FirstCountingDetailVM> {
        params(safeArgs.stockTakingHeaderId)
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentFirstCountingDetailBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }

    override fun setClicks() {
        binding.shelfAddressEdt.requestFocus()

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

        binding.toolbar.setMenuOnClickListener {
            popupMenu(it)
        }

        binding.searchProductTil.setEndIconOnClickListener {
            findNavController().navigate(FirstCountingDetailFragmentDirections.actionFirstCountingDetailFragmentToProductListDialogFragment3())
        }

        binding.shelfAddressEdt.onBarcodeOrShelfReadListener {
            viewModel.getShelfByCode()
        }
        binding.searchProductEdt.onBarcodeOrShelfReadListener {
            viewModel.getBarcodeByCode()

        }

        binding.shelfAddress.setEndIconOnClickListener {

            if (viewModel.validShelf.value) {
                findNavController().navigate(
                    FirstCountingDetailFragmentDirections.actionFirstCountingDetailFragmentToInfoFirstCountingFragment(
                        viewModel.stHeaderId, viewModel.assignedShelfId
                    )
                )
            } else {
                errorDialog.show(
                    requireContext(),
                    ErrorDialogDto(
                        titleRes = R.string.warning,
                        messageRes = R.string.enter_shelf
                    )
                )
            }

        }

        binding.addProductBtn.setOnSingleClickListener {

            viewModel.checkProductExistsInShelfForPda { isSuccess, oldQty ->
                when {
                    oldQty > 0.0 -> {
                        errorDialog.show(
                            context, ErrorDialogDto(
                                titleRes = R.string.attached_product,
                                messageRes = R.string.msg_attached_before,
                                positiveButton = ButtonDto(
                                    text = R.string.add_on,
                                    onClickListener = {
                                        viewModel.addProduct(oldQty)
                                        errorDialog.dismiss()
                                    }),
                                negativeButton = ButtonDto(
                                    text = R.string.save_last_entered_quantity,
                                    onClickListener = {
                                        viewModel.addProduct()
                                        errorDialog.dismiss()
                                    })
                            )
                        )
                    }

                    else -> viewModel.addProduct()
                }

            }
        }
    }

    override fun subscribeToObservables() {

        setFragmentResultListener(ProductListDialogFragment.REQUEST_KEY) { _, bundle ->
            val dto = bundle.parcelable<ProductDto>(ProductListDialogFragment.ARG_PRODUCT_DTO)
            dto?.let {
                viewModel.setEnteredProduct(it)
            }
        }

        viewModel.readShelfBarcode.asLiveData().observe(this) {
            if (it) {
                binding.searchProductEdt.requestFocus()
            }
        }

        viewModel.hasNotProductBarcode.asLiveData().observe(this) {
            if (it) {
                errorDialog.show(
                    context, ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.msg_no_barcode_and_new_barcode,
                        positiveButton = ButtonDto(text = R.string.yes, onClickListener = {
                            findNavController().navigate(
                                FirstCountingDetailFragmentDirections.actionFirstCountingDetailFragmentToCreateBarcodeDialog()
                            )
                            errorDialog.dismiss()
                        }),
                        negativeButton = ButtonDto(text = R.string.no, onClickListener = {
                            errorDialog.dismiss()
                            toast(getString(R.string.process_cancelled))
                        })
                    )
                )
            }
        }

        viewModel.showProductDetail.observe(this) {
            if (it) {
                binding.quantityEdt.requestFocus()
            }
        }

        viewModel.productDetail.asLiveData().observe(viewLifecycleOwner) {
            if (it != null) {
                binding.quantityEdt.requestFocus()
            }
        }

        viewModel.actionAddProduct.asLiveData().observe(this) {
            if (it) binding.searchProductEdt.requestFocus()
            toast(getString(R.string.msg_process_success))
        }

        viewModel.actionIsFinished.asLiveData().observe(this) {
            if (it)
                binding.shelfAddressEdt.requestFocus()
        }

        viewModel.uiState.asLiveData().observe(viewLifecycleOwner) { data ->
            binding.addProductBtn.handleProgress(data, progressColor = Color.WHITE)
        }
    }

    private fun popupMenu(view: View) {
        PopupMenu(requireContext(), view).apply {
            inflate(R.menu.counting_menu)

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.save -> {
                        if (viewModel.stDetailId != 0) {
                            viewModel.saveBtn()
                        } else {
                            errorDialog.show(
                                context,
                                ErrorDialogDto(
                                    titleRes = R.string.error,
                                    messageRes = R.string.counting_empty
                                )
                            )
                        }
                        true
                    }

                    else -> false
                }
            }
            show()
        }
    }

    private fun backToPage() {
        findNavController().navigateUp()
    }
}



