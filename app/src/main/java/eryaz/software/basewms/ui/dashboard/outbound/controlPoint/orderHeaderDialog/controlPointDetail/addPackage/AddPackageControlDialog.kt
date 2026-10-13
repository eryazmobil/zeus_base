package eryaz.software.basewms.ui.dashboard.outbound.controlPoint.orderHeaderDialog.controlPointDetail.addPackage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.R
import eryaz.software.basewms.databinding.DialogAddPackageControlBinding
import eryaz.software.basewms.ui.base.BaseDialogFragment
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.observe
import eryaz.software.basewms.util.extensions.toast
import eryaz.software.zeus.zeusViewModel
import eryaz.software.zeus.params

class AddPackageControlDialog : BaseDialogFragment() {
    private val safeArgs by navArgs<AddPackageControlDialogArgs>()

    override val viewModel by zeusViewModel<AddPackageControlVM> {
        params(safeArgs.packgeList.toList(), safeArgs.orderHeaderId)
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        DialogAddPackageControlBinding.inflate(layoutInflater)
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
        binding.saveBtn.setOnSingleClickListener {
            viewModel.createPackage()
        }
    }

    override fun subscribeToObservables() {
        viewModel.savePackage.observe(this) {
            if (it) {
                toast(getString(R.string.packageAdded))
            }
        }
    }
}