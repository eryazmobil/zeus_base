package eryaz.software.basewms.util.adapter.outbound

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.OrderDetailDto

class OrderDetailListDialogAdapter :
    ListAdapter<OrderDetailDto, RecyclerView.ViewHolder>(WaybillDetailDiffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        OrderDetailDialogVH.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is OrderDetailDialogVH -> holder.bind(getItem(position))
        }
    }
}

object WaybillDetailDiffCallBack : DiffUtil.ItemCallback<OrderDetailDto>() {
    override fun areItemsTheSame(
        oldItem: OrderDetailDto,
        newItem: OrderDetailDto
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: OrderDetailDto,
        newItem: OrderDetailDto
    ) = oldItem == newItem
}
