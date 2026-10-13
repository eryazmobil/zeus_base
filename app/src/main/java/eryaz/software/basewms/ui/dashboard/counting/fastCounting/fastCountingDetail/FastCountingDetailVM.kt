package eryaz.software.basewms.ui.dashboard.counting.fastCounting.fastCountingDetail

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.BarcodeDto
import eryaz.software.basewms.data.models.dto.ButtonDto
import eryaz.software.basewms.data.models.dto.CountingComparisonDto
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.ProductShelfQuantityDto
import eryaz.software.basewms.data.models.remote.request.FastCountingProcessRequestModel
import eryaz.software.basewms.data.models.remote.response.ProductQuantityResponse
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.repositories.CountingRepo
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.extensions.toIntOrZero
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FastCountingDetailVM(
    private val countingRepo: CountingRepo,
    private val workActivityRepo: WorkActivityRepo,
    val stHeaderId: Int
) : BaseViewModel() {

    private var assignedShelfId: Int = 0
    private var productID: Int = 0
    private var stDetailId: Int = 0

    private var productQuantityList = listOf<ProductQuantityResponse>()
    private var productShelfQuantityList = listOf<ProductShelfQuantityDto>()
    var willCountedProductList = listOf<CountingComparisonDto>()

    val readShelfBarcode = MutableStateFlow(false)
    val searchProduct = MutableStateFlow("")
    val searchShelf = MutableStateFlow("")
    var quantityEdt = MutableStateFlow("")
    val hasNotProductBarcode = MutableStateFlow(false)

    private val _actionAddProduct = MutableStateFlow(false)
    val actionAddProduct = _actionAddProduct.asStateFlow()

    private val _productDetail = MutableStateFlow<BarcodeDto?>(null)
    val productDetail = _productDetail.asStateFlow()

    private val _actionIsFinished = MutableStateFlow(true)
    val actionIsFinished = _actionIsFinished.asStateFlow()

    private val _showProductDetail = MutableStateFlow(false)
    val showProductDetail = _showProductDetail.asStateFlow()

    fun getBarcodeByCode() =
        executeInBackground(showProgressDialog = true, showErrorDialog = false) {
            workActivityRepo.getBarcodeByCode(
                searchProduct.value.trim(), SessionManager.companyId
            ).onSuccess {
                productID = it.product.id

                willCountedProductList.find { listItem ->
                    listItem.productDto.id == productID
                }?.let {
                    if (it.newQuantity.get().toIntOrZero() != 0) {
                        showError(
                            ErrorDialogDto(
                                titleRes = R.string.warning,
                                messageRes = R.string.msg_barcode_scanned_before,
                                negativeButton = ButtonDto(text = 0)
                            )
                        )
                    }
                }

                _productDetail.emit(it)
                _showProductDetail.emit(true)
            }.onError { _, _ ->
                hasNotProductBarcode.emit(true)

                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.msg_empty_shelf
                    )
                )
            }
        }

    fun getShelfByCode() = executeInBackground(showProgressDialog = true, showErrorDialog = true) {
        workActivityRepo.getShelfByCode(
            code = searchShelf.value.trim(),
            warehouseId = SessionManager.warehouseId,
            storageId = 0
        ).onSuccess {
            assignedShelfId = it.shelfId

            isSuitableShelf()
        }.onError { _, _ ->
            showError(
                ErrorDialogDto(
                    title = stringProvider.invoke(R.string.error),
                    message = stringProvider.invoke(R.string.msg_shelf_not_found)
                )
            )
        }
    }

    private fun isSuitableShelf() = executeInBackground(showProgressDialog = true) {
        countingRepo.isSuitableShelf(
            stHeaderId = stHeaderId,
            shelfId = assignedShelfId
        ).onSuccess {
            if (it) {
                getAllAssignedDetailsToUser()
            } else {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.counting_and_shelf_warehouses_not_same
                    )
                )
            }
        }
    }

    private fun getAllAssignedDetailsToUser() {
        executeInBackground(showProgressDialog = true) {
            countingRepo.getAllAssignedDetailsToUser(
                stHeaderId = stHeaderId,
                shelfId = assignedShelfId,
                userId = SessionManager.userId
            ).onSuccess {
                if (it.isNotEmpty()) {
                    stDetailId = it[0].id
                    getShelfList()
                } else {
                    showError(
                        ErrorDialogDto(
                            titleRes = R.string.error,
                            messageRes = R.string.user_not_found
                        )
                    )
                }
            }
        }
    }

    private fun getShelfList() {
        executeInBackground(showProgressDialog = true) {
            countingRepo.getProductQuantityListWithShelf(
                stHeaderId = stHeaderId,
                shelfId = assignedShelfId,
                productId = 0,
                warehouseId = SessionManager.warehouseId,
                companyId = SessionManager.companyId
            ).onSuccess {
                if (it.isNotEmpty()) {
                    readShelfBarcode.emit(true)

                    productShelfQuantityList = it

                    willCountedProductList = it.map { dto ->
                        CountingComparisonDto(
                            productDto = dto.product,
                            oldQuantity = dto.quantity,
                            newQuantity = ObservableField("")
                        )
                    }
                } else {
                    showError(
                        ErrorDialogDto(
                            titleRes = R.string.error,
                            messageRes = R.string.user_not_found
                        )
                    )
                }
            }
        }
    }

    fun saveBtn() = executeInBackground(showProgressDialog = true) {
        productQuantityList = willCountedProductList.map {
            ProductQuantityResponse(
                product = it.productDto,
                quantity = it.newQuantity.get().toIntOrZero()
            )
        }
        val fastCountingProcessRequest = FastCountingProcessRequestModel(
            shelfId = assignedShelfId,
            productQuantityDtoList = productQuantityList,
            stockTakingDetailId = stDetailId
        )

        countingRepo.finishFastStocktakingDetail(
            fastCountingProcessRequest
        ).onSuccess {
            searchShelf.value = ""
            searchProduct.value = ""
            quantityEdt.value = ""

            _actionIsFinished.emit(true)
            _showProductDetail.emit(false)
            _productDetail.emit(null)
        }
    }

    fun isValidFinish(): Boolean {
        return willCountedProductList.any {
            it.oldQuantity.toIntOrZero() != it.newQuantity.get().toIntOrZero()
        }
    }

    fun addProductToList() {
        if (isValidFields()) {
            willCountedProductList.find {
                it.productDto.id == productID
            }?.let {
                val resulQuantity =
                    quantityEdt.value.toIntOrZero() + it.newQuantity.get().toIntOrZero()
                it.newQuantity.set(resulQuantity.toString())
            } ?: run {

                _productDetail.value?.let {
                    val countingComparisonDto = CountingComparisonDto(
                        productDto = it.product,
                        oldQuantity = "0",
                        newQuantity = ObservableField(quantityEdt.value)
                    )

                    willCountedProductList = willCountedProductList.toMutableList().apply {
                        add(countingComparisonDto)
                    }
                }
            }
            viewModelScope.launch {
                _showProductDetail.emit(false)
                quantityEdt.value = ""
                searchProduct.value = ""
            }
        }
    }

     fun isValidFields(): Boolean {
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

            quantityEdt.value.isEmpty() || quantityEdt.value.toInt() == 0 -> {
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
}