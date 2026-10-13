package eryaz.software.basewms.util.adapter.movement.packageList

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.PackageOrderDetailDto

class PackageProductAdapter :
    ListAdapter<PackageOrderDetailDto, RecyclerView.ViewHolder>(WorkActivityDiffCallBackPr) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PackageProductViewHolder.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PackageProductViewHolder -> holder.bind(getItem(position))
        }
    }
}

object WorkActivityDiffCallBackPr : DiffUtil.ItemCallback<PackageOrderDetailDto>() {
    override fun areItemsTheSame(
        oldItem: PackageOrderDetailDto,
        newItem: PackageOrderDetailDto
    ) = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: PackageOrderDetailDto,
        newItem: PackageOrderDetailDto
    ) = oldItem == newItem
}
