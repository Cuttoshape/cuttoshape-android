package com.example.cuttoshapenew

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
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
import com.example.cuttoshapenew.views.tailorviews.BusinessRegistrationScreen
import com.example.cuttoshapenew.views.tailorviews.UserProfileScreen
import com.example.cuttoshapenew.views.customerviews.product.ProductDetailScreen
import com.example.cuttoshapenew.views.tailorviews.BusinessProfileScreen
import com.example.cuttoshapenew.views.tailorviews.DashboardScreen
import com.example.cuttoshapenew.views.tailorviews.MyProductsScreen
import com.example.cuttoshapenew.ui.theme.CuttoshapenewTheme
import com.example.cuttoshapenew.utils.DataStoreManager
import com.example.cuttoshapenew.utils.DataStoreManager.clearAuthData
import com.example.cuttoshapenew.views.customerviews.cart.CartScreen
import com.example.cuttoshapenew.views.customerviews.order.OrderScreen
import com.example.cuttoshapenew.views.customerviews.order.OrderDetailsScreen
import com.example.cuttoshapenew.views.tailorviews.order.TailorOrderScreen
import com.example.cuttoshapenew.views.tailorviews.order.TailorOrderDetailsScreen
import com.example.cuttoshapenew.views.customerviews.measurement.MeasurementModal
import com.example.cuttoshapenew.views.tailorviews.Chat.ChatScreen
import com.example.cuttoshapenew.views.tailorviews.Chat.MessageListScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
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
    var userRole by remember { mutableStateOf<String?>(null) }
    var userId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        launch {
            DataStoreManager.getToken(context).collectLatest { token ->
                isLoggedIn = token != null
                userRole = DataStoreManager.getRole(context)
                userId = DataStoreManager.getUserId(context).first()
            }
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navigationItems = listOf(
        NavigationItem("dashboard", "Dashboard", Icons.Default.DateRange),
        NavigationItem("myproduct", "My Products", Icons.Default.ShoppingCart),
        NavigationItem("quotation", "Quotation", Icons.Default.Info),
        NavigationItem("message", "Message", Icons.Default.Email),
        NavigationItem("transaction", "Transactions", Icons.Default.DateRange),
        NavigationItem("business_profile", "Business Profile", Icons.Default.Create),
        NavigationItem("userProfile", "User Profile", Icons.Default.Person),
        NavigationItem("logout", "Logout", Icons.Default.ExitToApp)
    )

    if (isLoggedIn && userRole == "TAILOR") {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = { DrawerContent(navController, drawerState, navigationItems) }
        ) {
            TailoringAppContent(
                navController = navController,
                showLoginDialog = showLoginDialog,
                showSignUpDialog = showSignUpDialog,
                showAddNewProductDialog = showAddNewProductDialog,
                onLoginDialogToggle = { showLoginDialog = it },
                onSignUpDialogToggle = { showSignUpDialog = it },
                onAddNewProductDialogToggle = { showAddNewProductDialog = it },
                buttonColor = buttonColor,
                context = context,
                isLoggedIn = isLoggedIn,
                userRole = userRole,
                scope = scope,
                drawerState = drawerState,
                userId = userId.toString()
            )
        }
    } else {
        TailoringAppContent(
            navController = navController,
            showLoginDialog = showLoginDialog,
            showSignUpDialog = showSignUpDialog,
            showAddNewProductDialog = showAddNewProductDialog,
            onLoginDialogToggle = { showLoginDialog = it },
            onSignUpDialogToggle = { showSignUpDialog = it },
            onAddNewProductDialogToggle = { showAddNewProductDialog = it },
            buttonColor = buttonColor,
            context = context,
            isLoggedIn = isLoggedIn,
            userRole = userRole,
            scope = scope,
            drawerState = drawerState,
            userId = userId.toString()
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TailoringAppContent(
    navController: NavHostController,
    showLoginDialog: Boolean,
    showSignUpDialog: Boolean,
    showAddNewProductDialog: Boolean,
    onLoginDialogToggle: (Boolean) -> Unit,
    onSignUpDialogToggle: (Boolean) -> Unit,
    onAddNewProductDialogToggle: (Boolean) -> Unit,
    buttonColor: Color,
    context: android.content.Context,
    isLoggedIn: Boolean,
    userRole: String?,
    scope: CoroutineScope,
    drawerState: DrawerState,
    userId: String
) {
    var showMeasurementModal by remember { mutableStateOf(false) }
    val modalSheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Market Place", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (isLoggedIn && userRole == "TAILOR") {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            enabled = isLoggedIn
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = buttonColor
                            )
                        }
                    }
                },
                actions = {
                    if (isLoggedIn) {
                        IconButton(onClick = { /* Handle notifications */ }) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = buttonColor)
                        }
                    } else {
                        IconButton(
                            onClick = { onLoginDialogToggle(true) }
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
            if (isLoggedIn && userRole == "CUSTOMER") {
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
                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Scan", tint = buttonColor) },
                        label = { Text("Scan") },
                        selected = navController.currentDestination?.route == "scan",
                        onClick = {
                            scope.launch { showMeasurementModal = true; modalSheetState.show() }
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
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = "marketplace",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("marketplace") { ProductScreen(navController) }
                composable("productDetail/{productId}") { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull()
                    ProductDetailScreen(productId, navController)
                }
                composable("cart") { CartScreen(navController) }
                composable("order") { OrderScreen(navController) }
                composable("orderDetails/{orderId}/{buyerId}") { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId")?.toIntOrNull() ?: 0
                    val buyerId = backStackEntry.arguments?.getString("buyerId")?.toIntOrNull() ?: 0
                    OrderDetailsScreen(navController, orderId, buyerId)
                }
                composable("chat") { /* ChatScreen() */ }
                composable("profile") { ProfileScreen() }
                composable("business_registration") { BusinessRegistrationScreen(navController) }
                composable("userProfile") { UserProfileScreen(navController = navController) }
                composable("business_profile") { BusinessProfileScreen(navController) }
                composable("dashboard") { DashboardScreen(navController) }
                composable("myproduct") {
                    MyProductsScreen(navController, onAddNewProductClick = {
                        onAddNewProductDialogToggle(true)
                    })
                }
                composable("quotation") {
                    TailorOrderScreen( navController)
                }
                composable("orderDetails/tailor/{orderId}/{sellerId}") { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId")?.toIntOrNull() ?: 0
                    val sellerId = backStackEntry.arguments?.getString("sellerId")?.toIntOrNull() ?: 0
                    TailorOrderDetailsScreen(navController, orderId, sellerId)
                }
                composable("message") {
                    MessageListScreen(navController = navController, userId = userId)
                }
                composable("chat_screen/{receiverId}") { backStackEntry ->
                    val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
                    ChatScreen(receiverId = receiverId, userId = userId)
                }
                composable("transaction") {
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
            if (showMeasurementModal) {
                ModalBottomSheet(
                    onDismissRequest = { scope.launch { modalSheetState.hide(); showMeasurementModal = false } },
                    sheetState = modalSheetState,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    containerColor = Color.White
                ) {
                    MeasurementModal(
                        onDismiss = { scope.launch { modalSheetState.hide(); showMeasurementModal = false } },
                        userId = userId
                    )
                }
            }
            if (showLoginDialog) {
                LoginDialog(
                    onDismiss = { onLoginDialogToggle(false) },
                    navController = navController,
                    onSignUpClick = {
                        onSignUpDialogToggle(true)
                        onLoginDialogToggle(false)
                    }
                )
            }
            if (showSignUpDialog) {
                SignupDialog(
                    onDismiss = { onSignUpDialogToggle(false) },
                    onLoginClick = {
                        onLoginDialogToggle(true)
                        onSignUpDialogToggle(false)
                    },
                    onSignupSuccess = {
                        onSignUpDialogToggle(false)
                        onLoginDialogToggle(true)
                    }
                )
            }
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
                    .padding(vertical = 20.dp, horizontal = 16.dp),
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