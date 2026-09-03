package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.DynamicGlassBackground
import com.example.ui.components.GlassBottomNavigation
import com.example.ui.components.GlassMiniPlayer
import com.example.ui.components.NowPlayingScreen
import com.example.ui.components.SongInfoDialog
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BottomTab
import com.example.ui.viewmodel.MusicUiState
import com.example.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current

                // Storage Permission Launcher
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    viewModel.onPermissionResult(isGranted)
                }

                LaunchedEffect(Unit) {
                    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }

                    val isAlreadyGranted = ContextCompat.checkSelfPermission(
                        context,
                        permissionToRequest
                    ) == PackageManager.PERMISSION_GRANTED

                    viewModel.onPermissionResult(isAlreadyGranted)

                    if (!isAlreadyGranted) {
                        permissionLauncher.launch(permissionToRequest)
                    }
                }

                // Intercept back button if Now Playing sheet is expanded
                BackHandler(enabled = uiState.isNowPlayingExpanded) {
                    viewModel.setNowPlayingExpanded(false)
                }

                BlackDudeMusicApp(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun BlackDudeMusicApp(
    uiState: MusicUiState,
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    DynamicGlassBackground(
        currentSong = uiState.currentSong,
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main Screen Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AnimatedContent(
                        targetState = uiState.selectedTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(250)) togetherWith
                                    fadeOut(animationSpec = tween(250))
                        },
                        label = "tabTransition"
                    ) { tab ->
                        when (tab) {
                            BottomTab.HOME -> HomeScreen(uiState = uiState, viewModel = viewModel)
                            BottomTab.LIBRARY -> LibraryScreen(uiState = uiState, viewModel = viewModel)
                            BottomTab.SEARCH -> SearchScreen(uiState = uiState, viewModel = viewModel)
                            BottomTab.EQUALIZER -> EqualizerScreen(uiState = uiState, viewModel = viewModel)
                            BottomTab.ABOUT -> AboutScreen(uiState = uiState, viewModel = viewModel)
                        }
                    }
                }
            }

            // Floating Bottom Controls: Mini Player & Translucent Glass Navigation Bar
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                if (uiState.currentSong != null) {
                    GlassMiniPlayer(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }

                GlassBottomNavigation(
                    selectedTab = uiState.selectedTab,
                    accentColor = GoldPrimary,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }

            // Fullscreen Glass Now Playing Overlay
            AnimatedVisibility(
                visible = uiState.isNowPlayingExpanded && uiState.currentSong != null,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) + fadeIn(tween(200)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(tween(180))
            ) {
                NowPlayingScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            // Global Dialogs
            if (uiState.songDetailDialogTrack != null) {
                SongInfoDialog(
                    song = uiState.songDetailDialogTrack,
                    onDismiss = { viewModel.showSongDetails(null) }
                )
            }

            if (uiState.addToPlaylistDialogTrack != null) {
                AddToPlaylistDialog(
                    song = uiState.addToPlaylistDialogTrack,
                    playlists = uiState.playlists,
                    onDismiss = { viewModel.showAddToPlaylist(null) },
                    onAddToPlaylist = { playlistId ->
                        viewModel.addSongToPlaylist(playlistId, uiState.addToPlaylistDialogTrack.id)
                    },
                    onCreateAndAdd = { name ->
                        viewModel.createPlaylist(name)
                        // will be updated in repository
                    }
                )
            }
        }
    }
}
