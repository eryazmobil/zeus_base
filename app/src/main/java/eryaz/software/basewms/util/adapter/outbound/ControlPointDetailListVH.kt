package eryaz.software.basewms.util.adapter.outbound

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.OrderDetailDto
import eryaz.software.basewms.databinding.ItemControlPointDetailBinding

class ControlPointDetailListVH(val binding: ItemControlPointDetailBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        dto: OrderDetailDto
    ) {
        binding.dto = dto
        binding.executePendingBindings()

        setStatus(dto)
    }

    fun animateBackground() {
        binding.backgroundView.animate().alpha(1f).withEndAction {
            binding.backgroundView.animate().alpha(0f)
        }
    }

    private fun setStatus(dto: OrderDetailDto) {
        if (dto.quantityCollected.toInt() == dto.quantityShipped.toInt() && dto.quantityShipped.toInt() != 0) {
            binding.itemParent.setBackgroundColor(Color.parseColor("#6CC654"))
        }
    }

    companion object {
        fun from(parent: ViewGroup): ControlPointDetailListVH {
            val binding = ItemControlPointDetailBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ControlPointDetailListVH(binding)
        }
    }
}