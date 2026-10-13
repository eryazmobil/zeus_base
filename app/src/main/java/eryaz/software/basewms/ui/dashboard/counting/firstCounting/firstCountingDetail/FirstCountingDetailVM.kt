package eryaz.software.basewms.ui.dashboard.counting.firstCounting.firstCountingDetail

import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.enums.UiState
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.repositories.CountingRepo
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.extensions.orZero
import eryaz.software.basewms.util.extensions.toDoubleOrZero
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FirstCountingDetailVM(
    private val countingRepo: CountingRepo,
    private val workActivityRepo: WorkActivityRepo,
    val stHeaderId: Int
) : BaseViewModel() {

    var assignedShelfId: Int = 0
    private var productID: Int = 0
    var stDetailId: Int = 0
    val searchProduct = MutableStateFlow("")
    val searchShelf = MutableStateFlow("")
    var quantityEdt = MutableStateFlow("")

    val hasNotProductBarcode = MutableSharedFlow<Boolean>()
    val readShelfBarcode = MutableSharedFlow<Boolean>()
    val actionAddProduct = MutableSharedFlow<Boolean>()

    val actionIsFinished = MutableSharedFlow<Boolean>()

    private val _productDetail = MutableStateFlow<ProductDto?>(null)
    val productDetail = _productDetail.asStateFlow()

    private val _showProductDetail = MutableStateFlow(false)
    val showProductDetail = _showProductDetail.asStateFlow()

    private val _validShelf = MutableStateFlow(false)
    val validShelf = _validShelf.asStateFlow()

    init {
        _uiState.value = UiState.EMPTY
    }

    fun getBarcodeByCode() {
        executeInBackground(
            showErrorDialog = false,
            showProgressDialog = true
        ) {
            workActivityRepo.getBarcodeByCode(
                searchProduct.value.trim(), SessionManager.companyId
            ).onSuccess {
                if (it.product.id == 0) {
                    showError(
                        ErrorDialogDto(
                            titleRes = R.string.error,
                            messageRes = R.string.msg_empty_shelf
                        )
                    )
                } else {
                    _productDetail.emit(it.product)
                    productID = it.product.id
                    _showProductDetail.emit(true)
                }
            }.onError { _, _ ->
                hasNotProductBarcode.emit(true)
            }
        }
    }

    fun getShelfByCode() {
        executeInBackground(
            showProgressDialog = true,
            hasNextRequest = true
        ) {
            workActivityRepo.getShelfByCode(
                code = searchShelf.value.trim(),
                warehouseId = SessionManager.warehouseId,
                storageId = 0
            ).onSuccess {
                assignedShelfId = it.shelfId
                getShelfIsOnAssignedUser()
            }.onError { _, _ ->
                showError(
                    ErrorDialogDto(
                        title = stringProvider.invoke(R.string.error),
                        message = stringProvider.invoke(R.string.msg_shelf_not_found)
                    )
                )
            }
        }
    }

    private fun getShelfIsOnAssignedUser() {
        executeInBackground(showProgressDialog = true) {
            countingRepo.getShelfIsOnAssignedUser(
                stHeaderId = stHeaderId,
                userId = SessionManager.userId,
                shelfId = assignedShelfId
            ).onSuccess {
                if (it) {
                    createSTActionProcessFromSTDetail()
                } else {
                    showError(
                        ErrorDialogDto(
                            titleRes = R.string.error,
                            messageRes = R.string.can_not_counting_in_shelf
                        )
                    )
                }
            }
        }
    }

    private fun createSTActionProcessFromSTDetail() {
        executeInBackground(showProgressDialog = true) {
            countingRepo.createSTActionProcessFromSTDetail(
                stHeaderId = stHeaderId,
                userId = SessionManager.userId,
                assignedShelfId = assignedShelfId
            ).onSuccess {
                stDetailId = it.first().stockTakingDetail?.id.orZero()
                readShelfBarcode.emit(true)
                _validShelf.emit(true)

            }
        }
    }

    fun checkProductExistsInShelfForPda(onResult: (Boolean, Double) -> Unit) {
        executeInBackground(showProgressDialog = true) {
            countingRepo.checkProductExistsInShelfForPda(
                stHeaderId = stHeaderId,
                assignedShelfId = assignedShelfId,
                productId = productID
            ).onSuccess {
                onResult(true, it)
            }.onError { _, _ ->
                onResult(false, 0.0)
            }
        }
    }

    fun addProduct(oldQty: Double = 0.0) {
        if (isValidFields()) {
            val qty = oldQty + quantityEdt.value.toDoubleOrZero()
            executeInBackground(showProgressDialog = true) {
                countingRepo.addProduct(
                    stHeaderId = stHeaderId,
                    userId = SessionManager.userId,
                    productId = productID,
                    assignedShelfId = assignedShelfId,
                    countedQuantity = qty
                ).onSuccess {
                    actionAddProduct.emit(true)
                    searchProduct.value = ""
                    quantityEdt.value = ""
                    _showProductDetail.emit(false)
                    _productDetail.emit(null)
                }
            }
        }
    }

    private fun isValidFields(): Boolean {
        when {
            assignedShelfId == 0 -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error, messageRes = R.string.enter_shelf_
                    )
                )
                return false
            }

            productID == 0 -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error, messageRes = R.string.product_code_empty
                    )
                )
                return false
            }

            quantityEdt.value.isEmpty() || quantityEdt.value.toDoubleOrZero() == 0.0 -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error, messageRes = R.string.enter_valid_qty
                    )
                )
                return false
            }

            else -> return true
        }
    }

    fun saveBtn() {
        executeInBackground(showProgressDialog = true) {
            countingRepo.finishSTDetail(
                stDetailId = stDetailId,
                userId = SessionManager.userId
            ).onSuccess {
                actionIsFinished.emit(true)
                searchShelf.value = ""
                searchProduct.value = ""
                quantityEdt.value = ""
                _validShelf.emit(false)
                _showProductDetail.emit(false)
                _productDetail.emit(null)
                stDetailId = 0
            }
        }
    }

    fun setEnteredProduct(dto: ProductDto) {
        viewModelScope.launch {
            _showProductDetail.emit(true)
            _productDetail.emit(dto)
            productID = dto.id
        }
    }

}