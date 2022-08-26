package com.dot.customizations.model.color

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dot.customizations.R
import com.dot.customizations.picker.CollapsingToolbarFragment
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter

class MonetSettingsFragment : CollapsingToolbarFragment(), PreferencesAdapter.OnScreenChangeListener {

    override var layoutRes: Int? = R.layout.fragment_monet

    private val viewModel: MonetSettingsViewModel by viewModels()
    private val preferencesAdapter get() = viewModel.preferencesAdapter as PreferencesAdapter
    private val monetRecycler by lazy { view!!.findViewById<RecyclerView>(R.id.monetRecycler)}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            monetRecycler.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = preferencesAdapter
                layoutAnimation = AnimationUtils.loadLayoutAnimation(
                    requireContext(),
                    R.anim.preference_layout_fall_down
                )
            }
            preferencesAdapter.restoreAndObserveScrollPosition(monetRecycler)
            onScreenChanged(
                preferencesAdapter.currentScreen,
                preferencesAdapter.isInSubScreen()
            )
            preferencesAdapter.onScreenChangeListener = this@MonetSettingsFragment
        }
    }

    override fun onBackPressed(): Boolean {
        return preferencesAdapter.goBack()
    }

    override fun onDestroy() {
        preferencesAdapter.onScreenChangeListener = null
        monetRecycler.adapter = null
        super.onDestroy()
    }

    companion object {
        fun newInstance(
            title: CharSequence?
        ): MonetSettingsFragment {
            val fragment = MonetSettingsFragment()
            fragment.arguments = createArguments(title)
            return fragment
        }
    }

    override fun onScreenChanged(screen: PreferenceScreen, subScreen: Boolean) {
        setTitle(screen.title)
        monetRecycler.scheduleLayoutAnimation()
    }

}