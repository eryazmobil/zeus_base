package eryaz.software.basewms.ui.dashboard.movement.routeList.chooseStep.vehicleDown.orderDetailViewPager.packageDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import eryaz.software.basewms.databinding.FragmentPackageDetailBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.util.adapter.movement.route.RouteAdapter
import eryaz.software.zeus.params
import eryaz.software.zeus.zeusViewModel

class PackageDetailFragment : BaseFragment() {

    override val viewModel by zeusViewModel<PackageDetailVM> {
        val shippingRouteId = arguments?.getInt(SHIPPING_ROUTE_ID)
        val orderHeaderId = arguments?.getInt(ORDER_HEADER_ID)

        params(shippingRouteId, orderHeaderId)
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
        viewModel.getOrderHeaderRouteDetailList()

        viewModel.packageList
            .asLiveData()
            .observe(this) {
                adapter.submitList(it)
            }
    }

    private val adapter by lazy {
        RouteAdapter().also {
            binding.recyclerView.adapter = it
        }
    }

    companion object {
        const val SHIPPING_ROUTE_ID = "SHIPPING_ROUTE_ID"
        const val ORDER_HEADER_ID = "ORDER_HEADER_ID"
    }
}