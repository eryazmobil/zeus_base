package eryaz.software.basewms.ui.dashboard.settings.companies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.databinding.DialogCompanyListBinding
import eryaz.software.basewms.ui.base.BaseDialogFragment
import eryaz.software.basewms.ui.dashboard.settings.adapter.CompanyListAdapter
import eryaz.software.zeus.zeusViewModel
import eryaz.software.zeus.params

class CompanyListDialog : BaseDialogFragment() {
    private val safeArgs by navArgs<CompanyListDialogArgs>()

    override val viewModel by zeusViewModel<CompanyListVM>{
        params(safeArgs.companyList.toList())
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        DialogCompanyListBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        viewModel.companyList.asLiveData()
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
    }

    private val adapter by lazy(LazyThreadSafetyMode.NONE) {
        CompanyListAdapter().also {
            binding.recyclerView.adapter = it
        }
    }

    companion object {
        const val REQUEST_KEY = "CompanyListDialog"
        const val ARG_COMPANY_DTO = "company_dto"
    }
}