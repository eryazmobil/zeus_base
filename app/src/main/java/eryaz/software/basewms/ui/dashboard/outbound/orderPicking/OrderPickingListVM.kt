package eryaz.software.basewms.ui.dashboard.outbound.orderPicking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.enums.ActionType
import eryaz.software.basewms.data.enums.ActivityType
import eryaz.software.basewms.data.enums.OrderType
import eryaz.software.basewms.data.models.dto.WarningDialogDto
import eryaz.software.basewms.data.models.dto.WorkActivityDto
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.persistence.TemporaryCashManager
import eryaz.software.basewms.data.stepService.StepCountManager
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.extensions.orZero
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class OrderPickingListVM(private val repo: WorkActivityRepo) : BaseViewModel() {

    val search = MutableLiveData("")

    private val _navigateToDetail = MutableSharedFlow<Boolean>()
    val navigateToDetail = _navigateToDetail.asSharedFlow()

    private val _orderPickingList = MutableLiveData<List<WorkActivityDto?>>(emptyList())
    val orderPickingList: LiveData<List<WorkActivityDto?>> = _orderPickingList

    fun searchList() = search.switchMap { query ->
        MutableLiveData<List<WorkActivityDto?>>().apply {
            value = filterData(query)
        }
    }

    private fun filterData(query: String): List<WorkActivityDto?> {
        val dataList = _orderPickingList.value.orEmpty()

        val filteredList = dataList.filter { data ->
            data?.notes?.contains(query, ignoreCase = true) ?: true
        }
        return filteredList
    }

    fun getActiveWorkAction() =
        executeInBackground(showErrorDialog = false) {
            repo.getWorkActionActive(
                companyId = SessionManager.companyId,
                warehouseId = SessionManager.warehouseId,
                workActivityType = ActivityType.SHIPPING.type,
                workActionType = ActionType.PICKING.type
            ).onSuccess {
                _navigateToDetail.emit(true)
                TemporaryCashManager.getInstance().workAction = it
                TemporaryCashManager.getInstance().workActivity = it.workActivity
            }.onError { _, _ ->
                _navigateToDetail.emit(false)
                getShippingWorkActivityList()
            }
        }

     private fun getShippingWorkActivityList() {
        executeInBackground(_uiState) {
            repo.getShippingWorkActivityList(
                SessionManager.companyId,
                SessionManager.warehouseId
            ).onSuccess {
                if (it.isEmpty()) {
                    _orderPickingList.value = emptyList()
                    showWarning(
                        WarningDialogDto(
                            title = stringProvider.invoke(R.string.not_found_work_activity),
                            message = stringProvider.invoke(R.string.list_is_empty)
                        )
                    )
                }
                _orderPickingList.value = it
            }.onError { _, _ ->
                _orderPickingList.value = emptyList()
            }
        }
    }

    fun getWorkActionForPda() {
        executeInBackground(
            showProgressDialog = true,
            showErrorDialog = false,
            hasNextRequest = true
        ) {
            repo.getWorkActionForPda(
                userId = SessionManager.userId,
                workActivityId = TemporaryCashManager.getInstance().workActivity?.workActivityId.orZero(),
                actionTypeId = TemporaryCashManager.getInstance().workActionTypeList?.find { model -> model.code == ActionType.PICKING.type }?.id.orZero()
            ).onSuccess {
                TemporaryCashManager.getInstance().workAction = it
                _navigateToDetail.emit(true)
            }.onError { _, _ ->
                createWorkAction()
            }
        }
    }

    private fun createWorkAction() {
        executeInBackground(showProgressDialog = true) {
            repo.createWorkActionWithStep(
                activityId = TemporaryCashManager.getInstance().workActivity?.workActivityId ?: 0,
                actionTypeCode = ActionType.PICKING.type,
                startStep = StepCountManager.currentStep()
            ).onSuccess {
                TemporaryCashManager.getInstance().workAction = it
                _navigateToDetail.emit(true)
            }
        }
    }

}