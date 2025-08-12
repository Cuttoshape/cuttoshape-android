package com.example.cuttoshapenew.views.tailorviews.order

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuttoshapenew.apiclients.Order

// Order Card Composable
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrderCard(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.orderNumber,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Buyer: ${order.buyerName}")
                Text(text = "Date: ${formatDate(order.createdAt)}")
                Text(text = "Items: ${order.itemCount}")
                Text(
                    text = "Status: ${order.status}",
                    color = when (order.status) {
                        "NEW" -> Color(0xFF6200EE) // Purple
                        "IN_PROGRESS" -> Color.Green
                        "REVIEWED" -> Color(0xFFFF9800) // Orange
                        else -> Color.Gray
                    }
                )
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Details",
                tint = Color.Gray
            )
        }
    }
}