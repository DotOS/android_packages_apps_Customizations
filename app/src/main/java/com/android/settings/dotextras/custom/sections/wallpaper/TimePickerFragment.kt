package com.android.settings.dotextras.custom.sections.wallpaper

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.android.settings.dotextras.databinding.FragmentSheetTimePickerBinding
import com.michaldrabik.classicmaterialtimepicker.OnTime12PickedListener
import com.michaldrabik.classicmaterialtimepicker.OnTime24PickedListener
import com.michaldrabik.classicmaterialtimepicker.model.CmtpTime
import com.michaldrabik.classicmaterialtimepicker.model.CmtpTime12
import com.michaldrabik.classicmaterialtimepicker.model.CmtpTime24
import com.michaldrabik.classicmaterialtimepicker.model.CmtpTimeType
import com.michaldrabik.classicmaterialtimepicker.model.CmtpTimeType.HOUR_12
import com.michaldrabik.classicmaterialtimepicker.model.CmtpTimeType.HOUR_24

fun TimePickerFragment.setOnTime24PickedListener(listener: (CmtpTime24) -> Unit) {
    this.setOnTime24PickedListener(object : OnTime24PickedListener {
        override fun onTimePicked(time: CmtpTime24) = listener(time)
    })
}

class TimePickerFragment : DialogFragment() {

    companion object {
        private const val ARG_POSITIVE_BUTTON_TEXT = "ARG_POSITIVE_BUTTON_TEXT"
        private const val ARG_NEGATIVE_BUTTON_TEXT = "ARG_NEGATIVE_BUTTON_TEXT"
        private const val ARG_HOUR = "ARG_HOUR"
        private const val ARG_MINUTE = "ARG_MINUTE"
        private const val ARG_PM_AM = "ARG_PM_AM"
        private const val ARG_TYPE = "ARG_TYPE"

        /**
         * Create new instance of CmtpTimeDialogFragment with CmtpTimePickerView embedded.
         * @param positiveButtonText Custom positive button text. "OK" by default.
         * @param negativeButtonText Custom negative button text. "CANCEL" by default.
         */
        @JvmOverloads
        @JvmStatic
        fun newInstance(
            positiveButtonText: String = "OK",
            negativeButtonText: String = "Cancel"
        ) = TimePickerFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_POSITIVE_BUTTON_TEXT, positiveButtonText)
                putString(ARG_NEGATIVE_BUTTON_TEXT, negativeButtonText)
            }
        }
    }

    private lateinit var time: CmtpTime
    private var _binding: FragmentSheetTimePickerBinding? = null
    private val binding get() = _binding!!

    private lateinit var onTime12PickedListener: OnTime12PickedListener
    private lateinit var onTime24PickedListener: OnTime24PickedListener

    private fun saveState(outState: Bundle) {
        if (_binding == null) return
        when (binding.timePicker.getType()) {
            HOUR_24 -> {
                outState.putInt(ARG_HOUR, binding.timePicker.getTime24().hour)
                outState.putInt(ARG_MINUTE, binding.timePicker.getTime24().minute)
            }
            HOUR_12 -> {
                outState.putInt(ARG_HOUR, binding.timePicker.getTime12().hour)
                outState.putInt(ARG_MINUTE, binding.timePicker.getTime12().minute)
                outState.putString(ARG_PM_AM, binding.timePicker.getTime12().pmAm.name)
            }
        }
        outState.putString(ARG_TYPE, binding.timePicker.getType().name)
    }

    private fun restoreState(stateBundle: Bundle) {
        val type = enumValueOf<CmtpTimeType>(stateBundle.getString(ARG_TYPE)!!)
        time = when (type) {
            HOUR_24 -> {
                val hour = stateBundle.getInt(ARG_HOUR, CmtpTime24.DEFAULT.hour)
                val minute = stateBundle.getInt(ARG_MINUTE, CmtpTime24.DEFAULT.minute)
                CmtpTime24(hour, minute)
            }
            HOUR_12 -> {
                val hour = stateBundle.getInt(ARG_HOUR, CmtpTime12.DEFAULT.hour)
                val minute = stateBundle.getInt(ARG_MINUTE, CmtpTime12.DEFAULT.hour)
                val pmAm = stateBundle.getString(ARG_PM_AM, CmtpTime12.DEFAULT.pmAm.name)
                CmtpTime12(hour, minute, enumValueOf(pmAm))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireDialog().requestWindowFeature(Window.FEATURE_NO_TITLE)
        requireDialog().window!!.setBackgroundDrawableResource(android.R.color.transparent)
        requireDialog().window!!.setGravity(Gravity.BOTTOM)
        _binding = FragmentSheetTimePickerBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireDialog().window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val context = this
        lifecycleScope.launchWhenCreated {
            with(binding) {
                savedInstanceState?.let { restoreState(it) }
                if (context::time.isInitialized) binding.timePicker.setTime(time)
                timeApply.setOnClickListener {
                    onTimePicked()
                    dismiss()
                }
                timeCancel.setOnClickListener {
                    dismiss()
                }
            }
        }
    }

    private fun onTimePicked() {
        when (binding.timePicker.getType()) {
            HOUR_12 -> {
                if (this::onTime12PickedListener.isInitialized) {
                    onTime12PickedListener.onTimePicked(binding.timePicker.getTime12())
                }
            }
            HOUR_24 -> {
                if (this::onTime24PickedListener.isInitialized) {
                    onTime24PickedListener.onTimePicked(binding.timePicker.getTime24())
                }
            }
        }
    }

    /**
     * Set initial time and initialize picker with 24-Hour format.
     * @param hour from 0 to 23.
     * @param minute from 0 to 59.
     * @throws IllegalStateException when given hour or minute is out of valid range.
     */
    fun setInitialTime24(hour: Int, minute: Int) {
        time = CmtpTime24(hour, minute)
    }

    /**
     * Set time picked listener for 24-Hour format.
     */
    fun setOnTime24PickedListener(listener: OnTime24PickedListener) {
        check(time.getType() == HOUR_24) { "Invalid listener type. Time picker has been initialised as 12-Hour type" }
        this.onTime24PickedListener = listener
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveState(outState)
    }
}