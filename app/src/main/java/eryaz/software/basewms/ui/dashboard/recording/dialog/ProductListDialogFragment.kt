package eryaz.software.basewms.ui.dashboard.recording.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.databinding.DialogProductListBinding
import eryaz.software.basewms.ui.base.BaseDialogFragment
import eryaz.software.basewms.util.adapter.inbound.adapter.ProductListAdapter
import eryaz.software.zeus.zeusViewModel

class ProductListDialogFragment : BaseDialogFragment() {

    override val viewModel by zeusViewModel<ProductListDialogVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        DialogProductListBinding.inflate(layoutInflater)
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
                    ARG_PRODUCT_DTO to it,
                )
            )
            findNavController().navigateUp()
        }
    }

    override fun subscribeToObservables() {
        val displayMetrics = resources.displayMetrics
        val widthPercentage = 0.8
        val heightPercentage = 0.8

        val width = (displayMetrics.widthPixels * widthPercentage).toInt()
        val height = (displayMetrics.heightPixels * heightPercentage).toInt()

        dialog?.window?.setLayout(width, height)

        binding.searchTxt.requestFocus()

        viewModel.productList.asLiveData()
            .observe(viewLifecycleOwner) {
                adapter.submitList(it) {
                    binding.recyclerView.scrollToPosition(0)
                }
            }
    }

    private val adapter by lazy(LazyThreadSafetyMode.NONE) {
        ProductListAdapter().also {
            binding.recyclerView.adapter = it
        }
    }

    companion object {
        const val REQUEST_KEY = "ProductListDialogFragment"
        const val ARG_PRODUCT_DTO = "product_dto"
    }
}