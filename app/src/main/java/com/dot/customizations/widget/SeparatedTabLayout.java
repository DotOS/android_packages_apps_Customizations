package com.dot.customizations.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dot.customizations.R;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;

/**
 * Custom {@link TabLayout} for separated tabs.
 *
 * <p>Don't use {@code TabLayoutMediator} for the tab layout, which binds the tab scrolling
 * animation that is unwanted for the separated tab design. Uses {@link
 * SeparatedTabLayout#setViewPager} to bind a {@link ViewPager2OSS} to use the proper tab effect.
 */
public final class SeparatedTabLayout extends TabLayout {

    public SeparatedTabLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    @NonNull
    public Tab newTab() {
        Tab tab = super.newTab();
        tab.view.setBackgroundResource(R.drawable.separated_tabs_ripple_mask);
        return tab;
    }

    /**
     * Binds the given {@code viewPager} to the {@link SeparatedTabLayout}.
     */
    public void setViewPager(ViewPager2OSS viewPager) {
        viewPager.registerOnPageChangeCallback(new SeparatedTabLayoutOnPageChangeCallback(this));
        addOnTabSelectedListener(new SeparatedTabLayoutOnTabSelectedListener(viewPager));
    }

    private static class SeparatedTabLayoutOnTabSelectedListener implements
            OnTabSelectedListener {
        private final WeakReference<ViewPager2OSS> mViewPagerRef;

        private SeparatedTabLayoutOnTabSelectedListener(ViewPager2OSS viewPager) {
            mViewPagerRef = new WeakReference<>(viewPager);
        }

        @Override
        public void onTabSelected(Tab tab) {
            ViewPager2OSS viewPager = mViewPagerRef.get();
            if (viewPager != null && viewPager.getCurrentItem() != tab.getPosition()) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        }

        @Override
        public void onTabUnselected(Tab tab) {
        }

        @Override
        public void onTabReselected(Tab tab) {
        }
    }

    private static class SeparatedTabLayoutOnPageChangeCallback extends ViewPager2OSS.OnPageChangeCallback {
        private final WeakReference<TabLayout> mTabLayoutRef;
        private int mPreviousScrollState = ViewPager2OSS.SCROLL_STATE_IDLE;
        private int mScrollState = ViewPager2OSS.SCROLL_STATE_IDLE;

        private SeparatedTabLayoutOnPageChangeCallback(TabLayout tabLayout) {
            mTabLayoutRef = new WeakReference<>(tabLayout);
        }

        @Override
        public void onPageSelected(final int position) {
            if (isUserDragging()) {
                // Don't update tab position here, wait for page scrolling done to update the tabs.
                return;
            }
            // ViewPager2#setCurrentItem would run into here.
            updateTabPositionIfNeeded(position);
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            mPreviousScrollState = mScrollState;
            mScrollState = state;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // Update the tab when the scrolling page is full displayed and is user dragging case.
            if (positionOffset == 0f && isUserDragging()) {
                updateTabPositionIfNeeded(position);
            }
        }

        private boolean isUserDragging() {
            return mPreviousScrollState == ViewPager2OSS.SCROLL_STATE_DRAGGING
                    && mScrollState == ViewPager2OSS.SCROLL_STATE_SETTLING;
        }

        private void updateTabPositionIfNeeded(int position) {
            TabLayout tabLayout = mTabLayoutRef.get();
            if (tabLayout != null
                    && tabLayout.getSelectedTabPosition() != position
                    && position < tabLayout.getTabCount()) {
                tabLayout.selectTab(tabLayout.getTabAt(position), /* updateIndicator= */ true);
            }
        }
    }
}
