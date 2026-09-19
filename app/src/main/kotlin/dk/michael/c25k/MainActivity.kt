package dk.michael.c25k

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dk.michael.c25k.ui.navigation.C25KNavGraph
import dk.michael.c25k.ui.theme.C25KTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            C25KTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    C25KNavGraph()
                }
            }
        }
    }
}
