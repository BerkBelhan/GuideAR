package com.berkbelhan.indoornavigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.berkbelhan.indoornavigation.presentation.nav.AppNavGraph
import com.berkbelhan.indoornavigation.presentation.settings.SettingsViewModel
import com.berkbelhan.indoornavigation.presentation.theme.IndoorNavigationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.settings.collectAsState()

            IndoorNavigationTheme(themeMode = settings.themeMode) {
                AppNavGraph()
            }
        }
    }
}
