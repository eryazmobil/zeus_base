package eryaz.software.basewms.ui.dashboard.outbound.datControl.datControlDetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.R
import eryaz.software.basewms.databinding.FragmentTransferControlPointDetailBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.outbound.controlPoint.orderHeaderDialog.controlPointDetail.ControlPointDetailFragmentArgs
import eryaz.software.basewms.ui.dashboard.outbound.controlPoint.orderHeaderDialog.controlPointDetail.ControlPointDetailFragmentDirections
import eryaz.software.basewms.ui.dashboard.outbound.orderPicking.orderPickingDetail.OrderPickingDetailFragmentDirections
import eryaz.software.basewms.util.adapter.outbound.ControlPointDetailListAdapter
import eryaz.software.basewms.util.adapter.outbound.ControlPointDetailListVH
import eryaz.software.basewms.util.adapter.outbound.dat.TransferControlPointDetailListAdapter
import eryaz.software.basewms.util.adapter.outbound.dat.TransferControlPointDetailListVH
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.enums.DatType
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.observe
import eryaz.software.basewms.util.extensions.onBarcodeOrShelfReadListener
import eryaz.software.basewms.util.extensions.toast
import eryaz.software.zeus.zeusViewModel
import eryaz.software.zeus.params

class TransferControlPointDetailFragment : BaseFragment() {
    private val safeArgs by navArgs<TransferControlPointDetailFragmentArgs>()

    override val viewModel by zeusViewModel<TransferControlPointDetailVM> {
        params(safeArgs.id, safeArgs.workActivityCode)
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentTransferControlPointDetailBinding.inflate(layoutInflater)
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
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.searchEdt.onBarcodeOrShelfReadListener {
            viewModel.getBarcodeByCode()

        }

        binding.toolbar.setMenuOnClickListener {
            popupMenu(it)
        }

        binding.controlBtn.setOnSingleClickListener {
            viewModel.addQuantityForControl(viewModel.enterQuantity.value.toInt())
        }
    }

    override fun subscribeToObservables() {
        viewModel.serialCheckBox.observe(this) {
            if (binding.quantityEdt.hasFocus()) binding.quantityEdt.hideSoftKeyboard()
        }

        viewModel.processSuccess.asLiveData()
            .observe(this) {
                if (it)
                    binding.searchEdt.requestFocus()

            }

        viewModel.showProductDetail.asLiveData()
            .observe(this) {
                if (it) {
                    binding.quantityEdt.requestFocus()
                }
            }

        viewModel.transferDetailList.asLiveData().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.packageList.observe(this) { list ->
            if (list.isNotEmpty()) {
                binding.stateView.setViewVisible(binding.packageSpinner, true)

            }
            context?.let {
                val adapter = ArrayAdapter(it, android.R.layout.simple_spinner_item, list)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.packageSpinner.adapter = adapter
            }
        }

        viewModel.scrollToPosition.asLiveData().observe(viewLifecycleOwner) {
            binding.recyclerView.scrollToPosition(it)

            (binding.recyclerView.findViewHolderForAdapterPosition(it) as? TransferControlPointDetailListVH)?.animateBackground()
        }
    }

    private val adapter by lazy {
        TransferControlPointDetailListAdapter().also {
            binding.recyclerView.adapter = it
        }
    }

    private fun popupMenu(view: View) {
        PopupMenu(requireContext(), view).apply {
            inflate(R.menu.order_picking_menu)

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.order_detail_list -> {
                        findNavController().navigate(
                            TransferControlPointDetailFragmentDirections.actionTransferControlPointDetailFragmentToTransferDetailListDialog(DatType.FromControl)
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

    override fun onResume() {
        super.onResume()
        binding.searchEdt.requestFocus()
    }
}
