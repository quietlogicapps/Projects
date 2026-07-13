package com.quietlogic.allisok.ui.care

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.quietlogic.allisok.R
import com.quietlogic.allisok.data.local.db.AppDatabase
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.security.AdminSession
import com.quietlogic.allisok.security.LockGate
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class CareEditActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    private val times: MutableList<LocalTime> = mutableListOf()

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null

    private val selectedDays: MutableList<String> = mutableListOf()

    private lateinit var dayCodes: Array<String>
    private lateinit var dayLabels: Array<String>

    private lateinit var checkedDays: BooleanArray

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private var dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", MODE_PRIVATE)
        val languageCode = prefs.getString("app_language", "en") ?: "en"
        val locale = if (languageCode.contains("-")) {
            val parts = languageCode.split("-")
            java.util.Locale(parts[0], parts[1])
        } else {
            java.util.Locale(languageCode)
        }
        java.util.Locale.setDefault(locale)
        val configuration = android.content.res.Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AdminSession.isActive()) {
            LockGate.requireAdminUnlock(this)
        }

        setContentView(R.layout.activity_care_edit)

        title = getString(R.string.care_edit_title)

        db = DatabaseProvider.getDatabase(applicationContext)

        lifecycleScope.launch {
            val settings = com.quietlogic.allisok.data.repository.SettingsRepository(db.appSettingsDao()).getSettings().first()
            val pattern = if (settings?.dateFormat == "US") "MM/dd/yyyy" else "dd/MM/yyyy"
            dateFormatter = DateTimeFormatter.ofPattern(pattern)

            val textStart = findViewById<TextView>(R.id.textStart)
            val textEnd = findViewById<TextView>(R.id.textEnd)

            if (startDate != null) {
                textStart.text = getString(R.string.care_start_value, startDate!!.format(dateFormatter))
            }
            if (endDate != null) {
                textEnd.text = getString(R.string.care_end_value, endDate!!.format(dateFormatter))
            }
        }

        dayCodes = arrayOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        dayLabels = arrayOf(
            getString(R.string.care_day_mon),
            getString(R.string.care_day_tue),
            getString(R.string.care_day_wed),
            getString(R.string.care_day_thu),
            getString(R.string.care_day_fri),
            getString(R.string.care_day_sat),
            getString(R.string.care_day_sun)
        )
        checkedDays = BooleanArray(dayCodes.size)

        val nameInput = findViewById<EditText>(R.id.inputName)

        val groupInstruction = findViewById<RadioGroup>(R.id.groupInstruction)
        val groupRepeat = findViewById<RadioGroup>(R.id.groupRepeat)

        val btnPickDays = findViewById<MaterialButton>(R.id.btnPickDays)
        val textRepeatDays = findViewById<TextView>(R.id.textRepeatDays)

        val btnPickStart = findViewById<View>(R.id.btnPickStart)
        val btnPickEnd = findViewById<View>(R.id.btnPickEnd)

        val textStart = findViewById<TextView>(R.id.textStart)
        val textEnd = findViewById<TextView>(R.id.textEnd)

        val btnAddTime = findViewById<View>(R.id.btnAddTime)
        val textAddTime = findViewById<TextView>(R.id.textAddTime)
        val layoutTimes = findViewById<LinearLayout>(R.id.layoutTimes)
        val textNoTimes = findViewById<TextView>(R.id.textNoTimes)

        val btnSave = findViewById<View>(R.id.btnSaveCare)

        val timesController = CareEditTimesController(
            activity = this,
            times = times,
            layoutTimes = layoutTimes,
            textAddTime = textAddTime,
            textNoTimes = textNoTimes,
            timeFormatter = timeFormatter
        )

        val repeatAndDateHelper = CareEditRepeatAndDateHelper(
            activity = this,
            selectedDays = selectedDays,
            checkedDays = checkedDays,
            dayCodes = dayCodes,
            dayLabels = dayLabels,
            dateFormatterProvider = { dateFormatter },
            startDateProvider = { startDate },
            endDateProvider = { endDate },
            groupRepeat = groupRepeat,
            btnPickStart = btnPickStart,
            btnPickEnd = btnPickEnd,
            textStart = textStart,
            textEnd = textEnd
        )

        val saveExecutor = CareEditSaveExecutor(
            context = this,
            db = db
        )

        btnPickDays.visibility = View.GONE
        repeatAndDateHelper.updateDateButtonsState()
        timesController.updateAddTimeUi()

        groupInstruction.setOnCheckedChangeListener { _, _ ->
            hideKeyboardAndClearFocus(nameInput)
        }

        groupRepeat.setOnCheckedChangeListener { _, checkedId ->

            hideKeyboardAndClearFocus(nameInput)

            if (checkedId == R.id.radioDaily) {
                btnPickDays.visibility = View.GONE
                textRepeatDays.text = getString(R.string.care_days_daily)
                selectedDays.clear()

                for (i in checkedDays.indices) {
                    checkedDays[i] = false
                }

                repeatAndDateHelper.updateDateButtonsState()
            }

            if (checkedId == R.id.radioSpecific) {
                if (repeatAndDateHelper.shouldSuppressDaysDialogOpen()) {
                    return@setOnCheckedChangeListener
                }

                btnPickDays.visibility = View.GONE

                startDate = null
                endDate = null
                textStart.text = getString(R.string.care_start_not_set)
                textEnd.text = getString(R.string.care_end_not_set)

                repeatAndDateHelper.updateDateButtonsState()

                repeatAndDateHelper.openDaysDialog(textRepeatDays)
            }
        }

        timesController.renderTimes()

        btnPickStart.setOnClickListener {
            hideKeyboardAndClearFocus(nameInput)

            if (groupRepeat.checkedRadioButtonId != R.id.radioDaily) return@setOnClickListener

            repeatAndDateHelper.openDatePicker { date ->
                startDate = date
                textStart.text = getString(R.string.care_start_value, date.format(dateFormatter))
            }
        }

        btnPickEnd.setOnClickListener {
            hideKeyboardAndClearFocus(nameInput)

            if (groupRepeat.checkedRadioButtonId != R.id.radioDaily) return@setOnClickListener

            repeatAndDateHelper.openDatePicker { date ->
                endDate = date
                textEnd.text = getString(R.string.care_end_value, date.format(dateFormatter))
            }
        }

        btnAddTime.setOnClickListener {
            hideKeyboardAndClearFocus(nameInput)
            timesController.openTimePicker()
        }

        btnSave.setOnClickListener {

            hideKeyboardAndClearFocus(nameInput)

            val name = nameInput.text.toString().trim()
            val isDaily = groupRepeat.checkedRadioButtonId == R.id.radioDaily
            val instruction = when (groupInstruction.checkedRadioButtonId) {
                R.id.radioBefore -> getString(R.string.care_instruction_before_food)
                R.id.radioAfter -> getString(R.string.care_instruction_after_food)
                else -> getString(R.string.care_instruction_none)
            }

            lifecycleScope.launch {
                when (
                    val result = saveExecutor.validateAndSave(
                        CareEditSaveInput(
                            name = name,
                            instruction = instruction,
                            isDaily = isDaily,
                            selectedDays = selectedDays.toList(),
                            startDate = startDate,
                            endDate = endDate,
                            times = times.toList()
                        )
                    )
                ) {
                    is CareEditSaveResult.Success -> {
                        Toast.makeText(
                            this@CareEditActivity,
                            getString(R.string.care_saved),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }

                    is CareEditSaveResult.ValidationError -> {
                        Toast.makeText(
                            this@CareEditActivity,
                            getString(result.messageResId),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LockGate.REQUEST_ADMIN_UNLOCK) {
            if (resultCode != RESULT_OK) {
                finish()
            }
        }
    }

    private fun hideKeyboardAndClearFocus(view: View) {
        view.clearFocus()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}