package eryaz.software.basewms.ui.dashboard.outbound.datPicking.datPickingDetail.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.databinding.TransferDetailDialogListBinding
import eryaz.software.basewms.ui.base.BaseDialogFragment
import eryaz.software.basewms.util.adapter.outbound.dat.TransferDetailListDialogAdapter
import eryaz.software.basewms.util.enums.DatType
import eryaz.software.zeus.zeusViewModel

class TransferDetailListDialog : BaseDialogFragment() {

    override val viewModel by zeusViewModel<TransferDetailListDialogVM>()

    private val safeArgs by navArgs<TransferDetailListDialogArgs>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        TransferDetailDialogListBinding.inflate(layoutInflater)
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

    override fun subscribeToObservables() {


        if (safeArgs.datType == DatType.FromControl) {
            viewModel.getControlList()
        } else {
            viewModel.getTransferListDetail()
        }

        val displayMetrics = resources.displayMetrics
        val widthPercentage = 0.8
        val heightPercentage = 0.9

        val width = (displayMetrics.widthPixels * widthPercentage).toInt()
        val height = (displayMetrics.heightPixels * heightPercentage).toInt()

        dialog?.window?.setLayout(width, height)

        viewModel.transferDetailList
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }

        viewModel.searchList()
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
    }

    private val adapter by lazy {
        TransferDetailListDialogAdapter().also {
            binding.recyclerView.adapter = it
            it.datType = safeArgs.datType
        }
    }
}