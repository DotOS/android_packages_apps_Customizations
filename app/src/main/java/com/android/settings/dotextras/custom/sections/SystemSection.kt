package com.android.settings.dotextras.custom.sections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.cards.ContextCards
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SECURE
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SWIPE
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SWITCH
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SYSTEM
import com.android.settings.dotextras.custom.utils.GridSpacingItemDecoration
import com.android.settings.dotextras.system.FeatureManager

open class SystemSection : Fragment() {

    private var contextCardList: ArrayList<ContextCards> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.section_system, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val featureManager = FeatureManager(requireActivity().contentResolver)
        contextCardList.clear()
        contextCardList.add(
            ContextCards(
                iconID = R.drawable.ic_torch,
                title = getString(R.string.disabled),
                subtitle = getString(R.string.hold_to_torch),
                accentColor = R.color.dot_sky,
                feature = featureManager.Secure().TORCH_POWER_BUTTON_GESTURE,
                featureType = SECURE,
                summary = getString(R.string.hold_to_torch_summary)
            )
        )
        contextCardList.add(
            ContextCards(
                iconID = R.drawable.ic_lock,
                title = getString(R.string.disabled),
                subtitle = getString(R.string.pocket_mode),
                accentColor = R.color.dot_blue,
                feature = featureManager.System().POCKET_JUDGE,
                featureType = SYSTEM
            )
        )
        val recyclerView: RecyclerView = view.findViewById(R.id.contextRecycler)
        val adapter =
            ContextCardsAdapter(requireActivity().contentResolver, SWITCH, contextCardList)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                2,
                resources.getDimension(R.dimen.recyclerSpacer).toInt(),
                true
            )
        )
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

    }
}