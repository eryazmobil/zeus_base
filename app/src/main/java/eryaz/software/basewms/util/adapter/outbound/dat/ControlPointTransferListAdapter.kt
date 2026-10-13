package eryaz.software.basewms.util.adapter.outbound.dat

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.WorkActivityDto

class ControlPointTransferListAdapter:
    ListAdapter<WorkActivityDto, RecyclerView.ViewHolder>(DiffCallBack) {

    var onItemClick: ((WorkActivityDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ControlPointTransferListVH.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ControlPointTransferListVH -> holder.bind(getItem(position), onItemClick)
        }
    }
}

object DiffCallBack : DiffUtil.ItemCallback<WorkActivityDto>() {
    override fun areItemsTheSame(
        oldItem: WorkActivityDto,
        newItem: WorkActivityDto
    ) = oldItem.workActivityCode == newItem.workActivityCode

    override fun areContentsTheSame(
        oldItem: WorkActivityDto,
        newItem: WorkActivityDto
    ) = oldItem == newItem
}
