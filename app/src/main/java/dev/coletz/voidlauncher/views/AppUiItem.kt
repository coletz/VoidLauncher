package dev.coletz.voidlauncher.views

class AppUiItem (
    uiIdentifier: String,
    uiName: String,
    isFavorite: Boolean,
    isHidden: Boolean,
    tags: List<String>
): MainListUiItem(
    Type.APP,
    uiIdentifier,
    uiName,
    isFavorite,
    isHidden,
    tags
)