package com.coletz.voidlauncher.models

object Preference {
    data class Key(
        val id: String,
        val name: String,
        val type: Type
    )

    fun int(id: String, name: String,): Key = Key(id, name, Type.INTEGER)

    data class Entity(
        val key: Key,
        val value: Any?
    )

    enum class Type {
        INTEGER
    }
}