package eryaz.software.basewms.ui.dashboard

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import eryaz.software.basewms.R
import eryaz.software.basewms.data.models.dto.DashboardItemDto
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.enums.DashboardPermissionType
import eryaz.software.basewms.data.models.dto.CurrentUserDto
import eryaz.software.basewms.data.models.remote.request.StepCountRequest
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.repositories.UserRepo
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.data.stepService.StepCountManager
import eryaz.software.basewms.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class DashboardViewModel(
    private val repo: UserRepo,
    private val workActivityRepo: WorkActivityRepo
) : BaseViewModel() {
    private val _dashboardItemList = MutableLiveData<List<DashboardItemDto>>(emptyList())
    val dashboardItemList: LiveData<List<DashboardItemDto>> = _dashboardItemList

    private val _currentUserDto = MutableStateFlow<CurrentUserDto?>(null)

    val stepCount = StepCountManager.stepCount

    init {
        getCurrentLoginHasPermissionsForPDAMenu()
        loadList()
        getCurrentLoginInformation()
    }

    private fun loadList() {
        _dashboardItemList.value?.toMutableList()?.apply {
            add(
                DashboardItemDto(
                    iconRes = R.drawable.inbound,
                    titleRes = R.string.menu_inbound,
                    type = DashboardPermissionType.INBOUND,
                    hasPermission = ObservableField(false)
                )
            )
            add(
                DashboardItemDto(
                    iconRes = R.drawable.outbound,
                    titleRes = R.string.menu_outbound,
                    type = DashboardPermissionType.OUTBOUND,
                    hasPermission = ObservableField(false)
                )
            )
            add(
                DashboardItemDto(
                    iconRes = R.drawable.delivery,
                    titleRes = R.string.menu_movement,
                    type = DashboardPermissionType.MOVEMENT,
                    hasPermission = ObservableField(false)
                )
            )
            add(
                DashboardItemDto(
                    iconRes = R.drawable.recording,
                    titleRes = R.string.menu_recording,
                    type = DashboardPermissionType.RECORDING,
                    hasPermission = ObservableField(false)
                )
            )
            add(
                DashboardItemDto(
                    iconRes = R.drawable.return_icon,
                    titleRes = R.string.menu_returning,
                    type = DashboardPermissionType.RETURNING,
                    hasPermission = ObservableField(false)
                )
            )
            add(
                DashboardItemDto(
                    iconRes = R.drawable.counting,
                    titleRes = R.string.menu_counting,
                    type = DashboardPermissionType.COUNTING,
                    hasPermission = ObservableField(false)
                )
            )
            add(
                DashboardItemDto(
                    iconRes = R.drawable.smart_factory,
                    titleRes = R.string.menu_production,
                    type = DashboardPermissionType.PRODUCTION,
                    hasPermission = ObservableField(false)
                )
            )
            add(
                DashboardItemDto(
                    iconRes = R.drawable.query,
                    titleRes = R.string.menu_query,
                    type = DashboardPermissionType.QUERY,
                    hasPermission = ObservableField(false)
                )
            )

            _dashboardItemList.value = this
        }
    }

    private fun getCurrentLoginHasPermissionsForPDAMenu() =
        executeInBackground {
            repo.getCurrentLoginHasPermissionsForPDAMenu().onSuccess { permissionList ->
                _dashboardItemList.value?.forEach {
                    it.hasPermission.set(
                        permissionList.contains(it.type.permission) || it.type
                                == DashboardPermissionType.SETTING
                    )
                }
            }
        }

    private fun getCurrentLoginInformation() =
        executeInBackground {
            repo.getCurrentLoginInformation().onSuccess {
                _currentUserDto.emit(it)

                SessionManager.companyId = it.companyId
                SessionManager.warehouseId = it.warehouseId
                SessionManager.userId = it.userId
            }
        }

    fun syncStepCountToServer() {
        executeInBackground(showErrorDialog = false, showProgressDialog = false) {
            workActivityRepo.updateGeneralStepByUserId(
                StepCountRequest(stepCount = StepCountManager.currentStep())
            )
        }
    }
}
