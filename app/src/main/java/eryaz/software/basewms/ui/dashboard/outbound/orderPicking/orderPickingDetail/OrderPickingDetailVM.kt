package eryaz.software.basewms.ui.dashboard.outbound.orderPicking.orderPickingDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.ButtonDto
import eryaz.software.basewms.data.models.dto.ConfirmationDialogDto
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.OrderDetailDto
import eryaz.software.basewms.data.models.dto.OrderPickingDto
import eryaz.software.basewms.data.models.dto.PickingSuggestionDto
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.data.models.dto.ProductShelfQuantityDto
import eryaz.software.basewms.data.models.dto.WorkActionDto
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.persistence.TemporaryCashManager
import eryaz.software.basewms.data.stepService.StepCountManager
import eryaz.software.basewms.data.repositories.OrderRepo
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.CombinedStateFlow
import eryaz.software.basewms.util.extensions.orZero
import eryaz.software.basewms.util.extensions.toDoubleOrZero
import eryaz.software.basewms.util.extensions.toIntOrZero
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderPickingDetailVM(
    private val orderRepo: OrderRepo,
    private val workActivityRepo: WorkActivityRepo
) : BaseViewModel() {

    val productBarcode = MutableStateFlow("")
    val enteredQuantity = MutableStateFlow("")
    val shelfAddress = MutableStateFlow("")
    private val fifoCode = MutableStateFlow("")
    val parentView = MutableStateFlow(false)
    val readNewBarcode = MutableSharedFlow<Boolean>()
    val stockNotEnough = MutableSharedFlow<Boolean>()

    var productId: Int = 0
    private var lastIx: Int = 0

    private var orderPickingDto: OrderPickingDto? = null
    private var selectedOrderDetailProduct: OrderDetailDto? = null

    private var selectedSuggestionIndex: Int = 0

    var orderDetailList: List<OrderDetailDto> = emptyList()

    private var shelfId: Int = 0

    private var multiplier = 1

    private val _selectedSuggestion = MutableStateFlow<PickingSuggestionDto?>(null)
    var selectedSuggestion = _selectedSuggestion.asStateFlow()

    private val _showProductDetail = MutableStateFlow(false)
    val showProductDetail = _showProductDetail.asStateFlow()

    private val _pickedAndOrderQty = MutableStateFlow("")
    val pickedAndOrderQty = _pickedAndOrderQty.asStateFlow()

    private val _orderQuantityTxt = MutableStateFlow("")
    val orderQuantityTxt = _orderQuantityTxt.asStateFlow()

    private val _controlQtyAndCollectPoint = MutableStateFlow("")
    val controlQtyAndCollectPoint = _controlQtyAndCollectPoint.asStateFlow()

    private val _productQuantity = MutableStateFlow("")
    val productQuantity = _productQuantity.asStateFlow()

    private val _productDetail = MutableStateFlow<ProductDto?>(null)
    val productDetail = _productDetail.asStateFlow()

    private val _createStockOut = MutableStateFlow(false)
    val createStockOut = _createStockOut.asStateFlow()

    private val _shelfRead = MutableSharedFlow<Boolean>()
    val shelfRead = _shelfRead.asSharedFlow()

    private val _nextOrder = MutableStateFlow(false)
    val nextOrder = _nextOrder.asStateFlow()

    private val _finishWorkAction = MutableStateFlow(false)
    val finishWorkAction = _finishWorkAction.asStateFlow()

    private val _orderPickingList = MutableLiveData<List<PickingSuggestionDto?>>(emptyList())
    val orderPickingList: LiveData<List<PickingSuggestionDto?>> = _orderPickingList

    private val _workActionDto = MutableSharedFlow<WorkActionDto>()
    val workActionDto = _workActionDto.asSharedFlow()

    private val _pageNum = MutableStateFlow("")
    val pageNum = _pageNum.asStateFlow()

    private val _shelfList = MutableStateFlow(listOf<ProductShelfQuantityDto>())
    val shelfList = _shelfList.asStateFlow()

    val pickEnableBtn = CombinedStateFlow(enteredQuantity) {
        enteredQuantity.value.isNotEmpty()
    }

    init {
        getOrderDetailPickingList()
    }

    fun getOrderDetailPickingList() = executeInBackground(_uiState) {
        orderRepo.getOrderDetailPickingList(
            workActivityId = TemporaryCashManager.getInstance().workActivity?.workActivityId.orZero(),
            userId = SessionManager.userId
        ).onSuccess {
            if (it.orderDetailList.isNotEmpty()) {
                orderDetailList = it.orderDetailList
                if (it.pickingSuggestionList.isNotEmpty()) {
                    orderPickingDto = it
                    _selectedSuggestion.emit(it.pickingSuggestionList[lastIx])
                    setSelectedSuggestion()
                    readNewBarcode.emit(true)

                } else {
                    checkAllOrderCompleted(it.orderDetailList)
                }

            } else {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error, messageRes = R.string.work_activity_error_2
                    )
                )
            }
        }.onError { message, _ ->
            showError(
                ErrorDialogDto(
                    titleRes = R.string.error, message = message
                )
            )
        }
    }

    private fun setSelectedSuggestion() {

        viewModelScope.launch {
            val orderDetail = orderPickingDto?.orderDetailList?.find {
                selectedSuggestion.value?.id == it.id
            }

            productId = _selectedSuggestion.value?.product?.id ?: 0

            val remainingQty =
                orderDetail?.quantity.toDoubleOrZero() - orderDetail?.quantityCollected.toDoubleOrZero()
            _orderQuantityTxt.emit(
                "0 / $remainingQty"
            )

            _pageNum.emit("${selectedSuggestionIndex + 1} / ${orderPickingDto?.pickingSuggestionList?.size}")
        }
    }

    private suspend fun checkAllOrderCompleted(orderList: List<OrderDetailDto>) {
        val allIsCollected = orderList.all { it.quantityCollected == it.quantity }
        if (allIsCollected) {
            parentView.emit(true)
        } else {
            stockNotEnough.emit(true)
        }
    }

    private fun createStockOut() {
        executeInBackground(showProgressDialog = true) {
            orderRepo.createStockOut(
                productId = selectedSuggestion.value?.product?.id.orZero(), shelfId = shelfId
            ).onSuccess {
                _createStockOut.emit(it)
            }
        }
    }

    fun getBarcodeByCode() {
        executeInBackground(
            showProgressDialog = true
        ) {
            workActivityRepo.getBarcodeByCode(
                code = productBarcode.value, companyId = SessionManager.companyId
            ).onSuccess {
                if (checkProductOrder(it.product.id)) {
                    productId = it.product.id
                    _productQuantity.emit("x " + it.quantity.toString())
                    multiplier = it.quantity
                    _productDetail.emit(it.product)
                }
            }.onError { _, _ ->
                productBarcode.emit("")
                _showProductDetail.emit(false)

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
            showErrorDialog = true,
            showProgressDialog = true,
            hasNextRequest = true
        ) {
            workActivityRepo.getShelfByCode(
                code = shelfAddress.value.trim(),
                warehouseId = SessionManager.warehouseId,
                storageId = 0
            ).onSuccess {
                shelfId = it.shelfId

                getProductShelfQuantityList()
            }.onError { _, _ ->
                shelfAddress.emit("")

                showError(
                    ErrorDialogDto(
                        title = stringProvider.invoke(eryaz.software.basewms.data.R.string.error),
                        message = stringProvider.invoke(eryaz.software.basewms.data.R.string.msg_shelf_not_found)
                    )
                )
            }
        }
    }

    private fun getProductShelfQuantityList() = executeInBackground(
        showErrorDialog = true, showProgressDialog = true
    ) {
        workActivityRepo.getProductShelfQuantityList(
            productId = productId,
            shelfId = shelfId,
            warehouseId = SessionManager.warehouseId,
            companyId = SessionManager.companyId,
            storageId = 0
        ).onSuccess {
            _shelfList.emit(it)
            checkProductInShelf()
        }
    }

    fun updateOrderDetailCollectedAddQuantityForPda() {
        executeInBackground(showProgressDialog = true) {
            val quantity = enteredQuantity.value.toIntOrZero() * multiplier

            orderRepo.updateOrderDetailCollectedAddQuantityForPda(
                workActionId = TemporaryCashManager.getInstance().workAction?.workActionId.orZero(),
                productId = productId,
                shelfId = shelfId,
                containerId = 0,
                quantity = quantity,
                orderDetailId = selectedOrderDetailProduct?.id.orZero(),
                fifoCode = fifoCode.value
            ).onSuccess {
                getOrderDetailPickingList()
                checkPickingFromOrder()

                enteredQuantity.value = ""
                shelfAddress.value = ""
                _showProductDetail.emit(false)
            }
        }
    }

    fun checkCrossDockNeedByActionId() {
        executeInBackground {
            orderRepo.checkCrossDockNeedByActionId(
                workActionId = TemporaryCashManager.getInstance().workAction?.workActionId.orZero()
            ).onSuccess {
                if (it) {
                    showConfirmation(
                        ConfirmationDialogDto(
                            titleRes = R.string.attention,
                            messageRes = R.string.picking_not_completed,
                            positiveButton = ButtonDto(
                                text = R.string.create_crossdock,
                                onClickListener = {
                                    showConfirmation(
                                        ConfirmationDialogDto(
                                            titleRes = R.string.attention,
                                            messageRes = R.string.are_you_sure,
                                            positiveButton = ButtonDto(
                                                text = R.string.yes,
                                                onClickListener = {
                                                    createCrossDockRequest()
                                                }
                                            ), negativeButton = ButtonDto(
                                                text = R.string.no
                                            )
                                        )
                                    )
                                }
                            ),
                            negativeButton = ButtonDto(
                                text = R.string.i_leave_half,
                                onClickListener = {
                                    finishWorkAction()
                                }
                            )
                        )
                    )
                } else {
                    finishWorkAction()
                }
            }
        }
    }

    private fun createCrossDockRequest() {
        executeInBackground(showErrorDialog = true, showProgressDialog = true) {
            orderRepo.createCrossDockRequest(
                workActionId = TemporaryCashManager.getInstance().workAction?.workActionId.orZero()
            ).onSuccess {
                finishWorkAction()
            }
        }
    }

    fun finishWorkAction() {
        executeInBackground(showProgressDialog = true) {
            workActivityRepo.finishWorkActionForStep(
                actionId = TemporaryCashManager.getInstance().workAction?.workActionId.orZero(),
                endStep = StepCountManager.currentStep()
            ).onSuccess {
                    _finishWorkAction.emit(true)
                }
        }
    }

    private fun checkPickingFromOrder() {
        val isQuantityCollectedLess = orderPickingDto?.orderDetailList?.any {

            it.quantityCollected.toDoubleOrZero() < it.quantity.toDoubleOrZero()
        } ?: false

        if (!isQuantityCollectedLess) {
            finishWorkAction()
        }
    }

    private fun checkProductOrder(checkProductId: Int): Boolean {
        val orderDetail = orderPickingDto?.orderDetailList?.find {
            it.quantityCollected.toDoubleOrZero() < it.quantity.toDoubleOrZero() && it.product.id == checkProductId
        }
        if (orderDetail != null) {
            selectedOrderDetailProduct = orderDetail

            viewModelScope.launch {
                productBarcode.emit("")
                _showProductDetail.emit(true)
                _controlQtyAndCollectPoint.emit("${orderDetail.orderHeader.controlPoint?.code} / ${orderDetail.orderHeader.collectPoint}")

                orderPickingDto?.pickingSuggestionList?.indexOfFirst { it.product.id == orderDetail.product.id }
                    ?.let { index ->
                        lastIx =
                            if ((index == (orderPickingDto?.pickingSuggestionList?.size?.minus(1)
                                    ?: 0) && index != 0)
                            ) {
                                index - 1
                            } else {
                                index
                            }
                        selectedSuggestionIndex = index - 1
                        _pickedAndOrderQty.emit("${orderDetail.quantityCollected} / ${orderDetail.quantity}")

                        showNext()
                    }
            }
            return true
        } else {
            showError(
                ErrorDialogDto(
                    title = stringProvider.invoke(R.string.error),
                    message = stringProvider.invoke(R.string.msg_not_in_picking_list)
                )
            )
            return false
        }
    }

    private fun checkProductInShelf() {
        if (_shelfList.value.none {
                it.quantity.isNotEmpty()
            }) {
            showError(
                ErrorDialogDto(
                    titleRes = R.string.warning, messageRes = R.string.msg_not_in_this_shelf
                )
            )
        } else {
            viewModelScope.launch {
                _shelfRead.emit(true)
            }
        }
    }

    fun showNext() {
        viewModelScope.launch {
            selectedSuggestionIndex++

            orderPickingDto?.pickingSuggestionList?.getOrNull(selectedSuggestionIndex)?.let {
                _selectedSuggestion.emit(it)

                productId = it.product.id
            } ?: run { selectedSuggestionIndex-- }

            _orderQuantityTxt.emit(
                "${orderPickingDto?.pickingSuggestionList?.getOrNull(selectedSuggestionIndex)?.quantityPicked} / " +
                        "${orderPickingDto?.pickingSuggestionList?.getOrNull(selectedSuggestionIndex)?.quantityWillBePicked}"
            )

            _pageNum.emit("${selectedSuggestionIndex + 1} / ${orderPickingDto?.pickingSuggestionList?.size}")
        }
    }

    fun showPrevious() {
        viewModelScope.launch {
            selectedSuggestionIndex--

            orderPickingDto?.pickingSuggestionList?.getOrNull(selectedSuggestionIndex)?.let {
                _selectedSuggestion.emit(it)
                productId = it.product.id
            } ?: run { selectedSuggestionIndex++ }

            _pageNum.emit("${selectedSuggestionIndex + 1} / ${orderPickingDto?.pickingSuggestionList?.size}")

            _orderQuantityTxt.emit(
                "${orderPickingDto?.pickingSuggestionList?.getOrNull(selectedSuggestionIndex)?.quantityPicked} / " +
                        "${orderPickingDto?.pickingSuggestionList?.getOrNull(selectedSuggestionIndex)?.quantityWillBePicked}"
            )
        }
    }

    fun showInfo() {
        showError(
            ErrorDialogDto(
                titleRes = R.string.attention,
                message = "${selectedSuggestion.value?.product?.code} ${stringProvider.invoke(R.string.stock_out_message_1)}" + "${selectedSuggestion.value?.shelfForPicking?.shelf?.shelfAddress} ${
                    stringProvider.invoke(
                        R.string.stock_out_message_2
                    )
                }",
                positiveButton = ButtonDto(text = R.string.confirm, onClickListener = {
                    createStockOut()
                }),
                negativeButton = ButtonDto(
                    text = R.string.cancel
                )
            )
        )
    }

    fun setEnteredProduct(dto: ProductDto) {
        if (checkProductOrder(dto.id)) {
            productId = dto.id

            viewModelScope.launch {
                _productDetail.emit(dto)
                _productQuantity.emit("x " + 1)
            }
        }
    }
}