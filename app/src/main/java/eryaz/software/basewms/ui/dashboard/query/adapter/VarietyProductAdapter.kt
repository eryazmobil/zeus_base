package eryaz.software.basewms.ui.dashboard.query.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.ProductSpecialShelfDto

class VarietyProductAdapter :
    ListAdapter<ProductSpecialShelfDto, RecyclerView.ViewHolder>(DiffCallBacksVH) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return VarietyProductVH.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is VarietyProductVH -> {
                holder.bind(
                    dto = getItem(position),
                    isLastItem = position == itemCount.minus(1)
                )
            }
        }
    }
}

object DiffCallBacksVH : DiffUtil.ItemCallback<ProductSpecialShelfDto>() {
    override fun areItemsTheSame(
        oldItem: ProductSpecialShelfDto,
        newItem: ProductSpecialShelfDto
    ) = oldItem == newItem

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(
        oldItem: ProductSpecialShelfDto,
        newItem: ProductSpecialShelfDto
    ) = oldItem == newItem
}