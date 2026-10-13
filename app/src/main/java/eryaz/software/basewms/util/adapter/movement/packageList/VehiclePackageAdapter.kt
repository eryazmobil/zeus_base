package eryaz.software.basewms.util.adapter.movement.packageList

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.VehiclePackageDto

class VehiclePackageAdapter :
    ListAdapter<VehiclePackageDto, RecyclerView.ViewHolder>(WorkActivityDiffCallBack) {

    var onItemClick : ((VehiclePackageDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VehiclePackageViewHolder.from(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is VehiclePackageViewHolder -> holder.bind(getItem(position),onItemClick)
        }
    }
}

object WorkActivityDiffCallBack : DiffUtil.ItemCallback<VehiclePackageDto>() {
    override fun areItemsTheSame(
        oldItem: VehiclePackageDto,
        newItem: VehiclePackageDto
    ) = oldItem.code == newItem.code

    override fun areContentsTheSame(
        oldItem: VehiclePackageDto,
        newItem: VehiclePackageDto
    ) = oldItem == newItem
}
