package eryaz.software.basewms.ui.dashboard.inbound.acceptance.acceptanceProcess

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
import eryaz.software.basewms.R
import eryaz.software.basewms.data.enums.IconType
import eryaz.software.basewms.data.models.dto.ButtonDto
import eryaz.software.basewms.data.models.dto.ConfirmationDialogDto
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.data.persistence.TemporaryCashManager
import eryaz.software.basewms.databinding.FragmentAcceptanceProcessDetailBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.recording.dialog.ProductListDialogFragment
import eryaz.software.basewms.ui.dashboard.recording.recordBarcode.BarcodeRecordingFragmentDirections
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.dialogs.QuestionDialog
import eryaz.software.basewms.util.extensions.handleProgress
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.onBackPressedCallback
import eryaz.software.basewms.util.extensions.onBarcodeOrShelfReadListener
import eryaz.software.basewms.util.extensions.parcelable
import eryaz.software.basewms.util.extensions.toDoubleOrZero
import kotlinx.coroutines.FlowPreview
import eryaz.software.zeus.zeusViewModel

@OptIn(FlowPreview::class)
class AcceptanceProcessFragment : BaseFragment() {

    override val viewModel by zeusViewModel<AcceptanceProcessVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentAcceptanceProcessDetailBinding.inflate(layoutInflater)
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

    override fun subscribeToObservables() {
        setFragmentResultListener(ProductListDialogFragment.REQUEST_KEY) { _, bundle ->
            val dto = bundle.parcelable<ProductDto>(ProductListDialogFragment.ARG_PRODUCT_DTO)
            dto?.let {
                viewModel.setEnteredProduct(it)
            }
        }

        viewModel.showProductDetailView
            .asLiveData()
            .observe(viewLifecycleOwner) {
                binding.quantityEdt.requestFocus()
            }

        viewModel.controlSuccess
            .asLiveData()
            .observe(viewLifecycleOwner) {
                if (it) {
                    showFinishDialog()
                }
                binding.quantityEdt.setText("")
                binding.searchEdt.setText("")

                binding.searchEdt.requestFocus()
            }

        viewModel.uiState.asLiveData().observe(viewLifecycleOwner) { data ->
            binding.controlBtn.handleProgress(data, progressColor = Color.WHITE)
        }

        viewModel.actionIsFinished.asLiveData()
            .observe(viewLifecycleOwner) {
                if (it) {
                    findNavController().navigateUp()
                    TemporaryCashManager.getInstance().workActivity = null
                    TemporaryCashManager.getInstance().workAction = null
                }

            }

        viewModel.showCreateBarcode
            .asLiveData()
            .observe(this) {
                if (it)
                    QuestionDialog(
                        onPositiveClickListener = {
                            findNavController().navigate(
                                AcceptanceProcessFragmentDirections.actionAcceptanceProcessFragmentToCreateBarcodeDialog3(
                                    binding.searchEdt.text.toString()
                                )
                            )

                        },
                        textHeader = resources.getString(eryaz.software.basewms.data.R.string.attention),
                        textMessage = resources.getString(eryaz.software.basewms.data.R.string.msg_no_barcode_and_new_barcode),
                        positiveBtnText = resources.getString(eryaz.software.basewms.data.R.string.yes),
                        negativeBtnText = resources.getString(eryaz.software.basewms.data.R.string.no),
                        negativeBtnViewVisible = true,
                        icType = IconType.Warning.ordinal
                    ).show(parentFragmentManager, "dialog")
            }
    }

    override fun setClicks() {
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

        binding.toolbar.setMenuOnClickListener {
            popupMenu(it)
        }

        binding.toolbar.setNavigationOnClickListener {
            backToPage()
        }

        binding.searchProductBarcodeAcceptance.setEndIconOnClickListener {
            findNavController().navigate(
                AcceptanceProcessFragmentDirections.actionAcceptanceProcessFragmentToProductListDialogFragment()
            )
        }

        binding.searchEdt.onBarcodeOrShelfReadListener {
            viewModel.getBarcodeByCode()
        }

        binding.controlBtn.setOnSingleClickListener {
            when {
                viewModel.isQuantityValid() -> {
                    errorDialog.show(
                        context = context,
                        ErrorDialogDto(
                            messageRes = R.string.msg_control_qty_must_more_than_0
                        )
                    )
                }

                else -> viewModel.updateWaybillControlAddQuantity(viewModel.quantity.value.toDoubleOrZero())
            }
        }
    }

    private fun showFinishDialog() {
        showConditionDialog(
            ConfirmationDialogDto(
                title = getString(R.string.control_finished),
                message = getString(R.string.all_control_finished),
                positiveButton = ButtonDto(
                    text = R.string.yes,
                    onClickListener = {
                        backToPage()
                    }
                ),
                negativeButton = ButtonDto(
                    text = R.string.no,
                    onClickListener = {
                    }
                )
            ))
    }

    private fun popupMenu(view: View) {
        PopupMenu(requireContext(), view).apply {
            inflate(R.menu.acceptance_menu)

            val menuItem = menu.findItem(R.id.quick_control)
            menuItem.isChecked = viewModel.serialCheck.value

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.quick_control -> {
                        val newValue = !(viewModel.serialCheck.value)
                        viewModel.serialCheck.value = newValue
                        menuItem.isChecked = newValue
                        true
                    }

                    R.id.menu_list_detail -> {
                        findNavController().navigate(
                            AcceptanceProcessFragmentDirections
                                .actionAcceptanceProcessFragmentToWaybillDetailListDialog()
                        )
                        true
                    }

                    R.id.menu_finish_action -> {
                        backToPage()
                        true
                    }

                    else -> false
                }
            }

            show()
        }
    }

    private fun backToPage() {
        viewModel.finishWorkAction()
    }

    override fun onResume() {
        super.onResume()
        binding.searchEdt.requestFocus()
    }
}
