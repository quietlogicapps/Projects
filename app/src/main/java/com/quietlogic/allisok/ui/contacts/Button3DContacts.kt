package com.quietlogic.allisok.ui.contacts

import android.content.res.ColorStateList
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.util.TypedValue
import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

enum class CutCorner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

object Button3DContacts {

    fun apply(button: MaterialButton, cornerDp: Float, cutCorner: CutCorner, depthDp: Float = 6f) {
        val faceColor = button.backgroundTintList ?: ColorStateList.valueOf(resolvePrimaryColor(button))
        val cornerPx = dp(button, cornerDp)
        val cutPx = dp(button, 28f)
        val depthPx = dp(button, depthDp).toInt()

        val shapeModel = ShapeAppearanceModel.builder()
            .setTopLeftCorner(if (cutCorner == CutCorner.TOP_LEFT) CornerFamily.CUT else CornerFamily.ROUNDED, if (cutCorner == CutCorner.TOP_LEFT) cutPx else cornerPx)
            .setTopRightCorner(if (cutCorner == CutCorner.TOP_RIGHT) CornerFamily.CUT else CornerFamily.ROUNDED, if (cutCorner == CutCorner.TOP_RIGHT) cutPx else cornerPx)
            .setBottomLeftCorner(if (cutCorner == CutCorner.BOTTOM_LEFT) CornerFamily.CUT else CornerFamily.ROUNDED, if (cutCorner == CutCorner.BOTTOM_LEFT) cutPx else cornerPx)
            .setBottomRightCorner(if (cutCorner == CutCorner.BOTTOM_RIGHT) CornerFamily.CUT else CornerFamily.ROUNDED, if (cutCorner == CutCorner.BOTTOM_RIGHT) cutPx else cornerPx)
            .build()

        fun depthDrawable(alphaHex: Int): MaterialShapeDrawable {
            return MaterialShapeDrawable(shapeModel).apply {
                fillColor = ColorStateList.valueOf((alphaHex shl 24) or 0x000000)
            }
        }

        fun faceDrawable(): MaterialShapeDrawable {
            return MaterialShapeDrawable(shapeModel).apply {
                fillColor = faceColor
            }
        }

        fun layer(normal: Boolean): LayerDrawable {
            val depth = depthDrawable(if (normal) 0x3A else 0x33)
            val face = faceDrawable()
            return LayerDrawable(arrayOf(depth, face)).apply {
                setLayerInset(0, depthPx, depthPx, 0, 0)
                if (normal) {
                    setLayerInset(1, 0, 0, depthPx, depthPx)
                } else {
                    setLayerInset(1, depthPx, depthPx, 0, 0)
                }
            }
        }

        val states = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), layer(normal = false))
            addState(intArrayOf(), layer(normal = true))
        }

        button.backgroundTintList = null
        button.setBackground(states)
        button.stateListAnimator = null
    }

    private fun dp(button: MaterialButton, dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, button.resources.displayMetrics)

    private fun resolvePrimaryColor(button: MaterialButton): Int {
        val tv = TypedValue()
        val found = button.context.theme.resolveAttribute(
            com.google.android.material.R.attr.colorPrimary, tv, true
        )
        return if (found) tv.data else 0xFF000000.toInt()
    }
}
