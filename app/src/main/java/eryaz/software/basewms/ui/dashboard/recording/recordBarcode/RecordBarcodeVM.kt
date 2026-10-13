package eryaz.software.basewms.ui.dashboard.recording.recordBarcode

import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.data.models.remote.request.BarcodeRequestModel
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.repositories.BarcodeRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.extensions.toIntOrZero
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecordBarcodeVM(private val barcodeRepo: BarcodeRepo) : BaseViewModel() {

    private var productID: Int = 0

    val createdBarcode = MutableSharedFlow<Boolean>()
    val searchProductBarcode = MutableStateFlow("")
    val searchProductCode = MutableStateFlow("")
    val enteredQuantity = MutableStateFlow("1")

    private val _productDetail = MutableStateFlow<ProductDto?>(null)
    val productDetail = _productDetail.asStateFlow()

    private val _showProductDetail = MutableStateFlow(false)
    val showProductDetail = _showProductDetail.asStateFlow()

    fun getProductByCode() = executeInBackground(
        showProgressDialog = true, showErrorDialog = false
    ) {
        barcodeRepo.getProductByCode(
            SessionManager.companyId,
            searchProductCode.value.trim(),
        ).onSuccess {
            productID = it.id
            _productDetail.emit(it)
            _showProductDetail.emit(true)
        }.onError { _, _ ->
            showError(
                ErrorDialogDto(
                    titleRes = R.string.error,
                    messageRes = R.string.msg_no_product
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

            searchProductBarcode.value.isEmpty() -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error, messageRes = R.string.enter_barcode
                    )
                )
                return false
            }

            enteredQuantity.value.toIntOrZero() == 0 -> {
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

    fun createBarcode() {
        if (isValidFields()) {
            val createBarcode = BarcodeRequestModel(
                productId = productID,
                companyId = SessionManager.companyId,
                code = searchProductBarcode.value,
                quantity = enteredQuantity.value.toIntOrZero()
            )
            executeInBackground {
                barcodeRepo.createBarcode(
                    createBarcode
                ).onSuccess {
                    createdBarcode.emit(true)
                    productID = 0
                    searchProductCode.emit("")
                    enteredQuantity.emit("")
                    _productDetail.emit(null)
                    _showProductDetail.emit(false)
                }
            }
        }
    }

    fun setEnteredProduct(dto: ProductDto) {
        productID = dto.id
        viewModelScope.launch {
            _productDetail.emit(dto)
            _showProductDetail.emit(true)
        }
    }
}