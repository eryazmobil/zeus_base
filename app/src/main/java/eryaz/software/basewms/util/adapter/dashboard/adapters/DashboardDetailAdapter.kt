package eryaz.software.basewms.util.adapter.dashboard.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.DashboardDetailItemDto
import eryaz.software.basewms.util.adapter.dashboard.viewHolders.DashboardDetailVH

class DashboardDetailAdapter : ListAdapter<DashboardDetailItemDto, RecyclerView.ViewHolder>(
    DiffCallBackDash
) {
    var onItemClick: ((DashboardDetailItemDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DashboardDetailVH.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DashboardDetailVH -> holder.bind(getItem(position), onItemClick)
        }
    }
}

object DiffCallBackDash : DiffUtil.ItemCallback<DashboardDetailItemDto>() {
    override fun areItemsTheSame(
        oldItem: DashboardDetailItemDto,
        newItem: DashboardDetailItemDto
    ) = oldItem.iconRes == newItem.iconRes

    override fun areContentsTheSame(
        oldItem: DashboardDetailItemDto,
        newItem: DashboardDetailItemDto
    ) = oldItem == newItem
}