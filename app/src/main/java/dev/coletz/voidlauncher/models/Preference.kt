package dev.coletz.voidlauncher.models

import dev.coletz.voidlauncher.models.support.PersistableEnum
import kotlin.reflect.KClass

object Preference {
    @ConsistentCopyVisibility
    data class Info private constructor(
        val key: String,
        val name: String,
        val type: KClass<*>,
        val possibleValue: Any? = null
    ) {
        companion object {
            fun int(id: String, name: String): Info = Info(
                key = id,
                name = name,
                type = Int::class
            )

            fun bool(id: String, name: String): Info = Info(
                key = id,
                name = name,
                type = Boolean::class
            )

            fun <T: PersistableEnum> enum(id: String, name: String, possibleValue: List<T>): Info = Info(
                key = id,
                name = name,
                type = PersistableEnum::class,
                possibleValue = possibleValue.map { it.id }.toList()
            )
        }
    }

    data class Entity(
        val info: Info,
        val rawValue: String?
    )
}