package dk.michael.c25k.ui.chooserun

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dk.michael.c25k.ui.summaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseRunScreen(onStartRun: (Int) -> Unit) {
    val viewModel: ChooseRunViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    if (!uiState.loaded) return

    var selectedIndex by remember(uiState.nextIndex) { mutableStateOf(uiState.nextIndex) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var pickedManually by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Vælg løb", style = MaterialTheme.typography.titleLarge)

        OptionCard(
            title = "Næste løb",
            index = uiState.nextIndex,
            program = viewModel.program(uiState.nextIndex),
            selected = !pickedManually && selectedIndex == uiState.nextIndex,
            onClick = { selectedIndex = uiState.nextIndex; pickedManually = false }
        )

        uiState.sameAsLastIndex?.let { idx ->
            OptionCard(
                title = "Samme som sidst",
                index = idx,
                program = viewModel.program(idx),
                selected = !pickedManually && selectedIndex == idx,
                onClick = { selectedIndex = idx; pickedManually = false }
            )
        }

        uiState.sameAsOneBeforeIndex?.let { idx ->
            OptionCard(
                title = "Samme som forrige",
                index = idx,
                program = viewModel.program(idx),
                selected = !pickedManually && selectedIndex == idx,
                onClick = { selectedIndex = idx; pickedManually = false }
            )
        }

        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            val current = viewModel.program(selectedIndex)
            OutlinedTextField(
                value = if (pickedManually && current != null) "Uge ${current.week} · dag ${current.day}" else "Vælg selv",
                onValueChange = {},
                readOnly = true,
                label = { Text("Vælg selv") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                viewModel.programs.forEach { program ->
                    DropdownMenuItem(
                        text = { Text("Uge ${program.week} · dag ${program.day}") },
                        onClick = {
                            selectedIndex = program.index
                            pickedManually = true
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        viewModel.program(selectedIndex)?.let { program ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text("Uge ${program.week} · dag ${program.day}", style = MaterialTheme.typography.titleSmall)
                Text(program.summaryText() + " (plus 5 min opvarmning og nedkøling)", style = MaterialTheme.typography.bodySmall)
            }
        }

        Button(
            onClick = { onStartRun(selectedIndex) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Start løb")
        }
    }
}

@Composable
private fun OptionCard(title: String, index: Int, program: dk.michael.c25k.data.model.Program?, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val borderWidth = if (selected) 2.dp else 0.5.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        program?.let { Text("Uge ${it.week}, dag ${it.day}", style = MaterialTheme.typography.bodySmall) }
    }
}
