package eryaz.software.basewms.ui.dashboard.inbound.acceptance.acceptanceProcess

import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.enums.UiState
import eryaz.software.basewms.data.models.dto.ButtonDto
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.data.models.dto.WaybillListDetailDto
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.persistence.TemporaryCashManager
import eryaz.software.basewms.data.stepService.StepCountManager
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.extensions.toIntOrZero
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AcceptanceProcessVM(
    private val repo: WorkActivityRepo
) : BaseViewModel() {

    private val SERIAL_WORK = 1.0

    val serialCheck = MutableStateFlow(false)
    private var waybillDetailList: List<WaybillListDetailDto> = emptyList()
    private var productID: Int = 0
    private var allowOverload: Boolean = false

    val searchProduct = MutableStateFlow("")
    val showCreateBarcode = MutableSharedFlow<Boolean>()
    val actionIsFinished = MutableStateFlow(false)
    val buttonEnabled = MutableStateFlow(false)
    val controlSuccess = MutableSharedFlow<Boolean>()
    val quantity = MutableStateFlow("1")
    val multiplier = MutableStateFlow("")
    private val multiplierValue = MutableStateFlow(1)
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

    private val _productDetail = MutableStateFlow<ProductDto?>(null)
    val productDetail = _productDetail.asStateFlow()

    private val _hasSerial = MutableStateFlow(false)
    val hasSerial = _hasSerial.asStateFlow()


    init {

        _uiState.value = UiState.EMPTY

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
            repo.getWaybillListDetail(
                workActivityId = workActivityID
            )
                .onSuccess {
                    waybillDetailList = it
                    checkIfAllFinished()
                }
        }
    }

    fun isQuantityValid(): Boolean {
        return quantity.value.isEmpty() || quantity.value == "0" || multiplierValue.value == 0
    }

    private fun isProductValid(productId: Int) {
        waybillDetailList.any {
            it.product.id == productId
        }.let { hasProduct ->
            if (hasProduct) {
                viewModelScope.launch {
                    _showProductDetailView.emit(true)
                }
            } else {
                viewModelScope.launch {
                    _showProductDetailView.emit(false)
                }
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
            showProgressDialog = true,
            uiState = _uiState
        ) {
            repo.getBarcodeByCode(
                code = searchProduct.value,
                companyId = SessionManager.companyId
            ).onSuccess {
                searchProduct.emit("")
                productID = it.product.id
                if (serialCheck.value) {
                    updateWaybillControlAddQuantity(SERIAL_WORK)
                } else {
                    _productDetail.emit(it.product)
                    _hasSerial.emit(it.product.hasSerial)
                    multiplierValue.emit(it.quantity)
                    multiplier.emit("× " + it.quantity.toString())
                }
                isProductValid(it.product.id)
            }.onError { _, _ ->
                showCreateBarcode.emit(true)
                _showProductDetailView.emit(false)
            }
        }
    }

    private suspend fun checkIfAllFinished() {
        controlSuccess.emit(waybillDetailList.all { waybillDetail ->
            waybillDetail.quantityControlled.toIntOrZero() >= waybillDetail.quantity
        })
    }

    fun updateWaybillControlAddQuantity(quantity: Double) {
        val resultQuantity = quantity * multiplierValue.value

        TemporaryCashManager.getInstance().workAction?.let {
            executeInBackground(showProgressDialog = true, showErrorDialog = false) {
                repo.updateWaybillControlAddQuantity(
                    actionId = it.workActionId,
                    productId = productID,
                    quantity = resultQuantity,
                    allowOverload = allowOverload,
                    serialLot = serialValue.value,
                    containerCount = qtyContainer.value
                ).onSuccess {
                    _showProductDetailView.emit(false)
                    getWaybillListDetail()
                }.onError { message, _ ->
                    if (message?.startsWith("QTY-EXC") == true) {
                        val messageBody =
                            stringProvider.invoke(R.string.msg_over_quantity_str) + stringProvider.invoke(
                                R.string.empty_label
                            ) + stringProvider.invoke(R.string.msg_are_you_sure_for_this)
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
        TemporaryCashManager.getInstance().workAction?.let {
            executeInBackground {
                repo.finishWorkActionForStep(
                    actionId = it.workActionId,
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

    fun setEnteredProduct(dto: ProductDto) {
        productID = dto.id
        viewModelScope.launch {
            _productDetail.emit(dto)
            isProductValid(dto.id)
        }
    }
}