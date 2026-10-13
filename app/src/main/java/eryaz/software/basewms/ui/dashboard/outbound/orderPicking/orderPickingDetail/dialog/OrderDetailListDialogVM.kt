package eryaz.software.basewms.ui.dashboard.outbound.orderPicking.orderPickingDetail.dialog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import eryaz.software.basewms.data.models.dto.OrderDetailDto
import eryaz.software.basewms.ui.base.BaseViewModel

class OrderDetailListDialogVM(
    val orderDetailList: List<OrderDetailDto>
) : BaseViewModel() {

    val search = MutableLiveData("")

    fun searchList() = search.switchMap { query ->
        MutableLiveData<List<OrderDetailDto>>().apply {
            value = orderDetailList.filter { data ->
                data.product.code.contains(query, ignoreCase = true)
            }
        }
    }
}