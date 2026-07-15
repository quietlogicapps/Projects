package com.quietlogic.allisok.ui.backup

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.quietlogic.allisok.R
import com.quietlogic.allisok.alarm.engine.AlarmRescheduler
import com.quietlogic.allisok.data.backup.BackupRepository
import com.quietlogic.allisok.data.backup.RestoreRepository
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.security.LockGate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BackupActivity : AppCompatActivity() {

    private lateinit var textExportStatus: TextView
    private lateinit var textImportStatus: TextView

    private var pendingImportJson: String? = null
    private var importConfirmDialog: Dialog? = null

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        try {
            if (uri == null) {
                updateExportStatus(getString(R.string.backup_export_status_cancelled))
                return@registerForActivityResult
            }

            val json = pendingExportJson
            if (json == null) {
                updateExportStatus(getString(R.string.backup_export_status_error))
                return@registerForActivityResult
            }

            lifecycleScope.launch {
                val success = withContext(Dispatchers.IO) {
                    try {
                        contentResolver.openOutputStream(uri)?.use { stream ->
                            stream.write(json.toByteArray(Charsets.UTF_8))
                        } ?: return@withContext false
                        true
                    } catch (_: Exception) {
                        false
                    }
                }

                pendingExportJson = null
                updateExportStatus(
                    if (success) {
                        getString(R.string.backup_export_status_success)
                    } else {
                        getString(R.string.backup_export_status_error)
                    },
                    success = success
                )
            }
        } finally {
            LockGate.endExternalUi()
        }
    }

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        try {
            if (uri == null) {
                clearPendingImport()
                updateImportStatus(getString(R.string.backup_import_status_cancelled))
                return@registerForActivityResult
            }

            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    try {
                        val json = contentResolver.openInputStream(uri)?.use { stream ->
                            stream.bufferedReader(Charsets.UTF_8).readText()
                        } ?: return@withContext ImportReadResult.Error

                        val validationError = BackupJsonValidator.validate(json)
                        if (validationError != null) {
                            return@withContext ImportReadResult.ValidationError(validationError)
                        }

                        ImportReadResult.Ready(json)
                    } catch (_: Exception) {
                        ImportReadResult.Error
                    }
                }

                when (result) {
                    is ImportReadResult.Ready -> {
                        pendingImportJson = result.json
                        showImportConfirmDialog()
                    }

                    is ImportReadResult.ValidationError -> {
                        clearPendingImport()
                        updateImportStatus(
                            getString(
                                R.string.backup_import_status_validation_error,
                                result.message
                            )
                        )
                    }

                    ImportReadResult.Error -> {
                        clearPendingImport()
                        updateImportStatus(getString(R.string.backup_import_status_error))
                    }
                }
            }
        } finally {
            LockGate.endExternalUi()
        }
    }

    private var pendingExportJson: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.backup_title)

        textExportStatus = findViewById(R.id.textExportStatus)
        textImportStatus = findViewById(R.id.textImportStatus)

        findViewById<MaterialButton>(R.id.buttonExport).setOnClickListener {
            startExport()
        }

        findViewById<MaterialButton>(R.id.buttonImport).setOnClickListener {
            LockGate.beginExternalUi()
            openDocumentLauncher.launch(arrayOf("application/json", "application/*", "*/*"))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        importConfirmDialog?.dismiss()
        importConfirmDialog = null
        clearPendingImport()
        super.onDestroy()
    }

    private fun startExport() {
        updateExportStatus(getString(R.string.export_generating))

        lifecycleScope.launch {
            val json = withContext(Dispatchers.IO) {
                try {
                    val db = DatabaseProvider.getDatabase(applicationContext)
                    BackupRepository(applicationContext, db).buildExportJson()
                } catch (_: Exception) {
                    null
                }
            }

            if (json == null) {
                pendingExportJson = null
                updateExportStatus(getString(R.string.backup_export_status_error))
                return@launch
            }

            pendingExportJson = json
            LockGate.beginExternalUi()
            createDocumentLauncher.launch(DEFAULT_EXPORT_FILE_NAME)
        }
    }

    private fun showImportConfirmDialog() {
        importConfirmDialog?.dismiss()

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_import_confirm)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)
        dialog.setOnCancelListener {
            clearPendingImport()
            updateImportStatus(getString(R.string.backup_import_status_cancelled))
        }

        dialog.findViewById<MaterialButton>(R.id.buttonImportCancel).setOnClickListener {
            dialog.dismiss()
            clearPendingImport()
            updateImportStatus(getString(R.string.backup_import_status_cancelled))
        }

        dialog.findViewById<MaterialButton>(R.id.buttonImportConfirm).setOnClickListener {
            dialog.dismiss()
            performImport()
        }

        importConfirmDialog = dialog
        dialog.show()
    }

    private fun performImport() {
        val json = pendingImportJson
        if (json == null) {
            updateImportStatus(getString(R.string.backup_import_status_error))
            return
        }

        updateImportStatus(getString(R.string.backup_import_status_running))

        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val db = DatabaseProvider.getDatabase(applicationContext)
                    RestoreRepository(applicationContext, db).restoreFromJson(json)
                    AlarmRescheduler(applicationContext).rescheduleAll()
                    true
                } catch (_: Exception) {
                    false
                }
            }

            clearPendingImport()
            updateImportStatus(
                if (success) {
                    getString(R.string.backup_import_status_success)
                } else {
                    getString(R.string.backup_import_status_error)
                },
                success = success
            )
        }
    }

    private fun updateExportStatus(message: String, success: Boolean = false) {
        textExportStatus.text = message
        textExportStatus.setTextColor(
            if (success) STATUS_SUCCESS_COLOR else STATUS_DEFAULT_COLOR
        )
    }

    private fun updateImportStatus(message: String, success: Boolean = false) {
        textImportStatus.text = message
        textImportStatus.setTextColor(
            if (success) STATUS_SUCCESS_COLOR else STATUS_DEFAULT_COLOR
        )
    }

    private fun clearPendingImport() {
        pendingImportJson = null
    }

    private sealed class ImportReadResult {
        data class Ready(val json: String) : ImportReadResult()
        data class ValidationError(val message: String) : ImportReadResult()
        data object Error : ImportReadResult()
    }

    companion object {
        private const val DEFAULT_EXPORT_FILE_NAME = "allisok_backup.json"
        private val STATUS_DEFAULT_COLOR = Color.parseColor("#B3FFFFFF")
        private val STATUS_SUCCESS_COLOR = Color.parseColor("#16A34A")
    }
}
