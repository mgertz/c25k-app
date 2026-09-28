package dk.michael.c25k.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dk.michael.c25k.service.ActiveRunRegistry
import dk.michael.c25k.ui.activerun.ActiveRunBanner
import dk.michael.c25k.ui.activerun.ActiveRunScreen
import dk.michael.c25k.ui.chooserun.ChooseRunScreen
import dk.michael.c25k.ui.history.HistoryScreen
import dk.michael.c25k.ui.history.RunDetailScreen
import dk.michael.c25k.ui.home.Home2Screen
import dk.michael.c25k.ui.postrun.PostRunScreen
import dk.michael.c25k.ui.workout.WorkoutDetailScreen

private object Routes {
    const val HOME = "home"
    const val CHOOSE_RUN = "choose_run"
    const val WORKOUT = "workout/{programIndex}"
    const val ACTIVE_RUN = "active_run/{programIndex}"
    const val POST_RUN = "post_run/{sessionId}"
    const val HISTORY = "history"
    const val RUN_DETAIL = "run_detail/{sessionId}"
}

private fun activeRunRoute(programIndex: Int): String = "active_run/$programIndex"

@Composable
fun C25KNavGraph() {
    val navController = rememberNavController()
    val activeRun by ActiveRunRegistry.activeRun.collectAsState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = Routes.HOME) {

            composable(Routes.HOME) {
                Home2Screen(
                    onOpenWorkout = { index -> navController.navigate("workout/$index") },
                    onOpenHistory = { navController.navigate(Routes.HISTORY) }
                )
            }

            composable(Routes.CHOOSE_RUN) {
                ChooseRunScreen(onStartRun = { index ->
                    navController.navigate(activeRunRoute(index))
                })
            }

            composable(
                Routes.WORKOUT,
                arguments = listOf(navArgument("programIndex") { type = NavType.IntType })
            ) { backStackEntry ->
                val programIndex = backStackEntry.arguments?.getInt("programIndex") ?: 0
                WorkoutDetailScreen(
                    programIndex = programIndex,
                    onStartRun = { index -> navController.navigate(activeRunRoute(index)) },
                    onOpenActiveRun = { index ->
                        navController.navigate(activeRunRoute(index)) {
                            launchSingleTop = true
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
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
                HistoryScreen(
                    onOpenDetail = { sessionId -> navController.navigate("run_detail/$sessionId") },
                    onOpenActivity = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } }
                )
            }

            composable(
                Routes.RUN_DETAIL,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                RunDetailScreen(sessionId = sessionId, onBack = { navController.popBackStack() })
            }
        }

        activeRun?.takeIf { currentRoute != Routes.ACTIVE_RUN }?.let { info ->
            ActiveRunBanner(
                info = info,
                onClick = {
                    navController.navigate(activeRunRoute(info.programIndex)) {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 10.dp)
            )
        }
    }
}
