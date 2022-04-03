package com.dot.customizations.picker.color

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settingslib.collapsingtoolbar.databinding.CollapsingToolbarBaseLayoutBinding
import com.dot.customizations.R
import com.dot.customizations.databinding.FragmentMonetBinding
import com.dot.customizations.picker.AppbarFragment
import com.dot.customizations.picker.extras.ExtrasViewModel
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter

class MonetFragment : AppbarFragment(), PreferencesAdapter.OnScreenChangeListener {

    private val viewModel: MonetViewModel by viewModels()
    private val preferencesAdapter get() = viewModel.preferencesAdapter
    private lateinit var preferencesView: RecyclerView
    private var _rootbinding: CollapsingToolbarBaseLayoutBinding? = null
    private val rootbinding get() = _rootbinding!!
    private var _binding: FragmentMonetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _rootbinding = CollapsingToolbarBaseLayoutBinding.inflate(inflater)
        val parent = rootbinding.root.findViewById<ViewGroup>(com.android.settingslib.collapsingtoolbar.R.id.content_frame)
        parent?.removeAllViews()
        _binding = FragmentMonetBinding.inflate(LayoutInflater.from(rootbinding.root.context), parent, true)
        binding.root.setOnApplyWindowInsetsListener { v: View, windowInsets: WindowInsets ->
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                windowInsets.systemWindowInsetBottom
            )
            windowInsets.consumeSystemWindowInsets()
        }
        setUpToolbar(rootbinding.root, true)
        return rootbinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            with(binding) {
                preferencesView = monetRecycler.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = preferencesAdapter
                    layoutAnimation = AnimationUtils.loadLayoutAnimation(
                        requireContext(),
                        R.anim.preference_layout_fall_down
                    )
                }
                preferencesAdapter.restoreAndObserveScrollPosition(preferencesView)
                onScreenChanged(
                    preferencesAdapter.currentScreen,
                    preferencesAdapter.isInSubScreen()
                )
                preferencesAdapter.onScreenChangeListener = this@MonetFragment
            }
        }
    }


    override fun getToolbarId(): Int {
        return com.android.settingslib.collapsingtoolbar.R.id.action_bar
    }

    override fun onBackPressed(): Boolean {
        return preferencesAdapter.goBack()
    }

    override fun onDestroy() {
        preferencesAdapter.onScreenChangeListener = null
        preferencesView.adapter = null
        super.onDestroy()
    }

    override fun setTitle(title: CharSequence?) {
        rootbinding.collapsingToolbar.title = title
        super.setTitle(title)
    }

    companion object {
        fun newInstance(
            title: CharSequence?
        ): MonetFragment {
            val fragment = MonetFragment()
            fragment.arguments = createArguments(title)
            return fragment
        }
    }

    override fun onScreenChanged(screen: PreferenceScreen, subScreen: Boolean) {
        setTitle(screen.title)
        preferencesView.scheduleLayoutAnimation()
    }

}