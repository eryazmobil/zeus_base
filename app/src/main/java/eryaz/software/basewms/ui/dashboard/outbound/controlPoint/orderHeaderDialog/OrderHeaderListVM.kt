package eryaz.software.basewms.ui.dashboard.outbound.controlPoint.orderHeaderDialog

import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.data.models.dto.OrderHeaderDto
import eryaz.software.basewms.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderHeaderListVM(
     val orderHeaderList :List<OrderHeaderDto>
) : BaseViewModel() {
    private var selectedSuggestionIndex: Int = -1

    private val _orderHeaderDto = MutableStateFlow<OrderHeaderDto?>(null)
    var orderHeaderDto = _orderHeaderDto.asStateFlow()

    private val _pageNum = MutableStateFlow("")
    val pageNum = _pageNum.asStateFlow()

    init {
        showNext()
    }

    fun showNext() {

        viewModelScope.launch {
            selectedSuggestionIndex++

            orderHeaderList.getOrNull(selectedSuggestionIndex)?.let {
                _orderHeaderDto.emit(it)

            } ?: run { selectedSuggestionIndex-- }
            _pageNum.emit("${selectedSuggestionIndex + 1} / ${orderHeaderList.size}")
        }
    }

    fun showPrevious() {
        viewModelScope.launch {
            selectedSuggestionIndex--

            orderHeaderList.getOrNull(selectedSuggestionIndex)?.let {
                _orderHeaderDto.emit(it)
            } ?: run { selectedSuggestionIndex++ }

            _pageNum.emit("${selectedSuggestionIndex + 1} / ${orderHeaderList.size}")
        }
    }

}