package eryaz.software.basewms.ui.dashboard.movement.routeList.chooseStep.vehicleDown.orderDetailViewPager.orderDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import eryaz.software.basewms.databinding.FragmentPackageDetailBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.movement.routeList.chooseStep.vehicleDown.orderDetailViewPager.packageDetail.PackageDetailFragment
import eryaz.software.basewms.util.adapter.movement.packageList.PackageProductAdapter
import eryaz.software.zeus.zeusViewModel
import eryaz.software.zeus.params

class OrderDetailFragment : BaseFragment(){

    override val viewModel by zeusViewModel<OrderDetailVM> {
        val orderHeaderId = arguments?.getInt(ORDER_HEADER_ID)

        params(orderHeaderId)
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentPackageDetailBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }

    override fun subscribeToObservables() {
        viewModel.fetchOrderHeaderRouteDetailList()

        viewModel.orderList
            .asLiveData()
            .observe(this){
                adapter.submitList(it)
            }
    }

    private val adapter by lazy {
        PackageProductAdapter().also {
            binding.recyclerView.adapter = it
        }
    }

    companion object {
        const val ORDER_HEADER_ID = "ORDER_HEADER_ID"
    }
}