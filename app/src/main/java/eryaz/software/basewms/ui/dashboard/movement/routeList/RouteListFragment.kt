package eryaz.software.basewms.ui.dashboard.movement.routeList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.databinding.FragmentRouteListBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.util.adapter.movement.route.RouteAdapter
import eryaz.software.zeus.zeusViewModel

class RouteListFragment : BaseFragment() {
    override val viewModel by zeusViewModel<RouteListVM>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentRouteListBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.routeListVM = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        viewModel.fetchRouteList()
    }

    override fun subscribeToObservables() {

        viewModel.routeList
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }

        viewModel.searchList()
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
    }

    override fun setClicks() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        adapter.onItemClick = {
            findNavController().navigate(
                RouteListFragmentDirections.actionRouteListFragmentToChooseVehicleFragment(
                    it.id
                )
            )
        }
    }

    private val adapter by lazy {
        RouteAdapter().also {
            binding.recyclerView.adapter = it
        }
    }
}