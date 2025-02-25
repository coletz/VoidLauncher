package dev.coletz.voidlauncher.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View,
                                parent: RecyclerView, state: RecyclerView.State) {
        outRect.apply {
            if (parent.getChildAdapterPosition(view) == 0) {
                top = spaceHeight
            }
            left =  0
            right = 0
            bottom = spaceHeight
        }
    }
}