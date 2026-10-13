package eryaz.software.basewms.ui.dashboard.inbound.dat.acceptance.acceptanceProcess

import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.BarcodeDto
import eryaz.software.basewms.data.models.dto.ButtonDto
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.TransferRequestDetailDto
import eryaz.software.basewms.data.models.dto.WaybillListDetailDto
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.persistence.TemporaryCashManager
import eryaz.software.basewms.data.stepService.StepCountManager
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.extensions.toIntOrZero
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DatAcceptanceProcessVM(
    private val repo: WorkActivityRepo
) : BaseViewModel() {

    private val SERIAL_WORK = 1

    val serialCheck = MutableStateFlow(false)
    private var waybillDetailList: List<TransferRequestDetailDto> = emptyList()
    private var productID: Int = 0
    private var allowOverload: Boolean = false

    val searchProduct = MutableStateFlow("")
    val showCreateBarcode = MutableSharedFlow<Boolean>()
    val actionIsFinished = MutableStateFlow(false)
    val checkAllFinished = MutableStateFlow(false)
    val nextBarcode = MutableSharedFlow<Boolean>()
    val quantity = MutableStateFlow("")
    val multiplier = MutableStateFlow("")
    val multiplierValue = MutableStateFlow(1)
    val qtyContainer = MutableStateFlow(0)
    val serialValue = MutableStateFlow("")

    private val _orderDate = MutableStateFlow("")
    val orderDate = _orderDate.asStateFlow()

    private val _clientName = MutableStateFlow<String?>(null)
    val clientName = _clientName.asStateFlow()

    private val _productCode = MutableStateFlow("")
    val productCode = _productCode.asStateFlow()

    private val _showProductDetailView = MutableStateFlow(false)
    val showProductDetailView = _showProductDetailView.asStateFlow()

    private val _productDetail = MutableStateFlow<BarcodeDto?>(null)
    val productDetail = _productDetail.asStateFlow()

    private val _hasSerial = MutableStateFlow(false)
    val hasSerial = _hasSerial.asStateFlow()


    init {
        TemporaryCashManager.getInstance().workActivity?.let {

            viewModelScope.launch {
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
            )
                .onSuccess {
                    if (it.first().warehouseTo != null && it.first().warehouseFrom != null) {
                        _clientName.emit(it.first().warehouseRoute)
                    }
                    waybillDetailList = it
                    checkIfAllFinished()
                }
        }
    }

    fun isQuantityValid(): Boolean {
        return quantity.value.isEmpty() || quantity.value == "0"
    }

    private fun isProductValid() {
        waybillDetailList.any {
            it.product.id == productID
        }.let { hasProduct ->
            if (hasProduct) {
                viewModelScope.launch {
                    _showProductDetailView.emit(true)
                }
            } else {
                showError(
                    ErrorDialogDto(
                        title = stringProvider.invoke(eryaz.software.basewms.data.R.string.error),
                        message = stringProvider.invoke(eryaz.software.basewms.data.R.string.no_exist_in_waybill)
                    )
                )
            }
        }
    }

    fun getBarcodeByCode() {
        executeInBackground(
            showErrorDialog = false,
            showProgressDialog = true
        ) {
            repo.getBarcodeByCode(
                searchProduct.value.trim(),
                SessionManager.companyId
            ).onSuccess {
                if (serialCheck.value) {
                    updateWaybillControlAddQuantity(SERIAL_WORK)
                } else {
                    productID = it.product.id
                    _productDetail.emit(it)
                    _hasSerial.emit(it.product.hasSerial)
                    multiplier.emit("× " + it.quantity.toString())
                    multiplierValue.emit(it.quantity)
                }
                isProductValid()
            }.onError { _, _ ->
                showCreateBarcode.emit(true)
            }
        }
    }

    private suspend fun checkIfAllFinished() {
        checkAllFinished.emit(waybillDetailList.all { waybillDetail ->
            waybillDetail.receivingQuantityControlled.toIntOrZero() >= waybillDetail.quantity.toIntOrZero()
        })
    }

    fun updateWaybillControlAddQuantity(quantity: Int) {
        TemporaryCashManager.getInstance().workAction?.let {
            executeInBackground(showErrorDialog = false) {
                repo.updateTransferRequestControlAddQuantity(
                    actionId = it.workActionId,
                    productId = productID,
                    quantity = quantity * multiplierValue.value,
                    allowOverload = allowOverload,
                    serialLot = serialValue.value,
                    containerCount = qtyContainer.value
                ).onSuccess {
                    _showProductDetailView.emit(false)
                    nextBarcode.emit(true)
                    getWaybillListDetail()
                }.onError { message, _ ->
                    if (message?.startsWith("QTY-EXC") == true) {
                        val messageBody = stringProvider.invoke(R.string.msg_over_quantity_str) +
                                message.split(" ")[1] + stringProvider.invoke(R.string.msg_are_you_sure_for_this)
                        showError(
                            ErrorDialogDto(
                                title = stringProvider.invoke(R.string.msg_over_quantity),
                                message = messageBody,
                                positiveButton = ButtonDto(
                                    R.string.yes_uppercase,
                                    onClickListener = {
                                        allowOverload = true
                                        updateWaybillControlAddQuantity(quantity)
                                    }
                                )
                            )
                        )
                    } else {
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
    }

    fun finishWorkAction() {
        executeInBackground {
            repo.finishWorkActionForStep(
                actionId = TemporaryCashManager.getInstance().workAction?.workActionId ?: 0,
                endStep = StepCountManager.currentStep()
            ).onSuccess {
                actionIsFinished.emit(true)
            }.onError { message, _ ->
                ErrorDialogDto(
                    title = stringProvider.invoke(R.string.error),
                    message = message
                )
            }
        }

    }
}