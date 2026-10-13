package eryaz.software.basewms.ui.dashboard.query.queryStorage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.databinding.FragmentQueryStorageBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.query.adapter.ShelfQuantityAdapter
import eryaz.software.basewms.ui.dashboard.query.adapter.StorageProductQuantityAdapter
import eryaz.software.basewms.ui.dashboard.recording.dialog.ProductListDialogFragment
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.onBarcodeOrShelfReadListener
import eryaz.software.basewms.util.extensions.parcelable
import eryaz.software.zeus.zeusViewModel

class QueryStorageFragment : BaseFragment() {
    override val viewModel by zeusViewModel<QueryStorageFragmentVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentQueryStorageBinding.inflate(layoutInflater)
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

        binding.searchTil.setEndIconOnClickListener {
            findNavController().navigate(
                QueryStorageFragmentDirections.actionQueryProductFragmentToProductListDialogFragment()
            )
        }
    }

    override fun subscribeToObservables() {
        viewModel.storageList.asLiveData()
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }

        viewModel.barcodeScanned.asLiveData().observe(viewLifecycleOwner) {
            binding.searchEdt.requestFocus()
        }

        viewModel.shelfList.asLiveData()
            .observe(viewLifecycleOwner) {
                shelfAdapter.submitList(it)
            }

        setFragmentResultListener(ProductListDialogFragment.REQUEST_KEY) { _, bundle ->
            val dto = bundle.parcelable<ProductDto>(ProductListDialogFragment.ARG_PRODUCT_DTO)
            dto?.let {
                viewModel.setExitStorage(it)
            }
        }
    }

    private val adapter by lazy(LazyThreadSafetyMode.NONE) {
        StorageProductQuantityAdapter().also {
            binding.recyclerViewForStorage.adapter = it
        }
    }

    private val shelfAdapter by lazy(LazyThreadSafetyMode.NONE) {
        ShelfQuantityAdapter().also {
            binding.recyclerViewForShelf.adapter = it
        }
    }

    override fun onStart() {
        super.onStart()

        binding.searchEdt.requestFocus()
    }

    override fun hideActionBar() = false
}