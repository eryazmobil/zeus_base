package eryaz.software.basewms.ui.dashboard.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import eryaz.software.basewms.data.models.dto.CompanyDto
import eryaz.software.basewms.data.models.dto.WarehouseDto
import eryaz.software.basewms.databinding.FragmentSettingsBinding
import eryaz.software.basewms.ui.base.BaseFragment
import eryaz.software.basewms.ui.dashboard.settings.changeLanguage.LanguageFragment
import eryaz.software.basewms.ui.dashboard.settings.companies.CompanyListDialog
import eryaz.software.basewms.ui.dashboard.settings.warehouses.WarehouseListDialog
import eryaz.software.basewms.util.bindingAdapter.setOnSingleClickListener
import eryaz.software.basewms.util.extensions.observe
import eryaz.software.basewms.util.extensions.parcelable
import eryaz.software.zeus.zeusViewModel

class SettingsFragment : BaseFragment() {
    override val viewModel by zeusViewModel<SettingsViewModel>()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        FragmentSettingsBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
        return binding.root
    }

    override fun setClicks() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        viewModel.companyList.observe(this) { companyList ->
            binding.companyItem.setOnSingleClickListener {
                viewModel.checkCompany.observe(this) {
                    if (!it) {
                        findNavController().navigate(
                            SettingsFragmentDirections.actionSettingsFragmentToCompanyListDialog(
                                companyList.toTypedArray()
                            )
                        )
                    }
                }
            }
        }

        binding.appVersionUpdate.setOnSingleClickListener {

            if (!unknownAppInstallPermission()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startActivity(intent)
            } else {
                viewModel.startVersionService(requireContext())
            }
        }

        binding.appLanguage.setOnSingleClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToLanguageFragment())
        }

        viewModel.warehouseList.observe(this) { warehouseList ->
            binding.warehouseItem.setOnSingleClickListener {
                viewModel.checkWarehouse.observe(this) {
                    if (!it) {
                        findNavController().navigate(
                            SettingsFragmentDirections.actionSettingsFragmentToWarehouseListDialog(
                                warehouseList.toTypedArray()
                            )
                        )
                    }
                }
            }
        }

        binding.changePassword.setOnSingleClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingFragmentToPasswordDialog()
            )
        }
    }

    override fun subscribeToObservables() {

        setFragmentResultListener(CompanyListDialog.REQUEST_KEY) { _, bundle ->
            val dto = bundle.parcelable<CompanyDto>(CompanyListDialog.ARG_COMPANY_DTO)
            dto?.let {
                viewModel.setCompany(it)
            }
        }

        setFragmentResultListener(WarehouseListDialog.REQUEST_KEY) { _, bundle ->
            val dto = bundle.parcelable<WarehouseDto>(WarehouseListDialog.ARG_COMPANY_DTO)
            dto?.let {
                viewModel.setWarehouse(it)
            }
        }

        setFragmentResultListener(LanguageFragment.LANGUAGE_FRAGMENT_TAG) { _, bundle ->
            bundle.getString(LanguageFragment.LANGUAGE_FRAGMENT_KEY)?.let {
                Log.d("TAG", "language: $it")
                viewModel.setLanguage(it)
            }
        }
    }

    private fun unknownAppInstallPermission(): Boolean {
        val canInstall = requireContext().packageManager.canRequestPackageInstalls()
        return canInstall
    }
}