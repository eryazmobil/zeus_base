package eryaz.software.basewms.util.adapter.outbound

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.ControlPointScreenDto

class ControlPointListAdapter :
    ListAdapter<ControlPointScreenDto, RecyclerView.ViewHolder>(DiffCallBack) {

    var onItemClick: ((ControlPointScreenDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ControlPointListVH.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ControlPointListVH -> holder.bind(getItem(position), onItemClick)
        }
    }
}

object DiffCallBack : DiffUtil.ItemCallback<ControlPointScreenDto>() {
    override fun areItemsTheSame(
        oldItem: ControlPointScreenDto,
        newItem: ControlPointScreenDto
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: ControlPointScreenDto,
        newItem: ControlPointScreenDto
    ) = oldItem == newItem
}
