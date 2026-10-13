package eryaz.software.basewms.ui.dashboard.movement.supply.supplyShelf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.R
import eryaz.software.basewms.data.enums.ShelfType
import eryaz.software.basewms.data.models.dto.ButtonDto
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.databinding.FragmentSupplyShelfBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.movement.supply.supplyList.SupplyListFragmentDirections
import eryaz.software.basewms.ui.dashboard.outbound.orderPicking.orderPickingDetail.OrderPickingDetailFragmentDirections
import eryaz.software.basewms.ui.dashboard.recording.dialog.ProductListDialogFragment
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.observe
import eryaz.software.basewms.util.extensions.onBackPressedCallback
import eryaz.software.basewms.util.extensions.parcelable
import eryaz.software.basewms.util.extensions.toArrayList
import eryaz.software.basewms.util.extensions.toIntOrZero
import eryaz.software.zeus.zeusViewModel

class SupplyShelfFragment : BaseFragment() {

    override val viewModel by zeusViewModel<SupplyShelfVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentSupplyShelfBinding.inflate(layoutInflater)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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

        viewModel.finishWorkAction.observe(this) {
            if (it) {
                findNavController().navigateUp()
            }
        }
        viewModel.showProductDetail.observe(this) {
            if (it) {
                binding.shelfAddressEdt.requestFocus()
            } else {
                binding.searchProductEdt.requestFocus()
            }
        }


        viewModel.parentView.observe(this) {
            if (it) {
                binding.parentView.visibility = View.GONE

                errorDialog.show(
                    context, ErrorDialogDto(
                        titleRes = R.string.warning,
                        messageRes = R.string.empty_work_activity_detail,
                        positiveButton = ButtonDto(text = R.string.close_screen, onClickListener = {
                            findNavController().navigateUp()
                        })
                    )
                )
            }
        }

        viewModel.checkAllFinished.observe(this) {
            if (it) {
                errorDialog.show(
                    context, ErrorDialogDto(
                        titleRes = R.string.warning,
                        messageRes = R.string.all_control_finished,
                        positiveButton = ButtonDto(text = R.string.yes, onClickListener = {
                            viewModel.finishWorkActivity { finish ->
                                if (finish) findNavController().navigateUp()
                            }
                        }),
                        negativeButton = ButtonDto(text = R.string.cancel)
                    )
                )
            } else {
                viewModel.showNext()
            }

        }

        binding.shelfAddressEdt.setOnEditorActionListener { _, actionId, _ ->

            val shelfAddress = viewModel.firstShelfName.value.trim().isNotEmpty()

            if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_DONE) && shelfAddress) {
                viewModel.getShelfByCode(ShelfType.STOCK, viewModel.firstShelfName.value)
            }

            hideSoftKeyboard()
            true
        }
        binding.shelfAddressEdtPicking.setOnEditorActionListener { _, actionId, _ ->

            val shelfAddress = viewModel.secondShelfName.value.trim().isNotEmpty()

            if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_DONE) && shelfAddress) {
                viewModel.getShelfByCode(ShelfType.GATHER, viewModel.secondShelfName.value)
            }

            hideSoftKeyboard()
            true
        }

        binding.searchProductEdt.setOnEditorActionListener { _, actionId, _ ->

            val isValidBarcode = viewModel.productBarcode.value.trim().isNotEmpty()

            if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_DONE) && isValidBarcode) {
                viewModel.getBarcodeByCode()
            }

            hideSoftKeyboard()
            true
        }


    }

    override fun setClicks() {

        onBackPressedCallback {
            viewModel.finishWorkAction {
                findNavController().navigateUp()
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            viewModel.finishWorkAction {
                findNavController().navigateUp()
            }
        }

        binding.shelfPlacement.setOnSingleClickListener {

            viewModel.createShelfMovementForReplenishment(
                quantity = binding.quantityEdt.text.toString().toIntOrZero()
            )
        }

        binding.searchProductBarcodeTill.setEndIconOnClickListener {
            findNavController().navigate(
                SupplyShelfFragmentDirections.actionSupplyShelfFragmentToProductListDialogFragment()
            )
        }

        binding.shelfListBtn.setOnSingleClickListener {

            val action = SupplyShelfFragmentDirections.actionSupplyShelfFragmentToSupplyShelfListDialog()
            action.productShelfSupplyDto = viewModel.stockMovementShelfList.value?.toTypedArray()
            findNavController().navigate(action)
        }


        binding.toolbar.setMenuOnClickListener {
            popupMenu(it)
        }

    }

    private fun popupMenu(view: View) {
        PopupMenu(requireContext(), view).apply {
            inflate(R.menu.supply_shelf_menu)

            setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.finish_workActivity -> {
                        viewModel.finishWorkActivity {
                            if (it) findNavController().navigateUp()
                        }
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }


}