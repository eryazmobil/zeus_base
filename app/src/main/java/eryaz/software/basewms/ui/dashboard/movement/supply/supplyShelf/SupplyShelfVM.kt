package eryaz.software.basewms.ui.dashboard.movement.supply.supplyShelf

import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.enums.ShelfType
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.data.models.dto.ProductShelfQuantityDto
import eryaz.software.basewms.data.models.dto.ProductShelfSupplyDto
import eryaz.software.basewms.data.models.dto.ShelfDto
import eryaz.software.basewms.data.models.dto.WorkActionDto
import eryaz.software.basewms.data.models.dto.WorkActivityDetailDto

import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.persistence.TemporaryCashManager
import eryaz.software.basewms.data.stepService.StepCountManager
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.extensions.orZero
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SupplyShelfVM(
    val workActivityRepo: WorkActivityRepo
) : BaseViewModel() {

    val productBarcode = MutableStateFlow("")
    val enteredQuantity = MutableStateFlow("")
    val firstShelfName = MutableStateFlow("")
    val secondShelfName = MutableStateFlow("")
    val supplyQuantity = MutableStateFlow("")
    val quantityInfo = MutableStateFlow("")
    val parentView = MutableStateFlow(false)
    val checkAllFinished = MutableStateFlow(false)
    var productId: Int = 0


    private val _workActivityDetailList =
        MutableStateFlow<List<WorkActivityDetailDto?>?>(emptyList())
    private val _stockMovementShelfList =
        MutableStateFlow<List<ProductShelfSupplyDto?>?>(emptyList())
    val stockMovementShelfList = _stockMovementShelfList.asStateFlow()

    private var selectedSuggestionIndex: Int = -1
    private var shelfId: Int = 0
    private val firstShelf = MutableStateFlow<ShelfDto?>(null)
    private val secondShelf = MutableStateFlow<ShelfDto?>(null)
    val shelfIsValidForSupply = MutableStateFlow<Boolean>(false)

    private val _selectedSuggestion = MutableStateFlow<WorkActivityDetailDto?>(null)
    var selectedSuggestion = _selectedSuggestion.asStateFlow()

    private val _showProductDetail = MutableStateFlow(false)
    val showProductDetail = _showProductDetail.asStateFlow()

    private val _productQuantity = MutableStateFlow("")
    val productQuantity = _productQuantity.asStateFlow()

    private val _productDetail = MutableStateFlow<ProductDto?>(null)
    val productDetail = _productDetail.asStateFlow()

    private val _nextOrder = MutableStateFlow(false)
    val nextOrder = _nextOrder.asStateFlow()

    private val _finishWorkAction = MutableStateFlow(false)
    val finishWorkAction = _finishWorkAction.asStateFlow()

    private val _workActionDto = MutableSharedFlow<WorkActionDto>()
    val workActionDto = _workActionDto.asSharedFlow()

    private val _pageNum = MutableStateFlow("")
    val pageNum = _pageNum.asStateFlow()

    private val _shelfList = MutableStateFlow(listOf<ProductShelfQuantityDto>())
    val shelfList = _shelfList.asStateFlow()

    init {
        getWorkActivityDetailToCollectProductForReplenishmentForPda()
    }

    fun getWorkActivityDetailToCollectProductForReplenishmentForPda() {

        executeInBackground(_uiState) {
            workActivityRepo.getWorkActivityDetailToCollectProductForReplenishmentForPda(
                TemporaryCashManager.getInstance().workActivity?.workActivityId.orZero()
            ).onSuccess {
                if (it.isEmpty()) {
                    parentView.emit(true)
                } else {
                    _workActivityDetailList.emit(it)
                    showNext()
                    getProductShelfQuantityListForProductShelfSupplyForPda(selectedSuggestion.value?.product?.id.orZero())

                }
            }

        }

    }

    fun getBarcodeByCode() {
        executeInBackground(showProgressDialog = true) {
            workActivityRepo.getBarcodeByCode(
                code = productBarcode.value, companyId = SessionManager.companyId
            ).onSuccess {
                productId = it.product.id
                _productQuantity.emit("x " + it.quantity.toString())
                _productDetail.emit(it.product)
                checkProductOrder()
            }.onError { _, _ ->
                _showProductDetail.emit(false)
                productBarcode.emit("")
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error, messageRes = R.string.msg_no_barcode
                    )
                )
            }
        }
    }

    fun getShelfByCode(shelfType: ShelfType, shelfCode: String) {
        executeInBackground(
            showErrorDialog = true, showProgressDialog = true, hasNextRequest = true
        ) {
            workActivityRepo.getShelfByCode(
                code = shelfCode,
                warehouseId = SessionManager.warehouseId,
                storageId = 0
            ).onSuccess {
                shelfId = it.shelfId
                when (shelfType) {
                    ShelfType.STOCK -> firstShelf.emit(it)
                    ShelfType.GATHER -> secondShelf.emit(it)
                }
                getShelfTypeByShelfId(shelfType)

            }.onError { _, _ ->
                when (shelfType) {
                    ShelfType.STOCK -> {
                        firstShelfName.emit("")
                        shelfIsValidForSupply.emit(false)
                    }

                    ShelfType.GATHER -> secondShelfName.emit("")
                }
                showError(
                    ErrorDialogDto(
                        title = stringProvider.invoke(eryaz.software.basewms.data.R.string.error),
                        message = stringProvider.invoke(eryaz.software.basewms.data.R.string.msg_shelf_not_found)
                    )
                )
            }
        }
    }

    private fun checkProductOrder() {

        _workActivityDetailList.value?.find {
            viewModelScope.launch {
                _showProductDetail.emit(it?.product?.id == productId)
            }
            it?.product?.id == productId
        }?.let { workActivityDetail ->
            viewModelScope.launch {
                _productDetail.emit(workActivityDetail.product)
                _workActivityDetailList.value?.indexOfFirst { it?.product?.id == workActivityDetail.product.id }
                    ?.let { index ->
                        selectedSuggestionIndex = index - 1
                        showNext()
                    }
                getProductShelfQuantityListForProductShelfSupplyForPda(productId)
            }
        } ?: showError(
            ErrorDialogDto(
                title = stringProvider.invoke(eryaz.software.basewms.data.R.string.error),
                message = stringProvider.invoke(R.string.not_replenishment_product)
            )
        )
    }

    fun showNext() {
        viewModelScope.launch {
            selectedSuggestionIndex++

            _workActivityDetailList.value?.getOrNull(selectedSuggestionIndex)?.let {
                _selectedSuggestion.emit(it)
                quantityInfo.emit("Yerleştirilen: " + it.placedReplenishmentQty + " Tavsiye: " + it.quantity + " Toplanan: " + it.placedCollectQuantity)
                productId = it.product.id
                getProductShelfQuantityListForProductShelfSupplyForPda(productId)
            } ?: run { selectedSuggestionIndex-- }

            _pageNum.emit("${selectedSuggestionIndex + 1} / ${_workActivityDetailList.value?.size}")
        }
    }

    fun showPrevious() {
        viewModelScope.launch {
            selectedSuggestionIndex--

            _workActivityDetailList.value?.getOrNull(selectedSuggestionIndex)?.let {
                _selectedSuggestion.emit(it)
                quantityInfo.emit("Yerleştirilen: " + it.placedReplenishmentQty + " Tavsiye: " + it.quantity + " Toplanan: " + it.placedCollectQuantity)
                productId = it.product.id
                getProductShelfQuantityListForProductShelfSupplyForPda(productId)
            } ?: run { selectedSuggestionIndex++ }

            _pageNum.emit("${selectedSuggestionIndex + 1} / ${_workActivityDetailList.value?.size}")

        }
    }

    private fun getShelfTypeByShelfId(shelf: ShelfType) {
        executeInBackground(showProgressDialog = true)
        {
            workActivityRepo.getShelfTypeByShelfId(shelfId)
                .onSuccess {
                    shelfIsValidForSupply.emit(it.code == shelf.type)
                    if (!shelfIsValidForSupply.value) {
                        showError(
                            ErrorDialogDto(
                                title = stringProvider.invoke(eryaz.software.basewms.data.R.string.error),
                                message = stringProvider.invoke(R.string.msg_invalid_shelf)
                            )
                        )
                        when (shelf) {
                            ShelfType.STOCK -> {
                                firstShelfName.emit("")
                                firstShelf.emit(null)
                            }

                            ShelfType.GATHER -> {
                                secondShelfName.emit("")
                                secondShelf.emit(null)
                                shelfIsValidForSupply.emit(true)
                            }
                        }
                    }
                }
        }
    }

    fun finishWorkAction(whenComplete: () -> Unit) {
        executeInBackground(showProgressDialog = true) {
            workActivityRepo.finishWorkActionForStep(
                actionId = TemporaryCashManager.getInstance().workAction?.workActionId.orZero(),
                endStep = StepCountManager.currentStep()
            ).onSuccess {
                    whenComplete()
                }
        }
    }

    fun createShelfMovementForReplenishment(quantity: Int) {
        executeInBackground() {
            workActivityRepo.createShelfMovementForReplenishment(
                TemporaryCashManager.getInstance().workActivity?.workActivityId.orZero(),
                productId,
                firstShelf.value?.shelfId.orZero(),
                secondShelf.value?.shelfId.orZero(),
                quantity
            ).onSuccess {
                getWorkActivityDetailToCollectProductForReplenishmentForPda()

            }.onError { message, _ ->
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error, message = message
                    )
                )
            }
        }
    }

    private fun checkAllFinished(): Boolean {
        val remain = _workActivityDetailList.value?.find {
            it?.sourceId != 1
        }
        return remain == null
    }

    fun finishWorkActivity(whenComplete: (Boolean) -> Unit) {
        executeInBackground(
            showProgressDialog = true
        ) {
            workActivityRepo.finishWorkActivity(
                TemporaryCashManager.getInstance().workActivity?.workActivityId.orZero()
            ).onSuccess {
                whenComplete(it)
            }
                .onError { message, _ ->
                    showError(
                        ErrorDialogDto(
                            titleRes = R.string.error, message = message
                        )
                    )
                }
        }
    }

    fun setEnteredProduct(dto: ProductDto) {
        productId = dto.id
        viewModelScope.launch {
            checkProductOrder()
            productBarcode.emit(dto.code)
        }
    }

    private fun getProductShelfQuantityListForProductShelfSupplyForPda(productId:Int) {
        executeInBackground(
            showErrorDialog = true,
            showProgressDialog = true
        ) {
            workActivityRepo.getProductShelfQuantityListForProductShelfSupplyForPda(
                productId = productId,
                companyId = SessionManager.companyId,
                warehouseId = SessionManager.warehouseId,
                storageId = 1
            ).onSuccess {
                if (it.isNotEmpty()) {
                    _stockMovementShelfList.emit(it)
                    supplyQuantity.emit(it[0].quantity.orEmpty())
                    checkAllFinished.emit(checkAllFinished())
                }
            }
        }
    }

}