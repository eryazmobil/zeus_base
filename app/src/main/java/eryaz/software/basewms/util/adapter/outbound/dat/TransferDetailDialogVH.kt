package eryaz.software.basewms.util.adapter.outbound.dat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import eryaz.software.basewms.data.models.dto.TransferRequestDetailDto
import eryaz.software.basewms.databinding.ItemTransferDetailListBinding
import androidx.core.graphics.toColorInt
import eryaz.software.basewms.R
import eryaz.software.basewms.util.enums.DatType
import eryaz.software.basewms.util.extensions.toIntOrZero

class TransferDetailDialogVH(val binding: ItemTransferDetailListBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(dto: TransferRequestDetailDto, datType: DatType?) {
        resetViews()
        binding.dto = dto
        binding.executePendingBindings()
        setControl(dto, datType)
    }

    private fun resetViews() = with(binding) {
        controlStatus.visibility = View.GONE
        statusImage.visibility = View.GONE
        statusImage.setImageDrawable(null)
        statusValueTxt.text = ""
        controlTitle.text = ""
        controlTitle.setBackgroundColor(0x00000000) // Şeffaf renk
    }

    private fun setControl(dto: TransferRequestDetailDto, datType: DatType?) {

        when (datType) {
            DatType.FromControl -> {
                val statusText = dto.quantityShipped + " / " + dto.quantity
                binding.statusValueTxt.text = statusText

                val (statusName, statusColor) = when {
                    dto.quantityShipped.toIntOrZero() == dto.quantity.toIntOrZero() -> "TAM" to "#4caf50"
                    dto.quantityShipped.toIntOrZero() < dto.quantity.toIntOrZero() -> "EKSIK" to "#df0029"
                    else -> "FAZLA" to "#ffb822"
                }

                binding.controlTitle.apply {
                    text = statusName
                    setBackgroundColor(statusColor.toColorInt())
                }
            }

            DatType.FromPlacement -> {
                val statusText = "Yerlş: " + dto.quantityPlaced + " / " + dto.quantity
                val controlledText =
                    "Kont: " + dto.receivingQuantityControlled + " / " + dto.quantity
                binding.statusValueTxt.text = statusText

                binding.statusImage.visibility = View.VISIBLE
                binding.controlStatus.visibility = View.VISIBLE

                binding.controlStatus.text = controlledText
                if (dto.quantity.toIntOrZero() == dto.receivingQuantityControlled.toIntOrZero()) {
                    binding.statusImage.setImageResource(R.drawable.ic_done)
                } else {
                    binding.statusImage.setImageResource(R.drawable.ic_danger)
                }

                val (statusName, statusColor) = when {
                    dto.quantityPlaced.toIntOrZero() == dto.quantity.toIntOrZero() -> "TAM" to "#4caf50"
                    dto.quantityPlaced.toIntOrZero() < dto.quantity.toIntOrZero() -> "EKSIK" to "#df0029"
                    else -> "FAZLA" to "#ffb822"
                }

                binding.controlTitle.apply {
                    text = statusName
                    setBackgroundColor(statusColor.toColorInt())
                }
            }

            DatType.FromAcceptance -> {

                val statusText = dto.receivingQuantityControlled + " / " + dto.quantity
                binding.statusValueTxt.text = statusText

                val (statusName, statusColor) = when {
                    dto.receivingQuantityControlled.toIntOrZero() == dto.quantity.toIntOrZero() -> "TAM" to "#4caf50"
                    dto.receivingQuantityControlled.toIntOrZero() < dto.quantity.toIntOrZero() -> "EKSIK" to "#df0029"
                    else -> "FAZLA" to "#ffb822"
                }

                binding.controlTitle.apply {
                    text = statusName
                    setBackgroundColor(statusColor.toColorInt())
                }
            }

            DatType.FromPicking -> {
                val statusText = dto.quantityPicked + " / " + dto.quantity
                binding.statusValueTxt.text = statusText

                val (statusName, statusColor) = when {
                    dto.quantityPicked.toIntOrZero() == dto.quantity.toIntOrZero() -> "TAM" to "#4caf50"
                    dto.quantityPicked.toIntOrZero() < dto.quantity.toIntOrZero() -> "EKSIK" to "#df0029"
                    else -> "FAZLA" to "#ffb822"
                }

                binding.controlTitle.apply {
                    text = statusName
                    setBackgroundColor(statusColor.toColorInt())
                }
            }

            null -> {}
        }

    }

    companion object {
        fun from(parent: ViewGroup): TransferDetailDialogVH {
            val binding = ItemTransferDetailListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return TransferDetailDialogVH(binding)
        }
    }
}