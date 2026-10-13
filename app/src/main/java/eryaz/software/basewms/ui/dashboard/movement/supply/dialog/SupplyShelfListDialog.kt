package eryaz.software.basewms.ui.dashboard.movement.supply.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.databinding.FragmentSupplyShelfListDialogBinding
import eryaz.software.basewms.ui.base.BaseDialogFragment
import eryaz.software.basewms.ui.dashboard.query.adapter.SupplyShelfProductQuantityAdapter


class SupplyShelfListDialog : BaseDialogFragment() {

    private val args: SupplyShelfListDialogArgs by navArgs()


    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentSupplyShelfListDialogBinding.inflate(layoutInflater)
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

    override fun subscribeToObservables() {


        val displayMetrics = resources.displayMetrics
        val widthPercentage = 0.8
        val heightPercentage = 0.6

        val width = (displayMetrics.widthPixels * widthPercentage).toInt()
        val height = (displayMetrics.heightPixels * heightPercentage).toInt()

        dialog?.window?.setLayout(width, height)

        adapter.submitList(args.productShelfSupplyDto?.toList())

    }

    private val adapter by lazy {
        SupplyShelfProductQuantityAdapter().also {
            binding.recyclerView.adapter = it
        }
    }
}