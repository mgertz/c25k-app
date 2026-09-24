package dk.michael.c25k.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.michael.c25k.data.RunSuggestion
import dk.michael.c25k.data.db.RunOutcome
import dk.michael.c25k.data.model.Program
import dk.michael.c25k.ui.formatClock
import dk.michael.c25k.ui.navigation.BottomTab
import dk.michael.c25k.ui.navigation.C25KBottomBar
import dk.michael.c25k.ui.theme.C25KPalette
import dk.michael.c25k.ui.runSeconds
import dk.michael.c25k.ui.theme.CancelledRed
import dk.michael.c25k.ui.theme.CancelledRedText
import dk.michael.c25k.ui.theme.CompletedGreen
import dk.michael.c25k.ui.theme.CompletedGreenText
import dk.michael.c25k.ui.totalSeconds
import dk.michael.c25k.ui.walkSeconds

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Home2Screen(
    onOpenWorkout: (Int) -> Unit,
    onOpenHistory: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel()
    val sessions by viewModel.sessions.collectAsState()
    val programs = viewModel.programs

    val latestOutcomeByIndex = remember(sessions) {
        val map = mutableMapOf<Int, RunOutcome>()
        for (session in sessions) map.putIfAbsent(session.programIndex, session.outcome)
        map
    }
    val suggestedIndex = remember(sessions, programs) {
        RunSuggestion.next(sessions, programs.lastIndex.coerceAtLeast(0))
    }
    val suggestedPage = programs.indexOfFirst { it.index == suggestedIndex }.takeIf { it >= 0 } ?: 0
    val pagerState = rememberPagerState(initialPage = suggestedPage, pageCount = { programs.size })

    LaunchedEffect(programs.size, suggestedPage) {
        if (programs.isNotEmpty() && pagerState.currentPage != suggestedPage) {
            pagerState.scrollToPage(suggestedPage)
        }
    }

    Scaffold(
        bottomBar = {
            C25KBottomBar(
                selected = BottomTab.ACTIVITY,
                onActivity = {},
                onHistory = onOpenHistory
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            C25KPalette.FjordLight,
                            C25KPalette.Fjord,
                            C25KPalette.FjordDeep
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 28.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Din Couch to 5K progress",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Swipe mellem dine workouts",
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )

                ProgressDots(
                    count = programs.size,
                    current = pagerState.currentPage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 18.dp)
                )

                if (programs.isEmpty()) {
                    Text("Ingen workouts fundet", color = Color.White)
                    return@Column
                }

                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 34.dp),
                    pageSpacing = 14.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(330.dp)
                ) { page ->
                    val program = programs[page]
                    WorkoutCard(
                        program = program,
                        outcome = latestOutcomeByIndex[program.index],
                        suggested = program.index == suggestedIndex,
                        onOpenWorkout = { onOpenWorkout(program.index) }
                    )
                }

                val selectedProgram = programs.getOrNull(pagerState.currentPage)
                selectedProgram?.let { program ->
                    Text(
                        text = "Uge ${program.week}: hold fast i rytmen. Dagens fokus er rolig start, sikre intervaller og en god afslutning.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 18.dp)
                    )
                    Button(
                        onClick = { onOpenWorkout(program.index) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 42.dp)
                            .height(52.dp)
                    ) {
                        Text("Gå til workout")
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    program: Program,
    outcome: RunOutcome?,
    suggested: Boolean,
    onOpenWorkout: () -> Unit
) {
    Card(
        onClick = onOpenWorkout,
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = C25KPalette.Surface),
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Uge ${program.week} · dag ${program.day}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                StatusChip(outcome = outcome, suggested = suggested)
            }

            Text(
                text = formatClock(program.totalSeconds()),
                color = C25KPalette.Fjord,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 18.dp)
            )
            Text("total tid", style = MaterialTheme.typography.labelMedium, color = C25KPalette.TextSecondary)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WorkoutMetric("Opvarmning", formatClock(program.warmupSeconds))
                WorkoutArrow()
                WorkoutMetric("Løb", formatClock(program.runSeconds()))
                WorkoutMetric("Gå", formatClock(program.walkSeconds()))
                WorkoutArrow()
                WorkoutMetric("Nedkøling", formatClock(program.cooldownSeconds))
            }

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${program.intervals.size} intervaller · tryk for detaljer",
                style = MaterialTheme.typography.bodySmall,
                color = C25KPalette.TextSecondary
            )
        }
    }
}

@Composable
private fun StatusChip(outcome: RunOutcome?, suggested: Boolean) {
    val (text, bg, fg) = when (outcome) {
        RunOutcome.COMPLETED -> Triple("Gennemført", CompletedGreen, CompletedGreenText)
        RunOutcome.CANCELLED -> Triple("Afbrudt", CancelledRed, CancelledRedText)
        null -> if (suggested) Triple("Næste", C25KPalette.Accent, C25KPalette.AccentText)
            else Triple("Planlagt", C25KPalette.SurfaceTint, C25KPalette.TextSecondary)
    }
    Text(
        text = text,
        color = fg,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
private fun WorkoutMetric(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(C25KPalette.SurfaceTint),
            contentAlignment = Alignment.Center
        ) {
            Text(title.take(1), color = C25KPalette.Fjord, fontWeight = FontWeight.Bold)
        }
        Text(title, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 6.dp))
        Text(value, style = MaterialTheme.typography.labelSmall, color = C25KPalette.TextMuted)
    }
}

@Composable
private fun WorkoutArrow() {
    Text(
        text = "-",
        color = C25KPalette.TextMuted,
        modifier = Modifier.padding(top = 9.dp)
    )
}

@Composable
private fun ProgressDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (index == current) 7.dp else 5.dp)
                    .padding(horizontal = 1.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (index <= current) C25KPalette.Accent else Color.White.copy(alpha = 0.28f))
            )
        }
    }
}
