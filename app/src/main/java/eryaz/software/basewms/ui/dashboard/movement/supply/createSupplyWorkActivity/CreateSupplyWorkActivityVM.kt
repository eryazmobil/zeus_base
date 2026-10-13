package eryaz.software.basewms.ui.dashboard.movement.supply.createSupplyWorkActivity

import androidx.lifecycle.MutableLiveData
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.ProductShelfWorkActivityDto
import eryaz.software.basewms.data.models.dto.SupplyProductShelfDapperDto
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.persistence.TemporaryCashManager
import eryaz.software.basewms.data.stepService.StepCountManager
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.extensions.orZero
import kotlinx.coroutines.flow.MutableStateFlow

class CreateSupplyWorkActivityVM(
    private val workActivityRepo: WorkActivityRepo
) : BaseViewModel() {

     val supplyProductShelfDapperDtoList =
        MutableLiveData<List<SupplyProductShelfDapperDto>>(
            emptyList()
        )

    val finishThePage = MutableStateFlow(false)

    fun getReportProductShelfListForWorkActivityForPda(typeId: Int) {
        executeInBackground(_uiState) {
            workActivityRepo.getReportProductShelfListForWorkActivityForPda(
                typeId,
                SessionManager.companyId,
                SessionManager.warehouseId
            ).onSuccess {
                supplyProductShelfDapperDtoList.value = it
            }
        }
    }

    fun createProductShelfWorkActivityForPda(
        typeId: Int,
        productShelfIdList : List<Int?>?
    ){
        executeInBackground(
            showProgressDialog = true,
            showErrorDialog = true
        ){
            val productShelfWorkActivityDto = ProductShelfWorkActivityDto(
                productShelfIdList = productShelfIdList
            )
            workActivityRepo.createProductShelfWorkActivityForPda(
                typeId,
                productShelfWorkActivityDto
            ).onSuccess {
                finishWorkAction()
            }
        }
    }

    private fun finishWorkAction() {
        executeInBackground(showProgressDialog = true) {
            workActivityRepo.finishWorkActionForStep(
                actionId = TemporaryCashManager.getInstance().workAction?.workActionId.orZero(),
                endStep = StepCountManager.currentStep()
            ).onSuccess {
                    finishThePage.emit(true)
                }
        }
    }
}