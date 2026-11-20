package dev.coletz.voidlauncher.keyboard

open class CustomKeyManager {

    open fun getCustomKeys(): List<CustomKey> {
        return listOf()
    }

    fun getCustomKeyByCode(keyPrimaryCode: Int): CustomKey {
        val codes = getCustomKeys()
        val code = codes.first { it.keyCode == keyPrimaryCode }
        return code
    }
}