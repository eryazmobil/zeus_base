package eryaz.software.basewms.ui.dashboard.settings.changePassword

import androidx.lifecycle.LiveData
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.remote.request.ChangePasswordRequest
import eryaz.software.basewms.data.repositories.UserRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import eryaz.software.basewms.util.CombinedStateFlow
import eryaz.software.basewms.util.SingleLiveEvent
import eryaz.software.basewms.util.extensions.isValidPassword
import kotlinx.coroutines.flow.MutableStateFlow

class ChangePasswordVM (private val repo: UserRepo) : BaseViewModel() {

    val oldPassword = MutableStateFlow("")
    val newPassword = MutableStateFlow("")
    val againPassword = MutableStateFlow("")

    private val _wasChangePassword = SingleLiveEvent<Boolean>()
    val wasChangedPassword: LiveData<Boolean> = _wasChangePassword

    val loginEnable = CombinedStateFlow(oldPassword, newPassword, againPassword) {
        oldPassword.value.isValidPassword() && newPassword.value.isValidPassword()
                && againPassword.value.isValidPassword()
    }

    val passwordMatching = CombinedStateFlow(newPassword, againPassword) {
        newPassword.value == againPassword.value
    }

    fun changePassword() = executeInBackground() {
        val request = ChangePasswordRequest(
            oldPassword = oldPassword.value,
            newPassword = newPassword.value
        )
        repo.changePassword(request).onSuccess {
            _wasChangePassword.value = true
        }
    }


}
