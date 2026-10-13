package eryaz.software.basewms.ui.dashboard.query.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.ProductShelfSupplyDto


class SupplyShelfProductQuantityAdapter:
    ListAdapter<ProductShelfSupplyDto, RecyclerView.ViewHolder>(DiffCallBacks) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SupplyShelfProductQuantityViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SupplyShelfProductQuantityViewHolder -> {
                holder.bind(
                    dto = getItem(position),
                    isLastItem = position == itemCount.minus(1)
                )
            }
        }
    }
}

object DiffCallBacks : DiffUtil.ItemCallback<ProductShelfSupplyDto>() {
    override fun areItemsTheSame(
        oldItem: ProductShelfSupplyDto,
        newItem: ProductShelfSupplyDto
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: ProductShelfSupplyDto,
        newItem: ProductShelfSupplyDto
    ) = oldItem == newItem
}