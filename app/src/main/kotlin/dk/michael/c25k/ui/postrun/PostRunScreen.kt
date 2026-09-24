package dk.michael.c25k.ui.postrun

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.michael.c25k.data.db.RunOutcome
import dk.michael.c25k.ui.theme.CancelledRed
import dk.michael.c25k.ui.theme.CancelledRedText
import dk.michael.c25k.ui.theme.CompletedGreen
import dk.michael.c25k.ui.theme.CompletedGreenText

@Composable
fun PostRunScreen(sessionId: Long, onSaved: () -> Unit) {
    val viewModel: PostRunViewModel = viewModel()
    LaunchedEffect(sessionId) { viewModel.load(sessionId) }
    val session by viewModel.session.collectAsState()

    var note by remember { mutableStateOf("") }
    var energyBefore by remember { mutableStateOf(3) }
    var energyAfter by remember { mutableStateOf(3) }
    var outcome by remember { mutableStateOf<RunOutcome?>(null) }

    LaunchedEffect(session?.id) {
        session?.let {
            note = it.note
            energyBefore = it.energyBefore.takeIf { value -> value > 0 } ?: 3
            energyAfter = it.energyAfter.takeIf { value -> value > 0 } ?: 3
            outcome = it.outcome
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF14B8C6), Color(0xFF087A9B), Color(0xFF073A60))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Efter løbet",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Gem hvordan turen gik, mens følelsen stadig sidder i kroppen.",
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                color = Color.White,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Hvordan gik det?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutcomeCard(
                            title = "Gennemført",
                            subtitle = "Jeg nåede målet",
                            selected = outcome == RunOutcome.COMPLETED,
                            selectedColor = CompletedGreen,
                            selectedTextColor = CompletedGreenText,
                            onClick = { outcome = RunOutcome.COMPLETED },
                            modifier = Modifier.weight(1f)
                        )
                        OutcomeCard(
                            title = "Afbrudt",
                            subtitle = "Ikke i dag",
                            selected = outcome == RunOutcome.CANCELLED,
                            selectedColor = CancelledRed,
                            selectedTextColor = CancelledRedText,
                            onClick = { outcome = RunOutcome.CANCELLED },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    EnergyCard(
                        title = "Energi før løbet",
                        value = energyBefore,
                        onChange = { energyBefore = it },
                        modifier = Modifier.padding(top = 18.dp)
                    )
                    EnergyCard(
                        title = "Energi efter løbet",
                        value = energyAfter,
                        onChange = { energyAfter = it },
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note") },
                        placeholder = { Text("Hvordan føltes kroppen, tempoet og vejrtrækningen?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                            .padding(top = 16.dp)
                    )

                    Button(
                        onClick = { outcome?.let { viewModel.save(sessionId, it, note, energyBefore, energyAfter, onSaved) } },
                        enabled = outcome != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .padding(top = 16.dp)
                    ) {
                        Text("Gem løbet")
                    }
                }
            }
        }
    }
}

@Composable
private fun OutcomeCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    selectedColor: Color,
    selectedTextColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) selectedColor else Color(0xFFF1F6F8)
    val fg = if (selected) selectedTextColor else Color(0xFF506872)
    val border = if (selected) selectedTextColor.copy(alpha = 0.28f) else Color.Transparent

    Column(
        modifier = modifier
            .height(98.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(if (selected) fg else Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(if (selected) "✓" else "", color = bg, style = MaterialTheme.typography.labelSmall)
        }
        Column {
            Text(title, color = fg, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, color = fg.copy(alpha = 0.78f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun EnergyCard(title: String, value: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFF7FAFB))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF324C56))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (i in 1..5) {
                Text(
                    text = if (i <= value) "★" else "☆",
                    color = if (i <= value) Color(0xFF0A88B0) else Color(0xFFB6C4CA),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.clickable { onChange(i) }
                )
            }
        }
    }
}
