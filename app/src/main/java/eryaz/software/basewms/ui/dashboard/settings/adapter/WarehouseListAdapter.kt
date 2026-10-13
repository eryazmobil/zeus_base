package eryaz.software.basewms.ui.dashboard.settings.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.WarehouseDto

class WarehouseListAdapter : ListAdapter<WarehouseDto, RecyclerView.ViewHolder>(
    DiffCallBackWarehouse
) {

    var onItemClick: ((WarehouseDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return WarehouseViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is WarehouseViewHolder -> holder.bind(getItem(position), onItemClick)
        }
    }
}

object DiffCallBackWarehouse : DiffUtil.ItemCallback<WarehouseDto>() {
    override fun areItemsTheSame(
        oldItem: WarehouseDto,
        newItem: WarehouseDto
    ) = oldItem.code == newItem.code

    override fun areContentsTheSame(
        oldItem: WarehouseDto,
        newItem: WarehouseDto
    ) = oldItem == newItem
}