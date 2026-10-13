package eryaz.software.basewms.util.adapter.movement.route

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.RouteDto
import eryaz.software.basewms.util.adapter.movement.packageList.WorkActivityDiffCallBack

class RouteAdapter :
    ListAdapter<RouteDto, RecyclerView.ViewHolder>(WorkActivityDiffCallBackP) {

    var onItemClick : ((RouteDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RouteViewHolder.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RouteViewHolder -> holder.bind(getItem(position), onItemClick)
        }
    }
}

object WorkActivityDiffCallBackP : DiffUtil.ItemCallback<RouteDto>() {
    override fun areItemsTheSame(
        oldItem: RouteDto,
        newItem: RouteDto
    ) = oldItem.code == newItem.code

    override fun areContentsTheSame(
        oldItem: RouteDto,
        newItem: RouteDto
    ) = oldItem == newItem
}
