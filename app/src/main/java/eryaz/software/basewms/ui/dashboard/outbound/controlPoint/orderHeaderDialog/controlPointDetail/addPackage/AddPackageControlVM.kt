package eryaz.software.basewms.ui.dashboard.outbound.controlPoint.orderHeaderDialog.controlPointDetail.addPackage

import eryaz.software.basewms.R
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.ErrorDialogDto
import eryaz.software.basewms.data.models.dto.PackageDto
import eryaz.software.basewms.data.repositories.OrderRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AddPackageControlVM(
    val repo: OrderRepo,
    val packageList: List<PackageDto>,
    val orderHeaderId: Int
) : BaseViewModel() {

    var packageNo = MutableStateFlow(
        "${stringProvider.invoke(R.string.packageShort)}000${getPackNum()}"
    )
    var packageWidth = MutableStateFlow("")
    var packageHeight = MutableStateFlow("")
    var packageLength = MutableStateFlow("")
    var packageDeci = MutableStateFlow("")
    var packageWeight = MutableStateFlow("")

    private val _savedPackage = MutableStateFlow(false)
    val savePackage = _savedPackage.asStateFlow()

    fun createPackage() {
        if (isValidFields()) {
            executeInBackground(showProgressDialog = true) {
                repo.createPackage(
                    orderHeaderId = orderHeaderId,
                    sizeHeight = packageHeight.value.toDouble(),
                    sizeLength = packageLength.value.toDouble(),
                    sizeWidth = packageWidth.value.toDouble(),
                    deci = packageDeci.value.toDouble(),
                    tareWeight = packageWeight.value.toDouble(),
                    packageNo = packageNo.value.substring(3).toInt()
                ).onSuccess {
                    _savedPackage.emit(it)
                }
            }
        }
    }

    private fun isNumeric(str: String): Boolean {
        return str.matches("-?\\d+(\\.\\d+)?".toRegex())
    }
    fun calculateDeci() {
        val widthTxt = packageWidth.value.trim()
        val heightTxt = packageHeight.value.trim()
        val lengthTxt = packageLength.value.trim()

        if (isNumeric(widthTxt) && isNumeric(heightTxt) && isNumeric(lengthTxt)) {
            val deci = widthTxt.toDouble() * heightTxt.toDouble() * lengthTxt.toDouble() / 3000
            packageDeci.value = deci.toString()
        }
    }

    private fun isValidFields(): Boolean {
        when {
            packageWidth.value.isEmpty() -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.width_error
                    )
                )
                return false
            }

            packageHeight.value.isEmpty() -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.height_error
                    )
                )
                return false
            }

            packageLength.value.isEmpty() -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.length_error
                    )
                )
                return false
            }

            packageWeight.value.isEmpty() -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.weight_error
                    )
                )
                return false
            }

            packageDeci.value.isEmpty() -> {
                showError(
                    ErrorDialogDto(
                        titleRes = R.string.error,
                        messageRes = R.string.deci_error
                    )
                )
                return false
            }

            else ->
                return true
        }
    }

    private fun getPackNum(): String =
        if (packageList.isEmpty()) {
            "1"
        } else {
            "${packageList.size + 1}"
        }

}