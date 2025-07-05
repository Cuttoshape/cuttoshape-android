package com.example.cuttoshapenew

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cuttoshapenew.views.authviews.ProfileScreen
import com.example.cuttoshapenew.views.customerviews.product.ProductScreen
import com.example.cuttoshapenew.views.authviews.LoginDialog
import com.example.cuttoshapenew.views.authviews.SignupDialog
//import com.example.cuttoshapenew.views.tailorviews.AddProductDialog
import com.example.cuttoshapenew.views.tailorviews.BusinessRegistrationScreen
import com.example.cuttoshapenew.views.tailorviews.UserProfileScreen
import com.example.cuttoshapenew.views.customerviews.product.ProductDetailScreen
import com.example.cuttoshapenew.views.tailorviews.BusinessProfileScreen
import com.example.cuttoshapenew.views.tailorviews.DashboardScreen
import com.example.cuttoshapenew.views.tailorviews.MyProductsScreen
import com.example.cuttoshapenew.ui.theme.CuttoshapenewTheme
import com.example.cuttoshapenew.utils.DataStoreManager
import kotlinx.coroutines.flow.collectLatest
import com.example.cuttoshapenew.utils.DataStoreManager.clearAuthData
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CuttoshapenewTheme {
                TailoringApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TailoringApp() {
    val navController = rememberNavController()
    var showLoginDialog by remember { mutableStateOf(false) }
    var showSignUpDialog by remember { mutableStateOf(false) }
    var showAddNewProductDialog by remember { mutableStateOf(false) }
    val buttonColor = Color(0xFF4A90E2)
    val context = LocalContext.current
    var isLoggedIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        DataStoreManager.getToken(context).collectLatest { token ->
            isLoggedIn = token != null
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navigationItems = listOf(
        NavigationItem(
            route = "dashboard",
            label = "Dashboard",
            icon = Icons.Default.DateRange
        ),
        NavigationItem(
            route = "myproduct",
            label = "My Products",
            icon = Icons.Default.ShoppingCart
        ),
        NavigationItem(
            route = "quotation",
            label = "Quotation",
            icon = Icons.Default.Info
        ),
        NavigationItem(
            route = "message",
            label = "Message",
            icon = Icons.Default.Email
        ),
        NavigationItem(
            route = "transaction",
            label = "Transactions",
            icon = Icons.Default.DateRange
        ),
        NavigationItem(
            route = "business_profile",
            label = "Business Profile",
            icon = Icons.Default.Create
        ),
        NavigationItem(
            route = "userProfile",
            label = "User Profile",
            icon = Icons.Default.Person
        ),
        NavigationItem(
            route = "logout",
            label = "Logout",
            icon = Icons.Default.ExitToApp
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                navController = navController,
                drawerState = drawerState,
                items = navigationItems
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Market Place", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(
                            onClick = { if (isLoggedIn) scope.launch { drawerState.open() } },
                            enabled = isLoggedIn
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = buttonColor
                            )
                        }
                    },
                    actions = {
                        if (isLoggedIn) {
                            IconButton(onClick = {  }) {
                                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = buttonColor)
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    showLoginDialog = true
                                }
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = buttonColor
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Market Place", tint = buttonColor) },
                        label = { Text("Market Place") },
                        selected = navController.currentDestination?.route == "marketplace",
                        onClick = {
                            navController.navigate("marketplace") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = buttonColor) },
                        label = { Text("Cart") },
                        selected = navController.currentDestination?.route == "cart",
                        onClick = {
                            navController.navigate("cart") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = "Order", tint = buttonColor) },
                        label = { Text("Order") },
                        selected = navController.currentDestination?.route == "order",
                        onClick = {
                            navController.navigate("order") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Email, contentDescription = "Chat", tint = buttonColor) },
                        label = { Text("Chat") },
                        selected = navController.currentDestination?.route == "chat",
                        onClick = {
                            navController.navigate("chat") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "marketplace",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("marketplace") { ProductScreen(navController) }
                composable("productDetail/{productId}") { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull()
                    ProductDetailScreen(productId, navController) // Define this composable
                }
                composable("cart") { /* CartScreen() */ }
                composable("order") { /* OrderScreen() */ }
                composable("chat") { /* ChatScreen() */ }
                composable("profile") { ProfileScreen() }
                composable("business_registration") { BusinessRegistrationScreen(navController) }
                composable("userProfile") {
                    UserProfileScreen(navController = navController)
                }
                composable("business_profile") {
                    BusinessProfileScreen(navController)
                }
                composable("dashboard") {
                    DashboardScreen(navController)
                }
                composable("myproduct") {
                    MyProductsScreen(navController, onAddNewProductClick = {
                        showAddNewProductDialog = true
                    } )
                }
                composable("quotation") {
                    // Implement Quotation screen
                    Text("Quotation Screen", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
                }
                composable("message") {
                    // Implement Message screen
                    Text("Message Screen", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
                }
                composable("transaction") {
                    // Implement Transactions screen
                    Text("Transactions Screen", modifier = Modifier.fillMaxSize(), textAlign = TextAlign.Center)
                }
                composable("logout") {
                    LaunchedEffect(Unit) {
                        clearAuthData(context)
                        navController.navigate("marketplace") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }
            if (showLoginDialog) {
                LoginDialog(
                    onDismiss = { showLoginDialog = false },
                    navController = navController,
                    onSignUpClick = {
                        showSignUpDialog = true
                        showLoginDialog = false
                    }
                )
            }
            if (showSignUpDialog) {
                SignupDialog(
                    onDismiss = { showSignUpDialog = false },
                    onLoginClick = {
                        showLoginDialog = true
                        showSignUpDialog = false
                    },
                    onSignupSuccess = {
                        showSignUpDialog = false
                        showLoginDialog = true
                    }
                )
            }
//            if (showAddNewProductDialog) {
//                AddProductDialog(
//                    onDismiss = { showAddNewProductDialog = false },
//
//                )
//            }

        }
    }
}

data class NavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun DrawerContent(
    navController: NavHostController,
    drawerState: DrawerState,
    items: List<NavigationItem>
) {
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(250.dp)
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.LightGray,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    modifier = Modifier.padding(8.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "CuttoShape",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            drawerState.close()
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    }
                    .background(
                        color = if (currentRoute == item.route) Color.LightGray else Color.Transparent
                    )
                    .padding(vertical = 20.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.label,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}