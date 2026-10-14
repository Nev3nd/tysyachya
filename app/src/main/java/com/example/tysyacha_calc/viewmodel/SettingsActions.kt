package com.example.tysyacha_calc.viewmodel

import com.example.tysyacha_calc.data.model.Suit

/** Действия экрана новой партии. Реализует [SettingsViewModel]; превью — пустой объект. */
interface SettingsActions {
    fun setPlayerCount(count: Int) {}
    fun setName(index: Int, name: String) {}
    fun movePlayer(index: Int, delta: Int) {}
    fun setAllowNegative(value: Boolean) {}
    fun setCutRitual(value: Boolean) {}
    fun setUse555(value: Boolean) {}
    fun setShow555Progress(value: Boolean) {}
    fun changeMarriage(suit: Suit, delta: Int) {}
    fun changeAceMarriage(delta: Int) {}
    fun resetMarriages() {}
}
