package eryaz.software.basewms.util.adapter.inbound.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.WorkActivityDto
import eryaz.software.basewms.util.adapter.inbound.viewHolder.WorkActivityViewHolder

class WorkActivityAdapter :
    ListAdapter<WorkActivityDto, RecyclerView.ViewHolder>(WorkActivityDiffCallBack) {

    var onItemClick : ((WorkActivityDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        WorkActivityViewHolder.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is WorkActivityViewHolder -> holder.bind(getItem(position), onItemClick)
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
