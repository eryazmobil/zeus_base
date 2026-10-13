package eryaz.software.basewms.util.adapter.movement.supply.replenishmentTypeAdapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.enums.ReplenishStatusEnum

class ReplenishmentTypeAdapter :
    ListAdapter<ReplenishStatusEnum, RecyclerView.ViewHolder>(WorkActivityDiffCallBackP) {

    var onItemSelected: ((ReplenishStatusEnum) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ReplenishmentTypeViewHolder.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ReplenishmentTypeViewHolder -> holder.bind(
                getItem(position),
                onItemSelected
            )
        }
    }
}

object WorkActivityDiffCallBackP : DiffUtil.ItemCallback<ReplenishStatusEnum>() {
    override fun areItemsTheSame(
        oldItem: ReplenishStatusEnum,
        newItem: ReplenishStatusEnum
    ) = oldItem.name == newItem.name

    override fun areContentsTheSame(
        oldItem: ReplenishStatusEnum,
        newItem: ReplenishStatusEnum
    ) = oldItem == newItem
}