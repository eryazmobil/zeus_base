package eryaz.software.basewms.ui.dashboard.outbound.datPicking.datPickingDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onError
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.BarcodeDto
import eryaz.software.basewms.data.models.dto.ButtonDto
import eryaz.software.basewms.data.models.dto.ConfirmationDialogDto
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.PickingSuggestionDto
import eryaz.software.basewms.data.models.dto.ProductShelfQuantityDto
import eryaz.software.basewms.data.models.dto.TransferPickingDto
import eryaz.software.basewms.data.models.dto.TransferRequestDetailDto
import eryaz.software.basewms.data.models.dto.WorkActionDto
import eryaz.software.basewms.data.models.dto.WorkActivityDto
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.persistence.TemporaryCashManager
import eryaz.software.basewms.data.stepService.StepCountManager
import eryaz.software.basewms.data.repositories.OrderRepo
import eryaz.software.basewms.data.repositories.WorkActivityRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.extensions.orZero
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DatPickingDetailVM(
    private val orderRepo: OrderRepo,
    private val workActivityRepo: WorkActivityRepo
) : BaseViewModel() {

    val productBarcode = MutableStateFlow("")
    val enteredQuantity = MutableStateFlow("")
    val shelfAddress = MutableStateFlow("")
    val parentView = MutableStateFlow(false)
    var productId: Int = 0

    private var transferPickingDto: TransferPickingDto? = null
    private var selectedTransferDetail: TransferRequestDetailDto? = null
    private var selectedPickingSuggestion: PickingSuggestionDto? = null

    private var selectedSuggestionIndex: Int = -1
    private var shelfId: Int = 0

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

    private val _productDetail = MutableStateFlow<BarcodeDto?>(null)
    val productDetail = _productDetail.asStateFlow()

    private val _createStockOut = MutableStateFlow(false)
    val createStockOut = _createStockOut.asStateFlow()

    private val _shelfRead = MutableSharedFlow<Boolean>()
    val shelfRead = _shelfRead.asSharedFlow()

    private val _nextOrder = MutableStateFlow(false)
    val nextOrder = _nextOrder.asStateFlow()

    private val _finishWorkAction = MutableStateFlow(false)
    val finishWorkAction = _finishWorkAction.asStateFlow()

    private val _orderPickingList = MutableLiveData<List<WorkActivityDto?>>(emptyList())
    val orderPickingList: LiveData<List<WorkActivityDto?>> = _orderPickingList

    private val _workActionDto = MutableSharedFlow<WorkActionDto>()
    val workActionDto = _workActionDto.asSharedFlow()

    private val _pageNum = MutableStateFlow("")
    val pageNum = _pageNum.asStateFlow()

    private val _shelfList = MutableStateFlow(listOf<ProductShelfQuantityDto>())
    val shelfList = _shelfList.asStateFlow()

    init {

        getTransferDetailPickingList()
    }

    fun getTransferDetailPickingList() = executeInBackground(_uiState) {
        workActivityRepo.getTransferDetailPickingList(
            workActivityId = TemporaryCashManager.getInstance().workActivity?.workActivityId.orZero()
        ).onSuccess {
            if (it.transferRequestDetailList.isNotEmpty()) {
                if (it.pickingSuggestionList.isNotEmpty()) {
                    transferPickingDto = it

                    showNext()
                } else {
                    parentView.emit(true)
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
        executeInBackground(showProgressDialog = true) {
            workActivityRepo.getBarcodeByCode(
                code = productBarcode.value, companyId = SessionManager.companyId
            ).onSuccess {
                productId = it.product.id
                _productQuantity.emit("x " + it.quantity.toString())
                _productDetail.emit(it)

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

    fun getShelfByCode() {
        executeInBackground(
            showErrorDialog = true, showProgressDialog = true, hasNextRequest = true
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
            val quantity = enteredQuantity.value.toInt()
            workActivityRepo.updateTransferRequestDetailPickedAddQuantity(
                workActionId = TemporaryCashManager.getInstance().workAction?.workActionId.orZero(),
                productId = productId,
                shelfId = shelfId,
                containerId = 0,
                quantity = quantity,
                transferRequestDetailId = selectedTransferDetail?.id.orZero()
            ).onSuccess {
                getTransferDetailPickingList()
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
                                                    //krosdock olustur
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
        val isQuantityCollectedLess = transferPickingDto?.transferRequestDetailList?.any {
            it.quantityPicked.toInt() < it.quantity.toInt()
        } ?: false

        if (!isQuantityCollectedLess) {
            finishWorkAction()
        }
    }

    private fun checkProductOrder() {
        transferPickingDto?.transferRequestDetailList?.find {
            it.quantityPicked.toInt() < it.quantity.toInt() && it.product.id == productId
        }?.let { transferDetail ->
            selectedTransferDetail = transferDetail
            setPickCard()
        } ?: showError(
            ErrorDialogDto(
                title = stringProvider.invoke(R.string.error),
                message = stringProvider.invoke(R.string.msg_not_in_picking_list)
            )
        )
    }

    private fun setPickCard() {
        transferPickingDto?.pickingSuggestionList?.find {
            it.product.id == productId
        }?.let { transferDetail ->
            selectedPickingSuggestion = transferDetail

            viewModelScope.launch {
                productBarcode.emit("")
                _showProductDetail.emit(true)
                _controlQtyAndCollectPoint.emit("${transferDetail.controlPoint?.code} / ${transferDetail.collectPoint}")

                transferPickingDto?.pickingSuggestionList?.indexOfFirst { it.product.id == transferDetail.product.id }
                    ?.let { index ->
                        selectedSuggestionIndex = index - 1
                        _pickedAndOrderQty.emit("${transferDetail.quantityPicked} / ${transferDetail.quantityWillBePicked}")

                        showNext()
                    }
            }
        } ?: showError(
            ErrorDialogDto(
                title = stringProvider.invoke(R.string.error),
                message = stringProvider.invoke(R.string.msg_not_in_picking_list)
            )
        )
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

            transferPickingDto?.pickingSuggestionList?.getOrNull(selectedSuggestionIndex)?.let {
                _selectedSuggestion.emit(it)
            } ?: run { selectedSuggestionIndex-- }

            _orderQuantityTxt.emit(
                "${transferPickingDto?.pickingSuggestionList?.getOrNull(selectedSuggestionIndex)?.quantityPicked} / " +
                        "${
                            transferPickingDto?.pickingSuggestionList?.getOrNull(
                                selectedSuggestionIndex
                            )?.quantityWillBePicked
                        }"
            )

            _pageNum.emit("${selectedSuggestionIndex + 1} / ${transferPickingDto?.pickingSuggestionList?.size}")
        }
    }

    fun showPrevious() {
        viewModelScope.launch {
            selectedSuggestionIndex--

            transferPickingDto?.pickingSuggestionList?.getOrNull(selectedSuggestionIndex)?.let {
                _selectedSuggestion.emit(it)
            } ?: run { selectedSuggestionIndex++ }

            _pageNum.emit("${selectedSuggestionIndex + 1} / ${transferPickingDto?.pickingSuggestionList?.size}")

            _orderQuantityTxt.emit(
                "${transferPickingDto?.pickingSuggestionList?.getOrNull(selectedSuggestionIndex)?.quantityPicked} / " +
                        "${
                            transferPickingDto?.pickingSuggestionList?.getOrNull(
                                selectedSuggestionIndex
                            )?.quantityWillBePicked
                        }"
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
}