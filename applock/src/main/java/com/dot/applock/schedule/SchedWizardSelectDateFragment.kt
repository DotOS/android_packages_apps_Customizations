package com.dot.applock.schedule

import android.annotation.SuppressLint
import android.app.AppLockManager
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.dot.applock.R
import com.dot.applock.ui.CustomTextView
import com.dot.ui.utils.ObjectToolsAnimator
import com.dot.ui.utils.ResourceHelper
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.maltaisn.recurpicker.Recurrence
import com.maltaisn.recurpicker.Recurrence.Period
import com.maltaisn.recurpicker.format.RecurrenceFormatter
import kotlinx.android.synthetic.main.fragment_schedapps_date.*
import java.text.DateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.*

class SchedWizardSelectDateFragment : Fragment() {

    var wizardTitle: CollapsingToolbarLayout? = null
    var statusText: CustomTextView? = null
    var nextButton: MaterialButton? = null
    var pager: ViewPager2? = null

    fun initViews(
        wizardTitle: CollapsingToolbarLayout,
        statusText: CustomTextView,
        nextButton: MaterialButton,
        pager: ViewPager2,
    ) {
        this.wizardTitle = wizardTitle
        this.statusText = statusText
        this.nextButton = nextButton
        this.pager = pager
    }

    private lateinit var appLockManager: AppLockManager
    private var calendar = initScheduledCalendar()

