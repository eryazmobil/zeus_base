package eryaz.software.basewms.ui.dashboard.inbound.dat.acceptance

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
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.extensions.orZero
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class DatAcceptanceListVM(private val repo: WorkActivityRepo) : BaseViewModel() {

    private val _acceptanceList = MutableLiveData<List<WorkActivityDto?>>(emptyList())
    val acceptanceList: LiveData<List<WorkActivityDto?>> = _acceptanceList

    val search = MutableLiveData("")

    private val _workActionDto = MutableSharedFlow<WorkActionDto>()
    val workActionDto = _workActionDto.asSharedFlow()

    fun searchList() = search.switchMap { query ->
        MutableLiveData<List<WorkActivityDto?>>().apply {
            value = filterData(query)
        }
    }

    // Filtreleme koşulları
    private fun filterData(query: String): List<WorkActivityDto?> {
        val dataList = _acceptanceList.value.orEmpty()

        val filteredList = dataList.filter { data ->
            data?.client?.name?.contains(query, ignoreCase = true) ?: true
        }
        return filteredList
    }

    fun getWaybillWorkActivityList() {
        executeInBackground(_uiState) {
            repo.getTransferRequestReceivingWorkActivityList(
                SessionManager.companyId,
                SessionManager.warehouseId
            ).onSuccess {
                if (it.isEmpty()) {
                    _acceptanceList.value = emptyList()
                    showWarning(
                        WarningDialogDto(
                            title = stringProvider.invoke(R.string.not_found_work_activity),
                            message = stringProvider.invoke(R.string.list_is_empty)
                        )
                    )
                }
                _acceptanceList.value = it

            }.onError { _, _ ->
                _acceptanceList.value = emptyList()

            }
        }
    }

    fun getWorkActionForPda() {
        executeInBackground(
            _uiState,
            showErrorDialog = false,
            checkErrorState = false
        ) {
            repo.getWorkActionForPda(
                userId = SessionManager.userId,
                workActivityId = TemporaryCashManager.getInstance().workActivity?.workActivityId.orZero(),
                actionTypeId =  TemporaryCashManager.getInstance().workActionTypeList?.find { model -> model.code == "Control" }?.id.orZero()
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
            repo.createWorkActionWithStep(
                activityId = TemporaryCashManager.getInstance().workActivity?.workActivityId ?: 0,
                actionTypeCode = ActionType.CONTROL.type,
                startStep = StepCountManager.currentStep()
            ).onSuccess {
                TemporaryCashManager.getInstance().workAction = it
                _workActionDto.emit(it)
            }
        }
    }
}