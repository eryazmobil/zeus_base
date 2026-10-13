package eryaz.software.basewms.ui.dashboard.settings.warehouses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.databinding.DialogWarehouseListBinding
import eryaz.software.basewms.ui.base.BaseDialogFragment
import eryaz.software.basewms.ui.dashboard.settings.adapter.WarehouseListAdapter
import eryaz.software.zeus.zeusViewModel
import eryaz.software.zeus.params

class WarehouseListDialog : BaseDialogFragment() {
    private val safeArgs by navArgs<WarehouseListDialogArgs>()

    override val viewModel by zeusViewModel<WarehouseListVM>{
        params(safeArgs.warehouseList.toList())
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        DialogWarehouseListBinding.inflate(layoutInflater)
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
        adapter.onItemClick = {
            setFragmentResult(
                REQUEST_KEY, bundleOf(
                    ARG_COMPANY_DTO to it,
                )
            )
            findNavController().navigateUp()
        }
    }

    override fun subscribeToObservables() {
        viewModel.warehouseList.asLiveData()
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
    }

    private val adapter by lazy(LazyThreadSafetyMode.NONE) {
        WarehouseListAdapter().also {
            binding.recyclerView.adapter = it
        }
    }

    companion object {
        const val REQUEST_KEY = "WarehouseListDialog"
        const val ARG_COMPANY_DTO = "warehouse_dto"
    }
}