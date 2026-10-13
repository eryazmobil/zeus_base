package eryaz.software.basewms.ui.dashboard.movement.supply.createSupplyWorkActivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.R
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.SupplyProductShelfDapperDto
import eryaz.software.basewms.databinding.FragmentCreateSupplyWorkActivityAndShelfListInformationBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.util.adapter.movement.supply.suplyDapperShelfAdapter.SupplyDapperShelfAdapter
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.observe
import eryaz.software.zeus.zeusViewModel


class CreateSupplyWorkActivityAndShelfListInformation : BaseFragment() {

    override val viewModel by zeusViewModel<CreateSupplyWorkActivityVM>()
    private val args: CreateSupplyWorkActivityAndShelfListInformationArgs by navArgs()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentCreateSupplyWorkActivityAndShelfListInformationBinding.inflate(layoutInflater)
    }

    private val productShelfIdList = ArrayList<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }


    override fun subscribeToObservables() {

        viewModel.getReportProductShelfListForWorkActivityForPda(args.supplyStatus)

        viewModel.supplyProductShelfDapperDtoList.observe(this) {
            adapter.submitList(it)
        }

        viewModel.finishThePage.observe(this){
            if (it) {
                findNavController().navigateUp()
            }
        }

    }

    override fun setClicks() {

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        adapter.onItemChecked = {

            if (it.isChecked) {
                productShelfIdList.add(it.productId)
            } else {
                productShelfIdList.remove(it.productId)
            }

        }

        binding.createWorkActivityForSupply.setOnSingleClickListener {

            if (productShelfIdList.isEmpty()) {
                errorDialog.show(
                    requireContext(),
                    ErrorDialogDto(
                        titleRes = R.string.error, messageRes = R.string.must_select_product
                    )
                )
            }
            else{
                viewModel.createProductShelfWorkActivityForPda(
                    args.supplyStatus,
                    productShelfIdList
                )
            }
        }

    }

    private val adapter by lazy {
        SupplyDapperShelfAdapter(requireContext()).also {
            binding.supplyShelfDapperRecycler.adapter = it
        }
    }


}