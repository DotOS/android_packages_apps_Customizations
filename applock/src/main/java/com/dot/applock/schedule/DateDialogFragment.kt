package com.dot.applock.schedule

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class DateDialogFragment : DialogFragment() {

    @SuppressLint("InflateParams")
    override fun onCreateDialog(state: Bundle?): Dialog {
        val context = requireContext()

        val args = requireArguments()
        val date = args.getLong(KEY_DATE)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date

        // Since this is not a material component, colorAccent should be defined as either
        // colorPrimary or colorSecondary in styles.xml. (see demo app)

        return DatePickerDialog(context, { _, year, month, day ->
            calendar.set(year, month, day)
            calendar.setToStartOfDay()
            callback?.onDateDialogConfirmed(calendar.timeInMillis)
        }, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DATE])
    }

    private fun Calendar.setToStartOfDay() {
        this[Calendar.HOUR_OF_DAY] = 0
        this[Calendar.MINUTE] = 0
        this[Calendar.SECOND] = 0
        this[Calendar.MILLISECOND] = 0
    }

    private val callback: Callback?
        get() = (parentFragment as? Callback)
            ?: (targetFragment as? Callback)
            ?: (activity as? Callback)

    interface Callback {
        fun onDateDialogConfirmed(date: Long)
    }

    companion object {

        private const val KEY_DATE = "date"

        fun newInstance(date: Long): DateDialogFragment {
            val dialog = DateDialogFragment()
            dialog.arguments = bundleOf(KEY_DATE to date)
            return dialog
        }
    }
}