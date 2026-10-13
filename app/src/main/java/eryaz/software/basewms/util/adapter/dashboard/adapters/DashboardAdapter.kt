package eryaz.software.basewms.util.adapter.dashboard.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.DashboardItemDto
import eryaz.software.basewms.util.adapter.dashboard.viewHolders.DashboardViewHolder

class DashboardAdapter : ListAdapter<DashboardItemDto, RecyclerView.ViewHolder>(DiffCallBack) {

    var onItemClick: ((DashboardItemDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DashboardViewHolder.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DashboardViewHolder -> holder.bind(getItem(position), onItemClick)
        }
    }
}

object DiffCallBack : DiffUtil.ItemCallback<DashboardItemDto>() {
    override fun areItemsTheSame(
        oldItem: DashboardItemDto,
        newItem: DashboardItemDto
    ) = oldItem.iconRes == newItem.iconRes

    override fun areContentsTheSame(
        oldItem: DashboardItemDto,
        newItem: DashboardItemDto
    ) = oldItem == newItem
}
