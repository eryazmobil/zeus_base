package eryaz.software.basewms.util.adapter.movement.supply.suplyDapperShelfAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.R
import eryaz.software.basewms.data.models.dto.SupplyProductShelfDapperDto
import eryaz.software.basewms.databinding.SupplyShelfDapperItemBinding

class SupplyDapperShelfViewHolder(val binding: SupplyShelfDapperItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        dto: SupplyProductShelfDapperDto,
        onItemChecked: ((dto : SupplyProductShelfDapperDto) -> Unit),
        context : Context

    ) {
        binding.dto = dto
        val minQty = context.getString(R.string.max_qty_to_collect) + " " + dto.minSupplyQuantity
        binding.minQuantity.text = minQty
        binding.checkboxProduct.setChecked(dto.isChecked)
        binding.checkboxProduct.setOnCheckedChangeListener { _, isChecked ->
            dto.isChecked = isChecked
            onItemChecked(dto)
        }
    }

    companion object {
        fun from(parent: ViewGroup): SupplyDapperShelfViewHolder {
            val binding = SupplyShelfDapperItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return SupplyDapperShelfViewHolder(binding)
        }
    }
}