package eryaz.software.basewms.util.adapter.movement.packageList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.PackageOrderDetailDto
import eryaz.software.basewms.databinding.ItemPackageProductListBinding

class PackageProductViewHolder(val binding: ItemPackageProductListBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        dto: PackageOrderDetailDto,
    ) {
        binding.dto = dto
    }

    companion object {
        fun from(parent: ViewGroup): PackageProductViewHolder {
            val binding = ItemPackageProductListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return PackageProductViewHolder(binding)
        }
    }
}