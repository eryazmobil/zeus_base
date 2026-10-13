package eryaz.software.basewms.ui.dashboard.outbound.datPicking.datPickingDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.R
import eryaz.software.basewms.data.models.dto.ButtonDto
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.databinding.FragmentDatPickingDetailBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.enums.DatType
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.observe
import eryaz.software.basewms.util.extensions.onBackPressedCallback
import eryaz.software.basewms.util.extensions.onBarcodeOrShelfReadListener
import eryaz.software.basewms.util.extensions.toast
import eryaz.software.zeus.zeusViewModel

class DatPickingDetailFragment : BaseFragment() {
    override val viewModel by zeusViewModel<DatPickingDetailVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentDatPickingDetailBinding.inflate(layoutInflater)
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
        onBackPressedCallback {
            viewModel.checkCrossDockNeedByActionId()
        }

        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        binding.toolbar.setMenuOnClickListener {
            popupMenu(it)
        }

        binding.shelfListBtn.setOnSingleClickListener {
            findNavController().navigate(
                DatPickingDetailFragmentDirections.actionDatPickingDetailFragmentToShelfListDialog(
                    viewModel.productId
                )
            )
        }

        binding.pickProductBtn.setOnSingleClickListener {
            viewModel.updateOrderDetailCollectedAddQuantityForPda()
        }
    }

    override fun subscribeToObservables() {
        viewModel.parentView.observe(this) {
            if (it) {
                binding.parentView.visibility = View.GONE

                errorDialog.show(
                    context, ErrorDialogDto(
                        titleRes = R.string.warning,
                        messageRes = R.string.work_activity_error_1,
                        positiveButton = ButtonDto(text = R.string.close_screen, onClickListener = {
                            viewModel.checkCrossDockNeedByActionId()
                        })
                    )
                )
            }
        }

        viewModel.createStockOut.observe(this) {
            if (it) {
                toast(getString(R.string.success))
            }
        }

        viewModel.showProductDetail.observe(this) {
            if (it) {
                binding.shelfAddressEdt.requestFocus()
                binding.stateView.setViewVisible(binding.productDetailParent, true)
            } else {
                binding.searchProductEdt.requestFocus()
            }
        }

        binding.searchProductEdt.onBarcodeOrShelfReadListener {
            viewModel.getBarcodeByCode()
        }

        binding.shelfAddressEdt.onBarcodeOrShelfReadListener {
            viewModel.getShelfByCode()
        }

        viewModel.nextOrder.observe(this) {
            if (it) {
                binding.nextBtn.performClick()
            }
        }

        viewModel.shelfRead.asLiveData().observe(viewLifecycleOwner) {
            if (it) {
                binding.quantityEdt.requestFocus()
            }
        }

        viewModel.finishWorkAction.observe(this) {
            if (it) {
                findNavController().navigateUp()
            }
        }
    }

    private fun popupMenu(view: View) {
        PopupMenu(requireContext(), view).apply {
            inflate(R.menu.order_picking_menu)

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.order_detail_list -> {
                        findNavController().navigate(
                            DatPickingDetailFragmentDirections.actionDatPickingDetailFragmentToTransferDetailListDialog(DatType.FromPicking)
                        )
                        true
                    }

                    R.id.menu_finish_action -> {
                        activity?.onBackPressed()
                        true
                    }

                    else -> false
                }
            }
            show()
        }
    }

    override fun onStart() {
        super.onStart()
        binding.searchProductEdt.requestFocus()
    }
}