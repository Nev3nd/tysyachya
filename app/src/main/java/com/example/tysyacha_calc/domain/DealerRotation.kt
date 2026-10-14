package com.example.tysyacha_calc.domain

/**
 * Игроки сидят в порядке списка, ход идёт по возрастанию индекса.
 * Значит следующий раздающий — следующий по списку, а «игрок справа» от дилера — предыдущий.
 */
object DealerRotation {

    /** Раздающий в кону [roundNumber] (нумерация с 1). Первым раздаёт игрок с индексом 0. */
    fun dealerIndex(roundNumber: Int, playerCount: Int): Int {
        require(roundNumber >= 1)
        return (roundNumber - 1) % playerCount
    }

    /** Тот, кто сдвигает колоду, — игрок справа от раздающего. */
    fun cutterIndex(dealerIndex: Int, playerCount: Int): Int =
        (dealerIndex - 1 + playerCount) % playerCount

    /** Первый после раздающего — на нём автоматическая ставка 100. */
    fun firstBidderIndex(dealerIndex: Int, playerCount: Int): Int =
        (dealerIndex + 1) % playerCount

    /** Кто реально играет в кону: при 3 игроках — все, при 4 — все, кроме раздающего. */
    fun activeIndices(dealerIndex: Int, playerCount: Int): List<Int> =
        (0 until playerCount).filter { playerCount == 3 || it != dealerIndex }

    /** Есть ли у раздающего отдельная доля (режим наблюдателя). */
    fun dealerHasShare(playerCount: Int): Boolean = playerCount == 4
}
