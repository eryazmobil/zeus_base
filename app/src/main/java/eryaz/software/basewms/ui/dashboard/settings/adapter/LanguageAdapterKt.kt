package eryaz.software.basewms.ui.dashboard.settings.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import eryaz.software.basewms.data.models.remote.response.LanguageModel

class LanguageAdapterKt : ListAdapter<LanguageModel, LanguageVH>(DiffCallBack) {

    var onItemClick: ((LanguageModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LanguageVH.from(parent)

    override fun onBindViewHolder(holder: LanguageVH, position: Int) =
        holder.bind(getItem(position), onItemClick)

    private object DiffCallBack : DiffUtil.ItemCallback<LanguageModel>() {
        override fun areItemsTheSame(
            oldItem: LanguageModel,
            newItem: LanguageModel
        ) = oldItem.lang == newItem.lang

        override fun areContentsTheSame(
            oldItem: LanguageModel,
            newItem: LanguageModel
        ) = oldItem == newItem
    }
}