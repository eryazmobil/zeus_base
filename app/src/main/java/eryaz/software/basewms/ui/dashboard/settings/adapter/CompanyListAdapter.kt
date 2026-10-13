package eryaz.software.basewms.ui.dashboard.settings.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.CompanyDto

class CompanyListAdapter : ListAdapter<CompanyDto, RecyclerView.ViewHolder>(DiffCallBacks) {

    var onItemClick: ((CompanyDto) -> Unit) = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CompanyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CompanyViewHolder -> {
                holder.bind(
                    dto = getItem(position) as CompanyDto,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

object DiffCallBacks : DiffUtil.ItemCallback<CompanyDto>() {
    override fun areItemsTheSame(
        oldItem: CompanyDto,
        newItem: CompanyDto
    ) = oldItem.code == newItem.code

    override fun areContentsTheSame(
        oldItem: CompanyDto,
        newItem: CompanyDto
    ) = oldItem == newItem
}