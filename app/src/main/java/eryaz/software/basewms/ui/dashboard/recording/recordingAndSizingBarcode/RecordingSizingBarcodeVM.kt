package eryaz.software.basewms.ui.dashboard.recording.recordingAndSizingBarcode

import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.data.models.remote.request.ProductDimensionsModel
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.repositories.BarcodeRepo
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.CombinedStateFlow
import eryaz.software.basewms.util.extensions.toDoubleOrZero
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecordingSizingBarcodeVM(
    private val barcodeRepo: BarcodeRepo,
    private val workActivityRepo: WorkActivityRepo
) : BaseViewModel() {


    private var productID: Int = 0

    val searchProductCode = MutableStateFlow("")
    val enteredWidth = MutableStateFlow("")
    val enteredWeight = MutableStateFlow("")
    val enteredLength = MutableStateFlow("")
    val enteredHeight = MutableStateFlow("")

    val checkAllFieldIsValid = CombinedStateFlow(
        enteredWeight,
        enteredLength,
        enteredWidth,
        enteredHeight
    ) {
        enteredWidth.value.isNotEmpty() && enteredWeight.value.isNotEmpty()
                && enteredLength.value.isNotEmpty() && enteredHeight.value.isNotEmpty()
    }

    private val _productDetail = MutableStateFlow<ProductDto?>(null)
    val productDetail = _productDetail.asStateFlow()

    private val _showProductDetail = MutableStateFlow(false)
    val showProductDetail = _showProductDetail.asStateFlow()


    fun getProductByCode() = executeInBackground(
        showProgressDialog = true, showErrorDialog = false
    ) {
        workActivityRepo.getBarcodeByCode(
            searchProductCode.value.trim(),
            SessionManager.companyId,

            ).onSuccess {
            productID = it.product.id
            _productDetail.emit(it.product)
            _showProductDetail.emit(true)
            emitSizeValues()
        }.onError { _, _ ->
            _showProductDetail.emit(false)
            _productDetail.emit(null)
            searchProductCode.emit("")
            showError(
                ErrorDialogDto(
                    titleRes = R.string.error,
                    messageRes = R.string.msg_no_product
                )
            )
        }
    }

    private suspend fun emitSizeValues() {
        enteredWidth.emit(_productDetail.value?.sizeWidth.toString())
        enteredHeight.emit(_productDetail.value?.sizeHeight.toString())
        enteredLength.emit(_productDetail.value?.sizeLength.toString())
        enteredWeight.emit(_productDetail.value?.weight.toString())

    }


    fun updateProductDimensions() = executeInBackground(
        showProgressDialog = true,
        showErrorDialog = true
    ) {
        barcodeRepo.updateProductDimensions(
            ProductDimensionsModel(
                productId = productID,
                warehouseId = SessionManager.warehouseId,
                sizeWidth = enteredWidth.value.toDoubleOrZero(),
                sizeLength = enteredLength.value.toDoubleOrZero(),
                sizeHeight = enteredHeight.value.toDoubleOrZero(),
                weight = enteredWeight.value.toDoubleOrZero()
            )
        ).onSuccess {
            if (it) {
                _showProductDetail.emit(false)
                _productDetail.emit(null)
                searchProductCode.emit("")
            } else {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.error_try_again
                    )
                )
            }
        }.onError { message, _ ->
            showError(
                ErrorDialogDto(
                    titleRes = R.string.error,
                    message = message.toString()
                )
            )
        }
    }


    fun setEnteredProduct(dto: ProductDto) {
        productID = dto.id
        viewModelScope.launch {
            _productDetail.emit(dto)
            emitSizeValues()
            _showProductDetail.emit(true)
        }
    }
}