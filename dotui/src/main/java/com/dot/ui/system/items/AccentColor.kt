package com.dot.ui.system.items

class AccentColor() {

    var title: String? = null

    constructor(title: String) : this() {
        this.title = title
    }

    var selected = false

    /**
     * Monet Accents
     */
    var isMonet = false
    var color: Int? = null
    var textColorBody: Int? = null
    var textColorTitle: Int? = null

    /**
     * System Accents
     */
    var colorLight: Int? = null
    var colorDark: Int? = null
    var packageName: String? = null
}