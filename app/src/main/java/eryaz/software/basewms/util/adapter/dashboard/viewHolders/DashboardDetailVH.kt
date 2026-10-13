package eryaz.software.basewms.util.adapter.dashboard.viewHolders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.DashboardDetailItemDto
import eryaz.software.basewms.databinding.ItemDashboardDetailListBinding
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.onChanged

class DashboardDetailVH (val binding: ItemDashboardDetailListBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(dto: DashboardDetailItemDto, onItemClick :(
        (DashboardDetailItemDto)->Unit)) {
        binding.dto = dto
        dto.hasPermission.onChanged {
            binding.root.alpha = if (it) 1f else 0.5f
            binding.root.isEnabled = it
        }
        binding.root.setOnSingleClickListener {
            onItemClick(dto)
        }
    }
    companion object {
        fun from(parent: ViewGroup): DashboardDetailVH {
            val binding = ItemDashboardDetailListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return DashboardDetailVH(binding)
        }
    }

}