package com.example.l4m_esports_mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.l4m_esports_mobile.ui.viewmodel.NotificationViewModel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.l4m_esports_mobile.navigation.Screen
import com.example.l4m_esports_mobile.ui.screens.support.AiHelpWidget
import com.example.l4m_esports_mobile.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val currentUser by userViewModel.currentUser.collectAsState()
    
    // Charger le nombre de notifications non lues et l'utilisateur au démarrage
    LaunchedEffect(Unit) {
        notificationViewModel.loadUnreadCount()
        userViewModel.loadCurrentUser()
    }
    
    // Déterminer l'onglet sélectionné basé sur la route actuelle
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val isAdmin = currentUser?.role == "admin"
    val selectedTab = when (currentRoute) {
        Screen.GamesList.route -> 0
        Screen.TeamsList.route -> 1
        Screen.Notifications.route -> 2
        Screen.Users.route -> 2
        Screen.Profile.route -> 3
        else -> 0
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Jeux") },
                    label = { Text("Jeux") },
                    selected = selectedTab == 0,
                    onClick = {
                        navController.navigate(Screen.GamesList.route) {
                            popUpTo(Screen.GamesList.route) { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Groups, contentDescription = "Équipes") },
                    label = { Text("Équipes") },
                    selected = selectedTab == 1,
                    onClick = {
                        navController.navigate(Screen.TeamsList.route) {
                            popUpTo(Screen.TeamsList.route) { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = {
                        if (isAdmin) {
                            Icon(Icons.Default.Person, contentDescription = "Utilisateurs")
                        } else {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge {
                                            Text(unreadCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                            }
                        }
                    },
                    label = { Text(if (isAdmin) "Utilisateurs" else "Notifications") },
                    selected = selectedTab == 2,
                    onClick = {
                        if (isAdmin) {
                            navController.navigate(Screen.Users.route) {
                                popUpTo(Screen.Users.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Notifications.route) {
                                popUpTo(Screen.Notifications.route) { inclusive = true }
                            }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                    label = { Text("Profil") },
                    selected = selectedTab == 3,
                    onClick = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Profile.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            content()
            
            // Widget AI flottant
            AiHelpWidget(
                currentPage = currentRoute,
                userRole = currentUser?.role
            )
        }
    }
}

