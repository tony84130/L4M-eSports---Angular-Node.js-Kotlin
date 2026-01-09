package com.example.l4m_esports_mobile.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.l4m_esports_mobile.ui.screens.auth.SignInScreen
import com.example.l4m_esports_mobile.ui.screens.auth.SignUpScreen
import com.example.l4m_esports_mobile.ui.screens.auth.WelcomeScreen
import com.example.l4m_esports_mobile.ui.screens.events.EventDetailScreen
import com.example.l4m_esports_mobile.ui.screens.events.CreateEventScreen
import com.example.l4m_esports_mobile.ui.screens.events.EditEventScreen
import com.example.l4m_esports_mobile.ui.screens.games.GameDetailScreen
import com.example.l4m_esports_mobile.ui.screens.games.GamesListScreen
import com.example.l4m_esports_mobile.ui.screens.games.CreateGameScreen
import com.example.l4m_esports_mobile.ui.screens.teams.TeamsListScreen
import com.example.l4m_esports_mobile.ui.screens.teams.CreateTeamScreen
import com.example.l4m_esports_mobile.ui.screens.teams.TeamDetailScreen
import com.example.l4m_esports_mobile.ui.screens.teams.EditTeamScreen
import com.example.l4m_esports_mobile.ui.screens.teamrequests.TeamRequestsListScreen
import com.example.l4m_esports_mobile.ui.screens.teamrequests.TeamRequestsForTeamScreen
import com.example.l4m_esports_mobile.ui.screens.notifications.NotificationsListScreen
import com.example.l4m_esports_mobile.ui.screens.users.UsersListScreen
import com.example.l4m_esports_mobile.ui.screens.profile.EditProfileScreen
import com.example.l4m_esports_mobile.ui.screens.profile.ProfileScreen
import com.example.l4m_esports_mobile.ui.screens.eventregistrations.TeamRegistrationsScreen
import com.example.l4m_esports_mobile.ui.screens.eventregistrations.EventRegistrationDetailScreen
import com.example.l4m_esports_mobile.ui.screens.matches.MatchListScreen
import com.example.l4m_esports_mobile.ui.screens.matches.MatchDetailScreen
import com.example.l4m_esports_mobile.ui.components.MainScreen

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object SignIn : Screen("sign_in")
    object SignUp : Screen("sign_up")
    object GamesList : Screen("games_list")
    object GameDetail : Screen("game_detail/{gameId}") {
        fun createRoute(gameId: String) = "game_detail/$gameId"
    }
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object CreateGame : Screen("create_game")
    object CreateEvent : Screen("create_event/{gameId}") {
        fun createRoute(gameId: String) = "create_event/$gameId"
    }
    object EditEvent : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: String) = "edit_event/$eventId"
    }
    object TeamsList : Screen("teams_list")
    object CreateTeam : Screen("create_team")
    object TeamDetail : Screen("team_detail/{teamId}") {
        fun createRoute(teamId: String) = "team_detail/$teamId"
    }
    object EditTeam : Screen("edit_team/{teamId}") {
        fun createRoute(teamId: String) = "edit_team/$teamId"
    }
    object TeamRequestsList : Screen("team_requests_list")
    object TeamRequestsForTeam : Screen("team_requests/{teamId}") {
        fun createRoute(teamId: String) = "team_requests/$teamId"
    }
    object Notifications : Screen("notifications")
    object Users : Screen("users")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object TeamRegistrations : Screen("team_registrations/{teamId}") {
        fun createRoute(teamId: String) = "team_registrations/$teamId"
    }
    object EventRegistrationDetail : Screen("event_registration_detail/{registrationId}") {
        fun createRoute(registrationId: String) = "event_registration_detail/$registrationId"
    }
    object MatchList : Screen("match_list") {
        fun createRoute(eventId: String? = null, teamId: String? = null) = 
            when {
                eventId != null -> "match_list/event/$eventId"
                teamId != null -> "match_list/team/$teamId"
                else -> "match_list"
            }
    }
    object MatchDetail : Screen("match_detail/{matchId}") {
        fun createRoute(matchId: String) = "match_detail/$matchId"
    }
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: String = Screen.Welcome.route) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToSignIn = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignIn.route) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(Screen.GamesList.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.GamesList.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToSignIn = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.GamesList.route) {
            MainScreen(navController = navController) {
                GamesListScreen(
                    onGameClick = { gameId ->
                        navController.navigate(Screen.GameDetail.createRoute(gameId))
                    },
                    onCreateGame = {
                        navController.navigate(Screen.CreateGame.route)
                    }
                )
            }
        }

        composable(Screen.GameDetail.route) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            GameDetailScreen(
                gameId = gameId,
                onBack = {
                    navController.popBackStack()
                },
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                },
                onCreateEvent = {
                    navController.navigate(Screen.CreateEvent.createRoute(gameId))
                }
            )
        }

        composable(Screen.EventDetail.route) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailScreen(
                eventId = eventId,
                onBack = {
                    navController.popBackStack()
                },
                onEditEvent = { eventId ->
                    navController.navigate(Screen.EditEvent.createRoute(eventId))
                },
                onEventDeleted = {
                    navController.popBackStack()
                },
                onViewMatches = { eventId ->
                    navController.navigate(Screen.MatchList.createRoute(eventId = eventId))
                }
            )
        }

        composable(Screen.CreateGame.route) {
            CreateGameScreen(
                onGameCreated = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CreateEvent.route) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            CreateEventScreen(
                gameId = gameId,
                onEventCreated = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EditEvent.route) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EditEventScreen(
                eventId = eventId,
                onEventUpdated = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.TeamsList.route) {
            val teamViewModel: com.example.l4m_esports_mobile.ui.viewmodel.TeamViewModel = hiltViewModel()
            val userViewModel: com.example.l4m_esports_mobile.ui.viewmodel.UserViewModel = hiltViewModel()
            
            MainScreen(navController = navController) {
                TeamsListScreen(
                    onTeamClick = { teamId ->
                        navController.navigate(Screen.TeamDetail.createRoute(teamId))
                    },
                    onCreateTeam = {
                        navController.navigate(Screen.CreateTeam.route)
                    },
                    teamViewModel = teamViewModel,
                    userViewModel = userViewModel
                )
            }
        }

        composable(Screen.CreateTeam.route) {
            val teamViewModel: com.example.l4m_esports_mobile.ui.viewmodel.TeamViewModel = hiltViewModel()
            CreateTeamScreen(
                onTeamCreated = {
                    // Recharger les équipes avant de revenir
                    teamViewModel.loadTeams()
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                },
                teamViewModel = teamViewModel
            )
        }

        composable(Screen.TeamDetail.route) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
            val teamViewModel: com.example.l4m_esports_mobile.ui.viewmodel.TeamViewModel = hiltViewModel()
            val userViewModel: com.example.l4m_esports_mobile.ui.viewmodel.UserViewModel = hiltViewModel()
            
            TeamDetailScreen(
                teamId = teamId,
                onBack = {
                    navController.popBackStack()
                },
                onEditTeam = { editTeamId ->
                    navController.navigate(Screen.EditTeam.createRoute(editTeamId))
                },
                onTeamDeleted = {
                    // Recharger les équipes et l'utilisateur pour mettre à jour les données
                    teamViewModel.loadTeams()
                    userViewModel.loadCurrentUser()
                    navController.popBackStack()
                },
                onViewRequests = { viewTeamId ->
                    navController.navigate(Screen.TeamRequestsForTeam.createRoute(viewTeamId))
                },
                onViewRegistrations = { viewTeamId ->
                    navController.navigate(Screen.TeamRegistrations.createRoute(viewTeamId))
                },
                teamViewModel = teamViewModel,
                userViewModel = userViewModel
            )
        }

        composable(Screen.EditTeam.route) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
            EditTeamScreen(
                teamId = teamId,
                onTeamUpdated = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.TeamRequestsList.route) {
            TeamRequestsListScreen()
        }

        composable(Screen.TeamRequestsForTeam.route) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
            TeamRequestsForTeamScreen(
                teamId = teamId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Notifications.route) {
            MainScreen(navController = navController) {
                NotificationsListScreen(
                    onNotificationClick = { notification ->
                        // Navigate based on notification type
                        when (notification.relatedEntity?.entityType) {
                            "team_request" -> {
                                // Could navigate to team request detail or team detail
                                notification.relatedEntity.entityId?.let { entityId ->
                                    // For now, just show the notification
                                }
                            }
                            "team" -> {
                                notification.relatedEntity.entityId?.let { teamId ->
                                    navController.navigate(Screen.TeamDetail.createRoute(teamId))
                                }
                            }
                            else -> {
                                // Other types
                            }
                        }
                    }
                )
            }
        }

        composable(Screen.Users.route) {
            MainScreen(navController = navController) {
                UsersListScreen()
            }
        }

        composable(Screen.Profile.route) {
            MainScreen(navController = navController) {
                ProfileScreen(
                    onEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onSignOut = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onAccountDeleted = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onSaveSuccess = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.TeamRegistrations.route) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
            TeamRegistrationsScreen(
                teamId = teamId,
                onBack = {
                    navController.popBackStack()
                },
                onRegistrationClick = { registrationId ->
                    navController.navigate(Screen.EventRegistrationDetail.createRoute(registrationId))
                }
            )
        }
        
        composable(Screen.EventRegistrationDetail.route) { backStackEntry ->
            val registrationId = backStackEntry.arguments?.getString("registrationId") ?: ""
            EventRegistrationDetailScreen(
                registrationId = registrationId,
                onBack = {
                    navController.popBackStack()
                },
                onRegistrationDeleted = {
                    navController.popBackStack()
                }
            )
        }

        composable("match_list") {
            MatchListScreen(
                eventId = null,
                teamId = null,
                onBack = {
                    navController.popBackStack()
                },
                onMatchClick = { matchId ->
                    navController.navigate(Screen.MatchDetail.createRoute(matchId))
                }
            )
        }
        
        composable("match_list/event/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            MatchListScreen(
                eventId = if (eventId.isNullOrBlank() || eventId == "null") null else eventId,
                teamId = null,
                onBack = {
                    navController.popBackStack()
                },
                onMatchClick = { matchId ->
                    navController.navigate(Screen.MatchDetail.createRoute(matchId))
                }
            )
        }
        
        composable("match_list/team/{teamId}") { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString("teamId")
            MatchListScreen(
                eventId = null,
                teamId = if (teamId.isNullOrBlank() || teamId == "null") null else teamId,
                onBack = {
                    navController.popBackStack()
                },
                onMatchClick = { matchId ->
                    navController.navigate(Screen.MatchDetail.createRoute(matchId))
                }
            )
        }

        composable(Screen.MatchDetail.route) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
            MatchDetailScreen(
                matchId = matchId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

