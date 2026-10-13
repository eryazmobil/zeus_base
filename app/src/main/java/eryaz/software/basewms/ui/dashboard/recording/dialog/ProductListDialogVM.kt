package eryaz.software.basewms.ui.dashboard.recording.dialog

import androidx.lifecycle.viewModelScope
import eryaz.software.basewms.data.api.utils.onSuccess
import eryaz.software.basewms.data.models.dto.ProductDto
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.data.repositories.BarcodeRepo
import eryaz.software.basewms.ui.base.BaseViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ProductListDialogVM(
    private val repo: BarcodeRepo,
) : BaseViewModel() {

    private val _productList = MutableStateFlow(listOf<ProductDto>())
    val productList = _productList.asStateFlow()

    val search = MutableStateFlow("")

    init {
        searchList()
    }

    private fun getAllProductList(
        query: String
    ) = executeInBackground(
        _uiState,
        showErrorDialog = false
    ) {
        repo.getProductList(
            companyId = SessionManager.companyId,
            searchTxt = query,
            limit = 30
        ).onSuccess {
            _productList.emit(it)
        }
    }

    @OptIn(FlowPreview::class)
    private fun searchList() {
        viewModelScope.launch {
            search.debounce(500)
                .distinctUntilChanged()
                .collect { searchText ->
                    if (searchText.isEmpty()) {
                        _productList.emit(emptyList())
                    } else {
                        getAllProductList(searchText)
                    }
                }
        }
    }
}