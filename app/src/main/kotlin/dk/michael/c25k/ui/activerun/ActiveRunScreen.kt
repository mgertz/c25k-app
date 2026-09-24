package dk.michael.c25k.ui.activerun

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.michael.c25k.service.RunStepUi
import dk.michael.c25k.service.RunUiState
import dk.michael.c25k.ui.formatClock
import dk.michael.c25k.ui.formatDuration
import dk.michael.c25k.ui.theme.C25KPalette
import dk.michael.c25k.ui.theme.CancelledRed
import dk.michael.c25k.ui.theme.CompletedGreen
import dk.michael.c25k.ui.theme.CompletedGreenText

@Composable
fun ActiveRunScreen(programIndex: Int, onFinished: (Long) -> Unit) {
    val viewModel: ActiveRunViewModel = viewModel()
    LaunchedEffect(programIndex) { viewModel.start(programIndex) }

    val state by viewModel.state.collectAsState()
    val savedSessionId by viewModel.savedSessionId.collectAsState()
    var showStopDialog by remember { mutableStateOf(false) }

    LaunchedEffect(savedSessionId) {
        savedSessionId?.let { onFinished(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(C25KPalette.FjordLight, C25KPalette.Fjord, C25KPalette.FjordDeep)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar(onStop = { showStopDialog = true })

            val currentIndex = state.currentStepIndex.coerceAtLeast(0)
            val currentStep = state.steps.getOrNull(currentIndex)
            val currentRemaining = currentStepRemainingSeconds(state)
            val totalRemaining = remainingSeconds(state)
            val nextStep = state.steps.getOrNull(state.currentStepIndex + 1)
            val progress by animateFloatAsState(
                targetValue = state.fillFraction(state.currentStepIndex).coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 350),
                label = "activeStepProgress"
            )

            TimerDial(
                label = currentStep?.label ?: "Klar",
                currentTime = formatClock(currentRemaining),
                totalTime = formatClock(totalRemaining),
                progress = progress,
                modifier = Modifier.padding(top = 20.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RunStat("Færdige", state.completedStepIndices.size.toString(), Modifier.weight(1f))
                RunStat("Tilbage", stepsLeft(state).toString(), Modifier.weight(1f))
                RunStat("Næste", nextStep?.let { "${it.label} ${formatDuration(it.seconds)}" } ?: "Mål", Modifier.weight(1f))
            }

            Text(
                text = "Intervaller",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(state.steps) { index, step ->
                    SegmentRow(step = step, index = index, state = state)
                }
            }
        }
    }

    if (showStopDialog) {
        StopRunDialog(
            onDismiss = { showStopDialog = false },
            onConfirmed = {
                showStopDialog = false
                viewModel.stop()
            }
        )
    }
}

@Composable
private fun StopRunDialog(onDismiss: () -> Unit, onConfirmed: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(C25KPalette.Fjord, C25KPalette.FjordDeep)
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(30.dp))
                .padding(22.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("!", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Stop aktiviteten?",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "Hvis du stopper nu, kan du ikke gå tilbage til denne aktive løbetur. Hold knappen nede i 3 sekunder for at afslutte.",
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp)
                )
                HoldToStopButton(
                    onConfirmed = onConfirmed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp)
                )
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text("Fortsæt løbet")
                }
            }
        }
    }
}

@Composable
private fun TopBar(onStop: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "WALK & JOG",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Hold rytmen. Følg timeren.",
                color = Color.White.copy(alpha = 0.76f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Button(
            onClick = onStop,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.18f),
                contentColor = Color.White
            )
        ) {
            Text("STOP")
        }
    }
}

@Composable
private fun TimerDial(
    label: String,
    currentTime: String,
    totalTime: String,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(270.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            drawCircle(
                color = Color.White.copy(alpha = 0.20f),
                radius = size.minDimension / 2f - inset,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(C25KPalette.FjordLight, C25KPalette.Accent, C25KPalette.FjordLight),
                    center = Offset(size.width / 2f, size.height / 2f)
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = currentTime,
                color = Color.White,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tid tilbage i alt · $totalTime",
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RunStat(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(88.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.13f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.64f),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SegmentRow(step: RunStepUi, index: Int, state: RunUiState) {
    val completed = state.completedStepIndices.contains(index)
    val isCurrent = state.currentStepIndex == index
    val fill = state.fillFraction(index).coerceIn(0f, 1f)
    val background = when {
        isCurrent -> Color.White.copy(alpha = 0.23f)
        completed -> CompletedGreen.copy(alpha = 0.92f)
        else -> Color.White.copy(alpha = 0.12f)
    }
    val foreground = if (completed) CompletedGreenText else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isCurrent) 54.dp else 38.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
    ) {
        if (isCurrent) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fill)
                    .background(C25KPalette.Accent.copy(alpha = 0.78f))
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = (index + 1).toString(),
                    color = foreground.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text(
                    text = if (isCurrent) "${step.label} · ${formatDuration(step.seconds)}" else step.label,
                    color = foreground,
                    style = if (isCurrent) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.labelLarge,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = if (completed) "✓" else formatDuration(step.seconds),
                color = foreground,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun HoldToStopButton(onConfirmed: () -> Unit, modifier: Modifier = Modifier) {
    var isHolding by remember { mutableStateOf(false) }
    var hasConfirmed by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (isHolding) 1f else 0f,
        animationSpec = tween(durationMillis = if (isHolding) 3000 else 150),
        label = "stopProgress",
        finishedListener = { value ->
            if (value >= 1f && isHolding && !hasConfirmed) {
                hasConfirmed = true
                onConfirmed()
            }
        }
    )

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF4C1518))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isHolding = true
                    waitForUpOrCancellation()
                    if (!hasConfirmed) {
                        isHolding = false
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .align(Alignment.CenterStart)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFF5B5B), CancelledRed)
                    )
                )
        )
        Text(
            text = if (isHolding) "Bliv ved..." else "Hold 3 sekunder for at stoppe",
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private fun currentStepRemainingSeconds(state: RunUiState): Int {
    val step = state.steps.getOrNull(state.currentStepIndex) ?: return 0
    return (step.seconds - state.elapsedInStepSeconds).coerceAtLeast(0)
}

private fun remainingSeconds(state: RunUiState): Int {
    var remaining = 0
    state.steps.forEachIndexed { index, step ->
        when {
            state.completedStepIndices.contains(index) -> Unit
            index == state.currentStepIndex -> remaining += (step.seconds - state.elapsedInStepSeconds).coerceAtLeast(0)
            index > state.currentStepIndex -> remaining += step.seconds
        }
    }
    return remaining
}

private fun stepsLeft(state: RunUiState): Int =
    state.steps.countIndexed { index, _ -> index !in state.completedStepIndices }

private inline fun <T> List<T>.countIndexed(predicate: (Int, T) -> Boolean): Int {
    var count = 0
    forEachIndexed { index, value -> if (predicate(index, value)) count++ }
    return count
}
