package eryaz.software.basewms.util.adapter.inbound.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.StorageDto
import eryaz.software.basewms.util.adapter.inbound.viewHolder.StorageListViewHolder

class StorageListAdapter :
    ListAdapter<StorageDto, RecyclerView.ViewHolder>(StorageDiffCallBack) {

    var onItemClick: ((StorageDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        StorageListViewHolder.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is StorageListViewHolder -> holder.bind(getItem(position), onItemClick)
        }
    }
}

object StorageDiffCallBack : DiffUtil.ItemCallback<StorageDto>() {
    override fun areItemsTheSame(
        oldItem: StorageDto,
        newItem: StorageDto
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: StorageDto,
        newItem: StorageDto
    ) = oldItem == newItem
}
