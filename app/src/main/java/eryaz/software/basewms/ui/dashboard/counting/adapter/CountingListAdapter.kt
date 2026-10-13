package eryaz.software.basewms.ui.dashboard.counting.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.StockTakingHeaderDto

class CountingListAdapter :
    ListAdapter<StockTakingHeaderDto, RecyclerView.ViewHolder>(WorkActivityDiffCallBack) {

    var onItemClick : ((StockTakingHeaderDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CountingListVH.Companion.from(
            parent
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CountingListVH -> holder.bind(getItem(position), onItemClick)
        }
    }
}

object WorkActivityDiffCallBack : DiffUtil.ItemCallback<StockTakingHeaderDto>() {
    override fun areItemsTheSame(
        oldItem: StockTakingHeaderDto,
        newItem: StockTakingHeaderDto
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: StockTakingHeaderDto,
        newItem: StockTakingHeaderDto
    ) = oldItem == newItem
}
