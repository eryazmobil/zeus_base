package eryaz.software.basewms.ui.dashboard.dashboardDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import eryaz.software.basewms.data.enums.DashboardDetailPermissionType
import eryaz.software.basewms.databinding.FragmentDashboardDetailListBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.util.adapter.dashboard.adapters.DashboardDetailAdapter
import eryaz.software.zeus.zeusViewModel
import eryaz.software.zeus.params

class DashboardDetailFragment : BaseFragment() {
    private val safeArgs by navArgs<DashboardDetailFragmentArgs>()

    override val viewModel by zeusViewModel<DashboardDetailViewModel> {
        params(safeArgs.permissionType)
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentDashboardDetailListBinding.inflate(layoutInflater)
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
        adapter.onItemClick = {
            viewModel.checkPermission(it)
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun subscribeToObservables() {
        viewModel.dashboardDetailItemList
            .asLiveData()
            .observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }

        adapter.onItemClick = { dto ->
            when (dto.type) {
                DashboardDetailPermissionType.ACCEPTANCE ->
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToNavAcceptance()
                    )

                DashboardDetailPermissionType.PLACEMENT ->
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToNavPlacement()
                    )

                DashboardDetailPermissionType.CROSSDOCK ->
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToCrossDockListFragment()
                    )

                DashboardDetailPermissionType.DATACCEPTANCE ->
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToAcceptanceDatNav()
                    )

                DashboardDetailPermissionType.DATPLACEMENT -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToPlacementDatNav()
                    )
                }

                DashboardDetailPermissionType.DATCANCEL -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToNavDatCancel()
                    )
                }

                DashboardDetailPermissionType.ORDERPICKING -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToNavOrderPickingGraph()
                    )
                }

                DashboardDetailPermissionType.CROSSDOCKTRANSFER -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToCrossDockListFragment()
                    )
                }

                DashboardDetailPermissionType.CONTROLPOINT -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToNavControlPoint()
                    )
                }

                DashboardDetailPermissionType.DATPICKING -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToNavDatPicking()
                    )
                }

                DashboardDetailPermissionType.DATCONTROL -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToNavDatControl()
                    )
                }
                DashboardDetailPermissionType.REPLENISHMENT -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToNavSupply()
                    )
                }

                DashboardDetailPermissionType.TRANSFERWAREHOUSE ->
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToNavStorageTransfer()
                    )

                DashboardDetailPermissionType.TRANSFERSHELF ->
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToTransferAddressFragment()
                    )

                DashboardDetailPermissionType.TRANSFERSHELFALL -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToTransferAllShelfFragment()
                    )
                }

                DashboardDetailPermissionType.STOCKORRECTION -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToNavStockCorrection()
                    )
                }

                DashboardDetailPermissionType.VARIETY -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToNavVarietyShelf()
                    )
                }

                DashboardDetailPermissionType.BARCODEREGISTERATION -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToNavRecordingBarcode()
                    )
                }
                DashboardDetailPermissionType.BARCODEREGISTERATIONANDSIZING -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToSizingRecordingBarcodeNav()
                    )
                }


                DashboardDetailPermissionType.FIRSTWAREHOUSECOUNTING -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToNavWarehouseCountingList()
                    )
                }

                DashboardDetailPermissionType.FASTWAREHOUSECOUNTING -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToNavFastCounting()
                    )
                }

                DashboardDetailPermissionType.PARTIALWAREHOUSECOUNTING -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToPartialCountingFragment()
                    )
                }

                DashboardDetailPermissionType.PRODUCTQUERY -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToNavQueryProduct()
                    )
                }

                DashboardDetailPermissionType.SHELFQUERY -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToQueryShelfFragment()
                    )
                }

                DashboardDetailPermissionType.ROUTE -> {
                    findNavController().navigate(
                        DashboardDetailFragmentDirections.actionDashboardDetailFragmentToNavRoute()
                    )
                }

                else -> {}
            }
        }
    }

    private val adapter by lazy {
        DashboardDetailAdapter().also {
            it.also {
                binding.recyclerView.adapter = it
            }
        }
    }


}