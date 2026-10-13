package eryaz.software.basewms.ui.dashboard.inbound.dat.placement.placementDetail

import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.data.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.BarcodeDto
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.ShelfDto
import eryaz.software.basewms.data.models.dto.TransferRequestDetailDto
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.persistence.TemporaryCashManager
import eryaz.software.basewms.data.stepService.StepCountManager
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.extensions.toIntOrZero
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DatPlacementDetailVM(
    private val repo: WorkActivityRepo
) : BaseViewModel() {

    val suggestionShelfList = MutableStateFlow<List<ShelfDto>>(emptyList())
    val waybillDetailList = MutableStateFlow<List<TransferRequestDetailDto>>(emptyList())

    val searchProduct = MutableStateFlow("")
    val searchShelf = MutableStateFlow("")
    val productMultiplier = MutableStateFlow("")
    val productMultiplierValue = MutableStateFlow(1)
    val enteredAmount = MutableStateFlow("")
    val placementProductValue = MutableStateFlow("")
    val resultAmountTxt = MutableStateFlow("")
    val checkProductInPlacementList = MutableStateFlow(false)

    private var productID: Int = 0
    private var shelfID: Int = 0

    private val _orderDate = MutableStateFlow("")
    val orderDate = _orderDate.asStateFlow()

    private val _clientName = MutableStateFlow<String?>(null)
    val clientName = _clientName.asStateFlow()

    private val _productCode = MutableStateFlow("")
    val productCode = _productCode.asStateFlow()

    private val _productDetail = MutableStateFlow<BarcodeDto?>(null)
    val productDetail = _productDetail.asStateFlow()

    private val _actionIsFinished = MutableStateFlow(false)
    val actionIsFinished = _actionIsFinished.asStateFlow()

    private val _showProductDetail = MutableStateFlow(false)
    val showProductDetail = _showProductDetail.asStateFlow()

    private val _controlSuccess = MutableSharedFlow<Boolean>()
    val controlSuccess = _controlSuccess.asSharedFlow()

    private val _hasCrossDock = MutableSharedFlow<Boolean>()
    val hasCrossDock = _hasCrossDock.asSharedFlow()

    init {
        TemporaryCashManager.getInstance().workActivity?.let {
            viewModelScope.launch {
                _clientName.emit(it.client?.name)
                _orderDate.emit(it.creationTime)
                _productCode.emit(it.workActivityCode)
            }
        }
        getWaybillListDetail()

    }

    private fun getWaybillListDetail() {
        executeInBackground(_uiState) {
            val workActivityID =
                TemporaryCashManager.getInstance().workActivity?.workActivityId ?: 0
            repo.getTransferRequestDetailListForPdaReceiving(
                workActivityId = workActivityID
            ).onSuccess {
                waybillDetailList.emit(it)
                checkIfAllFinished()
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

    fun isQuantityValid(): Boolean {
        return productMultiplier.value.isEmpty() || productMultiplier.value == "0"
    }

    fun getBarcodeByCode() {
        executeInBackground(showErrorDialog = false) {
            repo.getBarcodeByCode(
                searchProduct.value.trim(), SessionManager.companyId
            ).onSuccess {
                _productDetail.emit(it)
                productID = it.product.id
                productMultiplier.value = it.quantity.toString()
                productMultiplierValue.emit(it.quantity)
                searchProduct.emit("")
                isProductValid()
            }.onError { _, _ ->
                showError(
                    ErrorDialogDto(
                        title = stringProvider.invoke(R.string.error),
                        message = stringProvider.invoke(R.string.msg_no_barcode)
                    )
                )
            }
        }
    }

    private fun isProductValid() {
        val currentList = waybillDetailList.value
        currentList.any {
            it.product.id == productID
        }.let { hasProduct ->
            when (hasProduct) {
                true -> {
                    getWaybillDetailQuantityWaitingForPlacement()
                }

                false -> {
                    showError(
                        ErrorDialogDto(
                            title = stringProvider.invoke(R.string.error),
                            message = stringProvider.invoke(R.string.msg_not_in_placement_list)
                        )
                    )
                }
            }
        }
    }

    private suspend fun checkIfAllFinished() {
        val controlledList = waybillDetailList.value.filter {
            it.receivingQuantityControlled.toIntOrZero() > 0
        }
        checkProductInPlacementList.emit(controlledList.all { waybillDetail ->
            waybillDetail.quantityPlaced.toIntOrZero() >= waybillDetail.receivingQuantityControlled.toIntOrZero()
        })
    }

    fun getShelfByCode() {
        executeInBackground(
            showErrorDialog = true,
            showProgressDialog = true
        ) {
            repo.getShelfByCode(
                code = searchShelf.value.trim(),
                warehouseId = SessionManager.warehouseId,
                storageId = 0
            ).onSuccess {
                shelfID = it.shelfId
                resultAmountTxt.emit(
                    (enteredAmount.value.toIntOrZero() * productMultiplierValue.value).toString() + " " +
                            stringProvider.invoke(
                                R.string.verify_qty_placement
                            )
                )
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

    private fun checkCrossDock() {
        TemporaryCashManager.getInstance().workAction?.let {
            executeInBackground {
                repo.getCrossDockRequestForPlacing(
                    productId = productID,
                    companyId = SessionManager.companyId,
                    warehouseId = SessionManager.warehouseId
                ).onSuccess {
                    _hasCrossDock.emit(true)
                }.onError { _, _ ->
                    getShelfListForPlacement()
                }
            }
        }
    }

    private fun getShelfListForPlacement() {
        TemporaryCashManager.getInstance().workAction?.let {
            executeInBackground {
                repo.getShelfListForPlacement(
                    productId = productID,
                    false,
                    it.workActionId
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

    fun updateWaybillControlAddQuantity() {
        TemporaryCashManager.getInstance().workAction?.let {
            executeInBackground(showErrorDialog = false) {
                repo.updateTransferRequestPlacementAddQuantity(
                    actionId = it.workActionId,
                    productId = productID,
                    quantity = enteredAmount.value.toIntOrZero() * productMultiplierValue.value,
                    shelfId = shelfID,
                    containerId = 0,
                    crossDockId = 0
                ).onSuccess {
                    _controlSuccess.emit(it)
                    _showProductDetail.emit(false)
                    getWaybillListDetail()
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
    }

    private fun getWaybillDetailQuantityWaitingForPlacement() {
        TemporaryCashManager.getInstance().workAction?.let {
            executeInBackground(showProgressDialog = true) {
                repo.getTransferDetailQuantityWaitingForPlacement(
                    actionId = it.workActionId, productId = productID
                ).onSuccess {
                    when (it) {
                        0 -> {
                            searchProduct.value = ""
                            showError(
                                ErrorDialogDto(
                                    title = stringProvider.invoke(R.string.error),
                                    message = stringProvider.invoke(R.string.controlled_notcompleted)
                                )
                            )
                        }

                        else -> {
                            viewModelScope.launch {
                                _showProductDetail.emit(true)
                            }
                            val multipleSign = "x ${productMultiplier.value} / $it"
                            placementProductValue.emit(multipleSign)
                            checkCrossDock()
                        }
                    }
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

    fun finishWorkAction() {
        TemporaryCashManager.getInstance().workAction?.let {
            executeInBackground {
                repo.finishWorkActionForStep(
                    actionId = it.workActionId,
                    endStep = StepCountManager.currentStep()
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

}