package eryaz.software.basewms.ui.dashboard.query.queryShelf

import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.ProductShelfQuantityDto
import eryaz.software.basewms.data.models.dto.ProductSpecialShelfDto
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class QueryShelfFragmentVM(
    private val repo: WorkActivityRepo
) : BaseViewModel() {
    val searchShelf = MutableStateFlow("")

    private val _shelfDetail = MutableStateFlow(false)
    val shelfDetail = _shelfDetail.asStateFlow()

    private val _productList = MutableStateFlow(listOf<ProductShelfQuantityDto>())
    val productList = _productList.asStateFlow()

    private val _varietyShelf = MutableStateFlow(listOf<ProductSpecialShelfDto>())
    val varietyShelf = _varietyShelf.asStateFlow()

    fun getShelfByCode() = executeInBackground(
        _uiState,
        showProgressDialog = true
    ) {
        repo.getShelfByCode(
            code = searchShelf.value.trim(),
            warehouseId = SessionManager.warehouseId,
            storageId = 0
        ).onSuccess {
            getProductShelfQuantityList(it.shelfId)
            getProductVarietyShelfListByShelfId(it.shelfId)
            searchShelf.emit("")
        }.onError { message, _ ->
            _shelfDetail.emit(false)
            searchShelf.emit("")
            showError(
                ErrorDialogDto(
                    title = stringProvider.invoke(R.string.error),
                    message = stringProvider.invoke(R.string.msg_shelf_not_found)
                )
            )
        }
    }

    private fun getProductShelfQuantityList(shelfId: Int) =
        executeInBackground(_uiState, showProgressDialog = true) {

            repo.getProductShelfQuantityList(
                productId = 0,
                warehouseId = SessionManager.warehouseId,
                storageId = 0,
                companyId = SessionManager.companyId,
                shelfId = shelfId
            ).onSuccess {
                _shelfDetail.emit(true)
                _productList.emit(it)
            }
        }

    private fun getProductVarietyShelfListByShelfId(id: Int) =
        executeInBackground(_uiState, showProgressDialog = true) {
            repo.getProductVarietyShelfListByShelfId(
                shelfId = id
            ).onSuccess {
                _varietyShelf.emit(it)
            }
        }
}