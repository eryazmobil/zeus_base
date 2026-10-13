package eryaz.software.basewms.util.adapter.outbound.dat

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.TransferRequestDetailDto

class TransferControlPointDetailListAdapter :
    ListAdapter<TransferRequestDetailDto, RecyclerView.ViewHolder>(DiffCallBackDetail) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TransferControlPointDetailListVH.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TransferControlPointDetailListVH -> holder.bind(getItem(position))
        }
    }
}

object DiffCallBackDetail : DiffUtil.ItemCallback<TransferRequestDetailDto>() {
    override fun areItemsTheSame(
        oldItem: TransferRequestDetailDto,
        newItem: TransferRequestDetailDto
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: TransferRequestDetailDto,
        newItem: TransferRequestDetailDto
    ) = oldItem == newItem
}
