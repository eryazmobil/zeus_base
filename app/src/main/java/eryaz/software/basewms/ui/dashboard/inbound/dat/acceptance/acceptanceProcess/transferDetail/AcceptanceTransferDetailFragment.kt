package eryaz.software.basewms.ui.dashboard.inbound.dat.acceptance.acceptanceProcess.transferDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.databinding.AcceptanceTransferDetailDialogListBinding
import eryaz.software.basewms.ui.base.BaseDialogFragment
import eryaz.software.basewms.ui.dashboard.outbound.datControl.datControlDetail.TransferControlPointDetailFragmentArgs
import eryaz.software.basewms.util.adapter.outbound.dat.TransferDetailListDialogAdapter
import eryaz.software.basewms.util.enums.DatType
import eryaz.software.zeus.zeusViewModel

class AcceptanceTransferDetailFragment :BaseDialogFragment(){

    override val viewModel by zeusViewModel<AcceptanceTransferDetailVM>()

    private val safeArgs by navArgs<AcceptanceTransferDetailFragmentArgs>()


    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        AcceptanceTransferDetailDialogListBinding.inflate(layoutInflater)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.waybillDetailList
            .observe(viewLifecycleOwner) { waybillDetailList ->
                adapter.submitList(waybillDetailList)
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