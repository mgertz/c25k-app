package dk.michael.c25k.ui.activerun

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.michael.c25k.service.RunStepUi
import dk.michael.c25k.service.RunUiState
import dk.michael.c25k.ui.formatDuration
import dk.michael.c25k.ui.theme.CancelledRed
import dk.michael.c25k.ui.theme.CancelledRedText
import dk.michael.c25k.ui.theme.CompletedGreen

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

    Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
        Text("Aktivt løb", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Tid tilbage: ${formatCountdown(remainingSeconds(state))}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        LazyColumn(modifier = Modifier
            .weight(1f)
            .padding(vertical = 8.dp)) {
            itemsIndexed(state.steps) { index, step ->
                StepRow(step = step, index = index, state = state)
            }
        }
        OutlinedButton(onClick = { showStopDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Stop")
        }
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("Stop aktiviteten?") },
            text = {
                Text("Hvis du stopper nu, kan du ikke gå tilbage til denne aktive løbetur. Hold stop-knappen nede i 3 sekunder for at afslutte aktiviteten.")
            },
            confirmButton = {
                HoldToStopButton(
                    onConfirmed = {
                        showStopDialog = false
                        viewModel.stop()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                OutlinedButton(onClick = { showStopDialog = false }) {
                    Text("Fortsæt løbet")
                }
            }
        )
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
            .height(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
                .background(CancelledRed)
        )
        Text(
            text = "Hold nede for at stoppe",
            color = CancelledRedText,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun StepRow(step: RunStepUi, index: Int, state: RunUiState) {
    val completed = state.completedStepIndices.contains(index)
    val isCurrent = state.currentStepIndex == index
    val baseColor = if (completed) CompletedGreen else MaterialTheme.colorScheme.surfaceVariant
    val rowHeight = if (isCurrent) 48.dp else 30.dp
    val textStyle = if (isCurrent) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.labelSmall
    val label = if (isCurrent) {
        "${step.label} ${formatDuration(step.seconds)}"
    } else {
        "${index + 1}. ${step.label}"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .padding(vertical = if (isCurrent) 3.dp else 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(baseColor)
    ) {
        if (isCurrent) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(state.fillFraction(index).coerceIn(0f, 1f))
                    .background(CompletedGreen)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isCurrent) 12.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (completed) Text("✓")
        }
    }
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

private fun formatCountdown(seconds: Int): String {
    val minutes = seconds / 60
    val rest = seconds % 60
    return "%d:%02d".format(minutes, rest)
}
