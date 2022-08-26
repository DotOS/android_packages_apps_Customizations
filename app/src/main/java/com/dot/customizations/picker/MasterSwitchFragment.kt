package com.dot.customizations.picker

import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Switch
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dot.customizations.R
import com.dot.customizations.picker.MasterSwitchFragment.SettingsType.*
import com.dot.customizations.widget.MainSwitchBar
import com.dot.customizations.widget.OnMainSwitchChangeListener
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter

open class MasterSwitchFragment : CollapsingToolbarFragment(),
    PreferencesAdapter.OnScreenChangeListener {

    override var layoutRes: Int? = R.layout.fragment_master_switch

    protected open var dependencyName: String? = null
    protected open var switchTitle: CharSequence? = null
    protected open var screen: PreferenceScreen? = null
    protected open var dependencyType: SettingsType = SYSTEM
    protected open var dependencyDefault: Int = 0

    private var contentRecycler: RecyclerView? = null
    private var masterSwitch: MainSwitchBar? = null
    private val contentResolver by lazy { requireContext().contentResolver }
    private val dependencyValue: Boolean by lazy {
        when (dependencyType) {
            SYSTEM -> Settings.System.getInt(
                contentResolver,
                dependencyName,
                dependencyDefault
            ) == 1
            GLOBAL -> Settings.Global.getInt(
                contentResolver,
                dependencyName,
                dependencyDefault
            ) == 1
            SECURE -> Settings.Secure.getInt(
                contentResolver,
                dependencyName,
                dependencyDefault
            ) == 1
        }
    }

    private val preferencesAdapter by lazy { PreferencesAdapter(screen) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (switchTitle != null && savedInstanceState == null) {
            val args = Bundle()
            args.putSerializable(
                "dataBlock",
                DataBlock(
                    switchTitle!!,
                    dependencyName!!,
                    dependencyDefault,
                    dependencyType
                )
            )
            arguments = args
        }
        if (arguments != null) {
            val dataBlock = arguments!!.getSerializable("dataBlock") as DataBlock
            switchTitle = dataBlock.switchTitle
            dependencyName = dataBlock.dependencyName
            dependencyDefault = dataBlock.dependencyDefault
            dependencyType = dataBlock.dependencyType
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        masterSwitch = requireView().findViewById(R.id.masterSwitch)
        masterSwitch!!.setSwitchChangeListener(object : OnMainSwitchChangeListener {
            override fun onSwitchChanged(switchView: Switch?, isChecked: Boolean) {
                val value = if (isChecked) 1 else 0
                when (dependencyType) {
                    SYSTEM -> Settings.System.putInt(
                        contentResolver,
                        dependencyName,
                        value
                    )
                    GLOBAL -> Settings.Global.putInt(
                        contentResolver,
                        dependencyName,
                        value
                    )
                    SECURE -> Settings.Secure.getInt(
                        contentResolver,
                        dependencyName,
                        value
                    )
                }
            }

        })
        contentRecycler = requireView().findViewById(R.id.contentRecycler)
        contentRecycler!!.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = preferencesAdapter
            layoutAnimation = AnimationUtils.loadLayoutAnimation(
                requireContext(),
                R.anim.preference_layout_fall_down
            )
        }
        preferencesAdapter.restoreAndObserveScrollPosition(contentRecycler!!)
        onScreenChanged(
            preferencesAdapter.currentScreen,
            preferencesAdapter.isInSubScreen()
        )
        updateState()
    }

    override fun onScreenChanged(screen: PreferenceScreen, subScreen: Boolean) {
        setTitle(screen.title)
        contentRecycler!!.scheduleLayoutAnimation()
    }

    private fun updateState() {
        switchTitle?.let {
            masterSwitch!!.setTitle(it)
        }
        dependencyValue.let { isChecked ->
            masterSwitch!!.isChecked = isChecked
            contentRecycler!!.isEnabled = isChecked
        }
    }

    enum class SettingsType {
        SYSTEM, SECURE, GLOBAL
    }

    private class DataBlock(
        var switchTitle: CharSequence,
        var dependencyName: String,
        var dependencyDefault: Int = 0,
        var dependencyType: SettingsType = SYSTEM
    ) : java.io.Serializable

    companion object {
        class Builder(
            var switchTitle: CharSequence,
            var dependencyName: String,
            var dependencyDefault: Int = 0,
            var dependencyType: SettingsType = SYSTEM,
            var screen: PreferenceScreen
        ) {

            fun getFragment(): MasterSwitchFragment {
                val fragment = MasterSwitchFragment()
                fragment.dependencyName = dependencyName
                fragment.dependencyDefault = dependencyDefault
                fragment.dependencyType = dependencyType
                fragment.switchTitle = switchTitle
                fragment.screen = screen
                return fragment
            }
        }
    }
}