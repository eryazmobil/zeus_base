package eryaz.software.basewms.ui.dashboard.outbound.datCancel.transferPlacementProcess


import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.ButtonDto
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.OrderCancelReplacementDto
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.data.models.dto.ShelfDto
import eryaz.software.basewms.data.models.dto.WarningDialogDto
import eryaz.software.basewms.data.models.dto.WorkActivityDto
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.persistence.TemporaryCashManager
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.CombinedStateFlow
import eryaz.software.basewms.util.extensions.orZero
import eryaz.software.basewms.util.extensions.toIntOrZero
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransferPlacementDetailVM(private val repo: WorkActivityRepo) : BaseViewModel() {

    val suggestionShelfList = MutableStateFlow<List<ShelfDto>>(emptyList())

    val searchProduct = MutableStateFlow("")
    val searchShelf = MutableStateFlow("")
    val enteredAmount = MutableStateFlow("")
    val placementProductValue = MutableStateFlow("")
    val checkProductInPlacementList = MutableSharedFlow<Boolean>()

    private var productID: Int = 0
    private var shelfID: Int = 0

    private val _workActivity = MutableStateFlow<WorkActivityDto?>(null)
    val workActivity = _workActivity.asStateFlow()

    private val _productDetail = MutableStateFlow<ProductDto?>(null)
    val productDetail = _productDetail.asStateFlow()

    private val _actionIsFinished = MutableStateFlow(false)
    val actionIsFinished = _actionIsFinished.asStateFlow()

    private val _controlSuccess = MutableSharedFlow<Boolean>()
    val controlSuccess = _controlSuccess.asSharedFlow()

    private val _showProductDetailView = MutableStateFlow(false)
    val showProductDetailView = _showProductDetailView.asStateFlow()

    private val _hasProductToReplacement = MutableStateFlow(false)
    val hasProductToReplacement = _hasProductToReplacement.asStateFlow()

    private val _cancelReplacementList =
        MutableStateFlow<List<OrderCancelReplacementDto?>>(emptyList())

    private var _selectedCancelOrder = MutableStateFlow<OrderCancelReplacementDto?>(null)


    val cancelReplacementList = _cancelReplacementList.asStateFlow()

    val shelfIsValid = CombinedStateFlow(MutableStateFlow(shelfID)) {
        shelfID != 0
    }

    init {
        TemporaryCashManager.getInstance().workActivity?.let {
            viewModelScope.launch {
                _workActivity.emit(it)

            }
        }
        getTransferCancelReplacementList()

    }

    private fun getTransferCancelReplacementList() {
        executeInBackground(
            _uiState,
            showErrorDialog = false,
            showProgressDialog = true
        ) {
            repo.getTransferCancelReplacementList(
                workActivityId = TemporaryCashManager.getInstance().workActivity?.workActivityId.orZero()
            ).onSuccess {
                if (it.isEmpty()) {
                    showWarning(
                        WarningDialogDto(
                            title = stringProvider.invoke(R.string.all_placements_done),
                            message = stringProvider.invoke(R.string.list_is_empty),
                            completeButton = ButtonDto {
                                viewModelScope.launch {
                                    checkProductInPlacementList.emit(true)
                                }
                            }
                        )
                    )
                    return@onSuccess
                }
                _hasProductToReplacement.emit(true)
                _cancelReplacementList.emit(it)
            }.onError { _, _ ->
                showError(
                    ErrorDialogDto(
                        title = stringProvider.invoke(R.string.error),
                        message = stringProvider.invoke(R.string.placement_is_empty)
                    )
                )
            }
        }
    }

    fun getBarcodeByCode() {
        executeInBackground(showProgressDialog = true) {
            repo.getBarcodeByCode(
                code = searchProduct.value, companyId = SessionManager.companyId
            ).onSuccess {
                productID = it.product.id
                _productDetail.emit(it.product)
                checkProductOrder()
            }.onError { _, _ ->
                _showProductDetailView.emit(false)
                searchProduct.emit("")
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error, messageRes = R.string.msg_no_barcode
                    )
                )
            }
        }
    }

    fun getShelfByCode() {
        executeInBackground(
            showErrorDialog = false,
            showProgressDialog = true
        ) {
            repo.getShelfByCode(
                code = searchShelf.value.trim(),
                warehouseId = SessionManager.warehouseId,
                storageId = 0
            ).onSuccess {
                shelfID = it.shelfId
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

    private fun getTransferDetailQuantityWaitingForPlacement() {
        executeInBackground(
            showProgressDialog = true,
            hasNextRequest = true
        ) {
            repo.getTransferDetailQuantityWaitingForPlacement(
                actionId = TemporaryCashManager.getInstance().workAction?.workActionId.orZero(),
                productId = productID
            ).onSuccess {
                if (it == 0) {
                    showError(
                        ErrorDialogDto(
                            title = stringProvider.invoke(R.string.error),
                            message = stringProvider.invoke(R.string.placement_completed)
                        )
                    )
                    return@onSuccess
                }
                _showProductDetailView.emit(true)
                placementProductValue.emit("/ $it")
                getShelfListForPlacement()
            }
        }
    }

    private fun getShelfListForPlacement() {
        TemporaryCashManager.getInstance().workAction?.let {
            executeInBackground(showProgressDialog = true) {
                repo.getShelfListForPlacement(
                    productId = productID,
                    includeOld = false,
                    actionId = it.workActionId
                ).onSuccess {
                    suggestionShelfList.emit(
                        it.takeIf {
                            it.isNotEmpty()
                        } ?: listOf(
                            ShelfDto(
                                shelfId = 0,
                                shelfAddress = stringProvider.invoke(R.string.msg_no_shelf_suggestions),
                                quantity = ""
                            )
                        )
                    )
                }
            }
        }
    }

    fun updateTransferReplacementAddQuantityForPda() {
        executeInBackground(
            showErrorDialog = false,
            hasNextRequest = true
        ) {
            repo.updateTransferReplacementAddQuantityForPda(
                actionId = TemporaryCashManager.getInstance().workAction?.workActionId.orZero(),
                productId = productID,
                quantity = enteredAmount.value.toIntOrZero(),
                shelfId = shelfID,
                containerId = 0
            ).onSuccess {
                _controlSuccess.emit(it)
                _showProductDetailView.emit(false)
                getTransferCancelReplacementList()
            }.onError { message, _ ->
                showError(
                    ErrorDialogDto(
                        title = stringProvider.invoke(R.string.error),
                        message = message
                    )
                )
            }
        }
    }

    private fun checkProductOrder() {

        _cancelReplacementList.value.find {
            it?.product?.id == productID
        }?.let { cancelReplacementDto ->
            viewModelScope.launch {
                _selectedCancelOrder.emit(cancelReplacementDto)
            }
            getTransferDetailQuantityWaitingForPlacement()

        } ?: showError(
            ErrorDialogDto(
                title = stringProvider.invoke(R.string.error),
                message = stringProvider.invoke(R.string.msg_not_in_placement_list)
            )
        )
        viewModelScope.launch {
            _showProductDetailView.emit(false)
        }
    }

    fun finishWorkAction() {
        TemporaryCashManager.getInstance().workAction?.let {
            executeInBackground {
                repo.finishWorkAction(
                    actionId = it.workActionId
                ).onSuccess {
                    _actionIsFinished.emit(true)
                }.onError { message, _ ->
                    showError(
                        ErrorDialogDto(
                            title = stringProvider.invoke(R.string.error), message = message
                        )
                    )
                }
            }
        }
    }

    fun setEnteredProduct(dto: ProductDto) {
        productID = dto.id
        viewModelScope.launch {
            _productDetail.emit(dto)
            checkProductOrder()
        }
    }
}