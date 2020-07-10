package com.coletz.voidlauncher.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import com.coletz.voidlauncher.R
import com.coletz.voidlauncher.models.NotificationObject
import com.coletz.voidlauncher.utils.debug
import android.animation.LayoutTransition

private val LEFT_BLOCK_ID = View.generateViewId()
private val RIGHT_BLOCK_ID = View.generateViewId()
private val NOTCH_ID = View.generateViewId()

class NotchNotificationLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var notificationIds = hashSetOf<Int>()

    private var leftViewSizeCounter = 0
    private var rightViewSizeCounter = 0

    var rightPadding: Int = 0

    private val set = ConstraintSet()

    private val notch = View(context).apply {
        id = NOTCH_ID
    }
    private val leftBlock = LinearLayout(context).apply {
        id = LEFT_BLOCK_ID
        layoutTransition = LayoutTransition()
    }
    private val rightBlock = LinearLayout(context).apply {
        id = RIGHT_BLOCK_ID
        layoutTransition = LayoutTransition()
    }

    init {
        addView(leftBlock)
        addView(notch)
        addView(rightBlock)

        set.apply {
            // Notch view
            constrainWidth(NOTCH_ID, 108)
            constrainHeight(NOTCH_ID, 0)
            connect(NOTCH_ID, TOP, PARENT_ID, TOP)
            connect(NOTCH_ID, BOTTOM, PARENT_ID, BOTTOM)
            connect(NOTCH_ID, START, PARENT_ID, START)
            connect(NOTCH_ID, END, PARENT_ID, END)

            // Left block
            constrainWidth(LEFT_BLOCK_ID, 0)
            constrainHeight(LEFT_BLOCK_ID, 0)
            connect(LEFT_BLOCK_ID, TOP, PARENT_ID, TOP)
            connect(LEFT_BLOCK_ID, BOTTOM, PARENT_ID, BOTTOM)
            connect(LEFT_BLOCK_ID, START, PARENT_ID, START)
            connect(LEFT_BLOCK_ID, END, NOTCH_ID, START)

            // Right block
            constrainWidth(RIGHT_BLOCK_ID, 0)
            constrainHeight(RIGHT_BLOCK_ID, 0)
            connect(RIGHT_BLOCK_ID, TOP, PARENT_ID, TOP)
            connect(RIGHT_BLOCK_ID, BOTTOM, PARENT_ID, BOTTOM)
            connect(RIGHT_BLOCK_ID, START, NOTCH_ID, END)
            connect(RIGHT_BLOCK_ID, END, PARENT_ID, END)

        }.applyTo(this)
    }

    private fun addNotificationView(view: View){
        val viewSize = view.layoutParams.let { it as LinearLayout.LayoutParams }.let { it.width + it.marginStart + it.marginEnd }
        when {
            leftViewSizeCounter + viewSize < leftBlock.measuredWidth -> {
                // There's space on the left view, add here the view
                leftBlock.addView(view, 0)
                leftViewSizeCounter += viewSize
            }
            rightViewSizeCounter + viewSize + rightPadding < rightBlock.measuredWidth -> {
                // There's space on the left view, add here the view
                rightBlock.addView(view, 0)
                rightViewSizeCounter += viewSize
            }
            else -> {
                // No space left, add a + sign
            }
        }
    }

    private fun removeNotificationView(tag: Any?){
        leftBlock.findViewWithTag<ImageView>(tag)?.let {
            leftBlock.removeView(it)
            leftViewSizeCounter -= it.layoutParams.width
        } ?: rightBlock.findViewWithTag<ImageView>(tag)?.let {
            rightBlock.removeView(it)
            rightViewSizeCounter -= it.layoutParams.width
        } ?: "Nope".debug()
    }

    fun removeDiff(notifications: List<NotificationObject>){
        val newIds = notifications.map { it.id }
        notificationIds.filter { it !in newIds }.forEach(::removeNotificationView)
    }

    fun addNotification(notification: NotificationObject, icDrawable: Drawable?){
        if(notificationIds.add(notification.id)){
            // Notification is not present, add it
            val size = measuredHeight
            ImageView(context).apply {
                tag = notification.id
                layoutParams = LinearLayout.LayoutParams(size, size)
                setPadding(6, 0, 6, 0)
                icDrawable?.let {
                    setImageDrawable(it)
                } ?: run {
                    setImageResource(R.drawable.ic_broken_icon)
                }
            }.let(::addNotificationView)
        }
    }
}