package eryaz.software.basewms.ui.dashboard.movement.transferAllShelf

import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class TransferAllShelfVM(
    private val repo: WorkActivityRepo
) : BaseViewModel() {

    private var enterShelfEId: Int = 0
    val exitShelfId = MutableStateFlow(0)
    val transferSuccess = MutableSharedFlow<Boolean>()
    val exitShelfValue = MutableStateFlow("")
    val enterShelfValue = MutableStateFlow("")

    fun getShelfByCode(shelfCode: String, enterShelf: Boolean) {
        executeInBackground(showProgressDialog = true) {
            repo.getShelfByCode(
                code = shelfCode.trim(),
                warehouseId = SessionManager.warehouseId,
                storageId = 0
            ).onSuccess {
                if (enterShelf) {
                    enterShelfEId = it.shelfId
                } else {
                    exitShelfId.value = it.shelfId
                }
            }
        }
    }

    private fun isValidFields(): Boolean {
        when {
            exitShelfId.value == 0 -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.exit_shelf_address
                    )
                )
                return false
            }
            enterShelfEId == 0 -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.enter_shelf_address_transfer
                    )
                )
                return false
            }

            else ->
                return true
        }
    }

    fun createAddressMovement() {
        if(isValidFields()){
            executeInBackground {
                repo.transferAllShelfMovementForPda(
                    warehouseId = SessionManager.warehouseId,
                    companyId = SessionManager.companyId,
                    exitShelfId = exitShelfId.value,
                    enterShelfId = enterShelfEId,
                    changeVariety = false
                ).onSuccess {
                    transferSuccess.emit(true)
                    enterShelfEId = 0
                    exitShelfId.emit(0)
                    exitShelfValue.emit("")
                    enterShelfValue.emit("")

                }
            }
        }
    }
}