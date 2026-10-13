package eryaz.software.basewms.ui.dashboard.query.queryShelf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.databinding.FragmentQueryShelfBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.query.adapter.ShelfProductQuantityAdapter
import eryaz.software.basewms.ui.dashboard.query.adapter.VarietyProductAdapter
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.onBarcodeOrShelfReadListener
import eryaz.software.zeus.zeusViewModel

class QueryShelfFragment : BaseFragment() {
    override val viewModel by zeusViewModel<QueryShelfFragmentVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentQueryShelfBinding.inflate(layoutInflater)
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
            viewModel.getShelfByCode()
        }
    }

    override fun subscribeToObservables() {
        viewModel.productList.asLiveData().observe(viewLifecycleOwner) {
            shelfAdapter.submitList(it)
        }

        viewModel.varietyShelf.asLiveData().observe(viewLifecycleOwner) {
            varietyAdapter.submitList(it)
        }
    }

    private val varietyAdapter by lazy(LazyThreadSafetyMode.NONE) {
        VarietyProductAdapter().also {
            binding.recyclerViewForVariety.adapter = it
        }
    }

    private val shelfAdapter by lazy(LazyThreadSafetyMode.NONE) {
        ShelfProductQuantityAdapter().also {
            binding.recyclerViewForShelf.adapter = it
            it.showProduct = true
        }
    }

    override fun onStart() {
        super.onStart()

        binding.searchEdt.requestFocus()
    }

    override fun hideActionBar() = false
}