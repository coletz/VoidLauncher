package dev.coletz.voidlauncher.utils

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.coletz.voidlauncher.core.R

/** False on e-ink flavors (see `R.bool.animations_enabled`). */
val Context.animationsEnabled: Boolean
    get() = resources.getBoolean(R.bool.animations_enabled)

/** Removes the default item add/remove/change animations when animations are disabled. */
fun RecyclerView.applyAnimationsSetting() {
    if (!context.animationsEnabled) itemAnimator = null
}

/**
 * Call after changing a view's state (e.g. checking a switch or checkbox): when animations are
 * disabled, it skips the state transition and draws the final state right away.
 */
fun View.skipStateAnimationIfDisabled() {
    if (!context.animationsEnabled) jumpDrawablesToCurrentState()
}
