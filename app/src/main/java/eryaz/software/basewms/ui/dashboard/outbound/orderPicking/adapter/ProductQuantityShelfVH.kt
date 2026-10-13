package eryaz.software.basewms.ui.dashboard.outbound.orderPicking.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.ProductStorageQuantityDto
import eryaz.software.basewms.databinding.ItemStorageQuantityTextBinding

class ProductQuantityShelfVH(val binding: ItemStorageQuantityTextBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        dto: ProductStorageQuantityDto,
        isLastItem: Boolean
    ) {
        binding.keyTxt.text = dto.storageText
        binding.valueTxt.text = dto.quantity

        binding.underline.isVisible = !isLastItem
    }

    companion object {
        fun from(parent: ViewGroup): ProductQuantityShelfVH {
            val binding = ItemStorageQuantityTextBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ProductQuantityShelfVH(binding)
        }
    }
}