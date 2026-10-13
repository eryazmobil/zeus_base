package eryaz.software.basewms.util.adapter.outbound.dat

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.TransferRequestDetailDto
import eryaz.software.basewms.util.enums.DatType

class TransferDetailListDialogAdapter :
    ListAdapter<TransferRequestDetailDto, RecyclerView.ViewHolder>(WaybillDetailDiffCallBack) {

    var datType: DatType? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TransferDetailDialogVH.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TransferDetailDialogVH -> holder.bind(getItem(position), datType)
        }
    }
}

object WaybillDetailDiffCallBack : DiffUtil.ItemCallback<TransferRequestDetailDto>() {
    override fun areItemsTheSame(
        oldItem: TransferRequestDetailDto,
        newItem: TransferRequestDetailDto
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: TransferRequestDetailDto,
        newItem: TransferRequestDetailDto
    ) = oldItem == newItem
}
