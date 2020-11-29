package com.android.settings.dotextras.custom.sections.clock

import android.widget.ImageView

class Clockface(val title: String, val id: String, val preview: Asset, val thumbnail: Asset) {

    fun bindThumbnail(imageView: ImageView) {
        thumbnail.loadDrawableWithTransition(
            imageView.context, imageView, 50, null,
            imageView.resources.getColor(android.R.color.transparent, null)
        )
    }
}