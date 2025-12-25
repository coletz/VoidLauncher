package dev.coletz.voidlauncher.models.support

import java.util.Locale

enum class VoiceSearchLanguage(override val id: String): PersistableEnum {
    ITALIAN(Locale.ITALY.toLanguageTag()),
    ENGLISH_US(Locale.US.toLanguageTag()),
    ENGLISH_UK(Locale.UK.toLanguageTag())
    ;

    companion object {
        fun getById(rawValue: String?): VoiceSearchLanguage? =
            entries.firstOrNull { it.id == rawValue }
    }
}