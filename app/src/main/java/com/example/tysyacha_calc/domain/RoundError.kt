package com.example.tysyacha_calc.domain

import com.example.tysyacha_calc.data.model.Rank
import com.example.tysyacha_calc.data.model.Suit

/** Почему кон нельзя записать. [playerId] — к какому игроку относится ошибка, если к конкретному. */
sealed interface RoundError {
    val playerId: Int? get() = null

    /** Текст для пользователя; [nameOf] подставляет имя игрока по id. */
    fun message(nameOf: (Int) -> String): String

    data object GameFinished : RoundError {
        override fun message(nameOf: (Int) -> String) = "Партия уже закончена"
    }
    data class MissingEntry(override val playerId: Int) : RoundError {
        override fun message(nameOf: (Int) -> String) = "Нет очков у игрока ${nameOf(playerId)}"
    }
    data class UnexpectedEntry(override val playerId: Int) : RoundError {
        override fun message(nameOf: (Int) -> String) = "${nameOf(playerId)} не играет в этом кону"
    }
    data class CardPointsOutOfRange(override val playerId: Int, val points: Int) : RoundError {
        override fun message(nameOf: (Int) -> String) =
            "${nameOf(playerId)}: очков картами может быть от 0 до ${GameRules.CARD_POINTS_TOTAL}"
    }
    data class CardPointsSum(val sum: Int) : RoundError {
        override fun message(nameOf: (Int) -> String) =
            "Сумма очков картами должна быть ${GameRules.CARD_POINTS_TOTAL}, сейчас $sum"
    }
    data class CardsMismatch(override val playerId: Int) : RoundError {
        override fun message(nameOf: (Int) -> String) = "${nameOf(playerId)}: очки не совпадают с выбранными картами"
    }
    data class CardLimitExceeded(val rank: Rank) : RoundError {
        override fun message(nameOf: (Int) -> String) =
            "Карт «${rank.label}» выбрано больше ${Rank.COPIES}"
    }
    data class DuplicateMarriage(val suit: Suit) : RoundError {
        override fun message(nameOf: (Int) -> String) = "Марьяж ${suit.symbol} объявлен дважды"
    }
    data object DuplicateAceMarriage : RoundError {
        override fun message(nameOf: (Int) -> String) = "Тузовый марьяж может быть только у одного"
    }
    data class BidTooLow(val amount: Int) : RoundError {
        override fun message(nameOf: (Int) -> String) = "Ставка не меньше ${GameRules.MIN_BID}"
    }
    data class BidNotMultipleOf5(val amount: Int) : RoundError {
        override fun message(nameOf: (Int) -> String) = "Ставка должна быть кратна ${GameRules.BID_STEP}"
    }
    data class TakerNotActive(override val playerId: Int) : RoundError {
        override fun message(nameOf: (Int) -> String) = "${nameOf(playerId)} не может взять прикуп в этом кону"
    }
    data object PrikupNotAllowed : RoundError {
        override fun message(nameOf: (Int) -> String) = "Очки прикупа бывают только при 4 игроках"
    }
    data object InvalidPrikup : RoundError {
        override fun message(nameOf: (Int) -> String) =
            "В прикупе ${GameRules.PRIKUP_CARDS} карты, максимум ${PrikupHandler.maxPoints} очка"
    }
    data object CutRitualDisabled : RoundError {
        override fun message(nameOf: (Int) -> String) = "Правило сдвига выключено"
    }
    data object InvalidCutCount : RoundError {
        override fun message(nameOf: (Int) -> String) = "Количество карт при сдвиге не может быть отрицательным"
    }
    data object RospisLimitReached : RoundError {
        override fun message(nameOf: (Int) -> String) = "У этого игрока все ${GameRules.ROSPIS_LIMIT} росписи уже использованы"
    }
    data object InvalidLong : RoundError {
        override fun message(nameOf: (Int) -> String) =
            "Длинная: у взявшего прикуп все ${GameRules.CARD_POINTS_TOTAL}, у остальных ничего"
    }
    data object RospisWithEntries : RoundError {
        override fun message(nameOf: (Int) -> String) = "При росписи кон не разыгрывается"
    }
}
