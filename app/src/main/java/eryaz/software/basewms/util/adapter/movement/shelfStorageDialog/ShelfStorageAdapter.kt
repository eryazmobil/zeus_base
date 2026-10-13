package eryaz.software.basewms.util.adapter.movement.shelfStorageDialog

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.remote.models.ShelfStorageModel

class ShelfStorageAdapter :
    ListAdapter<ShelfStorageModel, RecyclerView.ViewHolder>(WorkActivityDiffCallBackP) {

        var onCopyButtonClickListener: (String?) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ShelfStorageVH.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ShelfStorageVH -> holder.bind(getItem(position),onCopyButtonClickListener)
        }
    }
}

object WorkActivityDiffCallBackP : DiffUtil.ItemCallback<ShelfStorageModel>() {
    override fun areItemsTheSame(
        oldItem: ShelfStorageModel,
        newItem: ShelfStorageModel
    ) = oldItem.shelfDto == newItem.shelfDto

    override fun areContentsTheSame(
        oldItem: ShelfStorageModel,
        newItem: ShelfStorageModel
    ) = oldItem == newItem
}