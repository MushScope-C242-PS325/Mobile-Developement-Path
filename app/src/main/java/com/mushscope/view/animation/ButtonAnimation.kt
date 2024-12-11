package com.mushscope.view.animation

import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation

fun animateButton(button: View) {
    val scaleAnimation = ScaleAnimation(
        1f, 0.9f, 1f, 0.9f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f
    ).apply {
        duration = 50
        repeatCount = 0
        fillAfter = true
    }

    scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
            // nothing
        }

        override fun onAnimationEnd(animation: Animation?) {
            button.postDelayed({
                val resetAnimation = ScaleAnimation(
                    0.9f, 1f, 0.9f, 1f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                ).apply {
                    duration = 50
                }
                button.startAnimation(resetAnimation)
            }, 50)
        }

        override fun onAnimationRepeat(animation: Animation?) {
            // nothing
        }
    })

    button.startAnimation(scaleAnimation)
}