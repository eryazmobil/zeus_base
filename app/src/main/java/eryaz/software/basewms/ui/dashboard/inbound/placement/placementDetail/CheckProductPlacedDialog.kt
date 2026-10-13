package eryaz.software.basewms.ui.dashboard.inbound.placement.placementDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import eryaz.software.basewms.databinding.DialogCheckProductPlacedBinding
import eryaz.software.basewms.ui.base.BaseDialogFragment

class CheckProductPlacedDialog  : BaseDialogFragment()  {

//    override val viewModel by zeusViewModel<DialogVM> {
//        params(safeArgs.permissionType)
//    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        DialogCheckProductPlacedBinding.inflate(layoutInflater)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}