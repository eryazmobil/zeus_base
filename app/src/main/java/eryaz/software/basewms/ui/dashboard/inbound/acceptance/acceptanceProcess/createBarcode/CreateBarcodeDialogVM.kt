package eryaz.software.basewms.ui.dashboard.inbound.acceptance.acceptanceProcess.createBarcode

import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.data.models.remote.request.BarcodeRequestModel
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.repositories.BarcodeRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.CombinedStateFlow
import eryaz.software.basewms.util.extensions.toIntOrZero
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateBarcodeDialogVM(private val repo: BarcodeRepo) : BaseViewModel() {

    val productCode = MutableStateFlow("")
    val productBarcode = MutableStateFlow("")
    private var productId = 0
    val productQuantity = MutableStateFlow("1")
    private var _productDetail = MutableStateFlow<ProductDto?>(null)
    val productDetail = _productDetail.asStateFlow()

    private val _showProductViewDetail = MutableStateFlow(false)
    val showProductViewDetail = _showProductViewDetail.asStateFlow()

    private val _showSuccess = MutableSharedFlow<Boolean>()
    val showSuccess = _showSuccess.asSharedFlow()

    private val _showErrorProduct = MutableSharedFlow<Boolean>()
    val showErrorProduct = _showErrorProduct.asSharedFlow()

    private val _showErrorProductMessage = MutableSharedFlow<String>()
    val showErrorProductMessage = _showErrorProductMessage.asSharedFlow()

    val saveEnabled = CombinedStateFlow(productCode, productBarcode, productQuantity) {
        productBarcode.value.isNotEmpty() && productQuantity.value.isNotEmpty()
    }

    fun getProductByCode() {
        executeInBackground(showErrorDialog = false) {
            val companyId = SessionManager.companyId
            repo.getProductByCode(
                companyId = companyId,
                code = productCode.value
            ).onSuccess {
                _productDetail.emit(it)
                productId = it.id

                _showProductViewDetail.emit(true)
            }
                .onError { _, _ ->
                    _showErrorProduct.emit(true)
                }
        }
    }

    fun createBarcode() {
        val createBarcode = BarcodeRequestModel(
            productId = productId,
            companyId = SessionManager.companyId,
            code = productBarcode.value,
            quantity = productQuantity.value.toIntOrZero()
        )
        executeInBackground(showErrorDialog = false) {
            repo.createBarcode(
                createBarcode
            ).onSuccess {
                _showSuccess.emit(true)
            }.onError { message, _ ->
                _showErrorProductMessage.emit(message.toString())
            }
        }
    }

    fun setEnteredProduct(dto: ProductDto) {
        productId = dto.id
        viewModelScope.launch {
            _productDetail.emit(dto)
            productCode.emit(dto.code)
            _showProductViewDetail.emit(true)
        }
    }

}