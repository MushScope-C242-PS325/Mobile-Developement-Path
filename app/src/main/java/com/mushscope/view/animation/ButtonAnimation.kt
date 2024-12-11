package com.mushscope.view.animation

import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation

fun animateButton(button: View) {
    val scaleAnimation = ScaleAnimation(
        1f, 0.9f, 1f, 0.9f, // Membesar
        Animation.RELATIVE_TO_SELF, 0.5f, // Titik pusat animasi horizontal
        Animation.RELATIVE_TO_SELF, 0.5f  // Titik pusat animasi vertikal
    ).apply {
        duration = 50 // Durasi animasi
        repeatCount = 0 // Tidak ada pengulangan animasi
        fillAfter = true // Agar animasi tetap pada ukuran akhir setelah selesai
    }

    scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
            // Bisa menambahkan logika lain jika perlu
        }

        override fun onAnimationEnd(animation: Animation?) {
            // Kembali ke ukuran normal setelah animasi selesai
            button.postDelayed({
                val resetAnimation = ScaleAnimation(
                    0.9f, 1f, 0.9f, 1f, // Kembali ke ukuran asli
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                ).apply {
                    duration = 50
                }
                button.startAnimation(resetAnimation)
            }, 50) // Memberi sedikit delay sebelum animasi kembali ke normal
        }

        override fun onAnimationRepeat(animation: Animation?) {
            // Tidak digunakan dalam kasus ini
        }
    })

    button.startAnimation(scaleAnimation)
}