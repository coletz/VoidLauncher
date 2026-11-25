package dev.coletz.voidlauncher.models

import dev.coletz.voidlauncher.models.support.PersistableEnum
import kotlin.reflect.KClass

object Preference {
    @ConsistentCopyVisibility
    data class Info private constructor(
        val key: String,
        val name: String,
        val type: KClass<*>,
        val possibleValue: Any? = null,
        /** Raw value used when nothing has been stored yet. */
        val defaultValue: String? = null
    ) {
        companion object {
            fun int(id: String, name: String, defaultValue: Int, range: IntRange? = null): Info = Info(
                key = id,
                name = name,
                type = Int::class,
                possibleValue = range,
                defaultValue = defaultValue.toString()
            )

            fun bool(id: String, name: String, defaultValue: Boolean): Info = Info(
                key = id,
                name = name,
                type = Boolean::class,
                defaultValue = defaultValue.toString()
            )

            fun <T: PersistableEnum> enum(id: String, name: String, possibleValue: List<T>, defaultValue: T? = null): Info = Info(
                key = id,
                name = name,
                type = PersistableEnum::class,
                possibleValue = possibleValue.map { EnumOption(id = it.id, label = it.label) },
                defaultValue = defaultValue?.id
            )

            fun keyCombination(id: String, name: String): Info = Info(
                key = id,
                name = name,
                type = KeyCombinationType::class
            )
        }

        /** The allowed values of an int preference, or null if it is unbounded or not an int. */
        val intRange: IntRange?
            get() = possibleValue as? IntRange

        /** The choices of an enum preference, empty for any other type. */
        val enumOptions: List<EnumOption>
            get() = (possibleValue as? List<*>)?.filterIsInstance<EnumOption>().orEmpty()
    }

    data class EnumOption(val id: String, val label: String)

    object KeyCombinationType

    data class Entity(
        val info: Info,
        val rawValue: String?
    ) {
        /** The stored value, or the definition's default if nothing is stored. */
        val effectiveValue: String?
            get() = rawValue ?: info.defaultValue
    }
}