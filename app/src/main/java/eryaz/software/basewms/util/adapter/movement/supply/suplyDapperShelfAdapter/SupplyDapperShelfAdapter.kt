package eryaz.software.basewms.util.adapter.movement.supply.suplyDapperShelfAdapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.SupplyProductShelfDapperDto

class SupplyDapperShelfAdapter(val context: Context) :
    ListAdapter<SupplyProductShelfDapperDto, RecyclerView.ViewHolder>(WorkActivityDiffCallBackP) {

    var onItemChecked: ((shelfIdList : SupplyProductShelfDapperDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SupplyDapperShelfViewHolder.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        when (holder) {
            is SupplyDapperShelfViewHolder -> holder.bind(
                getItem(position),
                onItemChecked,
                context
            )
        }
    }
}

object WorkActivityDiffCallBackP : DiffUtil.ItemCallback<SupplyProductShelfDapperDto>() {
    override fun areItemsTheSame(
        oldItem: SupplyProductShelfDapperDto,
        newItem: SupplyProductShelfDapperDto
    ) = oldItem.productId == newItem.productId

    override fun areContentsTheSame(
        oldItem: SupplyProductShelfDapperDto,
        newItem: SupplyProductShelfDapperDto
    ) = oldItem == newItem
}