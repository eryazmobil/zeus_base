package eryaz.software.basewms.ui.dashboard.counting.partialcounting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.R
import eryaz.software.basewms.data.models.dto.ButtonDto
import eryaz.software.basewms.data.models.dto.ConfirmationDialogDto
import eryaz.software.basewms.databinding.FragmentPartialCountingBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.query.adapter.ShelfProductQuantityAdapter
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.hideSoftKeyboard
import eryaz.software.basewms.util.extensions.observe
import eryaz.software.basewms.util.extensions.onBackPressedCallback
import eryaz.software.basewms.util.extensions.onBarcodeOrShelfReadListener
import eryaz.software.zeus.zeusViewModel

class PartialCountingFragment : BaseFragment() {

    override val viewModel by zeusViewModel<PartialCountingVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentPartialCountingBinding.inflate(layoutInflater)
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

    override fun setClicks() {

        binding.shelfAddressEdt.requestFocus()

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

        binding.shelfAddressEdt.onBarcodeOrShelfReadListener {
            viewModel.getShelfByCode()
        }

        binding.quantityEdt.setOnEditorActionListener { _, actionId, _ ->

            when (actionId) {
                EditorInfo.IME_ACTION_UNSPECIFIED -> binding.quantityEdt.text.clear()
                EditorInfo.IME_ACTION_NEXT -> binding.continueToCounting.requestFocus()
            }
            true
        }

        binding.searchProductEdt.onBarcodeOrShelfReadListener {
            viewModel.getBarcodeByCode()
        }

        binding.completeCounting.setOnSingleClickListener {
            viewModel.finishPartialStockTacking {
                backToPage()
            }
        }

        viewModel.shelfIsValid.observe(this) {
            binding.newShelfButton.isEnabled = it
            binding.shelfAddressEdt.isEnabled = !it
            if (it) {
                binding.searchProductEdt.requestFocus()
            }
        }

        viewModel.showProductDetail.observe(this) {
            if (it) {
                binding.quantityEdt.requestFocus()
            }
        }

        binding.newShelfButton.setOnSingleClickListener {
            viewModel.nextPartialStockTackingDetail {
                binding.shelfAddressEdt.requestFocus()
            }
        }

        binding.continueToCounting.setOnSingleClickListener {
            viewModel.createSTActionProcessFromPartialStockTaking()
            binding.searchProductEdt.requestFocus()
        }

        viewModel.shelfList.asLiveData()
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
    }

    override fun subscribeToObservables() {
        super.subscribeToObservables()
        viewModel.shelfList.asLiveData()
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
    }

    private val adapter by lazy {
        ShelfProductQuantityAdapter().also {
            binding.recyclerView.adapter = it
            it.showProduct = true
        }
    }

    private fun backToPage() {
        findNavController().navigateUp()
    }
}

