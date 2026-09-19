package dk.michael.c25k.ui.activerun

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.michael.c25k.service.RunStepUi
import dk.michael.c25k.service.RunUiState
import dk.michael.c25k.ui.formatDuration
import dk.michael.c25k.ui.theme.CompletedGreen

@Composable
fun ActiveRunScreen(programIndex: Int, onFinished: (Long) -> Unit) {
    val viewModel: ActiveRunViewModel = viewModel()
    LaunchedEffect(programIndex) { viewModel.start(programIndex) }

    val state by viewModel.state.collectAsState()
    val savedSessionId by viewModel.savedSessionId.collectAsState()

    LaunchedEffect(savedSessionId) {
        savedSessionId?.let { onFinished(it) }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Aktivt løb", style = MaterialTheme.typography.titleLarge)
        LazyColumn(modifier = Modifier
            .weight(1f)
            .padding(vertical = 8.dp)) {
            itemsIndexed(state.steps) { index, step ->
                StepRow(step = step, index = index, state = state)
            }
        }
        OutlinedButton(onClick = { viewModel.cancel() }, modifier = Modifier.fillMaxWidth()) {
            Text("Afbryd")
        }
    }
}

@Composable
private fun StepRow(step: RunStepUi, index: Int, state: RunUiState) {
    val completed = state.completedStepIndices.contains(index)
    val isCurrent = state.currentStepIndex == index
    val baseColor = if (completed) CompletedGreen else MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(vertical = 3.dp)
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
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${step.label} ${formatDuration(step.seconds)}")
            if (completed) Text("✓")
        }
    }
}
