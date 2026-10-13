package eryaz.software.basewms.ui.dashboard.query.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.ProductShelfQuantityDto

class ShelfQuantityAdapter :
    ListAdapter<ProductShelfQuantityDto, RecyclerView.ViewHolder>(DiffCallBacksShelfNew) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ShelfQuantityVH.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ShelfQuantityVH -> {
                holder.bind(
                    dto = getItem(position),
                    isLastItem = position == itemCount.minus(1)
                )
            }
        }
    }
}

object DiffCallBacksShelfNew : DiffUtil.ItemCallback<ProductShelfQuantityDto>() {
    override fun areItemsTheSame(
        oldItem: ProductShelfQuantityDto,
        newItem: ProductShelfQuantityDto
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: ProductShelfQuantityDto,
        newItem: ProductShelfQuantityDto
    ) = oldItem == newItem
}