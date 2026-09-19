package dk.michael.c25k.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StarRow(value: Int, onChange: ((Int) -> Unit)? = null) {
    Row {
        for (i in 1..5) {
            val filled = i <= value
            Text(
                text = if (filled) "★" else "☆",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(2.dp)
                    .let { m -> if (onChange != null) m.clickable { onChange(i) } else m }
            )
        }
    }
}
