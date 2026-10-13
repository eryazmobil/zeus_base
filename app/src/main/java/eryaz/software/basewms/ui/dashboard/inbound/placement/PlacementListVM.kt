package eryaz.software.basewms.ui.dashboard.inbound.placement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.enums.ActionType
import eryaz.software.basewms.data.models.dto.WarningDialogDto
import eryaz.software.basewms.data.models.dto.WorkActionDto
import eryaz.software.basewms.data.models.dto.WorkActivityDto
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.persistence.TemporaryCashManager
import eryaz.software.basewms.data.stepService.StepCountManager
import eryaz.software.basewms.data.repositories.PlacementRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class PlacementListVM(private val repo: PlacementRepo) : BaseViewModel() {

    private val _placementList = MutableLiveData<List<WorkActivityDto?>>(emptyList())
    val placementWorkActivityList: LiveData<List<WorkActivityDto?>> = _placementList

    val search = MutableLiveData("")

    private val _workActionDto = MutableSharedFlow<WorkActionDto>()
    val workActionDto = _workActionDto.asSharedFlow()

    fun searchList() = search.switchMap { query ->
        MutableLiveData<List<WorkActivityDto?>>().apply {
            value = filterData(query)
        }
    }

    private fun filterData(query: String): List<WorkActivityDto?> {
        val dataList = _placementList.value.orEmpty()

        val filteredList = dataList.filter { data ->
            data?.client?.name?.contains(query, ignoreCase = true) ?: true
        }
        return filteredList
    }

    fun getPlacementList() {
        executeInBackground(_uiState) {
            repo.getPlacementWorkActivityList(
                companyId = SessionManager.companyId,
                warehouseId = SessionManager.warehouseId
            ).onSuccess {
                if (it.isEmpty()) {
                    _placementList.value = emptyList()
                    showWarning(
                        WarningDialogDto(
                            title = stringProvider.invoke(R.string.not_found_work_activity),
                            message = stringProvider.invoke(R.string.placement_is_empty)
                        )
                    )
                } else {
                    _placementList.value = it
                }
            }.onError { _, _ ->
                _placementList.value = emptyList()

            }
        }
    }

    fun getWorkAction() {
        executeInBackground(
            _uiState,
            showErrorDialog = false,
            checkErrorState = false
        ) {
            repo.getWorkAction(
                companyId = SessionManager.companyId,
                warehouseId = SessionManager.warehouseId
            ).onSuccess {
                _workActionDto.emit(it)

                TemporaryCashManager.getInstance().workAction = it
            }.onError { _, _ ->
                createWorkAction()
            }
        }
    }

    private fun createWorkAction() {
        executeInBackground {
            val activityID = TemporaryCashManager.getInstance().workActivity?.workActivityId ?: 0
            repo.createWorkActionWithStep(
                activityId = activityID,
                actionTypeCode = ActionType.PLACEMENT.type,
                startStep = StepCountManager.currentStep()
            ).onSuccess {
                TemporaryCashManager.getInstance().workAction = it
                _workActionDto.emit(it)
            }
        }
    }
}