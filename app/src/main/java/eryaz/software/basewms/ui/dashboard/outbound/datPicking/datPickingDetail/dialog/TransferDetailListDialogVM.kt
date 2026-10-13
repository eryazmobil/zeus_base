package eryaz.software.basewms.ui.dashboard.outbound.datPicking.datPickingDetail.dialog

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
import eryaz.software.basewms.util.extensions.orZero

class TransferDetailListDialogVM(
    private val repo: WorkActivityRepo
) : BaseViewModel() {

    private val _transferDetailList = MutableLiveData<List<TransferRequestDetailDto>>(emptyList())
    val transferDetailList: LiveData<List<TransferRequestDetailDto>> = _transferDetailList

    val search = MutableLiveData("")

    fun searchList() = search.switchMap { query ->
        MutableLiveData<List<TransferRequestDetailDto>>().apply {
            value = _transferDetailList.value.orEmpty().filter { data ->
                data.product.code.contains(query, ignoreCase = true)
            }
        }
    }

    fun getTransferListDetail() {
        executeInBackground(showProgressDialog = true) {
            val workActivityId =
                TemporaryCashManager.getInstance().workActivity?.workActivityId.orZero()
            repo.getTransferRequestDetailList(workActivityId = workActivityId)
                .onSuccess {
                    _transferDetailList.value = it
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

    fun getControlList() {
        executeInBackground(showProgressDialog = true) {
            repo.getTransferDetailControlListForPda(workActivityId = TemporaryCashManager.getInstance().workActivity?.workActivityId.orZero())
                .onSuccess {
                    if (it.transferRequestDetailPdaDto.isNotEmpty()) {
                        _transferDetailList.value = it.transferRequestDetailPdaDto
                    } else {
                        showError(
                            ErrorDialogDto(
                                titleRes = R.string.error, messageRes = R.string.no_data_to_list
                            )
                        )
                    }
                }.onError { message, _ ->
                    showError(
                        ErrorDialogDto(
                            titleRes = R.string.error, message = message
                        )
                    )
                }
        }
    }

}