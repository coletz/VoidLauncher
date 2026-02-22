package dev.coletz.voidlauncher.models.support

enum class SortingDirection(override val id: String): PersistableEnum {
    ASCENDING("asc"),
    DESCENDING("desc")
    ;

    companion object {
        fun getById(rawValue: String?): SortingDirection? =
            entries.firstOrNull { it.id == rawValue }
    }
}
