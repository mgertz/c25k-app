package dk.michael.c25k.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dk.michael.c25k.ui.activerun.ActiveRunScreen
import dk.michael.c25k.ui.chooserun.ChooseRunScreen
import dk.michael.c25k.ui.history.HistoryScreen
import dk.michael.c25k.ui.history.RunDetailScreen
import dk.michael.c25k.ui.home.HomeScreen
import dk.michael.c25k.ui.postrun.PostRunScreen

private object Routes {
    const val HOME = "home"
    const val CHOOSE_RUN = "choose_run"
    const val ACTIVE_RUN = "active_run/{programIndex}"
    const val POST_RUN = "post_run/{sessionId}"
    const val HISTORY = "history"
    const val RUN_DETAIL = "run_detail/{sessionId}"
}

@Composable
fun C25KNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onStartRun = { navController.navigate(Routes.CHOOSE_RUN) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) }
            )
        }

        composable(Routes.CHOOSE_RUN) {
            ChooseRunScreen(onStartRun = { index ->
                navController.navigate("active_run/$index")
            })
        }

        composable(
            Routes.ACTIVE_RUN,
            arguments = listOf(navArgument("programIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val programIndex = backStackEntry.arguments?.getInt("programIndex") ?: 0
            ActiveRunScreen(programIndex = programIndex, onFinished = { sessionId ->
                navController.navigate("post_run/$sessionId") {
                    popUpTo(Routes.HOME) { inclusive = false }
                }
            })
        }

        composable(
            Routes.POST_RUN,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            PostRunScreen(sessionId = sessionId, onSaved = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.HOME) { inclusive = true }
                }
            })
        }

        composable(Routes.HISTORY) {
            HistoryScreen(onOpenDetail = { sessionId ->
                navController.navigate("run_detail/$sessionId")
            })
        }

        composable(
            Routes.RUN_DETAIL,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            RunDetailScreen(sessionId = sessionId)
        }
    }
}
