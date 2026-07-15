package com.quietlogic.allisok.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.quietlogic.allisok.R
import com.quietlogic.allisok.billing.store.StoreModule
import com.quietlogic.allisok.billing.store.StoreNavigator
import com.quietlogic.allisok.security.AdminSession
import com.quietlogic.allisok.security.LockGate
import com.quietlogic.allisok.ui.backup.BackupActivity
import com.quietlogic.allisok.ui.history.HistoryActivity
import com.quietlogic.allisok.ui.language.LanguageActivity
import com.quietlogic.allisok.ui.pin.PinActivity
import com.quietlogic.allisok.ui.security.SecurityActivity
import com.quietlogic.allisok.ui.settings.DateFormatActivity

class SettingsFragment : Fragment() {

    private lateinit var rowAdmin: View
    private lateinit var textAdminTitle: TextView

    private val storeNavigator: StoreNavigator = StoreModule.createStoreNavigator()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsScroll = view.findViewById<View>(R.id.settingsScroll)
        val bottomAirPx = (8 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(settingsScroll) { scrollView, insets ->
            val navigationBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            scrollView.updatePadding(bottom = navigationBottom + bottomAirPx)
            insets
        }
        ViewCompat.requestApplyInsets(settingsScroll)

        bindRow(
            row = view.findViewById(R.id.rowSecurity),
            iconRes = R.drawable.ic_settings_security,
            titleRes = R.string.menu_security
        ) {
            startActivity(Intent(requireContext(), SecurityActivity::class.java))
        }

        bindRow(
            row = view.findViewById(R.id.rowLanguage),
            iconRes = R.drawable.ic_settings_language,
            titleRes = R.string.menu_language
        ) {
            startActivity(Intent(requireContext(), LanguageActivity::class.java))
        }

        bindRow(
            row = view.findViewById(R.id.rowBackup),
            iconRes = R.drawable.ic_settings_backup,
            titleRes = R.string.settings_backup_title
        ) {
            openBackupActivity()
        }

        bindRow(
            row = view.findViewById(R.id.rowDateFormat),
            iconRes = R.drawable.ic_settings_date_format,
            titleRes = R.string.menu_date_format
        ) {
            startActivity(Intent(requireContext(), DateFormatActivity::class.java))
        }

        bindRow(
            row = view.findViewById(R.id.rowHistory),
            iconRes = R.drawable.ic_settings_history,
            titleRes = R.string.menu_history
        ) {
            startActivity(Intent(requireContext(), HistoryActivity::class.java))
        }

        bindRow(
            row = view.findViewById(R.id.rowMoreApps),
            iconRes = R.drawable.ic_settings_more_apps,
            titleRes = R.string.menu_more_apps
        ) {
            requireContext().getSharedPreferences("home_prefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .putBoolean("returning_from_store", true)
                .apply()
            storeNavigator.openStorePage(requireContext())
        }

        rowAdmin = view.findViewById(R.id.rowAdmin)
        textAdminTitle = rowAdmin.findViewById(R.id.textSettingsRowTitle)
        rowAdmin.findViewById<ImageView>(R.id.iconSettingsRow)
            .setImageResource(R.drawable.ic_settings_admin)

        rowAdmin.setOnClickListener {
            if (AdminSession.isActive()) {
                AdminSession.stop()
                (activity as? HomeActivity)?.onAdminStateChanged()
                updateAdminRowTitle()
            } else {
                val intent = Intent(requireContext(), PinActivity::class.java)
                intent.putExtra("mode", LockGate.MODE_ADMIN_UNLOCK)
                startActivity(intent)
            }
        }

        updateAdminRowTitle()
    }

    override fun onResume() {
        super.onResume()
        updateAdminRowTitle()
    }

    private fun openBackupActivity() {
        startActivity(Intent(requireContext(), BackupActivity::class.java))
    }

    private fun bindRow(
        row: View,
        iconRes: Int,
        titleRes: Int,
        onClick: () -> Unit
    ) {
        row.findViewById<ImageView>(R.id.iconSettingsRow).setImageResource(iconRes)
        row.findViewById<TextView>(R.id.textSettingsRowTitle).setText(titleRes)
        row.setOnClickListener { onClick() }
    }

    private fun updateAdminRowTitle() {
        textAdminTitle.text = if (AdminSession.isActive()) {
            getString(R.string.menu_exit_admin_mode)
        } else {
            getString(R.string.menu_enter_admin)
        }
    }
}
