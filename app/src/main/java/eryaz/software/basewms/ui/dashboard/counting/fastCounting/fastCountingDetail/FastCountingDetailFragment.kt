package eryaz.software.basewms.ui.dashboard.counting.fastCounting.fastCountingDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.R
import eryaz.software.basewms.data.models.dto.ButtonDto
import eryaz.software.basewms.data.models.dto.ConfirmationDialogDto
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.databinding.FragmentFastCountingDetailBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.observe
import eryaz.software.basewms.util.extensions.onBackPressedCallback
import eryaz.software.zeus.zeusViewModel
import eryaz.software.zeus.params

class FastCountingDetailFragment : BaseFragment() {
    private val safeArgs by navArgs<FastCountingDetailFragmentArgs>()

    override val viewModel by zeusViewModel<FastCountingDetailVM> {
        params(safeArgs.headerId)
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentFastCountingDetailBinding.inflate(layoutInflater)
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
                        findNavController().navigateUp()
                    }),
                    negativeButton = ButtonDto(text = R.string.no,
                        onClickListener = { confirmationDialog.dismiss() })
                )
            )
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setMenuOnClickListener {
            //popupMenu(it)
        }

        binding.shelfAddressEdt.setOnEditorActionListener { _, actionId, _ ->

            val isValidBarcode = viewModel.searchShelf.value.trim().isNotEmpty()

            if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_DONE) && isValidBarcode) {
                viewModel.getShelfByCode()
            }

            hideSoftKeyboard()
            true
        }

        binding.searchProductEdt.setOnEditorActionListener { _, actionId, _ ->

            val isValidBarcode = viewModel.searchProduct.value.trim().isNotEmpty()

            if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_DONE) && isValidBarcode) {
                viewModel.getBarcodeByCode()
            }

            hideSoftKeyboard()
            true
        }

        binding.addProductBtn.setOnSingleClickListener {
            binding.saveBtn.visibility = View.VISIBLE
            viewModel.addProductToList()
            binding.searchProductEdt.requestFocus()
        }

        binding.infoBtn.setOnSingleClickListener {
            findNavController().navigate(
                FastCountingDetailFragmentDirections.actionFastCountingDetailFragmentToFastWillCountedListFragment(
                    viewModel.willCountedProductList.toTypedArray()
                )
            )
        }

        binding.saveBtn.setOnSingleClickListener {

            if (!viewModel.isValidFinish())
                return@setOnSingleClickListener

            errorDialog.show(context,
                ErrorDialogDto(
                    titleRes = R.string.different_value_enter,
                    messageRes = R.string.different_value_enter_sure,
                    positiveButton = ButtonDto(
                        text = R.string.yes,
                        onClickListener = {
                            errorDialog.show(
                                context, ErrorDialogDto(
                                    titleRes = R.string.sure,
                                    messageRes = R.string.sure_no_undo,
                                    positiveButton = ButtonDto(
                                        text = R.string.yes,
                                        onClickListener = {
                                            viewModel.saveBtn()
                                        }
                                    ),
                                    negativeButton = ButtonDto(
                                        text = R.string.no
                                    )
                                )
                            )
                        }
                    ),
                    negativeButton = ButtonDto(
                        text = R.string.no
                    )
                )
            )
        }
    }

    override fun subscribeToObservables() {

        viewModel.readShelfBarcode.observe(this) {
            if (it) {
                binding.searchProductEdt.requestFocus()
            }
        }

        viewModel.showProductDetail.observe(this) {
            if (it) {
                binding.quantityEdt.requestFocus()
            }
        }


//        viewModel.hasNotProductBarcode.observe(this) {
//            if (it) {
//                errorDialog.show(
//                    context, ErrorDialogDto(titleRes = R.string.error,
//                        messageRes = R.string.msg_no_barcode_and_new_barcode,
//                        positiveButton = ButtonDto(text = R.string.yes, onClickListener = {
//                            findNavController().navigate(
//                               FirstCountingDetailFragmentDirections.actionFirstCountingDetailFragmentToCreateBarcodeDialog()
//                            )
//                            errorDialog.dismiss()
//                        }),
//                        negativeButton = ButtonDto(text = R.string.no, onClickListener = {
//                            errorDialog.dismiss()
//                            toast(getString(R.string.process_cancelled))
//                        })
//                    )
//                )
//            }
//        }
//
//        viewModel.showProductDetail.observe(this) {
//            if (it) {
//                binding.quantityEdt.requestFocus()
//            }
//        }
//
//        viewModel.productDetail.asLiveData().observe(viewLifecycleOwner) {
//            if (it != null) {
//                binding.quantityEdt.requestFocus()
//            }
//        }
//
//        viewModel.actionAddProduct.asLiveData().observe(viewLifecycleOwner) {
//            if (it) {
//                binding.searchProductEdt.requestFocus()
//                toast(getString(R.string.msg_process_success))
//            }
//        }
//
//        viewModel.actionIsFinished.observe(this) {
//            if (it) {
//                binding.shelfAddressEdt.requestFocus()
//            }
//        }
    }

}



