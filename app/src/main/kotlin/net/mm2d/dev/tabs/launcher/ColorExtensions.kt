/*
 * Copyright (c) 2020 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dev.tabs.launcher

import android.graphics.Color

internal fun Int.contrastToWhiteForeground(): Float = calculateContrast(this, Color.WHITE)

internal fun Int.contrastToBlackForeground(): Float = calculateContrast(this, Color.BLACK)

// https://www.w3.org/TR/WCAG20/#contrast-ratiodef
private fun calculateContrast(
    color1: Int,
    color2: Int,
): Float {
    val l1 = Color.luminance(color1)
    val l2 = Color.luminance(color2)
    return if (l1 > l2) {
        (l1 + 0.05f) / (l2 + 0.05f)
    } else {
        (l2 + 0.05f) / (l1 + 0.05f)
    }
}
