package eryaz.software.basewms.util.adapter.inbound.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.util.adapter.inbound.viewHolder.ProductListViewHolder

class ProductListAdapter :
    ListAdapter<ProductDto, RecyclerView.ViewHolder>(ProductListDiffCallBack) {

    var onItemClick: ((ProductDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ProductListViewHolder.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductListViewHolder -> holder.bind(getItem(position), onItemClick)
        }
    }
}

object ProductListDiffCallBack : DiffUtil.ItemCallback<ProductDto>() {
    override fun areItemsTheSame(
        oldItem: ProductDto,
        newItem: ProductDto
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: ProductDto,
        newItem: ProductDto
    ) = oldItem == newItem
}
