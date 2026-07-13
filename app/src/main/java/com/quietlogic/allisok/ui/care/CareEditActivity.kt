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

    companion object {
        const val EXTRA_CARE_ITEM_ID = "extra_care_item_id"
        private const val INVALID_CARE_ITEM_ID = -1L
    }

    private lateinit var db: AppDatabase

    private val times: MutableList<LocalTime> = mutableListOf()

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null

    private var editCareItemId: Long = INVALID_CARE_ITEM_ID
    private var isLoadingEditData = false

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

        editCareItemId = intent.getLongExtra(EXTRA_CARE_ITEM_ID, INVALID_CARE_ITEM_ID)

        db = DatabaseProvider.getDatabase(applicationContext)

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
                if (isLoadingEditData) {
                    btnPickDays.visibility = View.GONE
                    return@setOnCheckedChangeListener
                }

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
                            times = times.toList(),
                            careItemId = editCareItemId.takeIf { it > 0L }
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

        lifecycleScope.launch {
            val settings = com.quietlogic.allisok.data.repository.SettingsRepository(db.appSettingsDao()).getSettings().first()
            val pattern = if (settings?.dateFormat == "US") "MM/dd/yyyy" else "dd/MM/yyyy"
            dateFormatter = DateTimeFormatter.ofPattern(pattern)

            if (editCareItemId > 0L) {
                loadCareItemForEdit(
                    careItemId = editCareItemId,
                    nameInput = nameInput,
                    groupInstruction = groupInstruction,
                    groupRepeat = groupRepeat,
                    btnPickDays = btnPickDays,
                    textRepeatDays = textRepeatDays,
                    timesController = timesController,
                    repeatAndDateHelper = repeatAndDateHelper
                )
            } else {
                if (startDate != null) {
                    textStart.text = getString(R.string.care_start_value, startDate!!.format(dateFormatter))
                }
                if (endDate != null) {
                    textEnd.text = getString(R.string.care_end_value, endDate!!.format(dateFormatter))
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

    private suspend fun loadCareItemForEdit(
        careItemId: Long,
        nameInput: EditText,
        groupInstruction: RadioGroup,
        groupRepeat: RadioGroup,
        btnPickDays: MaterialButton,
        textRepeatDays: TextView,
        timesController: CareEditTimesController,
        repeatAndDateHelper: CareEditRepeatAndDateHelper
    ) {
        val item = db.careItemDao().getAllActive().first().firstOrNull { it.id == careItemId }
            ?: return

        nameInput.setText(item.name)

        val instructionRadioId = when {
            item.instruction == getString(R.string.care_instruction_before_food) ||
                item.instruction.equals("Before food", ignoreCase = true) -> R.id.radioBefore

            item.instruction == getString(R.string.care_instruction_after_food) ||
                item.instruction.equals("After food", ignoreCase = true) -> R.id.radioAfter

            else -> R.id.radioNone
        }
        groupInstruction.check(instructionRadioId)

        isLoadingEditData = true

        if (item.repeatType == "DAILY") {
            startDate = item.startDate
            endDate = item.endDate
            groupRepeat.check(R.id.radioDaily)
            textRepeatDays.text = getString(R.string.care_days_daily)
            btnPickDays.visibility = View.GONE
            repeatAndDateHelper.updateDateButtonsState()
        } else if (item.repeatType.startsWith("DAYS:")) {
            val dayCodesFromItem = item.repeatType
                .removePrefix("DAYS:")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            selectedDays.clear()
            selectedDays.addAll(dayCodesFromItem)

            for (i in dayCodes.indices) {
                checkedDays[i] = dayCodes[i] in dayCodesFromItem
            }

            startDate = item.startDate
            endDate = item.endDate
            groupRepeat.check(R.id.radioSpecific)
            btnPickDays.visibility = View.GONE

            textRepeatDays.text = if (selectedDays.isEmpty()) {
                getString(R.string.care_days_not_selected)
            } else {
                val selectedDayLabels = selectedDays.map { mapDayCodeToLabel(it) }
                getString(R.string.care_days_selected, selectedDayLabels.joinToString(", "))
            }

            repeatAndDateHelper.updateDateButtonsState()
        }

        isLoadingEditData = false

        val timeEntities = db.careTimeDao().getTimesForItem(careItemId)
        times.clear()
        times.addAll(timeEntities.map { it.time })
        timesController.renderTimes()
    }

    private fun mapDayCodeToLabel(code: String): String {
        return when (code.trim()) {
            "MON" -> getString(R.string.care_day_mon)
            "TUE" -> getString(R.string.care_day_tue)
            "WED" -> getString(R.string.care_day_wed)
            "THU" -> getString(R.string.care_day_thu)
            "FRI" -> getString(R.string.care_day_fri)
            "SAT" -> getString(R.string.care_day_sat)
            "SUN" -> getString(R.string.care_day_sun)
            else -> code
        }
    }
}