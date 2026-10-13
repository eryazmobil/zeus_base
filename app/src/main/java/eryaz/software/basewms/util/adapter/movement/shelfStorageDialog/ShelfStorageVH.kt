package eryaz.software.basewms.util.adapter.movement.shelfStorageDialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.remote.models.ShelfStorageModel
import eryaz.software.basewms.databinding.ItemRowShelfStorageQtyBinding

class ShelfStorageVH(val binding: ItemRowShelfStorageQtyBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        dto: ShelfStorageModel,
        onCopyButtonClickListener: (String?) -> Unit
    ) {
        binding.dto = dto
       binding.copyBtn.setOnClickListener {
           onCopyButtonClickListener(dto.shelfDto?.shelfAddress)
       }
    }

    companion object {
        fun from(parent: ViewGroup): ShelfStorageVH {
            val binding = ItemRowShelfStorageQtyBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ShelfStorageVH(binding)
        }
    }
}