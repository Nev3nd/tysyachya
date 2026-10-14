package com.example.tysyacha_calc.viewmodel

import com.example.tysyacha_calc.data.model.Rank
import com.example.tysyacha_calc.data.model.Suit

/**
 * Всё, что экран калькулятора умеет попросить. Реализует [CalculatorViewModel];
 * превью подставляют пустую реализацию (`object : CalculatorActions {}`), поэтому у методов тела по умолчанию.
 */
interface CalculatorActions {
    fun setTaker(playerId: Int) {}
    fun changeBid(delta: Int) {}
    fun setBid(amount: Int) {}
    fun toggleRospis() {}
    fun toggleLong() {}
    fun changeNines(delta: Int) {}
    fun changeJacks(delta: Int) {}
    fun setPoints(playerId: Int, text: String) {}
    fun addCard(playerId: Int, rank: Rank) {}
    fun removeCard(playerId: Int, rank: Rank) {}
    fun toggleMarriage(playerId: Int, suit: Suit) {}
    fun toggleAceMarriage(playerId: Int) {}
    fun setPrikupPoints(text: String) {}
    fun addPrikupCard(rank: Rank) {}
    fun removePrikupCard(rank: Rank) {}
    fun resetRound() {}
    fun submit() {}
    fun stepBack() {}
    fun stepForward() {}
    fun moveToLatest() {}
}
