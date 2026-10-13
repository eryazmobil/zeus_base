package eryaz.software.basewms.ui.dashboard.outbound.controlPoint.orderHeaderDialog.controlPointDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.R
import eryaz.software.basewms.data.enums.SoundEnum
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.databinding.FragmentControlPointDetailBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.outbound.orderPicking.orderPickingDetail.OrderPickingDetailFragmentDirections
import eryaz.software.basewms.ui.dashboard.recording.dialog.ProductListDialogFragment
import eryaz.software.basewms.util.adapter.outbound.ControlPointDetailListAdapter
import eryaz.software.basewms.util.adapter.outbound.ControlPointDetailListVH
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.observe
import eryaz.software.basewms.util.extensions.onBarcodeOrShelfReadListener
import eryaz.software.basewms.util.extensions.parcelable
import eryaz.software.basewms.util.extensions.toast
import eryaz.software.zeus.zeusViewModel
import eryaz.software.zeus.params

class ControlPointDetailFragment : BaseFragment() {
    private val safeArgs by navArgs<ControlPointDetailFragmentArgs>()

    override val viewModel by zeusViewModel<ControlPointDetailVM> {
        params(safeArgs.workActivityCode, safeArgs.orderHeaderId)
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentControlPointDetailBinding.inflate(layoutInflater)
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

        binding.searchEdt.onBarcodeOrShelfReadListener {
            viewModel.getBarcodeByCode()
        }

        binding.searchProductTill.setEndIconOnClickListener {
            findNavController().navigate(
                ControlPointDetailFragmentDirections.actionControlPointDetailFragmentToProductListDialogFragment()
            )
        }

        binding.controlBtn.setOnSingleClickListener {
            viewModel.addQuantityForControl(viewModel.quantity.value.toInt())
        }

        binding.packageSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>?,
                    selectedItemView: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.setSelectedPackagePosition(position)
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {

                }
            }

        binding.detailPackage.setOnSingleClickListener {
            viewModel.getPackageList()
        }

        binding.addPackage.setOnSingleClickListener {
            viewModel.packageList.observe(this) { packageList ->
                findNavController().navigate(
                    ControlPointDetailFragmentDirections.actionControlPointDetailFragmentToPackageListDialog(
                        packageList.toTypedArray(), viewModel.orderHeaderId
                    )
                )
            }
        }

        binding.updatePackage.setOnSingleClickListener {
            if (viewModel.getSelectedPackagePosition() != 0) {
                findNavController().navigate(
                    ControlPointDetailFragmentDirections.actionControlPointDetailFragmentToUpdatePackageControlDialog(
                        viewModel.selectedPackageDto!!, viewModel.selectedPackageId
                    )
                )
            } else {
                toast(getString(R.string.pleaseSelectPackage))
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

        viewModel.controlSuccess.asLiveData().observe(viewLifecycleOwner) {
            if (it) {
                binding.searchEdt.requestFocus()
            }
        }

        viewModel.controlIsFinished.asLiveData().observe(viewLifecycleOwner) {
            if (it)
                toast(message = getString(R.string.control_finished))
        }

        viewModel.showProductDetail.asLiveData().observe(viewLifecycleOwner) {
            if (it)
                binding.quantityEdt.requestFocus()
        }

        viewModel.serialCheckBox.observe(this) {
            if (binding.quantityEdt.hasFocus())
                binding.quantityEdt.hideSoftKeyboard()
        }

        viewModel.orderDetailList.observe(this) {
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

        viewModel.scrollToPosition
            .asLiveData()
            .observe(viewLifecycleOwner) {
                binding.recyclerView.scrollToPosition(it)

                (binding.recyclerView.findViewHolderForAdapterPosition(it) as? ControlPointDetailListVH)?.animateBackground()
            }
    }

    private val adapter by lazy {
        ControlPointDetailListAdapter().also {
            binding.recyclerView.adapter = it
        }
    }

    override fun onResume() {
        super.onResume()
        binding.searchEdt.requestFocus()
    }
}
