package dev.coletz.voidlauncher.views

import java.util.Locale

abstract class MainListUiItem (
    val itemType: Type,
    val identifier: String,
    val uiName: String,
    val isFavorite: Boolean,
    val isHidden: Boolean,
    val tags: List<String>
): Comparable<MainListUiItem> {

    private val strippedName: String = uiName.replace("[^a-zA-Z0-9\\s]".toRegex(), "").trim().lowercase(Locale.getDefault())

    open fun areContentsTheSame(other: MainListUiItem): Boolean =
        itemType == other.itemType
                && uiName == other.uiName
                && isHidden == other.isHidden
                && isFavorite == other.isFavorite

    open fun areItemsTheSame(other: MainListUiItem): Boolean =
        itemType == other.itemType
                && identifier == other.identifier


    override fun compareTo(other: MainListUiItem): Int {
        if (itemType != other.itemType) return itemType.order - other.itemType.order
        if (identifier == other.identifier) return 0
        if (this.isHidden) return 1
        if (other.isHidden) return -1
        if (this.isFavorite && !other.isFavorite) return -1
        if (!this.isFavorite && other.isFavorite) return 1
        return strippedName.compareTo(other.strippedName)
    }

    enum class Type (val order: Int) {
        FOLDER(0),
        APP(1);
    }
}