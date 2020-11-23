package com.cemmmmer.primarycolor

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.palette.graphics.Palette

import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

class PrimaryColor {
    private val DEBUG = true
    private val mBaseColorComparator = BaseColorComparator()

    class BaseColor(
        var red: Int, var green: Int, var blue: Int, var h: Float, var s: Float,
        var v: Float, var rgba: Int, var count: Int
    ) {
        var type: Int = 0
        var isAssigned: Boolean = false

        init {
            this.type = if (v > 0.5f) COLOR_TYPE_LIGHTEN else COLOR_TYPE_DARKEN
            this.isAssigned = false
        }

        companion object {
            val COLOR_TYPE_DARKEN: Int = 0
            val COLOR_TYPE_LIGHTEN: Int = 1
        }
    }

    fun generate(bitmap: Bitmap?): Int {
        var primaryColor = Color.BLACK
        var startTime: Long = 0
        var endTime: Long = 0

        if (bitmap != null) {
            val colorPalette = Palette.generate(bitmap, MAX_COLOR_NUMS)
            val swatches = colorPalette.swatches

            if (DEBUG) {
                startTime = System.currentTimeMillis()
            }
            val hsv = FloatArray(3)
            var r = 0
            var g = 0
            var b = 0
            var rgba = 0
            val initColorsSets = ArrayList<BaseColor>()
            val colorSets = ArrayList<BaseColor>()
            var tempColorI: BaseColor? = null
            var tempColorJ: BaseColor? = null

            for (swatch in swatches) {
                rgba = swatch.getRgb()
                r = Color.red(rgba)
                g = Color.green(rgba)
                b = Color.blue(rgba)
                Color.RGBToHSV(r, g, b, hsv)
                if (hsv[1] < 0.1f && hsv[2] > 0.9f) {
                    continue
                } else if (hsv[2] < 0.25f) {
                    continue
                }
                initColorsSets.add(
                    BaseColor(
                        r, g, b, hsv[0], hsv[1],
                        hsv[2], rgba, swatch.population
                    )
                )
            }
            Collections.sort(initColorsSets, mBaseColorComparator)

            for (i in initColorsSets.indices) {
                tempColorI = initColorsSets[i]
                if (!tempColorI.isAssigned) {
                    tempColorI.isAssigned = true
                    for (j in i + 1 until initColorsSets.size) {
                        tempColorJ = initColorsSets[j]
                        if (!tempColorJ.isAssigned) {
                            if (Math.abs(tempColorI.h - tempColorJ.h) < 20.0f) {
                                tempColorJ.isAssigned = true
                                tempColorI.count += tempColorJ.count
                            }
                        }
                    }
                    colorSets.add(tempColorI)
                }
            }
            if (colorSets.size > 0) {
                var bestColor = colorSets[0]
                if (bestColor.type == BaseColor.COLOR_TYPE_LIGHTEN) {
                    for (i in 1 until colorSets.size) {
                        if (bestColor.count < 2 * colorSets[i].count) {
                            bestColor = colorSets[i]
                            break
                        }
                    }
                }
                if (bestColor.type == BaseColor.COLOR_TYPE_LIGHTEN) {
                    bestColor.v -= 0.16f
                    hsv[0] = bestColor.h
                    hsv[1] = clamp(bestColor.s, 0.0f, 0.8f)
                    hsv[2] = clamp(bestColor.v, 0.3f, 0.85f)
                    primaryColor = Color.HSVToColor(hsv)
                } else {
                    primaryColor = bestColor.rgba
                }
            }
            initColorsSets.clear()
            colorSets.clear()
            if (DEBUG) {
                endTime = System.currentTimeMillis()
                Log.e(TAG, "spend time:" + (endTime - startTime) + " ms")
            }
        }
        return primaryColor
    }

    private inner class BaseColorComparator : Comparator<BaseColor> {
        override fun compare(a: BaseColor, b: BaseColor): Int {
            return b.count - a.count
        }
    }

    companion object {

        private const  val TAG = "PrimaryColor"

        private const val MAX_COLOR_NUMS = 38

        private fun clamp(value: Float, min: Float, max: Float): Float {
            return if (value < min) min else if (value > max) max else value
        }
    }
}
