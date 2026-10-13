package eryaz.software.basewms.util.adapter.inbound.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.CrossDockDto
import eryaz.software.basewms.util.adapter.inbound.viewHolder.CrossDockViewHolder

class CrossDockListAdapter :
    ListAdapter<CrossDockDto, RecyclerView.ViewHolder>(CrossDockDiffCallBack) {

    var onItemClick: ((CrossDockDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CrossDockViewHolder.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CrossDockViewHolder -> holder.bind(getItem(position), onItemClick)
        }
    }
}

object CrossDockDiffCallBack : DiffUtil.ItemCallback<CrossDockDto>() {
    override fun areItemsTheSame(
        oldItem: CrossDockDto,
        newItem: CrossDockDto
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: CrossDockDto,
        newItem: CrossDockDto
    ) = oldItem == newItem
}
