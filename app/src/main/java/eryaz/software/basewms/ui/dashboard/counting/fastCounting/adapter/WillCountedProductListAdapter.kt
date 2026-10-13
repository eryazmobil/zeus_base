package eryaz.software.basewms.ui.dashboard.counting.fastCounting.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.CountingComparisonDto

class WillCountedProductListAdapter :
    ListAdapter<CountingComparisonDto, RecyclerView.ViewHolder>(WorkActivityDiffCallBack) {

    var onItemClick : ((CountingComparisonDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        WillCountedProductListVH.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is WillCountedProductListVH -> holder.bind(getItem(position), onItemClick)
        }
    }
}

object WorkActivityDiffCallBack : DiffUtil.ItemCallback<CountingComparisonDto>() {
    override fun areItemsTheSame(
        oldItem: CountingComparisonDto,
        newItem: CountingComparisonDto
    ) = oldItem.newQuantity == newItem.newQuantity

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(
        oldItem: CountingComparisonDto,
        newItem: CountingComparisonDto
    ) = oldItem == newItem
}
