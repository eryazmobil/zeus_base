package eryaz.software.basewms.ui.dashboard.counting.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.StockTackingProcessDto

class InfoProductEdtAdapter :
    ListAdapter<StockTackingProcessDto, RecyclerView.ViewHolder>(DiffCallBacksVH) {

    var onEditClick: (StockTackingProcessDto) -> Unit = {}
    var onDeleteClick: (StockTackingProcessDto) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return InfoProductEdtVH.Companion.from(
            parent
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is InfoProductEdtVH -> {
                holder.bind(
                    dto = getItem(position),
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    isLastItem = position == itemCount.minus(1)
                )
            }
        }
    }
}

object DiffCallBacksVH : DiffUtil.ItemCallback<StockTackingProcessDto>() {
    override fun areItemsTheSame(
        oldItem: StockTackingProcessDto,
        newItem: StockTackingProcessDto
    ) = oldItem == newItem

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(
        oldItem: StockTackingProcessDto,
        newItem: StockTackingProcessDto
    ) = oldItem == newItem
}