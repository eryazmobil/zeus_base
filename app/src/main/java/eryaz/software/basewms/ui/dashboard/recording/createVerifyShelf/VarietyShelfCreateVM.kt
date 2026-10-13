package eryaz.software.basewms.ui.dashboard.recording.createVerifyShelf

import android.util.Log
import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.data.models.remote.request.ProductShelfInsertRequest
import eryaz.software.basewms.data.models.remote.request.ProductShelfUpdateRequest
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.extensions.toIntOrZero
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VarietyShelfCreateVM(private val repo: WorkActivityRepo) : BaseViewModel() {

    private var productID: Int = 0
    private var productShelfId: Int = 0
    private var shelfId: Int = 0
    private var shelfTypeId: Int = 0

    val productBarcode = MutableStateFlow("")
    val shelfAddress = MutableStateFlow("")
    val maxQuantity = MutableStateFlow("")
    val safetyPercent = MutableStateFlow("")
    val createSuccess = MutableStateFlow(false)
    val shelfSuccess = MutableStateFlow(false)

    private val _createdBtn = MutableStateFlow(false)
    val createdBtn = _createdBtn.asStateFlow()

    private val _updateBtn = MutableStateFlow(false)
    val updateBtn = _updateBtn.asStateFlow()

    private val _deleteBtn = MutableStateFlow(false)
    val deleteBtn = _deleteBtn.asStateFlow()

    private val _productDetail = MutableStateFlow<ProductDto?>(null)
    val productDetail = _productDetail.asStateFlow()

    private val _showProductDetail = MutableStateFlow(false)
    val showProductDetail = _showProductDetail.asStateFlow()

    fun getBarcodeByCode() = executeInBackground(
        showProgressDialog = true,
        showErrorDialog = false
    )
    {
        repo.getBarcodeByCode(
            companyId = SessionManager.companyId,
            code = productBarcode.value.trim(),
        ).onSuccess {
            productID = it.id
            _productDetail.emit(it.product)
            _showProductDetail.emit(true)
            getProductVarietyShelf()

        }.onError { _, _ ->
            showError(
                ErrorDialogDto(
                    titleRes = R.string.error,
                    messageRes = R.string.msg_no_product
                )
            )
        }
    }

    fun getShelfAddressFromApi() = executeInBackground(showProgressDialog = true) {
        repo.getShelfByCode(
            code = shelfAddress.value.trim(),
            warehouseId = SessionManager.warehouseId,
            0
        ).onSuccess {
            shelfId = it.shelfId
            shelfSuccess.emit(true)
        }.onError { _, _ ->
            showError(
                ErrorDialogDto(
                    title = stringProvider.invoke(eryaz.software.basewms.data.R.string.error),
                    message = stringProvider.invoke(eryaz.software.basewms.data.R.string.msg_shelf_not_found)
                )
            )
        }
    }

    private fun isValidFields(): Boolean {
        when {
            productID == 0 -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error, messageRes = R.string.product_code_empty
                    )
                )
                return false
            }

            maxQuantity.value.toIntOrZero() == 0 -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error, messageRes = R.string.enter_valid_qty
                    )
                )
                return false
            }

            safetyPercent.value.toIntOrZero() == 0 -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error, messageRes = R.string.enter_percent_valid_qty
                    )
                )
                return false
            }

            else -> return true
        }
    }

    fun createShelfInsert() {
        if (isValidFields()) {
            val shelfInsert = ProductShelfInsertRequest(
                productId = productID,
                shelfId = shelfId,
                shelfTypeId = shelfTypeId,
                safetyPercent = safetyPercent.value.toInt(),
                maxQuantity = maxQuantity.value.toInt()
            )
            executeInBackground {
                repo.createProductShelfInsert(
                    shelfInsert
                ).onSuccess {
                    productID = 0
                    shelfId = 0
                    shelfTypeId = 0
                    maxQuantity.emit("")
                    safetyPercent.emit("")
                    _productDetail.emit(null)
                    _showProductDetail.emit(false)
                    _createdBtn.emit(true)
                }
            }
        }
    }

    fun updateProductShelf() {
        if (isValidFields()) {
            val shelfInsert = ProductShelfUpdateRequest(
                productShelfId = productShelfId,
                safetyPercent = safetyPercent.value.toInt(),
                maxQuantity = maxQuantity.value.toInt()
            )
            executeInBackground {
                repo.updateProductShelf(
                    shelfInsert
                ).onSuccess {
                    productID = 0
                    shelfId = 0
                    shelfTypeId = 0
                    maxQuantity.emit("")
                    safetyPercent.emit("")
                    _productDetail.emit(null)
                    _showProductDetail.emit(false)
                    _createdBtn.emit(true)
                }
            }
        }
    }

    fun deleteProductShelf() {
        if (isValidFields()) {
            executeInBackground {
                repo.deleteProductShelf(
                    productId = productID
                ).onSuccess {
                    productID = 0
                    shelfId = 0
                    shelfTypeId = 0
                    maxQuantity.emit("")
                    safetyPercent.emit("")
                    _productDetail.emit(null)
                    _showProductDetail.emit(false)
                    createSuccess.emit(true)
                }
            }
        }
    }

    fun getProductVarietyShelf() {
        executeInBackground {
            repo.getProductVarietyShelf(
                productId = productID
            ).onSuccess {
                Log.d("TAG", "getProductVarietyShelf: varmi yokmu")
                productShelfId = it.shelf.shelfId
                shelfAddress.value = it.shelf.shelfAddress
                maxQuantity.value = it.maxQuantity.toString()
                safetyPercent.value = it.safetyPercent.toString()
                _createdBtn.emit(false)
                _deleteBtn.emit(true)
                _updateBtn.emit(true)
            }.onError { _, _ ->
                _createdBtn.emit(true)
            }
        }
    }

    fun setEnteredProduct(dto: ProductDto) {
        productID = dto.id
        viewModelScope.launch {
            getProductVarietyShelf()
            _showProductDetail.emit(true)
            _productDetail.emit(dto)
        }
    }
}
