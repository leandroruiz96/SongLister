package com.leandroleu96.songlister.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.leandroleu96.songlister.core.AppContainer
import com.leandroleu96.songlister.ui.detail.SongDetailScreen
import com.leandroleu96.songlister.ui.detail.SongDetailViewModel
import com.leandroleu96.songlister.ui.home.HomeScreen
import com.leandroleu96.songlister.ui.home.HomeViewModel
import com.leandroleu96.songlister.ui.list.SongListScreen
import com.leandroleu96.songlister.ui.list.SongListViewModel

@Composable
fun SongListerNavHost(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.provideFactory(appContainer.songRepository),
            )
            HomeScreen(
                viewModel = homeViewModel,
                onSelectGame = { gameType ->
                    navController.navigate(SongListRoute(gameType = gameType))
                },
            )
        }

        composable<SongListRoute> { backStackEntry ->
            val route: SongListRoute = backStackEntry.toRoute()
            val listViewModel: SongListViewModel = viewModel(
                key = "song_list_${route.gameType.name}",
                factory = SongListViewModel.provideFactory(
                    gameType = route.gameType,
                    repository = appContainer.songRepository,
                ),
            )
            SongListScreen(
                viewModel = listViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSelectSong = { songId ->
                    navController.navigate(
                        SongDetailRoute(
                            gameType = route.gameType,
                            songId = songId,
                        ),
                    )
                },
            )
        }

        composable<SongDetailRoute> { backStackEntry ->
            val route: SongDetailRoute = backStackEntry.toRoute()
            val detailViewModel: SongDetailViewModel = viewModel(
                key = "song_detail_${route.gameType.name}_${route.songId}",
                factory = SongDetailViewModel.provideFactory(
                    gameType = route.gameType,
                    songId = route.songId,
                    repository = appContainer.songRepository,
                ),
            )
            SongDetailScreen(
                gameType = route.gameType,
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
