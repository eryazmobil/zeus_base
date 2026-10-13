package eryaz.software.basewms.ui.dashboard.inbound.dat.placement.placementDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.R
import eryaz.software.basewms.data.enums.IconType
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.persistence.TemporaryCashManager
import eryaz.software.basewms.databinding.FragmentDatPlacementDetailBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.util.adapter.inbound.adapter.SuggestionShelfListAdapter
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.dialogs.QuestionDialog
import eryaz.software.basewms.util.enums.DatType
import eryaz.software.basewms.util.extensions.copyToClipboard
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.onBackPressedCallback
import eryaz.software.zeus.zeusViewModel

class DatPlacementDetailFragment : BaseFragment() {
    override val viewModel by zeusViewModel<DatPlacementDetailVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentDatPlacementDetailBinding.inflate(layoutInflater)
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
            QuestionDialog(
                onPositiveClickListener = {
                    backToPage()
                },
                textHeader = resources.getString(eryaz.software.basewms.data.R.string.exit),
                textMessage = resources.getString(eryaz.software.basewms.data.R.string.are_you_sure),
                positiveBtnText = resources.getString(eryaz.software.basewms.data.R.string.yes),
                negativeBtnText = resources.getString(R.string.no),
                singleBtnText = "",
                negativeBtnViewVisible = true,
                icType = IconType.Success.ordinal
            ).show(parentFragmentManager, "dialog")
        }

        binding.toolbar.setNavigationOnClickListener {
            backToPage()
        }

        binding.toolbar.setMenuOnClickListener {
            popupMenu(it)
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
        viewModel.controlSuccess.asLiveData().observe(viewLifecycleOwner) {
            binding.searchEdt.setText("")
            binding.quantityEdt.setText("")
            binding.shelfAddressEdt.setText("")
            binding.searchEdt.requestFocus()
        }

        viewModel.productDetail.asLiveData().observe(viewLifecycleOwner) {
            binding.quantityEdt.requestFocus()
        }

        viewModel.suggestionShelfList.asLiveData().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.actionIsFinished.asLiveData().observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigateUp()
                TemporaryCashManager.getInstance().workActivity = null
                TemporaryCashManager.getInstance().workAction = null
            }
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
                            DatPlacementDetailFragmentDirections.actionDatPlacementDetailFragmentToAcceptanceTransferDetailFragment(
                                DatType.FromPlacement
                            )
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