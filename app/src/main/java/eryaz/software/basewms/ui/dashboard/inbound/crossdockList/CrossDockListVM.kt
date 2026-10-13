package eryaz.software.basewms.ui.dashboard.inbound.crossdockList

import eryaz.software.basewms.data.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.CrossDockDto
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.WarningDialogDto
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.repositories.OrderRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CrossDockListVM(private val repo: OrderRepo) : BaseViewModel() {

    private val _crossDockList = MutableStateFlow(listOf<CrossDockDto>())
    val crossDockList = _crossDockList.asStateFlow()

    init {
        fetchCrossDockList()
    }

    fun fetchCrossDockList() {
        executeInBackground(_uiState) {
            repo.fetchCrossDockList(
                companyId = SessionManager.companyId,
                warehouseId = SessionManager.warehouseId
            ).onSuccess {
                if (it.isEmpty()) {
                    showWarning(
                        WarningDialogDto(
                            title = stringProvider.invoke(R.string.warning),
                            message = stringProvider.invoke(R.string.crossdock_empty_list)
                        )
                    )
                }
                _crossDockList.emit(it)
            }.onError { message, _ ->
                showError(
                    ErrorDialogDto(
                        title = stringProvider.invoke(R.string.error),
                        message = message
                    )
                )

            }
        }
    }

}