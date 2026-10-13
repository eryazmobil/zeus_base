package eryaz.software.basewms.ui.dashboard.settings.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.remote.response.LanguageModel
import eryaz.software.basewms.databinding.ItemLanguageBinding
import eryaz.software.basewms.util.extensions.dpToPx
import eryaz.software.basewms.util.extensions.getDrawableRes
import eryaz.software.basewms.util.extensions.onChanged

class LanguageVH(val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        model: LanguageModel,
        onItemClick: ((LanguageModel) -> Unit)? = null,
    ) {
        binding.itemTxt.text = model.lang.fullName
        binding.iconImg.setImageResource(model.lang.getDrawableRes())

        model.isSelected.onChanged {
            binding.iconImg.strokeWidth = if (it) 2.dpToPx().toFloat() else 0f
        }

        binding.root.setOnClickListener {
            onItemClick?.invoke(model)
        }
    }

    companion object {
        fun from(parent: ViewGroup) = LanguageVH(
            ItemLanguageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
}
