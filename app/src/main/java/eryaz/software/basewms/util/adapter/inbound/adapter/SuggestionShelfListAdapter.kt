package eryaz.software.basewms.util.adapter.inbound.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.RouteDto
import eryaz.software.basewms.data.models.dto.ShelfDto
import eryaz.software.basewms.util.adapter.inbound.viewHolder.SuggestionShelfViewHolder

class SuggestionShelfListAdapter :
    ListAdapter<ShelfDto, RecyclerView.ViewHolder>(SuggestionShelfListAdapterCallBack) {

    var onItemClick: ((ShelfDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SuggestionShelfViewHolder.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SuggestionShelfViewHolder -> holder.bind(getItem(position), onItemClick)
        }
    }
}

object SuggestionShelfListAdapterCallBack : DiffUtil.ItemCallback<ShelfDto>() {
    override fun areItemsTheSame(
        oldItem: ShelfDto,
        newItem: ShelfDto
    ) = oldItem.shelfId == newItem.shelfId

    override fun areContentsTheSame(
        oldItem: ShelfDto,
        newItem: ShelfDto
    ) = oldItem == newItem
}
