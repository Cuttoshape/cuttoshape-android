package com.example.cuttoshapenew.views.tailorviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DashboardCard(title = "Total Earning", value = "$100.00", subtext = "-\$100.00", subtextColor = Color.Red)
                DashboardCard(title = "Total Earning\nLast Month", value = "$200.00")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DashboardCard(title = "Total Commission\nDeducted", value = "$50.00")
                DashboardCard(title = "Total Income", value = "$4500.00")
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    value: String,
    subtext: String? = null,
    subtextColor: Color = Color.Gray
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp)
            .background(color = Color(0xFFF9F9F9), shape = RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 12.sp, color = Color.Gray)
        Column {
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (subtext != null) {
                Text(text = subtext, fontSize = 12.sp, color = subtextColor)
            }
        }
    }
}
