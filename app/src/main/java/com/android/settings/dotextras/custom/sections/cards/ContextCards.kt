package com.android.settings.dotextras.custom.sections.cards

class ContextCards(
    var iconID: Int,
    var title: String,
    var subtitle: String,
    var accentColor: Int,
    val feature: String,
    val featureType: Int
) {

    //Common variables
    var summary: String? = null

    //Switch variables
    var isCardChecked: Boolean = false

    //Swipe variables
    var extraTitle: String? = null
    var value: Int = -1
    var max: Int = -1
    var min: Int = -1
    var default: Int = -1

    constructor(
        iconID: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        summary: String
    ) : this(iconID, title, subtitle, accentColor, feature, featureType) {
        this.summary = summary
    }

    constructor(
        iconID: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        min: Int,
        max: Int,
        default: Int
    ) : this(iconID, title, subtitle, accentColor, feature, featureType) {
        this.min = min
        this.max = max
        this.default = default
    }

    constructor(
        iconID: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        min: Int,
        max: Int,
        default: Int,
        summary: String
    ) : this(iconID, title, subtitle, accentColor, feature, featureType) {
        this.min = min
        this.max = max
        this.default = default
        this.summary = summary
    }

    constructor(
        iconID: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        min: Int,
        max: Int,
        default: Int,
        summary: String,
        extraTitle: String
    ) : this(iconID, title, subtitle, accentColor, feature, featureType) {
        this.min = min
        this.max = max
        this.default = default
        this.summary = summary
        this.extraTitle = extraTitle
    }

}