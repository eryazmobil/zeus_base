package eryaz.software.basewms.ui.dashboard.outbound.orderPicking.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.WorkActivityDto

class OrderWorkActivityListAdapter :
    ListAdapter<WorkActivityDto, RecyclerView.ViewHolder>(WorkActivityDiffCallBack) {

    var onItemClick : ((WorkActivityDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        OrderWorkActivityListVH.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is OrderWorkActivityListVH -> holder.bind(getItem(position), onItemClick)
        }
    }
}

object WorkActivityDiffCallBack : DiffUtil.ItemCallback<WorkActivityDto>() {
    override fun areItemsTheSame(
        oldItem: WorkActivityDto,
        newItem: WorkActivityDto
    ) = oldItem.workActivityCode == newItem.workActivityCode

    override fun areContentsTheSame(
        oldItem: WorkActivityDto,
        newItem: WorkActivityDto
    ) = oldItem == newItem
}
