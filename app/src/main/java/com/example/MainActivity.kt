package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.TradeBottomNav
import com.example.ui.screens.AccountLoginScreen
import com.example.ui.screens.AddEditTradeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PasscodeMode
import com.example.ui.screens.PasscodeScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TradeDetailsScreen
import com.example.ui.screens.TradeManagementScreen
import com.example.ui.screens.WorkScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.TradeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TradeApp()
            }
        }
    }
}

@Composable
fun TradeApp(viewModel: TradeViewModel = viewModel()) {
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val hasPasscode = remember(isAppLocked) { viewModel.passcodeManager.hasPasscode() }
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()

    // 1. Mandatory 4-Digit Passcode Gate (Step 1 Setup -> Step 2 Confirm -> GO to login)
    if (!hasPasscode) {
        PasscodeScreen(
            mode = PasscodeMode.SETUP,
            passcodeManager = viewModel.passcodeManager,
            onSuccess = {
                viewModel.unlockApp()
            }
        )
        return
    }

    // 2. User Email & Database Login Gate (Restores data from any phone)
    if (!isUserLoggedIn) {
        AccountLoginScreen(
            viewModel = viewModel,
            onLoginSuccess = {
                viewModel.syncWithCloud()
            }
        )
        return
    }

    // 3. Subsequent launches Passcode Unlock Gate
    if (isAppLocked) {
        PasscodeScreen(
            mode = PasscodeMode.UNLOCK,
            passcodeManager = viewModel.passcodeManager,
            onSuccess = {
                viewModel.unlockApp()
                viewModel.syncWithCloud()
            }
        )
        return
    }

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedTradeId by viewModel.selectedTradeId.collectAsStateWithLifecycle()
    val editingTrade by viewModel.editingTrade.collectAsStateWithLifecycle()

    // Back handling for clean navigation hierarchy
    BackHandler(enabled = currentScreen != ScreenDestination.MAIN_CONTAINER || currentTab != MainTab.HOME) {
        if (currentScreen != ScreenDestination.MAIN_CONTAINER) {
            viewModel.navigateBack()
        } else if (currentTab != MainTab.HOME) {
            viewModel.selectTab(MainTab.HOME)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                ScreenDestination.MAIN_CONTAINER -> {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = DarkBackground,
                        bottomBar = {
                            TradeBottomNav(
                                selectedTab = currentTab,
                                onTabSelected = { viewModel.selectTab(it) }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
                            when (currentTab) {
                                MainTab.HOME -> HomeScreen(viewModel = viewModel)
                                MainTab.TRADE -> TradeManagementScreen(
                                    viewModel = viewModel,
                                    onBackClick = { viewModel.selectTab(MainTab.HOME) }
                                )
                                MainTab.TASKS -> TasksScreen(viewModel = viewModel)
                                MainTab.WORK -> WorkScreen()
                            }
                        }
                    }
                }

                ScreenDestination.ADD_EDIT_TRADE -> {
                    AddEditTradeScreen(
                        viewModel = viewModel,
                        tradeToEdit = editingTrade,
                        onBackClick = { viewModel.navigateBack() }
                    )
                }

                ScreenDestination.TRADE_DETAILS -> {
                    selectedTradeId?.let { tradeId ->
                        TradeDetailsScreen(
                            tradeId = tradeId,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() }
                        )
                    } ?: run {
                        viewModel.navigateBack()
                    }
                }
            }
        }
    }
}
