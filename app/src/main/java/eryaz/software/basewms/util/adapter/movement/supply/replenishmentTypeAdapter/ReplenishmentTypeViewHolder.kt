package eryaz.software.basewms.util.adapter.movement.supply.replenishmentTypeAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.enums.ReplenishStatusEnum
import eryaz.software.basewms.databinding.ReplenishmentTypeItemBinding
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener

class ReplenishmentTypeViewHolder(val binding: ReplenishmentTypeItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        replenishmentType: ReplenishStatusEnum,
        onItemClick: ((ReplenishStatusEnum) -> Unit)

    ) {
        binding.replenishmentType = replenishmentType

        binding.root.setOnSingleClickListener {
            onItemClick(replenishmentType)
        }
    }

    companion object {
        fun from(parent: ViewGroup): ReplenishmentTypeViewHolder {
            val binding = ReplenishmentTypeItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ReplenishmentTypeViewHolder(binding)
        }
    }
}