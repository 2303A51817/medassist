package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppHeader
import com.example.ui.components.VoiceSearchDialog
import com.example.ui.screens.*
import com.example.ui.theme.MedAssistTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.MedAssistViewModel
import com.example.ui.viewmodel.UserRoleMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedAssistTheme {
                MedAssistApp()
            }
        }
    }
}

@Composable
fun MedAssistApp(
    viewModel: MedAssistViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val medicines by viewModel.filteredMedicines.collectAsStateWithLifecycle()
    val allMedicines by viewModel.medicines.collectAsStateWithLifecycle()
    val pharmaciesWithDist by viewModel.sortedPharmacies.collectAsStateWithLifecycle()
    val ambulancesWithDist by viewModel.sortedAmbulances.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val activeAmbulanceBooking by viewModel.activeAmbulanceBooking.collectAsStateWithLifecycle()
    val interPharmacyRequests by viewModel.interPharmacyRequests.collectAsStateWithLifecycle()
    val activePortalPharmacyId by viewModel.activePortalPharmacyId.collectAsStateWithLifecycle()

    var isViewingCart by remember { mutableStateOf(false) }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.refreshLocation(context)
        }
    }

    // Audio Permission Launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.openVoiceSearch(context)
    }

    // Request GPS location on launch
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Handle Back Press
    BackHandler(enabled = isViewingCart || uiState.activeTab != AppNavTab.HOME) {
        if (isViewingCart) {
            isViewingCart = false
        } else {
            viewModel.setNavTab(AppNavTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            AppHeader(
                location = uiState.userLocation,
                isLocating = uiState.isLocating,
                cartItemCount = cart.sumOf { it.quantity },
                roleMode = uiState.userRoleMode,
                onRefreshLocation = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onCartClick = { isViewingCart = !isViewingCart },
                onToggleRoleMode = {
                    val nextRole = if (uiState.userRoleMode == UserRoleMode.USER) {
                        UserRoleMode.PHARMACIST
                    } else {
                        UserRoleMode.USER
                    }
                    viewModel.setUserRoleMode(nextRole)
                },
                onSosClick = {
                    isViewingCart = false
                    viewModel.setNavTab(AppNavTab.AMBULANCE)
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_bottom_nav_bar")
            ) {
                if (uiState.userRoleMode == UserRoleMode.USER) {
                    NavigationBarItem(
                        selected = !isViewingCart && uiState.activeTab == AppNavTab.HOME,
                        onClick = {
                            isViewingCart = false
                            viewModel.setNavTab(AppNavTab.HOME)
                        },
                        icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Find") },
                        label = { Text("Find Meds", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_find_meds")
                    )

                    NavigationBarItem(
                        selected = !isViewingCart && uiState.activeTab == AppNavTab.PHARMACIES,
                        onClick = {
                            isViewingCart = false
                            viewModel.setNavTab(AppNavTab.PHARMACIES)
                        },
                        icon = { Icon(Icons.Default.LocalPharmacy, contentDescription = "Pharmacies") },
                        label = { Text("Pharmacies", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_pharmacies")
                    )

                    NavigationBarItem(
                        selected = !isViewingCart && uiState.activeTab == AppNavTab.CHATBOT,
                        onClick = {
                            isViewingCart = false
                            viewModel.setNavTab(AppNavTab.CHATBOT)
                        },
                        icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI MedBot") },
                        label = { Text("AI MedBot", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_ai_medbot")
                    )

                    NavigationBarItem(
                        selected = !isViewingCart && uiState.activeTab == AppNavTab.AMBULANCE,
                        onClick = {
                            isViewingCart = false
                            viewModel.setNavTab(AppNavTab.AMBULANCE)
                        },
                        icon = { Icon(Icons.Default.Emergency, contentDescription = "Ambulance") },
                        label = { Text("Ambulance", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.error,
                            indicatorColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_ambulance")
                    )

                    NavigationBarItem(
                        selected = !isViewingCart && uiState.activeTab == AppNavTab.ORDERS,
                        onClick = {
                            isViewingCart = false
                            viewModel.setNavTab(AppNavTab.ORDERS)
                        },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
                        label = { Text("Orders", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_orders")
                    )
                } else {
                    // Pharmacist Partner Navigation
                    NavigationBarItem(
                        selected = !isViewingCart && uiState.activeTab == AppNavTab.HOME,
                        onClick = {
                            isViewingCart = false
                            viewModel.setNavTab(AppNavTab.HOME)
                        },
                        icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory") },
                        label = { Text("Inventory", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_inventory")
                    )

                    NavigationBarItem(
                        selected = !isViewingCart && uiState.activeTab == AppNavTab.PHARMACIES,
                        onClick = {
                            isViewingCart = false
                            viewModel.setNavTab(AppNavTab.PHARMACIES)
                        },
                        icon = { Icon(Icons.Default.Hub, contentDescription = "Network") },
                        label = { Text("Partner Shops", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_partner_shops")
                    )

                    NavigationBarItem(
                        selected = !isViewingCart && uiState.activeTab == AppNavTab.CHATBOT,
                        onClick = {
                            isViewingCart = false
                            viewModel.setNavTab(AppNavTab.CHATBOT)
                        },
                        icon = { Icon(Icons.Default.SmartToy, contentDescription = "NLP Assistant") },
                        label = { Text("NLP Assistant", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_nlp_assistant")
                    )

                    NavigationBarItem(
                        selected = !isViewingCart && uiState.activeTab == AppNavTab.AMBULANCE,
                        onClick = {
                            isViewingCart = false
                            viewModel.setNavTab(AppNavTab.AMBULANCE)
                        },
                        icon = { Icon(Icons.Default.Emergency, contentDescription = "Ambulance") },
                        label = { Text("Emergency", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_emergency")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isViewingCart) {
                CartCheckoutScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    cartItems = cart
                )
            } else if (uiState.userRoleMode == UserRoleMode.PHARMACIST && uiState.activeTab == AppNavTab.HOME) {
                // In Pharmacist Mode, the Home tab shows the Pharmacist Portal
                PharmacyPortalScreen(
                    viewModel = viewModel,
                    pharmacies = pharmaciesWithDist.map { it.first },
                    medicines = allMedicines,
                    interPharmacyRequests = interPharmacyRequests,
                    activePharmacyId = activePortalPharmacyId
                )
            } else {
                when (uiState.activeTab) {
                    AppNavTab.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            uiState = uiState,
                            medicines = medicines,
                            pharmacies = pharmaciesWithDist.map { it.first },
                            onOpenVoiceSearch = {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        )
                    }
                    AppNavTab.PHARMACIES -> {
                        PharmaciesScreen(
                            viewModel = viewModel,
                            pharmaciesWithDist = pharmaciesWithDist,
                            allMedicines = allMedicines
                        )
                    }
                    AppNavTab.CHATBOT -> {
                        ChatbotScreen(
                            viewModel = viewModel,
                            messages = chatMessages,
                            pharmacies = pharmaciesWithDist.map { it.first },
                            onOpenVoiceSearch = {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        )
                    }
                    AppNavTab.AMBULANCE -> {
                        AmbulanceScreen(
                            viewModel = viewModel,
                            uiState = uiState,
                            ambulancesWithDist = ambulancesWithDist,
                            activeBooking = activeAmbulanceBooking
                        )
                    }
                    AppNavTab.ORDERS -> {
                        OrderHistoryScreen(
                            orders = orders,
                            onExploreMedicines = { viewModel.setNavTab(AppNavTab.HOME) }
                        )
                    }
                }
            }

            // Voice Search Modal Dialog
            if (uiState.isVoiceSearchActive) {
                VoiceSearchDialog(
                    transcript = uiState.voiceTranscript,
                    isListening = uiState.isListeningToVoice,
                    errorMessage = uiState.voiceError,
                    onQuerySelected = { query ->
                        viewModel.applyVoiceSearch(query)
                    },
                    onDismiss = {
                        viewModel.closeVoiceSearch()
                    }
                )
            }
        }
    }
}
