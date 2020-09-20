package mmatijevic.ferit.hr.utils

import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import android.view.View
import kotlin.math.abs


class DepthPageTransformer : androidx.viewpager.widget.ViewPager.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        val pageWidth: Int = view.width
        view.apply {
            when {
                position < -1 -> {
                    alpha =
                        MIN_FADE
                }
                position < 0 -> {
                    alpha = 1 + position * (1 - MIN_FADE)
                    translationX = -pageWidth * MAX_SCALE * position
                    ViewCompat.setTranslationZ(view, position)
                    val scaleFactor = (MIN_SCALE
                            + (MAX_SCALE - MIN_SCALE) * (1 - abs(
                            position
                    )))
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                position == 0f -> {
                    alpha = 1F
                    translationX = 0F
                    scaleX =
                        MAX_SCALE
                    ViewCompat.setTranslationZ(view, 0f)
                    scaleY =
                        MAX_SCALE
                }
                position <= 1 -> {
                    ViewCompat.setTranslationZ(view, -position)
                    alpha = 1 - position * (1 - MIN_FADE)
                    translationX = pageWidth * MAX_SCALE * -position
                    val scaleFactor = (MIN_SCALE
                            + (MAX_SCALE - MIN_SCALE) * (1 - abs(
                            position
                    )))
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                else -> alpha =
                    MIN_FADE
            }

        }

    }

    companion object {
        private const val MIN_SCALE = 0.5f
        private const val MAX_SCALE = 0.8f
        private const val MIN_FADE = 0.2f
    }
}