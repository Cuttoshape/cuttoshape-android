package com.example.cuttoshapenew.views.customerviews.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.cuttoshapenew.apiclients.CartItemResponse

@Composable
fun CartItemCard(item: CartItemResponse, buttonColor: Color, onDelete: (CartItemResponse) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageUrl = item.product?.documents?.firstOrNull()?.url
            imageUrl?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
            } ?: Image(
                painter = rememberAsyncImagePainter("https://via.placeholder.com/80"),
                contentDescription = "Placeholder Image",
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product?.name ?: "",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.userData?.name ?: "Unknown",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text("Cost: $${item.cost}", fontWeight = FontWeight.Bold, color = Color(0xFF2EA24C))

                Row(modifier = Modifier.padding(6.dp)){
                    Text("${item.configurations.color}, ${item.configurations.design}, ${item.configurations.fabric}")
                }

                Row() {
                    Button(
                        onClick = { onDelete(item) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEECED)),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Item",
                            tint = Color(0xFFDF4055),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = Color(0xFFDF4055), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(22.dp))
                    Button(
                        onClick = { /* Handle edit item */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE9F3FF)),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = "Edit Item",
                            tint = Color(0xFF1671B4),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", color = Color(0xFF1671B4), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}