/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dot.customizations.picker;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;
import android.widget.Toolbar.OnMenuItemClickListener;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.dot.customizations.R;
import com.dot.customizations.util.ResourceUtils;
import com.dot.customizations.widget.BottomActionBar;

/**
 * Base class for Fragments that own a {@link Toolbar} widget and a {@link BottomActionBar}.
 * <p>
 * A Fragment extending this class is expected to have a {@link Toolbar} in its root view, with id
 * {@link R.id#toolbar}, which can optionally have a TextView with id custom_toolbar_title for
 * the title.
 * If the Bundle returned by {@link #createArguments(CharSequence)} is used as Arguments for this
 * Fragment, the title provided to that method will be used as the Fragment's toolbar title,
 * otherwise, the value returned by {@link #getDefaultTitle()} (default being {@code null}) will be
 * used as title.
 *
 * @see #setArguments(Bundle)
 * @see BottomActionBarFragment
 */
public abstract class AppbarFragment extends BottomActionBarFragment
        implements OnMenuItemClickListener {

    public static final String ARG_TITLE = "ToolbarFragment.title";
    protected Toolbar mToolbar;
    private AppbarFragmentHost mHost;
    private boolean mUpArrowEnabled;
    private TextView mTitleView;

    /**
     * Returns a newly created {@link Bundle} containing the given title as an argument.
     * If set as a ToolbarFragment's arguments bundle, this will be used to set up the title of
     * the Toolbar in {@link #setUpToolbar(View)}
     */
    protected static Bundle createArguments(CharSequence title) {
        Bundle args = new Bundle();
        args.putCharSequence(ARG_TITLE, title);
        return args;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mHost = (AppbarFragmentHost) context;
    }

    /**
     * Configures a toolbar in the given rootView, with id {@code toolbar} and sets its title to
     * the value in Arguments or {@link #getDefaultTitle()}.
     * Default upArrow value is true.
     */
    public void setUpToolbar(View rootView) {
        setUpToolbar(rootView, /* upArrow= */ true);
    }

    /**
     * Configures a toolbar in the given rootView, inflating the menu corresponding to the given id
     * for the toolbar menu.
     *
     * @param rootView given root view.
     * @param upArrow  true to enable up arrow feature.
     */
    protected void setUpToolbar(View rootView, boolean upArrow) {
        mUpArrowEnabled = upArrow;
        mToolbar = rootView.findViewById(getToolbarId());
        if (mToolbar.findViewById(R.id.custom_toolbar_title) != null) {
            mTitleView = mToolbar.findViewById(R.id.custom_toolbar_title);
        }
        CharSequence title;
        if (getArguments() != null) {
            title = getArguments().getCharSequence(ARG_TITLE, getDefaultTitle());
        } else {
            title = getDefaultTitle();
        }
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }

        if (upArrow && mHost.isUpArrowSupported()) {
            setUpUpArrow();
        }
    }

    /**
     * Configures the menu in the toolbar.
     *
     * @param menuResId the resource id of the menu
     */
    public void setUpToolbarMenu(@MenuRes int menuResId) {
        mToolbar.inflateMenu(menuResId);
        mToolbar.setOnMenuItemClickListener(this);
    }

    protected int getToolbarId() {
        return R.id.toolbar;
    }

    /**
     * Set up arrow feature status to enabled or not. Enable it for updating
     * onBottomActionBarReady() while initializing without toolbar setup.
     *
     * @param upArrow true to enable up arrow feature.
     */
    public void setUpArrowEnabled(boolean upArrow) {
        mUpArrowEnabled = upArrow;
    }

    private void setUpUpArrow() {
        Drawable backIcon = ContextCompat.getDrawable(requireContext(), R.drawable.material_ic_arrow_back_black_24).mutate();
        backIcon.setAutoMirrored(true);
        backIcon.setTint(
                ResourceUtils.getColorAttr(requireActivity(), android.R.attr.textColorPrimary));
        mToolbar.setNavigationIcon(backIcon);
        mToolbar.setNavigationContentDescription(R.string.bottom_action_bar_back);
        mToolbar.setNavigationOnClickListener(v -> mHost.onUpArrowPressed());
    }

    /**
     * Configures a toolbar in the given rootView, inflating the menu corresponding to the given id
     * for the toolbar menu.
     * Override {@link #onMenuItemClick(MenuItem)} to listen to item click events.
     *
     * @see #setUpToolbar(View)
     */
    public void setUpToolbar(View rootView, @MenuRes int menuResId) {
        setUpToolbar(rootView);
        setUpToolbarMenu(menuResId);
    }

    /**
     * Provides a title for this Fragment's toolbar to be used if none is found in
     * {@link #getArguments()}.
     * Default implementation returns {@code null}.
     */
    public CharSequence getDefaultTitle() {
        return null;
    }

    protected String getAccessibilityTitle() {
        return null;
    }

    protected void setTitle(CharSequence title) {
        if (mToolbar == null) {
            return;
        }
        if (mTitleView != null) {
            mToolbar.setTitle(null);
            mTitleView.setText(title);
        } else {
            mToolbar.setTitle(title);
        }

        // Set Activity title to make TalkBack announce title after updating toolbar title.
        if (getActivity() != null) {
            String accessibilityTitle = getAccessibilityTitle();
            getActivity().setTitle(TextUtils.isEmpty(accessibilityTitle) ? title
                    : accessibilityTitle);
        }
    }

    @Override
    protected void onBottomActionBarReady(BottomActionBar bottomActionBar) {
        bottomActionBar.setBackButtonVisibility(
                mUpArrowEnabled && mHost.isUpArrowSupported() ? GONE : VISIBLE);
        super.onBottomActionBarReady(bottomActionBar);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    /**
     * Interface to be implemented by an Activity hosting a {@link AppbarFragment}.
     */
    public interface AppbarFragmentHost {
        /**
         * Called when a up arrow had been pressed.
         */
        void onUpArrowPressed();

        /**
         * Check if it supports up arrow.
         */
        boolean isUpArrowSupported();
    }
}
