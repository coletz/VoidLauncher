package dev.coletz.voidlauncher.views

class FolderUiItem (
    val folderId: Long,
    uiName: String,
    val apps: List<AppUiItem>,
    val isExpanded: Boolean
): MainListUiItem(
    Type.FOLDER,
    folderId.toString(),
    uiName,
    false,
    false,
    listOf()
) {
    override fun areContentsTheSame(other: MainListUiItem): Boolean =
        super.areContentsTheSame(other) && isExpanded == (other as FolderUiItem).isExpanded
}