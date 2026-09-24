package dk.michael.c25k

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsControllerCompat
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
        configureSystemBars()
    }

    private fun configureSystemBars() {
        window.statusBarColor = Color.rgb(0x13, 0xB6, 0xC5)
        window.navigationBarColor = Color.rgb(0x06, 0x42, 0x63)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }
}
