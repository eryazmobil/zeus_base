package eryaz.software.basewms.ui.dashboard.movement.transferStockCorrection

import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.enums.UiState
import eryaz.software.basewms.data.models.dto.BarcodeDto
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.StockTypeDto
import eryaz.software.basewms.data.models.dto.StorageDto
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.extensions.toDoubleOrZero
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransferStockCorrectionVM(
    private val repo: WorkActivityRepo
) : BaseViewModel() {

    private var productID: Int = 0
    private var storageId: Int = 0
    private var typeId: Int = 0
    private var shelfEId: Int = 0

    val exitShelfId = MutableStateFlow(0)
    val transferSuccess = MutableStateFlow(false)
    val isCheckBoxChecked = MutableStateFlow(false)
    val searchProduct = MutableStateFlow("")
    val enterShelfValue = MutableStateFlow("")
    val enteredQuantity = MutableStateFlow("")
    val enteredPrices = MutableStateFlow("")
    val enteredNotes = MutableStateFlow("")

    private val _storageName = MutableStateFlow("")
    val storageName = _storageName.asStateFlow()

    private val _stockTypeName = MutableStateFlow("")
    val stockTypeName = _stockTypeName.asStateFlow()

    private val _productDetail = MutableStateFlow<BarcodeDto?>(null)
    val productDetail = _productDetail.asStateFlow()

    private val _showProductDetail = MutableStateFlow(false)
    val showProductDetail = _showProductDetail.asStateFlow()

    init {
        _uiState.value = UiState.EMPTY

    }

    fun getBarcodeByCode() = executeInBackground(
        showProgressDialog = true,
        showErrorDialog = false
    ) {
        repo.getBarcodeByCode(
            searchProduct.value.trim(),
            SessionManager.companyId
        ).onSuccess {
            productID = it.product.id
            _productDetail.emit(it)
            _showProductDetail.emit(true)
        }.onError { message, _ ->
            showError(
                ErrorDialogDto(
                    title = stringProvider.invoke(R.string.error),
                    message = message
                )
            )
        }
    }

    fun getShelfByCode(shelfCode: String, enterShelf: Boolean) {
        executeInBackground(showProgressDialog = true) {
            repo.getShelfByCode(
                code = shelfCode.trim(),
                warehouseId = SessionManager.warehouseId,
                storageId = 0
            ).onSuccess {
                shelfEId = it.shelfId
            }
        }
    }

    private fun isValidFields(): Boolean {
        when {
            storageId == 0 -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.select_storage
                    )
                )
                return false
            }

            shelfEId == 0 -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.exit_shelf_address
                    )
                )
                return false
            }

            enteredQuantity.value.toInt() == 0 -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.enter_valid_qty
                    )
                )
                return false
            }

            else ->
                return true
        }
    }

    fun createStorageMovement() {
        if (isValidFields()) {
            executeInBackground(_uiState) {
                repo.createStockCorrection(
                    type = typeId,
                    isProcessToErp = isCheckBoxChecked.value,
                    storageId = storageId,
                    shelfId = shelfEId,
                    productId = productID,
                    quantity = enteredQuantity.value.toDoubleOrZero(),
                    price = enteredPrices.value.toDoubleOrZero(),
                    notes = enteredNotes.value
                ).onSuccess {
                    transferSuccess.emit(true)
                    productID = 0
                    exitShelfId.emit(0)
                    searchProduct.emit("")
                    _storageName.emit("")
                    enterShelfValue.emit("")
                    enteredQuantity.emit("")
                    _productDetail.emit(null)
                    _showProductDetail.emit(false)
                }
            }
        }
    }

    fun setStorage(storageDtoEnter: StorageDto) {
        this.storageId = storageDtoEnter.id
        viewModelScope.launch {
            _storageName.emit(storageDtoEnter.definition)
        }
    }

    fun setStockType(stockType: StockTypeDto) {
        this.typeId = stockType.type
        viewModelScope.launch {
            _stockTypeName.emit(stringProvider.invoke(stockType.titleRes))
        }
    }

}