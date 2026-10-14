package com.example.tysyacha_calc.domain

/** Захардкоженные правила компании. */
object GameRules {
    const val PENALTY = 120

    const val SCORE_STEP = 5
    const val WIN_SCORE = 1000

    /** Сумма очков всех карт колоды: (0 + 10 + 2 + 3 + 4 + 11) × 4. */
    const val CARD_POINTS_TOTAL = 120

    /** Первый после раздающего автоматически ставит 100. */
    const val MIN_BID = 100
    const val BID_STEP = 5
    /** Больше этого без марьяжа не ставят — картами больше не набрать. */
    const val MAX_BID_WITHOUT_MARRIAGE = CARD_POINTS_TOTAL

    const val PRIKUP_CARDS = 3

    const val RESET_SCORE = 555

    const val BOCHKA_SCORE = 880
    const val BOCHKA_ATTEMPTS = 3
    /** Сколько нужно набрать за один кон, стоя на бочке, чтобы её пробить. */
    const val BOCHKA_BREAK_POINTS = WIN_SCORE - BOCHKA_SCORE

    const val DASH_LIMIT = 3
    /** Росписей на каждого игрока за партию. */
    const val ROSPIS_LIMIT = 3
    const val CUT_CARDS_LIMIT = 3
}
