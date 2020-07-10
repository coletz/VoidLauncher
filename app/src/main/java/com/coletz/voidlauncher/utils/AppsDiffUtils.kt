package com.coletz.voidlauncher.utils

import androidx.recyclerview.widget.DiffUtil
import com.coletz.voidlauncher.models.AppObject

class AppsDiffUtils(private val oldList: List<AppObject>?, private val newList: List<AppObject>?): DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList?.size ?: 0

    override fun getNewListSize(): Int = newList?.size ?: 0

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // return true will invoke areContentsTheSame
        return true
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList?.get(oldItemPosition)
        val newItem = newList?.get(newItemPosition)

        // If both are null, true
        if(oldItem == null && newItem == null) return true

        // If only one is null, false
        if(oldItem == null || newItem == null) return false

        return oldItem.compareTo(newItem) == 0
    }

    companion object {
        fun calculateDiff(oldList: List<AppObject>?, newList: List<AppObject>?) = DiffUtil.calculateDiff(
            AppsDiffUtils(oldList, newList)
        )
    }
}