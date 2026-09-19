package dk.michael.c25k.ui.postrun

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.michael.c25k.ui.StarRow

@Composable
fun PostRunScreen(sessionId: Long, onSaved: () -> Unit) {
    val viewModel: PostRunViewModel = viewModel()
    LaunchedEffect(sessionId) { viewModel.load(sessionId) }

    var note by remember { mutableStateOf("") }
    var energyBefore by remember { mutableStateOf(3) }
    var energyAfter by remember { mutableStateOf(3) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Efter løbet", style = MaterialTheme.typography.titleLarge)

        Text("Energi før løbet", modifier = Modifier.padding(top = 12.dp))
        StarRow(value = energyBefore) { energyBefore = it }

        Text("Energi efter løbet", modifier = Modifier.padding(top = 12.dp))
        StarRow(value = energyAfter) { energyAfter = it }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(top = 12.dp)
        )

        Button(
            onClick = { viewModel.save(sessionId, note, energyBefore, energyAfter, onSaved) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Gem")
        }
    }
}
