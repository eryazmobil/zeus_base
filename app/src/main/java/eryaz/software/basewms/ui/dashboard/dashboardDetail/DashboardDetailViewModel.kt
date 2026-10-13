package eryaz.software.basewms.ui.dashboard.dashboardDetail

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.enums.DashboardDetailPermissionType
import eryaz.software.basewms.data.enums.DashboardPermissionType
import eryaz.software.basewms.data.models.dto.DashboardDetailItemDto
import eryaz.software.basewms.data.models.dto.PagesCountDto
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.repositories.UserRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardDetailViewModel(
    private val repo: UserRepo,
    private val permissionType: DashboardPermissionType
) : BaseViewModel() {

    private val _dashboardDetailItemList =
        MutableStateFlow<List<DashboardDetailItemDto>>(emptyList())

    val dashboardDetailItemList = _dashboardDetailItemList.asStateFlow()

    private val _shipmentListCount =
        MutableStateFlow<List<PagesCountDto>>(emptyList())

    private val _receivingListCount =
        MutableStateFlow<List<PagesCountDto>>(emptyList())

    private val _navigateToDetail = MutableSharedFlow<DashboardDetailItemDto>()
    val navigateToDetail = _navigateToDetail.asSharedFlow()

    init {
        viewModelScope.launch {
            val baseItems = createItems()
            _dashboardDetailItemList.emit(baseItems)

            when (permissionType) {
                DashboardPermissionType.INBOUND -> {
                    getPagesCountForAcceptance { counts ->
                        updateListCounts(counts)
                    }
                }

                DashboardPermissionType.OUTBOUND -> {
                    getPagesCount { counts ->
                        updateListCounts(counts)
                    }
                }

                else -> Unit
            }
        }
    }

    private fun createItems() =
        when (permissionType) {
            DashboardPermissionType.INBOUND -> {
                listOf(
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_inBound_accept,
                        type = DashboardDetailPermissionType.ACCEPTANCE,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_inBound_placement,
                        type = DashboardDetailPermissionType.PLACEMENT,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_inBound_crossdock,
                        type = DashboardDetailPermissionType.CROSSDOCK,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_inBound_transfer_accept,
                        type = DashboardDetailPermissionType.DATACCEPTANCE,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_inBound_transfer_placement,
                        type = DashboardDetailPermissionType.DATPLACEMENT,
                        hasPermission = ObservableField(true)
                    )

                )
            }

            DashboardPermissionType.OUTBOUND -> {
                listOf(
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_outbound_picking,
                        type = DashboardDetailPermissionType.ORDERPICKING,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_outbound_crossdock,
                        type = DashboardDetailPermissionType.CROSSDOCK,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = R.drawable.datacceptance,
                        titleRes = R.string.menu_sub_outbound_controlPoint,
                        type = DashboardDetailPermissionType.CONTROLPOINT,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = R.drawable.datpalcement,
                        titleRes = R.string.menu_sub_outbound_dat_picking,
                        type = DashboardDetailPermissionType.DATPICKING,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = R.drawable.datpalcement,
                        titleRes = R.string.menu_sub_outbound_dat_control,
                        type = DashboardDetailPermissionType.DATCONTROL,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = R.drawable.datpalcement,
                        titleRes = R.string.menu_sub_outbound_dat_replacement,
                        type = DashboardDetailPermissionType.DATCANCEL,
                        hasPermission = ObservableField(true)
                    )
                )
            }

            DashboardPermissionType.MOVEMENT -> {
                listOf(
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_movement_warehouse,
                        type = DashboardDetailPermissionType.TRANSFERWAREHOUSE,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_movement_shelf,
                        type = DashboardDetailPermissionType.TRANSFERSHELF,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_movement_shelfAll,
                        type = DashboardDetailPermissionType.TRANSFERSHELFALL,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_movement_supply,
                        type = DashboardDetailPermissionType.REPLENISHMENT,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_movement_stock_correction,
                        type = DashboardDetailPermissionType.STOCKORRECTION,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_movement_route_list,
                        type = DashboardDetailPermissionType.ROUTE,
                        hasPermission = ObservableField(true)
                    ),
                )
            }

            DashboardPermissionType.RECORDING -> {
                listOf(
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_recording_varietyshelf,
                        type = DashboardDetailPermissionType.VARIETY,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_recording_barcode,
                        type = DashboardDetailPermissionType.BARCODEREGISTERATION,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_recording_barcode_and_sizing,
                        type = DashboardDetailPermissionType.BARCODEREGISTERATIONANDSIZING,
                        hasPermission = ObservableField(true)
                    )
                )
            }

            DashboardPermissionType.RETURNING -> {
                listOf(
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_recording_varietyshelf,
                        type = DashboardDetailPermissionType.ACCEPTANCE,
                        hasPermission = ObservableField(true)
                    )
                )
            }

            DashboardPermissionType.COUNTING -> {
                listOf(
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_counting_first,
                        type = DashboardDetailPermissionType.FIRSTWAREHOUSECOUNTING,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_counting_fasting,
                        type = DashboardDetailPermissionType.FASTWAREHOUSECOUNTING,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_counting_partial_sub,
                        type = DashboardDetailPermissionType.PARTIALWAREHOUSECOUNTING,
                        hasPermission = ObservableField(true)
                    ),
                )
            }

            DashboardPermissionType.PRODUCTION -> {
                listOf()
            }

            DashboardPermissionType.QUERY -> {
                listOf(
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_query_product,
                        type = DashboardDetailPermissionType.PRODUCTQUERY,
                        hasPermission = ObservableField(true)
                    ),
                    DashboardDetailItemDto(
                        iconRes = 0,
                        titleRes = R.string.menu_sub_query_shelf,
                        type = DashboardDetailPermissionType.SHELFQUERY,
                        hasPermission = ObservableField(true)
                    )
                )
            }

            else -> {
                listOf()
            }
        }

    fun checkPermission(dto: DashboardDetailItemDto) {
        val itemPermission = dto.type.permission
        executeInBackground(_uiState, showErrorDialog = true) {
            repo.getCurrentLoginHasPermission(itemPermission).onSuccess {
                _navigateToDetail.emit(dto)
            }
        }
    }


    private fun getPagesCount(onSuccess: (List<PagesCountDto>) -> Unit) {
        executeInBackground(
            showErrorDialog = false,
            showProgressDialog = true
        ) {
            repo.getPagesCount(
                companyId = SessionManager.companyId,
                warehouseId = SessionManager.warehouseId
            ).onSuccess {
                _shipmentListCount.emit(it)
                onSuccess(it)
            }
        }
    }

    private fun getPagesCountForAcceptance(onSuccess: (List<PagesCountDto>) -> Unit) {
        executeInBackground(
            showErrorDialog = false,
            showProgressDialog = true
        ) {
            repo.getPagesCountForAcceptance(
                companyId = SessionManager.companyId,
                warehouseId = SessionManager.warehouseId
            ).onSuccess {
                _receivingListCount.emit(it)
                onSuccess(it)
            }
        }
    }

    private fun updateListCounts(counts: List<PagesCountDto>) {
        viewModelScope.launch {
            val updatedList = _dashboardDetailItemList.value.map { item ->
                val expectedType = permissionToApiType[item.type] ?: item.type.name
                val count = counts.find { it.type.equals(expectedType, ignoreCase = true) }?.count ?: 0
                item.copy(listCount = count)
            }
            _dashboardDetailItemList.emit(updatedList)
        }
    }


    private val permissionToApiType = mapOf(
        DashboardDetailPermissionType.ACCEPTANCE to "Acceptence"
    )

}