    override fun onResume() {
        super.onResume()
        view?.requestLayout()
        wizardTitle?.title = "Pick the date"
        statusText?.text = RecurrenceFormatter(DateFormat.getInstance()).format(requireContext(), selectedRecurrence)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_schedapps_date, container, false)
    }

    private val recurrencePresets = mutableListOf(
        Recurrence.DOES_NOT_REPEAT,
        Recurrence(Period.DAILY),
        Recurrence(Period.WEEKLY)
    )

    private var selectedRecurrence = recurrencePresets[0]

    private var selectedDayOfRecurrence: MaterialDayPicker.Weekday =
        MaterialDayPicker.Weekday.getFirstDayOfWeekFor(Locale.US)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appLockManager = ResourceHelper.getAppLockManager(requireContext())
        statusText?.removeTextChangedListeners()
        nextButton?.isEnabled = false
        val currentHour = if (is24HourFormat(context)) calendar.get(Calendar.HOUR_OF_DAY) else calendar.get(Calendar.HOUR)
        val currentMinute = calendar.get(Calendar.MINUTE)
        schedTime.text =
            "$currentHour : ${if (currentMinute < 10) "0$currentMinute" else currentMinute}"
        schedTimeContainer.setOnClickListener {
            val mTimePicker =
                TimePickerDialog(
                    requireContext(),
                    R.style.DialogThemeTest,
                    { _, selectedHour, selectedMinute ->
                        run {
                            updateScheduledHoursNow(selectedRecurrence == Recurrence.DOES_NOT_REPEAT, selectedHour, selectedMinute)
                        }
                    },
                    if (is24HourFormat(context)) calendar.get(Calendar.HOUR_OF_DAY) else calendar.get(Calendar.HOUR),
                    calendar.get(Calendar.MINUTE),
                    is24HourFormat(context)
                )
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()
        }
        ObjectToolsAnimator.show(schedTimeLayout, 500)
        segmented {

            orientation = LinearLayout.HORIZONTAL

            initialCheckedIndex = 0

            initWithItems {
                listOf("Once", "Daily")
            }

            onSegmentChecked { segment ->
                when (segment.text) {
                    "Once" -> selectedRecurrence = Recurrence.DOES_NOT_REPEAT
                    "Daily" -> selectedRecurrence = Recurrence(Period.DAILY)
                    "Weekly" -> {
                        selectedRecurrence = Recurrence(Period.WEEKLY)
                        ObjectToolsAnimator.show(weekendLayout, 500)
                        if (day_picker.selectedDays.isNotEmpty()) {
                            selectedDayOfRecurrence = day_picker.selectedDays[0]
                            val formatter = RecurrenceFormatter(DateFormat.getInstance())
                            val nextDay: LocalDate =
                                LocalDate.now().with(TemporalAdjusters.nextOrSame(getDayOfWeek()))
                            statusText?.text = "${formatter.format(
                                requireContext(),
                                selectedRecurrence
                            )} on ${nextDay.dayOfWeek.name.toLowerCase(Locale.ROOT)
                                .capitalize(Locale.ROOT)}"
                        }
                    }
                }
                nextButton?.isEnabled = false
                if (segment.text != "Weekly") {
                    if (weekendLayout.visibility != View.GONE) {
                        ObjectToolsAnimator.gone(weekendLayout, 500)
                    }
                }
                statusText?.text = RecurrenceFormatter(DateFormat.getInstance()).format(
                    requireContext(),
                    selectedRecurrence
                )
            }
        }

        day_picker.daySelectionChangedListener =
            object : MaterialDayPicker.DaySelectionChangedListener {
                @SuppressLint("SetTextI18n")
                override fun onDaySelectionChanged(selectedDays: List<MaterialDayPicker.Weekday>) {
                    if (selectedDays.isNotEmpty()) {
                        selectedDayOfRecurrence = selectedDays[0]
                        val formatter = RecurrenceFormatter(DateFormat.getInstance())
                        val nextDay: LocalDate =
                            LocalDate.now().with(TemporalAdjusters.nextOrSame(getDayOfWeek()))
                        statusText?.text = "${formatter.format(
                            requireContext(),
                            selectedRecurrence
                        )} on ${nextDay.dayOfWeek.name.toLowerCase(Locale.ROOT)
                            .capitalize(Locale.ROOT)}"
                    } else
                        statusText?.text = ""
                }
            }
    }

    private fun buildScheduledLocking() {

    }

    inner class ScheduledLockingObject {

    }

    @SuppressLint("SetTextI18n")
    private fun updateScheduledHoursNow(once: Boolean, hours: Int, minutes: Int) {
        val localDate = LocalDate.now()
        val calendarCheck1 = Calendar.getInstance()
        val calendarCheck2 = Calendar.getInstance()
        calendarCheck1.timeInMillis = System.currentTimeMillis()
        calendarCheck2.set(localDate.year, localDate.monthValue, localDate.dayOfMonth, hours, minutes)
        if (timeValid(calendarCheck1.get(Calendar.HOUR), calendarCheck1.get(Calendar.MINUTE), calendarCheck2.get(Calendar.HOUR), calendarCheck2.get(Calendar.MINUTE))) {
            calendar.set(localDate.year, localDate.monthValue, localDate.dayOfMonth, hours, minutes)
            schedTime.text = "$hours : ${if (minutes < 10) "0$minutes" else minutes}"
            if (once) {
                statusText?.text =
                    "Today" + " at $hours:${if (minutes < 10) "0$minutes" else minutes}"
            } else {
                statusText?.text = "Every ${
                    localDate.dayOfWeek.name.toLowerCase(Locale.ROOT)
                        .capitalize(Locale.ROOT)
                }" + " at $hours:${if (minutes < 10) "0$minutes" else minutes}"
            }
            nextButton?.isEnabled = true
        } else {
            Snackbar.make(requireView(), "Schedule Not valid, next schedule should be at least 15min after current time", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun timeValid(currentH: Int, currentM: Int, schedH: Int, schedM: Int): Boolean {
        return when {
            currentH == schedH -> currentM+15 <= schedM
            currentH > schedH -> false
            currentH < schedH -> true
            else -> false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateScheduledHours(hours: Int, minutes: Int) {
        val localDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(getDayOfWeek()))
        calendar.set(localDate.year, localDate.monthValue, localDate.dayOfMonth, hours, minutes)
        schedTime.text = "$hours : ${if (minutes < 10) "0$minutes" else minutes}"
        statusText?.text = "${RecurrenceFormatter(DateFormat.getInstance()).format(
            requireContext(),
            selectedRecurrence
        )} on ${localDate.dayOfWeek.name.toLowerCase(Locale.ROOT)
            .capitalize(Locale.ROOT)}" + " at $hours:${if (minutes < 10) "0$minutes" else minutes}"
    }

    private fun initScheduledCalendar(): Calendar {
        val localDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(getDayOfWeek()))
        val calendar = Calendar.getInstance()
        calendar.set(localDate.year, localDate.monthValue, localDate.dayOfMonth)
        return calendar
    }

    private fun getDayOfWeek(): DayOfWeek {
        return if (selectedDayOfRecurrence != null)
            when (selectedDayOfRecurrence) {
                MaterialDayPicker.Weekday.MONDAY -> DayOfWeek.MONDAY
                MaterialDayPicker.Weekday.TUESDAY -> DayOfWeek.TUESDAY
                MaterialDayPicker.Weekday.WEDNESDAY -> DayOfWeek.WEDNESDAY
                MaterialDayPicker.Weekday.THURSDAY -> DayOfWeek.THURSDAY
                MaterialDayPicker.Weekday.FRIDAY -> DayOfWeek.FRIDAY
                MaterialDayPicker.Weekday.SATURDAY -> DayOfWeek.SATURDAY
                MaterialDayPicker.Weekday.SUNDAY -> DayOfWeek.SUNDAY
            }
        else DayOfWeek.MONDAY
    }

}