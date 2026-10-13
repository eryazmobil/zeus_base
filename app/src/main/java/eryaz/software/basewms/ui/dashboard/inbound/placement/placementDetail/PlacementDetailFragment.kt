package eryaz.software.basewms.ui.dashboard.inbound.placement.placementDetail

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import android.widget.Toast
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
import eryaz.software.basewms.databinding.FragmentPlacementDetailBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.inbound.acceptance.acceptanceProcess.AcceptanceProcessFragmentDirections
import eryaz.software.basewms.ui.dashboard.recording.dialog.ProductListDialogFragment
import eryaz.software.basewms.util.adapter.inbound.adapter.SuggestionShelfListAdapter
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.dialogs.QuestionDialog
import eryaz.software.basewms.util.extensions.copyToClipboard
import eryaz.software.basewms.util.extensions.handleProgress
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.onBackPressedCallback
import eryaz.software.basewms.util.extensions.onBarcodeOrShelfReadListener
import eryaz.software.basewms.util.extensions.parcelable
import eryaz.software.zeus.zeusViewModel

class PlacementDetailFragment : BaseFragment() {
    override val viewModel by zeusViewModel<PlacementDetailVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentPlacementDetailBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding.placementViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
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

        binding.toolbar.setNavigationOnClickListener {
            backToPage()
        }

        binding.toolbar.setMenuOnClickListener {
            popupMenu(it)
        }

        binding.searchProductBarcodePlacement.setEndIconOnClickListener {
            findNavController().navigate(
                PlacementDetailFragmentDirections.actionPlacementDetailFragmentToProductListDialogFragment()
            )
        }

        binding.searchEdt.onBarcodeOrShelfReadListener {
            viewModel.getBarcodeByCode()

        }

        binding.shelfAddressEdt.onBarcodeOrShelfReadListener {
            viewModel.getShelfByCode()
        }

        binding.controlBtn.setOnSingleClickListener {
            when {
                viewModel.isQuantityValid() -> {
                    errorDialog.show(
                        context = context,
                        ErrorDialogDto(
                            messageRes = R.string.msg_placement_qty_must_more_than_0
                        )
                    )
                }

                else -> QuestionDialog(
                    onPositiveClickListener = {
                        viewModel.updateWaybillControlAddQuantity()
                    },
                    textHeader = resources.getString(R.string.verify_qty),
                    textMessage = viewModel.resultAmountTxt.value,
                    positiveBtnText = resources.getString(eryaz.software.basewms.data.R.string.yes),
                    negativeBtnText = resources.getString(R.string.no),
                    negativeBtnViewVisible = true,
                    icType = IconType.Warning.ordinal
                ).show(parentFragmentManager, "dialog")

            }
        }

        adapter.onItemClick = {
            context?.copyToClipboard(it.shelfAddress)
            Toast.makeText(context, "Kopyalandı", Toast.LENGTH_SHORT).show()
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
            binding.controlBtn.handleProgress(data, progressColor = Color.WHITE)
        }

        viewModel.controlSuccess.asLiveData().observe(viewLifecycleOwner) {
            binding.searchEdt.setText("")
            binding.quantityEdt.setText("")
            binding.shelfAddressEdt.setText("")
            binding.searchEdt.requestFocus()
        }

        viewModel.showProductDetailView.asLiveData().observe(viewLifecycleOwner) {
            binding.quantityEdt.requestFocus()
        }

        viewModel.actionIsFinished.asLiveData().observe(viewLifecycleOwner) {
            if (it){
                findNavController().navigateUp()
                TemporaryCashManager.getInstance().workActivity = null
                TemporaryCashManager.getInstance().workAction = null
            }

        }

        viewModel.suggestionShelfList.asLiveData().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.checkProductInPlacementList
            .asLiveData()
            .observe(viewLifecycleOwner) {
                if (it) {
                    backToPage()
                }
            }
    }

    private val adapter by lazy {
        SuggestionShelfListAdapter().also {
            binding.suggestionShelfRecycleView.adapter = it
        }
    }

    private fun popupMenu(view: View) {
        PopupMenu(requireContext(), view).apply {
            inflate(R.menu.placement_menu)

            setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.menu_list_detail -> {
                        findNavController().navigate(
                            PlacementDetailFragmentDirections.actionPlacementDetailFragmentToWaybillDetailListDialog()
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