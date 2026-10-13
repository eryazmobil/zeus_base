package eryaz.software.basewms.ui.dashboard.inbound.dat.acceptance.acceptanceProcess.transferDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.TransferRequestDetailDto
import eryaz.software.basewms.data.persistence.TemporaryCashManager
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel

class AcceptanceTransferDetailVM(
    private val repo: WorkActivityRepo
) : BaseViewModel() {

    private val _waybillDetailList = MutableLiveData<List<TransferRequestDetailDto>>(emptyList())
    val waybillDetailList: LiveData<List<TransferRequestDetailDto>> = _waybillDetailList

    val search = MutableLiveData("")

    init {
        getTransferListDetail()
    }

    fun searchList() = search.switchMap { query ->
        MutableLiveData<List<TransferRequestDetailDto>>().apply {
            value = _waybillDetailList.value.orEmpty().filter { data ->
                data.product.code.contains(query, ignoreCase = true)
            }
        }
    }

     fun getTransferListDetail() {
        executeInBackground(_uiState) {
            val workActivityId =
                TemporaryCashManager.getInstance().workActivity?.workActivityId ?: 0
            repo.getTransferRequestDetailListForPdaReceiving(
                workActivityId = workActivityId)
                .onSuccess {
                    _waybillDetailList.value = it
                }.onError {message, _ ->
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