package eryaz.software.basewms.util.adapter.inbound.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.WaybillListDetailDto
import eryaz.software.basewms.util.adapter.inbound.viewHolder.WaybillDetailDialogViewHolder

class WaybillDetailListDialogAdapter :
    ListAdapter<WaybillListDetailDto, RecyclerView.ViewHolder>(WaybillDetailDiffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        WaybillDetailDialogViewHolder.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is WaybillDetailDialogViewHolder -> holder.bind(getItem(position))
        }
    }
}

object WaybillDetailDiffCallBack : DiffUtil.ItemCallback<WaybillListDetailDto>() {
    override fun areItemsTheSame(
        oldItem: WaybillListDetailDto,
        newItem: WaybillListDetailDto
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: WaybillListDetailDto,
        newItem: WaybillListDetailDto
    ) = oldItem == newItem
}
