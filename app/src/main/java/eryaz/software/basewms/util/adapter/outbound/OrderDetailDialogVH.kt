package eryaz.software.basewms.util.adapter.outbound

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.OrderDetailDto
import eryaz.software.basewms.databinding.ItemOrderDetailListBinding
import androidx.core.graphics.toColorInt

class OrderDetailDialogVH(val binding: ItemOrderDetailListBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(dto: OrderDetailDto) {
        binding.dto = dto
        binding.executePendingBindings()
        setControl(dto)
    }

    private fun setControl(dto: OrderDetailDto) {
        val (statusName, statusColor) = when {
            dto.quantityCollected == dto.quantity -> "TAM" to "#4caf50"
            dto.quantityCollected < dto.quantity -> "EKSIK" to "#df0029"
            else -> "FAZLA" to "#ffb822"
        }

        binding.statusValueTxt.apply {
            val statusText = dto.quantityCollected + " / " + dto.quantity
            text = statusText
        }
        binding.controlTitle.apply {
            text = statusName
            setBackgroundColor(statusColor.toColorInt())
        }
    }

    companion object {
        fun from(parent: ViewGroup): OrderDetailDialogVH {
            val binding = ItemOrderDetailListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return OrderDetailDialogVH(binding)
        }
    }
}