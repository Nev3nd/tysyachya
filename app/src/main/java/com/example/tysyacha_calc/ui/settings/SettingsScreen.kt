package com.example.tysyacha_calc.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tysyacha_calc.data.model.GameSettings
import com.example.tysyacha_calc.data.model.GameSetup
import com.example.tysyacha_calc.data.model.Suit
import com.example.tysyacha_calc.domain.GameRules
import com.example.tysyacha_calc.ui.components.SectionCard
import com.example.tysyacha_calc.ui.components.Stepper
import com.example.tysyacha_calc.ui.preview.NoSettingsActions
import com.example.tysyacha_calc.ui.preview.PreviewData
import com.example.tysyacha_calc.ui.preview.ScreenPreviews
import com.example.tysyacha_calc.ui.theme.TysyachacalcTheme
import com.example.tysyacha_calc.viewmodel.AppViewModelProvider
import com.example.tysyacha_calc.viewmodel.SettingsActions
import com.example.tysyacha_calc.viewmodel.SettingsViewModel

private const val MARRIAGE_STEP = 10

/** Экран «Настройки» — подготовка новой партии. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onGameStarted: (Long) -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val setup by viewModel.setup.collectAsStateWithLifecycle()
    val isStarting by viewModel.isStarting.collectAsStateWithLifecycle()
    SettingsContent(
        setup = setup,
        isStarting = isStarting,
        onBack = onBack,
        onStart = { viewModel.startGame(onGameStarted) },
        actions = viewModel,
    )
}

/** Вёрстка экрана без ViewModel — её видно в превью. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    setup: GameSetup?,
    isStarting: Boolean,
    onBack: () -> Unit,
    onStart: () -> Unit,
    actions: SettingsActions,
) {
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("Новая партия") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 3.dp) {
                Button(
                    onClick = onStart,
                    enabled = setup != null && !isStarting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                ) { Text("Начать партию", style = MaterialTheme.typography.titleMedium) }
            }
        },
    ) { padding ->
        val current = setup
        if (current == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlayersSection(current, actions)
            RulesSection(current.settings, actions)
            MarriagesSection(current.settings, actions)
            FixedRulesSection()
        }
    }
}

@Composable
private fun PlayersSection(setup: GameSetup, actions: SettingsActions) {
    val count = setup.settings.playerCount
    SectionCard(
        title = "Игроки",
        subtitle = "Порядок — как сидите за столом по ходу игры. Первым раздаёт первый.",
    ) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            listOf(3, 4).forEachIndexed { i, n ->
                SegmentedButton(
                    selected = count == n,
                    onClick = { actions.setPlayerCount(n) },
                    shape = SegmentedButtonDefaults.itemShape(i, 2),
                ) { Text("$n игрока") }
            }
        }
        if (count == 4) {
            Text(
                "Вчетвером раздающий не играет и записывает себе очки прикупа.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        repeat(count) { i ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(32.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${i + 1}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Spacer(Modifier.width(12.dp))
                OutlinedTextField(
                    value = setup.playerNames[i],
                    onValueChange = { actions.setName(i, it) },
                    placeholder = { Text("Игрок ${i + 1}") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = if (i < count - 1) ImeAction.Next else ImeAction.Done,
                    ),
                    modifier = Modifier.weight(1f),
                )
                Column {
                    IconButton(onClick = { actions.movePlayer(i, -1) }, enabled = i > 0, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Выше")
                    }
                    IconButton(onClick = { actions.movePlayer(i, 1) }, enabled = i < count - 1, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Ниже")
                    }
                }
            }
        }
    }
}

@Composable
private fun RulesSection(settings: GameSettings, actions: SettingsActions) {
    SectionCard(title = "Правила") {
        SwitchRow(
            "Уход в минус",
            if (settings.allowNegativeScore) "Счёт может быть отрицательным" else "Счёт не опускается ниже 0",
            settings.allowNegativeScore,
            actions::setAllowNegative,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        SwitchRow(
            "Сдвиг колоды",
            "Каждые 3 девятки — −120 сдвигавшему, 3 вальта — −120 раздающему",
            settings.cutRitualEnabled,
            actions::setCutRitual,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        SwitchRow("Правило 555", "Ровно +555 или −555 — сброс в 0", settings.use555Rule, actions::setUse555)
        SwitchRow(
            "Показывать приближение к 555",
            "Иначе — сюрприз, когда случится",
            settings.show555Progress,
            actions::setShow555Progress,
            enabled = settings.use555Rule,
        )
    }
}

@Composable
private fun MarriagesSection(settings: GameSettings, actions: SettingsActions) {
    val isDefault = settings.marriageValues == GameSettings.DEFAULT_MARRIAGES &&
        settings.aceMarriageValue == GameSettings.DEFAULT_ACE_MARRIAGE
    SectionCard(
        title = "Марьяжи",
        trailing = {
            if (!isDefault) TextButton(onClick = actions::resetMarriages) { Text("Сбросить") }
        },
    ) {
        Suit.entries.forEach { suit ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    suit.symbol,
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (suit.isRed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(32.dp),
                )
                Text(suit.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Stepper(
                    value = "${settings.marriageValue(suit)}",
                    onDecrement = { actions.changeMarriage(suit, -MARRIAGE_STEP) },
                    onIncrement = { actions.changeMarriage(suit, MARRIAGE_STEP) },
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("A×4", style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(48.dp))
            Text("Тузовый", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Stepper(
                value = "${settings.aceMarriageValue}",
                onDecrement = { actions.changeAceMarriage(-MARRIAGE_STEP) },
                onIncrement = { actions.changeAceMarriage(MARRIAGE_STEP) },
            )
        }
    }
}

@Composable
private fun FixedRulesSection() {
    SectionCard(title = "Всегда действуют", containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        listOf(
            "Первый после раздающего ставит ${GameRules.MIN_BID}, дальше +${GameRules.BID_STEP}",
            "Бочка ${GameRules.BOCHKA_SCORE}: ${GameRules.BOCHKA_ATTEMPTS} кона, чтобы набрать ${GameRules.BOCHKA_BREAK_POINTS}, иначе −${GameRules.PENALTY}",
            "${GameRules.DASH_LIMIT} болта за партию (0 очков за кон) — −${GameRules.PENALTY}",
            "Роспись — до ${GameRules.ROSPIS_LIMIT} раз на игрока за партию, остальным по половине ставки",
            "Победа — ${GameRules.WIN_SCORE} очков",
        ).forEach {
            Text("•  $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}

@ScreenPreviews
@Composable
private fun SettingsPreview() {
    TysyachacalcTheme {
        SettingsContent(PreviewData.setup, isStarting = false, onBack = {}, onStart = {}, actions = NoSettingsActions)
    }
}

@ScreenPreviews
@Composable
private fun SettingsFourPlayersPreview() {
    TysyachacalcTheme {
        SettingsContent(
            PreviewData.setup.copy(settings = PreviewData.setup.settings.copy(playerCount = 4, use555Rule = false)),
            isStarting = false, onBack = {}, onStart = {}, actions = NoSettingsActions,
        )
    }
}
