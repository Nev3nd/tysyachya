package com.example.tysyacha_calc.data.local

import kotlinx.serialization.json.Json

/** Общий JSON для хранения конов и настроек: новые поля с дефолтами не ломают старые сохранения. */
val AppJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
