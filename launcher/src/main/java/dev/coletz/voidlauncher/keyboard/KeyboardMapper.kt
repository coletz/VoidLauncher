package dev.coletz.voidlauncher.keyboard

fun interface KeyboardMapper {
    fun mapKeyCode(keyCode: Int): Int
}